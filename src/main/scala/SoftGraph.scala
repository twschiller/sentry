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

import eu.fakod.neo4jscala._
import org.neo4j.graphdb.Node

case class Header(groupId : String, artifactId: String)

class SoftGraph(override val ds: DatabaseService) extends Neo4jWrapper with Neo4jIndexProvider {
  override def NodeIndexConfig = ("groupIndex", Some(Map("provider" -> "lucene", "type" -> "fulltext"))) :: Nil

  /** Add a project to the graph; if the project already exists, returns the existing project (without
    * updating any properties of the project).
    */
  def addProject(header : Header) : Node = {
    withTx {
      implicit neo =>
        val nodeIndex = getNodeIndex("groupIndex").get
        val result =  nodeIndex.get("group", header.groupId)

        while (result.hasNext){
          val n = result.next()

          if (n.toCC[Header].get == header){
            println(s"Found existing node in index: ${n.toCC[Header]}")
            result.close()
            return n
          }
        }

        val node = createNode(header)
        nodeIndex += (node, "group", header.groupId)
        node
    }
  }

  def addProject(header : maven.Header) : Node = {
    addProject(Header(header.groupId, header.artifactId))
  }

  /** Set license for project. Overwrites any existing information for that project. Creates a project node if the
    * project does not already exist.
    */
  def addLicense(header : maven.Header, license : maven.License) = {
    withTx {
      implicit neo =>
        val node = addProject(header)
        node.setProperty("license", license.name)
        node.setProperty("licenseUrl", license.url)
    }
  }

  /** Add dependencies for project, creating new project nodes as necessary */
  def addDependencies(project: maven.Header, dependencies: Seq[maven.Header]) = {
    withTx {
      implicit neo =>
        val start = addProject(project)
        for (d <- dependencies){
          val end = addProject(d)
          start --> "depends_on" --> end
        }
    }
  }
}
