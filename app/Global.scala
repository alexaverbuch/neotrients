import play.api._
import play.api.mvc.Results._
import play.api.mvc.RequestHeader
import org.neo4j.play.Neo4j

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Neo4j.shutDown
    Logger.info("Application shutdown...")
  }
}