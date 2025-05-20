package controllers
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.mvc.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
final class UIController @Inject() (
    controllerComponents: play.api.mvc.ControllerComponents,
    assets: Assets
) extends AbstractController(controllerComponents) {

def index: Action[AnyContent] =
   assets.at("/public", "index.html", aggressiveCaching = false)

 def assetOrDefault(resource: String): Action[AnyContent] = {
   if resource.startsWith("static") ||
     resource.endsWith(".css") ||
     resource.endsWith(".ico")
     || resource.endsWith(".js")
     || resource.endsWith(".png")
     || resource.endsWith(".svg")
   then assets.at("/public", resource, aggressiveCaching = false)
   else index

 }

 def unknown(resource: String): Action[AnyContent] =
   Action {
     NotFound(s"$resource not found")
   }
}