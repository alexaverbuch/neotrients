import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "neotrients"
    val appVersion      = "1.0"
      
    resolvers ++= Seq("neo4j-public-repository" at "http://m2.neo4j.org/releases")
      
    val appDependencies = Seq(
    		// "mysql" % "mysql-connector-java" % "5.1.18"
        "org.neo4j" % "neo4j" % "1.8"
    )
    
    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
