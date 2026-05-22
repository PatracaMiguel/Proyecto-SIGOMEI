package com.sigomei.servidor.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class ServerLog {

    private static final Logger LOGGER = Logger.getLogger("SIGOMEI");

    static {
        configurar();
    }

    private ServerLog() {
    }

    public static void info(String mensaje) {
        LOGGER.info(mensaje);
    }

    public static void warning(String mensaje) {
        LOGGER.warning(mensaje);
    }

    private static void configurar() {
        try {
            LOGGER.setUseParentHandlers(false);
            String logPath = AppConfig.get("server.log.path", "logs/server.log");
            Path path = Path.of(logPath);
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            FileHandler handler = new FileHandler(logPath, true);
            handler.setFormatter(new PlainFormatter());
            LOGGER.addHandler(handler);
            LOGGER.setLevel(Level.INFO);
        } catch (IOException e) {
            LOGGER.setUseParentHandlers(true);
            LOGGER.warning("No se pudo configurar el archivo de log del servidor");
        }
    }

    private static class PlainFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return "%1$tF %1$tT [%2$s] %3$s%n".formatted(
                    record.getMillis(),
                    record.getLevel().getName(),
                    record.getMessage()
            );
        }
    }
}
