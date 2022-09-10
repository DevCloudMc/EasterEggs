package api.logging;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import org.bukkit.Bukkit;
import ru.brainrtp.eastereggs.EasterEggs;

public final class Logger {

    private static LoggerHandler handler;

    private Logger() {
    }

    public static void init(LoggerHandler h) {
        handler = h;
    }

    public static void debug(Object message, Object... placeholders) {
        debug(message, null, placeholders);
    }

    public static void debug(Object message, Throwable throwable, Object... placeholders) {
        handler.debug(message, throwable, placeholders);
    }

    public static void info(Object message, Object... placeholders) {
        info(message, null, placeholders);
    }

    public static void info(Object message, Throwable throwable, Object... placeholders) {
        if (Bukkit.getName().equals("Paper")) {
            message = message.toString().replaceAll("§.", "");
        }
        handler.info(message, throwable, placeholders);
    }

    public static void warn(Object message, Object... placeholders) {
        warn(message, null, placeholders);
    }

    public static void warn(Object message, Throwable throwable, Object... placeholders) {
        if (Bukkit.getName().equals("Paper")) {
            message = message.toString().replaceAll("§.", "");
        }
        handler.warn(message, throwable, placeholders);
    }

    public static void error(Object message, Object... placeholders) {
        error(message, null, placeholders);
    }

    public static void error(Object message, Throwable throwable, Object... placeholders) {
        if (Bukkit.getName().equals("Paper")) {
            message = message.toString().replaceAll("§.", "");
        }

        handler.error(message, throwable, placeholders);

        if (throwable != null) {
            SentryEvent event = new SentryEvent();
            Message sentryMessage = new Message();
            sentryMessage.setMessage(String.valueOf(message));
            event.setMessage(sentryMessage);
            event.setTag("plugin.version", EasterEggs.getPlugin().getDescription().getVersion());
            event.setTag("bukkit.name", Bukkit.getName());
            event.setTag("bukkit.version", Bukkit.getBukkitVersion());
            event.setTag("bukkit.version.full", Bukkit.getVersion());
            event.setModule("User ID", "%%__USER__%%");
            event.setLevel(SentryLevel.ERROR);
            event.setLogger(handler.getClass().getName());
            event.setThrowable(throwable);

            Sentry.captureEvent(event);
        }
    }

}
