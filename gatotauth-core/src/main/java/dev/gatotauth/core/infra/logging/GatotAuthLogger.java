package dev.gatotauth.core.infra.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GatotAuthLogger wraps platform-independent logging capabilities.
 */
public interface GatotAuthLogger {
    /**
     * Logs info messages.
     *
     * @param message String format message.
     */
    void info(String message);

    /**
     * Logs warn messages.
     *
     * @param message String format message.
     */
    void warn(String message);

    /**
     * Logs error messages.
     *
     * @param message String format message.
     */
    void error(String message);

    /**
     * Logs error messages with underlying throwable causes.
     *
     * @param message String format message.
     * @param cause Underlying exception.
     */
    void error(String message, Throwable cause);

    /**
     * Factory method to build a standard Java logger wrapper.
     *
     * @param name The logger namespace.
     * @return Logger implementation.
     */
    static GatotAuthLogger create(String name) {
        final Logger logger = Logger.getLogger(name);
        return new GatotAuthLogger() {
            @Override
            public void info(String message) {
                logger.info(message);
            }

            @Override
            public void warn(String message) {
                logger.warning(message);
            }

            @Override
            public void error(String message) {
                logger.severe(message);
            }

            @Override
            public void error(String message, Throwable cause) {
                logger.log(Level.SEVERE, message, cause);
            }
        };
    }
}
