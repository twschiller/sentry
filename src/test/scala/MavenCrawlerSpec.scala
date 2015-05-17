package com.toddschiller.sentry.maven.test

import com.toddschiller.sentry.maven.{Header, Version, MavenCrawler, License}
import org.scalatest.{BeforeAndAfter, FunSuite}

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
class MavenCrawlerSpec extends FunSuite {

  val neo4jPom = <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <groupId>org.neo4j</groupId>
    <artifactId>neo4j</artifactId>
    <version>2.3.0-M01</version>
    <name>Neo4j - Community</name>
    <packaging>jar</packaging>
    <description>
      A meta package containing the most used Neo4j libraries. Intended use: as a Maven dependency.
    </description>
    </project>

  val guice40 = Header("com.google.inject", "guice", Some(Version("4.0")))

  test("can parse pom basic info") {
    val result = MavenCrawler.parsePom(neo4jPom)
    assert(result.header.groupId === "org.neo4j")
  }

  test("can fetch guice versions"){
    val versions = MavenCrawler.fetchVersions("com.google.inject", "guice")
    assert(versions.nonEmpty)
    assert(versions.head === guice40)
  }

  test("can fetch latest guice pom"){
    val pom = MavenCrawler.fetchPom(guice40)
    val result = MavenCrawler.parsePom(pom)

    assert(result.header === guice40)
    assert(result.dependencies.nonEmpty)
  }

  test("can fetch modules for guice-parent"){
    val pom = MavenCrawler.fetchPom(Header("com.google.inject", "guice-parent", Some(Version("4.0"))))
    val result = MavenCrawler.parsePom(pom)

    assert(result.modules.nonEmpty)
    assert(result.modules.contains("core"))
  }

  test("can fetch ancestors for guice"){
    val result = MavenCrawler.fetchAncestors(guice40)

    assert(result.length === 3)
    assert(result(0).header.artifactId === "guice")
    assert(result(1).header.artifactId === "guice-parent")
    assert(result(2).header.artifactId === "google")
  }

  test("can fetch license for guice"){
    val licenses = MavenCrawler.findLicenses(guice40)
    assert(licenses.size === 1)
    assert(licenses.head.name === "The Apache Software License, Version 2.0")
  }
}
