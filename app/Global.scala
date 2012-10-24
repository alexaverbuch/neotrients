import play.api._
import play.api.mvc.Results._
import play.api.mvc.RequestHeader
import models.NutrientsStore

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    NutrientsStore().shutDown
    Logger.info("Application shutdown...")
  }
}