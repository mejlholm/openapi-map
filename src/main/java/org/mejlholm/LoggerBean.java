package org.mejlholm;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.logging.InitialConfigurator;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logmanager.EmbeddedConfigurator;
import org.jboss.logmanager.formatters.JsonFormatter;
import org.jboss.logmanager.handlers.ConsoleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;

//fixme, this is a temporary workaround to quarkus not having json logging yet.
@Slf4j
public class LoggerBean {

    private static final Logger LOGGER = LoggerFactory.getLogger("LoggerBean");

    void onStart(@Observes StartupEvent ev) {
        ArrayList<Handler> handlers = new ArrayList(2);
        Formatter formatter = new JsonFormatter();
        ConsoleHandler handler = new ConsoleHandler(formatter);
        handler.setLevel(Level.INFO);
        handlers.add(handler);
        InitialConfigurator.DELAYED_HANDLER.setHandlers((Handler[]) handlers.toArray(EmbeddedConfigurator.NO_HANDLERS));
        log.info("The application is starting...");

    }
}
