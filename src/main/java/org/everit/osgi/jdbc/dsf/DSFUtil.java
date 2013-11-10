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

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.osgi.service.jdbc.DataSourceFactory;

public final class DSFUtil {

    private static void putIfNotNull(Map<String, Object> source, Hashtable<? super String, Object> target,
            String key) {
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

    public static Hashtable<String, Object> collectDataSourceServiceProperties(
            Map<String, Object> componentProperties, Map<String, Object> dsfServiceProperties) {
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

    private DSFUtil() {
    }
}
