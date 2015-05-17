name := "Sentry"

version := "1.0"

scalaVersion := "2.10.5"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "neo4j-releases" at "http://m2.neo4j.org/content/repositories/releases"

// resolvers += "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "joda-time" % "joda-time" % "2.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.7" % "test"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.4"

libraryDependencies += "org.neo4j" % "neo4j" % "1.9.9"

libraryDependencies += "org.neo4j" % "neo4j-kernel" % "1.9.9" % "test" classifier "tests"

libraryDependencies += "eu.fakod" %% "neo4j-scala" % "0.3.0"


//libraryDependencies += "org.apache.maven.indexer" %% "indexer-core" % "6.0-SNAPSHOT"
//
//libraryDependencies += "org.apache.maven.wagon" % "wagon-http-lightweight" % "2.8"
//
//libraryDependencies += "org.eclipse.sisu" % "org.eclipse.sisu.plexus"
//
//libraryDependencies += "org.sonatype.sisu" % "sisu-guice"
//
//libraryDependencies += "org.apache.maven" % "maven-model"

