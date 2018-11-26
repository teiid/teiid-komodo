/*
 * Copyright Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags and
 * the COPYRIGHT.txt file distributed with this work.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.komodo.utils;

import org.komodo.logging.DefaultKLogger;
import org.komodo.spi.logging.KLogger;

/**
 *
 */
public class KLog implements KLogger {

    private static KLog instance;

    /**
     * @return singleton instance of this logger
     */
    public static KLog getLogger() {
        if (instance == null)
            instance = new KLog();

        return instance;
    }

    private final KLogger kLogger;

    /**
     *
     */
    private KLog() {
        kLogger = new DefaultKLogger();
    }

    @Override
    public void dispose() {
        kLogger.dispose();
    }

    @Override
    public String getLogPath() throws Exception {
        return kLogger.getLogPath();
    }

    @Override
    public synchronized void setLogPath(String logPath) throws Exception {
        kLogger.setLogPath(logPath);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#setLevel(java.util.logging.Level)
     */
    @Override
    public synchronized void setLevel(Level level) throws Exception {
        kLogger.setLevel(level);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#info(java.lang.String, java.lang.Object[])
     */
    @Override
    public synchronized void info(String message, Object... args) {
        kLogger.info(message, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#info(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
    public synchronized void info(String message, Throwable throwable, Object... args) {
        kLogger.info(message, throwable, args);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.logging.KLogger#isInfoEnabled()
     */
    @Override
    public boolean isInfoEnabled() {
        return this.kLogger.isInfoEnabled();
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#warn(java.lang.String, java.lang.Object[])
     */
    @Override
    public synchronized void warn(String message, Object... args) {
        kLogger.warn(message, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#warn(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
    public synchronized void warn(String message, Throwable throwable, Object... args) {
        kLogger.warn(message, throwable, args);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.logging.KLogger#isWarnEnabled()
     */
    @Override
    public boolean isWarnEnabled() {
        return this.kLogger.isWarnEnabled();
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#error(java.lang.String, java.lang.Object[])
     */
    @Override
    public synchronized void error(String message, Object... args) {
        kLogger.error(message, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#error(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
    public synchronized void error(String message, Throwable throwable, Object... args) {
        kLogger.error(message, throwable, args);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.logging.KLogger#isErrorEnabled()
     */
    @Override
    public boolean isErrorEnabled() {
        return this.kLogger.isErrorEnabled();
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#debug(java.lang.String, java.lang.Object[])
     */
    @Override
    public synchronized void debug(String message, Object... args) {
        kLogger.debug(message, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#debug(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
    public synchronized void debug(String message, Throwable throwable, Object... args) {
        kLogger.debug(message, throwable, args);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.logging.KLogger#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled() {
        return this.kLogger.isDebugEnabled();
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#trace(java.lang.String, java.lang.Object[])
     */
    @Override
    public synchronized void trace(String message, Object... args) {
        kLogger.trace(message, args);
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.logging.KLogger#trace(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
    public synchronized void trace(String message, Throwable throwable, Object... args) {
        kLogger.trace(message, throwable, args);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.logging.KLogger#isTraceEnabled()
     */
    @Override
    public boolean isTraceEnabled() {
        return this.kLogger.isTraceEnabled();
    }

}
