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
import org.osgi.service.log.LogService;

@org.apache.felix.scr.annotations.Properties({ @Property(name = "dataSourceFactory.target"),
        @Property(name = DataSourceFactory.JDBC_URL), @Property(name = DataSourceFactory.JDBC_USER),
        @Property(name = DataSourceFactory.JDBC_PASSWORD, passwordValue = ""),
        @Property(name = DataSourceFactory.JDBC_DATASOURCE_NAME), @Property(name = DataSourceFactory.JDBC_DESCRIPTION),
        @Property(name = DSFUtil.PROP_LOGIN_TIMEOUT, intValue = 0), @Property(name = "logService.target") })
@Component(metatype = true, configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
public class XADataSourceComponent {

    @Reference(policy = ReferencePolicy.STATIC)
    private DataSourceFactory dataSourceFactory;

    @Reference(policy = ReferencePolicy.STATIC)
    private LogService logService;

    private ServiceRegistration<XADataSource> serviceRegistration;

    private Map<String, Object> dataSourceFactoryProperties;

    @Activate
    public void activate(BundleContext bundleContext, Map<String, Object> componentProperties) {

        Properties jdbcProps = DSFUtil.collectDataSourceProperties(componentProperties);

        try {
            XADataSource xaDataSource = dataSourceFactory.createXADataSource(jdbcProps);

            Hashtable<String, Object> serviceProperties =
                    DSFUtil.collectDataSourceServiceProperties(componentProperties, dataSourceFactoryProperties);

            DSFUtil.initializeDataSource(xaDataSource, componentProperties, logService);

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

    public void bindLogService(LogService logService) {
        this.logService = logService;
    }

    @Deactivate
    public void deActivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

}
