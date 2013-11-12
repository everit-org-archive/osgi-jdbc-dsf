package org.everit.osgi.jdbc.dsf;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sql.CommonDataSource;

import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.log.LogService;

public final class DSFUtil {

    public static final String PROP_LOGIN_TIMEOUT = "loginTimeout";

    private static void putIfNotNull(Map<String, Object> source, Hashtable<? super String, Object> target, String key) {
        Object value = source.get(key);
        if (value != null && !"".equals(value.toString().trim())) {
            target.put(key, value);
        }
    }

    private static void putVisibleProperties(Map<String, Object> source, Hashtable<? super String, Object> target) {
        putIfNotNull(source, target, DataSourceFactory.JDBC_URL);
        putIfNotNull(source, target, DataSourceFactory.JDBC_USER);
        putIfNotNull(source, target, DataSourceFactory.JDBC_DATASOURCE_NAME);
        putIfNotNull(source, target, DataSourceFactory.JDBC_DESCRIPTION);
    }

    public static Properties collectDataSourceProperties(Map<String, Object> componentProperties) {
        Properties jdbcProps = new Properties();
        putVisibleProperties(componentProperties, jdbcProps);
        putIfNotNull(componentProperties, jdbcProps, DataSourceFactory.JDBC_PASSWORD);
        return jdbcProps;
    }

    public static Hashtable<String, Object> collectDataSourceServiceProperties(Map<String, Object> componentProperties,
            Map<String, Object> dsfServiceProperties) {
        Hashtable<String, Object> serviceProperties = new Hashtable<String, Object>();
        putIfNotNull(componentProperties, serviceProperties, "service.pid");
        putIfNotNull(componentProperties, serviceProperties, DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        putIfNotNull(componentProperties, serviceProperties, DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        putIfNotNull(componentProperties, serviceProperties, DataSourceFactory.OSGI_JDBC_DRIVER_VERSION);
        putVisibleProperties(componentProperties, serviceProperties);

        if (dsfServiceProperties != null) {
            putIfNotNull(dsfServiceProperties, serviceProperties, DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
            putIfNotNull(dsfServiceProperties, serviceProperties, DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
            putIfNotNull(dsfServiceProperties, serviceProperties, DataSourceFactory.OSGI_JDBC_DRIVER_VERSION);
        }
        return serviceProperties;
    }

    public static void initializeDataSource(final CommonDataSource commonDataSource,
            final Map<String, Object> componentProperties, final LogService logService) {
        Integer loginTimeout = (Integer) componentProperties.get(PROP_LOGIN_TIMEOUT);
        if (loginTimeout != null) {
            try {
                commonDataSource.setLoginTimeout(loginTimeout);
            } catch (SQLException e) {
                throw new RuntimeException("Could not set timeout on data source" + commonDataSource.toString(), e);
            }
        }

        try {
            commonDataSource.setLogWriter(new PrintWriter(new Writer() {

                ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<String>();

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    String message = String.valueOf(cbuf, off, len);
                    messageQueue.add(message);
                }

                @Override
                public void flush() throws IOException {
                    StringBuilder sb = new StringBuilder();
                    String message = messageQueue.poll();
                    while (message != null) {
                        sb.append(message);
                        message = messageQueue.poll();
                    }
                    if (sb.length() > 0) {
                        logService.log(LogService.LOG_INFO, sb.toString());
                    }
                }

                @Override
                public void close() throws IOException {
                    // Do nothing

                }
            }));
        } catch (SQLException e) {
            throw new RuntimeException("Error during setting logWrtier to dataSource:" + commonDataSource.toString());
        }
    }

    private DSFUtil() {
    }
}
