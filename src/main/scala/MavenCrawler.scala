package com.toddschiller.sentry.maven

import java.net.URLEncoder

import play.api.libs.json._
import scala.io.Source
import scala.xml.{NodeSeq, Elem, XML}

import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax
import scala.util.Try

import java.net.URL
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

/**
 * Created by tschiller on 5/9/15.
 */

case class Header(groupId: String, artifactId: String, version: Option[Version])
case class Version(version: String)

case class License(name: String, url: Option[String])

case class Dependency(header: Header, optional: Boolean, scope: Option[String])
case class Artifact(header: Header, dependencies : List[Dependency], licenses : List[License], modules : List[String])

object MavenCrawler {

  // TODO: Read directly to Header case class. Can't figure out how to get the JSON combinator to work though
  // cf. https://stackoverflow.com/questions/14754092/how-to-turn-json-to-case-class-when-case-class-has-only-one-field
  case class MavenId(groupId: String, artifactId: String, version: String)

  implicit val mavenHeaderReads: Reads[MavenId] = (
      (__ \ "g").read[String] and
      (__ \ "a").read[String] and
      (__ \ "v").read[String]
    )(MavenId.apply _)

  def fetchVersions(groupId: String, artifactId: String) = {
    val enc = (e : String) => URLEncoder.encode(s"""${e}""", "UTF-8")
    val url = s"https://search.maven.org/solrsearch/select?q=g:${enc(groupId)}+AND+a:${enc(artifactId)}&core=gav&rows=20&wt=json"
    val json = Json.parse(Source.fromURL(url).mkString)
    (json \ "response" \ "docs").as[Seq[MavenId]].filter(_.groupId != null).map(h => {
      Header(h.groupId, h.artifactId, Some(Version(h.version)))
    })
  }

  /* Fetch the POM for the specified artifact */
  def fetchPom(id: Header) : Elem = {
    if (id.groupId == null || id.groupId.isEmpty){
      throw new IllegalArgumentException("group id cannot be null or empty")
    }
    val pomUrl = new URL(s"https://search.maven.org/remotecontent?filepath=${id.groupId.replaceAll("\\.", "/")}/${id.artifactId}/${id.version.get.version}/${id.artifactId}-${id.version.get.version}.pom")
    XML.load(pomUrl)
  }

  /** Fetch ancestors for the given artifact, includes the given artifact */
  def fetchAncestors(id: Header) : List[Artifact] = {
    val raw = fetchPom(id)
    val pom = parsePom(raw)

    val parent = fetchPom(id) \ "parent"
    if (parent.size > 0) {
      val a = (parent \ "artifactId").text
      val g = (parent \ "groupId").text
      val v = (parent \ "version").text
      List(pom) ++ fetchAncestors(Header(g, a, if (v.nonEmpty) Some(Version(v)) else None))
    } else {
      // At the root POM
      List(pom)
    }
  }

  /** Fetch the licenses for the given artifact, includes licenses for all parent artifacts **/
  def findLicenses(id: Header) : Set[License] = {
    fetchAncestors(id).flatMap(_.licenses).toSet
  }

  def parsePom(pom : Elem) : Artifact = {
    val artifactGroup = (pom \ "groupId").text
    val parentGroup = (pom \ "parent" \ "groupId").text
    val group = if (artifactGroup.isEmpty) parentGroup else artifactGroup
    val expand = Map("${project.groupId}" -> group, "${pom.groupId}" -> group)

    val artifactVersion = (pom \ "version").text
    val parentVersion = (pom \ "parent" \ "version").text

    val header = Header(
      group,
      (pom \ "artifactId").text,
      Some(Version(if (artifactVersion.isEmpty) parentVersion else artifactVersion))
    )

    val licenses = for { d <- pom \ "licenses" \ "license" } yield {
      License( (d \ "name").text, Some((d \ "url").text))
    }

    val dependencies = for { d <- pom \ "dependencies" \ "dependency" } yield {
      val v = (d \ "version").text
      val g = (d \ "groupId").text

      val h = Header(
        expand.getOrElse(g, g),
        (d \ "artifactId").text,
        if (!v.isEmpty) Some(Version(v)) else None)
      Dependency(h,
        Try((d \ "optional").text.toBoolean).getOrElse(false),
        None
      )
    }

    val modules = (pom \ "modules" \ "module").map(_.text)

    Artifact(header, dependencies.toList, licenses.toList, modules.toList)
  }
}
