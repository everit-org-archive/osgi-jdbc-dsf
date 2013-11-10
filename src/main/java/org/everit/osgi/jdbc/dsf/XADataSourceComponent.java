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
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.sql.XADataSource;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;

@org.apache.felix.scr.annotations.Properties({
        @Property(name = DataSourceFactory.JDBC_URL, value = "", label = "Jdbc Url",
                description = "The Jdbc Url that will be provided during the DataSourceFactory.createXADataSource()"
                        + " call."),

        @Property(name = DataSourceFactory.JDBC_USER, value = "", label = "User name",
                description = "The name of the user that is used during database authentication"),

        @Property(name = DataSourceFactory.JDBC_PASSWORD, passwordValue = "", label = "Password",
                description = "Password that is used during database access."),

        @Property(name = DataSourceFactory.JDBC_DATASOURCE_NAME, value = "", label = "DataSource name",
                description = "Name of the data source."),

        @Property(name = DataSourceFactory.JDBC_DESCRIPTION, value = "", label = "DataSource description.",
                description = "Description of the data source.") })
@Component(label = "XADataSource (Everit)", metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
public class XADataSourceComponent {

    @Property(name = "dataSourceFactory.target", label = "DataSourceFactory Service Filter",
            description = "Filter of the DataSourceFactory OSGi service")
    @Reference(policy = ReferencePolicy.STATIC)
    private DataSourceFactory dataSourceFactory;

    private ServiceRegistration<XADataSource> serviceRegistration;

    private Map<String, Object> dataSourceFactoryProperties;

    @Activate
    public void activate(BundleContext bundleContext, Map<String, Object> componentProperties) {

        Properties jdbcProps = DSFUtil.collectDataSourceProperties(componentProperties);

        try {
            XADataSource xaDataSource = dataSourceFactory.createXADataSource(jdbcProps);

            Hashtable<String, Object> serviceProperties =
                    DSFUtil.collectDataSourceServiceProperties(componentProperties, dataSourceFactoryProperties);

            serviceRegistration = bundleContext.registerService(XADataSource.class, xaDataSource, serviceProperties);
        } catch (SQLException e) {
            throw new RuntimeException("Error during creating XADataSource with properties: "
                    + componentProperties.toString(), e);
        }
    }

    public void bindDataSourceFactory(DataSourceFactory dataSourceFactory, Map<String, Object> serviceProperties) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataSourceFactoryProperties = serviceProperties;
    }

    @Deactivate
    public void deActivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

}
