import com.dmanchester.playfop.japi.PlayFopComponents;

import controllers.Application;
import controllers.Assets;
import play.ApplicationLoader;
import play.BuiltInComponentsFromContext;
import play.controllers.AssetsComponents;
import play.data.FormFactoryComponents;
import play.filters.components.HttpFiltersComponents;
import play.routing.Router;
import router.Routes;

public class PlayFopLabelsComponents extends BuiltInComponentsFromContext
        implements HttpFiltersComponents, FormFactoryComponents, PlayFopComponents, AssetsComponents {

    // BuiltInComponentsFromContext has no default constructor. Explicitly call
    // its one-argument constructor.
    public PlayFopLabelsComponents(ApplicationLoader.Context context) {
        super(context);
    }

    @Override
    public Router router() {
        Application applicationController = new Application(config(), formFactory(), playFop());
        Assets assets = new Assets(scalaHttpErrorHandler(), assetsMetadata());
        return new Routes(scalaHttpErrorHandler(), applicationController, assets).asJava();
    }
}
