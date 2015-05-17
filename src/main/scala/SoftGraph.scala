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
case class License(name: String, url: String)

class SoftGraph(override val ds: DatabaseService) extends Neo4jWrapper with Neo4jIndexProvider {
  override def NodeIndexConfig =
    ("groupIndex", Some(Map("provider" -> "lucene", "type" -> "fulltext"))) ::
    ("licenseIndex", Some(Map("provider" -> "lucene", "type" -> "fulltext"))) :: Nil

  /** Add a project to the graph; if the project already exists, returns the existing project (without
    * updating any properties of the project).
    */
  def addProject(header : Header) : Node = {
    withTx {
      implicit neo =>
        val nodeIndex = getNodeIndex("groupIndex").get
        val result = nodeIndex.get("group", header.groupId)

        while (result.hasNext){
          val n = result.next()

          if (n.toCC[Header].get == header){
            result.close()
            return n
          }
        }

        val node = createNode(header)
        nodeIndex += (node, "group", header.groupId)
        node
    }
  }

  def addLicense(license : License) : Node = {
    withTx {
      implicit neo =>
        val nodeIndex = getNodeIndex("licenseIndex").get
        val result =  nodeIndex.get("name", license.name)

        while (result.hasNext){
          val n = result.next()

          if (n.toCC[License].get == license){
            result.close()
            return n
          }
        }

        val node = createNode(license)
        nodeIndex += (node, "name", license.name)
        node
    }
  }

  def addProject(header : maven.Header) : Node = {
    addProject(Header(header.groupId, header.artifactId))
  }

  def addLicense(header : Header, license : License) : Node = {
    withTx {
      implicit neo =>
        val node = addLicense(license)
        addProject(header) --> "has_license" --> node
        node
    }
  }

  def addLicense(header : maven.Header, license : maven.License) : Node = {
    addLicense(Header(header.groupId, header.artifactId), License(license.name, license.url.getOrElse(null)))
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
