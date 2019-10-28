package org.mejlholm;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.logging.InitialConfigurator;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logmanager.EmbeddedConfigurator;
import org.jboss.logmanager.formatters.JsonFormatter;
import org.jboss.logmanager.handlers.ConsoleHandler;

import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;

//fixme, this is a temporary workaround to quarkus not having json logging yet.
@Slf4j
public class LoggerBean {

    @ConfigProperty(name = "NAMESPACE", defaultValue = "basic")
    String logFormat;

    void onStart(@Observes StartupEvent ev) {

        if (logFormat.equalsIgnoreCase("json")) {
            ArrayList<Handler> handlers = new ArrayList(2);
            Formatter formatter = new JsonFormatter();
            ConsoleHandler handler = new ConsoleHandler(formatter);
            handler.setLevel(Level.INFO);
            handlers.add(handler);
            InitialConfigurator.DELAYED_HANDLER.setHandlers((Handler[]) handlers.toArray(EmbeddedConfigurator.NO_HANDLERS));
            log.info("Logging set to json format");
        } else {
            log.info("Logging set to basic format");
        }
    }
}
