package api.logging.handlers;



import api.logging.LoggerHandler;

import java.util.logging.Logger;

public class JULHandler implements LoggerHandler {

    private final Logger logger;

    public JULHandler(Logger logger){
        this.logger = logger;
    }

    @Override
    public void debug(Object message, Throwable throwable, Object... placeholders) {
        info(message, throwable, placeholders);
    }

    @Override
    public void info(Object message, Throwable throwable, Object... placeholders) {
        logger.info(message.toString());
        if (throwable != null) throwable.printStackTrace();
    }

    @Override
    public void warn(Object message, Throwable throwable, Object... placeholders) {
        logger.warning(message.toString());
        if (throwable != null) throwable.printStackTrace();
    }

    @Override
    public void error(Object message, Throwable throwable, Object... placeholders) {
        logger.severe(message.toString());
        if (throwable != null) throwable.printStackTrace();
    }


}
