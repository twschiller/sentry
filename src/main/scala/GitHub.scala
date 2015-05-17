/*
 * Copyright (c) 2015 Todd Schiller. This file is part of Sentry.
 *
 * Sentry is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later version.
 *
 * Sentry is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Sentry.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toddschiller.sentry

import java.net.URL

import com.toddschiller.sentry.maven.MavenCrawler
import play.api.libs.json._
import play.api.libs.json.Reads._

import scala.xml.XML

// Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax
import scala.io.Source
import scala.io.Codec

object GitHub {

  // TODO: language statistics
  // TODO: number of forks and stargazers (importance)
  // TODO: add "forks" links to graph db
  // TODO: add "dependsOn" links to graph db
  // TODO: detect project license

  // python: requirements.txt
  // scala: build.sbt
  // java: maven, gradle, etc.

  case class GitHubRepo(id: Int, fullName: String, url: String)

  implicit val repoReads: Reads[GitHubRepo] = (
      (__ \ "id").read[Int] and
      (__ \ "full_name").read[String] and
      (__ \ "url").read[String]
    )(GitHubRepo.apply _)

  /**
   * Parse GitHub repository records. Format defined at https://developer.github.com/v3/repos/#list-all-public-repositories
   * @param json repository json
   * @return sequence of GitHub repositories
   */
  def parseRepositories(json : String) : Seq[GitHubRepo] = Json.parse(json).as[Seq[GitHubRepo]]

  case class BuildDef(name : String, config: Seq[String])

  val buildDefinitions = List(
    BuildDef("Travis CI", List(".travis.yml")),
    BuildDef("Drone CI", List(".drone.yml")),
    BuildDef("PHPCI", List(".phpci.yml")),
    BuildDef("Shippable", List("shippable.yml")),
    BuildDef("AppVeyor", List("appveyor.yml")) // Windows CI
  )

  def detectBuildDefinitions(r : GitHubRepo) : Seq[BuildDef] = {
    buildDefinitions.filter( b => {
    b.config.exists( c => {
        try {
          Source.fromURL(s"https://raw.githubusercontent.com/${r.fullName}/master/$c").mkString("\n")
          true
        }catch{
          case _ : java.io.FileNotFoundException => false
        }
      })
    })
  }

  def fetchPom(r : GitHubRepo) : Option[maven.Artifact] = {
    fetchPom(r, List[String]())
  }

  def fetchPom(r : GitHubRepo, modulePath : Seq[String]) : Option[maven.Artifact] = {
    try {
      val pom = XML.load(new URL(s"https://raw.githubusercontent.com/${r.fullName}/master/${modulePath.mkString("/")}/pom.xml"))
      Some(MavenCrawler.parsePom(pom))
    }catch {
      case _ : Exception => None
    }
  }

  /** Finds the mainline GitHub repository associated for a Maven header. Ignores version and license. */
  def findMavenArtifact(header : maven.Header) : Option[(maven.Artifact, GitHubRepo)] = {
    // TODO: instead of performing a search, should find the closest SCM entry in the POM chain
    val search = Source.fromURL(s"https://api.github.com/search/repositories?q=${header.artifactId}&sort=stars&order=desc&forks=false").mkString
    val rs = (Json.parse(search) \ "items").as[Seq[GitHubRepo]]

    // XXX: How do I write this using a find statement s.t. it doesn't explore all search results?
    for (r <- rs) {
      findMavenArtifact(r, header, List[String]()) match {
        case Some(a) => return Some((a, r))
        case _ => None
      }
    }
    None
  }

  def findMavenArtifact(repository : GitHubRepo, header : maven.Header, modulePath : List[String]) : Option[maven.Artifact] = {
    fetchPom(repository, modulePath) match {
      case Some(artifact) =>
        // Group Ids are probably consistent throughout the artifact
        if (artifact.header.groupId == header.groupId){
          if (artifact.header.artifactId == header.artifactId){
            Some(artifact)
          }else{
            for (m <- artifact.modules){
              // XXX: How do I write this using a find statement s.t. it doesn't explore all search results?
              findMavenArtifact(repository, header, modulePath :+ m) match {
                case Some(a) => return Some(a)
                case _ => None
              }
            }
            None
          }
        }else{
          None
        }
      case None =>
        None
    }
  }
}
