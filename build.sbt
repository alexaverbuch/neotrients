name := "neotrients"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.2"

resolvers ++= Seq("neo4j-public-repository" at "http://m2.neo4j.org/releases")

libraryDependencies += "org.neo4j" % "neo4j" % "1.8"

