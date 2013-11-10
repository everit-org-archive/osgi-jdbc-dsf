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

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.jdbc.DataSourceFactory;

@Component(label = "DataSource (Everit)", metatype = true, configurationFactory = true)
public class DataSourceComponent {
    @Property(name = DataSourceFactory.JDBC_URL, value = "", label = "Jdbc Url",
            description = "The Jdbc Url that will be provided during the DataSourceFactory.createXADataSource() call.")
    public static final String PROP_JDBC_URL = DataSourceFactory.JDBC_URL;

    @Property(name = DataSourceFactory.JDBC_USER, value = "", label = "User name",
            description = "The name of the user that is used during database authentication")
    public static final String PROP_JDBC_USER = DataSourceFactory.JDBC_USER;

    @Property(name = DataSourceFactory.JDBC_PASSWORD, passwordValue = "", label = "Password",
            description = "Password that is used during database access.")
    public static final String PROP_JDBC_PASSWORD = DataSourceFactory.JDBC_PASSWORD;

    @Property(name = DataSourceFactory.JDBC_DATASOURCE_NAME, value = "", label = "DataSource name",
            description = "Name of the data source.")
    public static final String PROP_JDBC_DATASOURCE_NAME = DataSourceFactory.JDBC_DATASOURCE_NAME;

    @Property(name = DataSourceFactory.JDBC_DESCRIPTION, value = "", label = "DataSource description.",
            description = "Description of the data source.")
    public static final String PROP_JDBC_DESCRIPTION = DataSourceFactory.JDBC_DESCRIPTION;

    private static void putIfNotNull(Dictionary<String, Object> source, Hashtable<? super String, Object> target,
            String key) {
        Object value = source.get(key);
        if (value != null && !"".equals(value.toString().trim())) {
            target.put(key, value);
        }
    }

    private static void putVisibleProperties(Dictionary<String, Object> source, Hashtable<? super String, Object> target) {
        putIfNotNull(source, target, PROP_JDBC_URL);
        putIfNotNull(source, target, PROP_JDBC_USER);
        putIfNotNull(source, target, PROP_JDBC_DATASOURCE_NAME);
        putIfNotNull(source, target, PROP_JDBC_DESCRIPTION);
    }

    @Property(name = "dataSourceFactory.target", label = "DataSourceFactory Service Filter",
            description = "Filter of the DataSourceFactory OSGi service")
    @Reference(policy = ReferencePolicy.STATIC)
    private DataSourceFactory dataSourceFactory;

    private ServiceRegistration<DataSource> serviceRegistration;

    @Activate
    public void activate(ComponentContext componentContext) {
        Properties jdbcProps = new Properties();
        System.out.println(componentContext.getProperties());

        @SuppressWarnings("unchecked")
        Dictionary<String, Object> componentProperties = componentContext.getProperties();
        putVisibleProperties(componentProperties, jdbcProps);
        putIfNotNull(componentProperties, jdbcProps, PROP_JDBC_PASSWORD);

        try {
            DataSource dataSource = dataSourceFactory.createDataSource(jdbcProps);
            BundleContext bundleContext = componentContext.getBundleContext();

            Hashtable<String, Object> serviceProperties = new Hashtable<String, Object>();
            putIfNotNull(componentProperties, serviceProperties, "service.pid");
            putVisibleProperties(componentProperties, serviceProperties);

            serviceRegistration = bundleContext.registerService(DataSource.class, dataSource, serviceProperties);
        } catch (SQLException e) {
            throw new RuntimeException("Error during creating DataSource with properties: "
                    + componentProperties.toString(), e);
        }
    }

    public void bindDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Deactivate
    public void deActivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }
}
