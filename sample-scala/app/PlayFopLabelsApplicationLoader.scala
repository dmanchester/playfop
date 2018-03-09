import play.api.ApplicationLoader
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.LoggerConfigurator
import play.filters.HttpFiltersComponents

import com.dmanchester.playfop.sapi.PlayFopComponents

import controllers.Application
import router.Routes

class PlayFopLabelsApplicationLoader extends ApplicationLoader {

  def load(context: Context) = {

    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }

    new PlayFopLabelsComponents(context).application
  }
}

class PlayFopLabelsComponents(context: Context) extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with controllers.AssetsComponents
    with PlayFopComponents {

  lazy val applicationController = new Application(configuration, controllerComponents, playFop)

  lazy val router = new Routes(httpErrorHandler, applicationController, assets)
}
