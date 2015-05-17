import java.io.FileNotFoundException

import com.toddschiller.sentry.{License, SoftGraph}
import com.toddschiller.sentry.maven.{MavenCrawler, Header}

import eu.fakod.neo4jscala.{Neo4jIndexProvider, RestGraphDatabaseServiceProvider, DatabaseServiceImpl, EmbeddedGraphDatabaseServiceProvider}

import scala.collection.mutable
import java.net.{URL, URI}

class HttpGraph extends RestGraphDatabaseServiceProvider {
  def uri  = new URI("http://localhost:7474/db/data/")
}

object HelloWorld {

  def main(args: Array[String]) {
    val now = new org.joda.time.DateTime()
    println("Hi SBT, the time is " + now.toString("hh:mm aa"))

    val graph = new SoftGraph(new HttpGraph().ds)

    val stack = new mutable.Stack[Header]
    val visited = new mutable.HashSet[Header]

    val start = Header("com.google.inject", "guice", None)
    stack.push(start)
    graph.addProject(start)

    while (stack.nonEmpty) {
      val next = stack.pop()

      if (!visited.contains(next)) {
        println(s"Crawling ${next.artifactId} from ${next.groupId}")

        MavenCrawler.fetchVersions(next.groupId, next.artifactId).headOption match {
          case Some(latest) =>
            try {
               val artifact = MavenCrawler.parsePom(MavenCrawler.fetchPom(latest))
               // TODO: consider optional dependencies and dependency version
               val required = artifact.dependencies.filterNot(_.optional).map(h => Header(h.header.groupId, h.header.artifactId, None))

               // TODO: if dependency has already been visited, don't need to look up the license again
               MavenCrawler.findLicenses(latest).foreach(l => {
                 val canonical = License.findCanonical(License.safeMake(l.name, l.url))
                 graph.addLicense(latest, if (canonical.nonEmpty) com.toddschiller.sentry.maven.License(canonical.get.name, l.url) else l)
               })

               graph.addDependencies(next, required)
               stack.pushAll(required)
            }catch{
              case e : FileNotFoundException =>
                println(s"Cannot find POM for ${next.artifactId} from ${next.groupId}")
            }

          case None =>
            println(s"No versions found for ${next.artifactId} from ${next.groupId}")
        }

        visited += next
      }
    }
  }
}
