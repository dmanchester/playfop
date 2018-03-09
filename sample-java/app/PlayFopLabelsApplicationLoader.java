import play.Application;
import play.ApplicationLoader;
import play.LoggerConfigurator;

public class PlayFopLabelsApplicationLoader implements ApplicationLoader {

    @Override
    public Application load(Context context) {

        LoggerConfigurator.apply(context.environment().classLoader()).ifPresent(loggerConfigurator ->
            loggerConfigurator.configure(context.environment(), context.initialConfig())
        );

        return new PlayFopLabelsComponents(context).application();
    }
}
