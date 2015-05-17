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

import com.toddschiller.sentry.Header
import eu.fakod.neo4jscala.{DatabaseService, DatabaseServiceImpl, Neo4jWrapper}
import org.neo4j.graphdb.{Direction, DynamicRelationshipType}
import org.neo4j.test.TestGraphDatabaseFactory
import org.scalatest.{BeforeAndAfter, FunSuite}

class SoftGraphSpec extends FunSuite with Neo4jWrapper with BeforeAndAfter {

  val ds : DatabaseService = DatabaseServiceImpl(new TestGraphDatabaseFactory().newImpermanentDatabase())

  before {
    // Delete everything in the DB
    withTx {
      implicit neo =>
        getAllNodes(ds).foreach(_.delete())
    }
  }

  after {
    ds.gds.shutdown()
  }


  // Copied from neo4j-scala's Neo4jWrapperTest.scala
  test("create a new relationship in --> relType --> notation") {
    withTx {
      implicit neo =>
        val start = createNode(Header("com.toddschiller.sentry", "sentry"))
        val end = createNode(Header("eu.fakod", "neo4j-scala"))
        val relType = DynamicRelationshipType.withName("depends_on")
        val rel1 = start --> relType --> end <
        val rel2 = start.getSingleRelationship(relType, Direction.OUTGOING)

        assert(rel2.getOtherNode(start) === end)
        assert(rel1 === rel2)
    }
  }
}
