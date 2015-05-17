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

package com.toddschiller.sentry.test

import com.toddschiller.sentry.GitHub.GitHubRepo
import com.toddschiller.sentry.maven.Header
import org.scalatest.FlatSpec
import com.toddschiller.sentry.GitHub
import java.net.URL

import scala.io.Source

/**
 * Created by tschiller on 4/5/15.
 */
class GitHubSpec extends FlatSpec {

  // https://developer.github.com/v3/repos/#list-all-public-repositories
  val repositoryJson = """[
  {
    "id": 1296269,
    "owner": {
      "login": "octocat",
      "id": 1,
      "avatar_url": "https://github.com/images/error/octocat_happy.gif",
      "gravatar_id": "",
      "url": "https://api.github.com/users/octocat",
      "html_url": "https://github.com/octocat",
      "followers_url": "https://api.github.com/users/octocat/followers",
      "following_url": "https://api.github.com/users/octocat/following{/other_user}",
      "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
      "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
      "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
      "organizations_url": "https://api.github.com/users/octocat/orgs",
      "repos_url": "https://api.github.com/users/octocat/repos",
      "events_url": "https://api.github.com/users/octocat/events{/privacy}",
      "received_events_url": "https://api.github.com/users/octocat/received_events",
      "type": "User",
      "site_admin": false
    },
    "name": "Hello-World",
    "full_name": "octocat/Hello-World",
    "description": "This your first repo!",
    "private": false,
    "fork": false,
    "url": "https://api.github.com/repos/octocat/Hello-World",
    "html_url": "https://github.com/octocat/Hello-World"
  }
  ]"""

  val guiceRepo = GitHubRepo(20275545, "google/guice", "https://github.com/google/guice")

  it should "parse respository listings" in {
    val res = GitHub.parseRepositories(repositoryJson)
    assert(res.size === 1)
  }

  it should "find Travis CI config for Sentry" in {
    val res = GitHub.detectBuildDefinitions(GitHub.GitHubRepo(0, "twschiller/sentry", "https://github.com/twschiller/sentry"))
    assert(res.size === 1)
    assert(res.exists(_.name == "Travis CI"))
  }

  it should "find root pom for Google Guice" in {
    val res = GitHub.fetchPom(guiceRepo)
    assert(res.nonEmpty)
    assert(res.get.header.groupId === "com.google.inject")
    assert(res.get.header.artifactId === "guice-parent")
  }

  it should "find Google Guice pom" in {
    val res = GitHub.fetchPom(guiceRepo, List("core"))
    assert(res.nonEmpty)
    assert(res.get.header.groupId === "com.google.inject")
    assert(res.get.header.artifactId === "guice")
  }

  it should "find Google Guice repository" in {
    val header = Header("com.google.inject", "guice", None)
    val result = GitHub.findMavenArtifact(header)

    assert(result.nonEmpty)
    assert(result.get._1.header.groupId === header.groupId)
    assert(result.get._1.header.artifactId === header.artifactId)
    assert(result.get._2.fullName === "google/guice")
  }
}
