/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.spi.logging;

/**
 *
 */
public interface KLogger {

    enum Level {
        OFF,
        ERROR,
        WARNING,
        INFO,
        DEBUG,
        TRACE;

        public static Level level(String level) {
            if (level == null)
                return null;

            for (Level myLevel : Level.values()) {
                if (myLevel.name().equalsIgnoreCase(level))
                    return myLevel;
            }

            return null;
        }
    }

    /**
     * Dispose of this logger, releasing any resources
     */
    void dispose();

    /**
     * @return path where logging is taking place
     * @throws Exception
     */
    String getLogPath() throws Exception;

    /**
     * Set the path where the logger will log to
     *
     * @param logPath
     * @throws Exception
     */
    void setLogPath(String logPath) throws Exception;

    /**
     * Set the logging level
     *
     * @param level preferred level
     * @throws Exception exception if level change fails
     */
    void setLevel(Level level) throws Exception;

    /**
     * @param message
     * @param args
     */
    void info(String message, Object... args);

    /**
     * @param message
     * @param throwable
     * @param args
     */
    void info(String message, Throwable throwable, Object... args);

    /**
     * @return <code>true</code> if info logging is enabled
     */
    boolean isInfoEnabled();

    /**
     * @param message
     * @param args
     */
    void warn(String message, Object... args);

    /**
     * @param message
     * @param throwable
     * @param args
     */
    void warn(String message, Throwable throwable, Object... args);

    /**
     * @return <code>true</code> if warning logging is enabled
     */
    boolean isWarnEnabled();

    /**
     * @param message
     * @param args
     */
    void error(String message, Object... args);

    /**
     * @param message
     * @param throwable
     * @param args
     */
    void error(String message, Throwable throwable, Object... args);

    /**
     * @return <code>true</code> if error logging is enabled
     */
    boolean isErrorEnabled();

    /**
     * @param message
     * @param args
     */
    void debug(String message, Object... args);

    /**
     * @param message
     * @param throwable
     * @param args
     */
    void debug(String message, Throwable throwable, Object... args);

    /**
     * @return <code>true</code> if debug logging is enabled
     */
    boolean isDebugEnabled();

    /**
     * @param message
     * @param args
     */
    void trace(String message, Object... args);

    /**
     * @param message
     * @param throwable
     * @param args
     */
    void trace(String message, Throwable throwable, Object... args);

    /**
     * @return <code>true</code> if trace logging is enabled
     */
    boolean isTraceEnabled();

}
