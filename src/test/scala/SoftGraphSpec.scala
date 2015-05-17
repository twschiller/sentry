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

import com.toddschiller.sentry.{License, SoftGraph, Header}
import eu.fakod.neo4jscala.{DatabaseService, DatabaseServiceImpl, Neo4jWrapper}
import org.neo4j.graphdb.{Direction, DynamicRelationshipType}
import org.neo4j.test.TestGraphDatabaseFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, FunSuite}

class SoftGraphSpec extends FunSuite with BeforeAndAfter with BeforeAndAfterAll {

  val ds : DatabaseService = DatabaseServiceImpl(new TestGraphDatabaseFactory().newImpermanentDatabase())
  val graph : SoftGraph = new SoftGraph(ds)

  val project1 = Header("com.toddschiller.sentry", "sentry")
  val project2 = Header("eu.fakod", "neo4j-scala")
  val license1 = License("Apache License Version 2.0", "https://www.apache.org/licenses/LICENSE-2.0.html")

  test("don't duplicate project nodes") {
    val node1 = graph.addProject(project1)
    val node2 = graph.addProject(project1)
    assert(node2 === node1)
  }

  test("don't duplicate license nodes") {
    val node1 = graph.addLicense(license1)
    val node2 = graph.addLicense(license1)
    assert(node2 === node1)
  }
}
