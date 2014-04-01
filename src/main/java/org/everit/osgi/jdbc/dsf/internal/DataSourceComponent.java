/**
 * This file is part of Everit - DataSourceFactory Component.
 *
 * Everit - DataSourceFactory Component is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - DataSourceFactory Component is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - DataSourceFactory Component.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.jdbc.dsf.internal;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.everit.osgi.jdbc.dsf.DSFConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.log.LogService;

@org.apache.felix.scr.annotations.Properties({ @Property(name = "dataSourceFactory.target"),
        @Property(name = DataSourceFactory.JDBC_URL), @Property(name = DataSourceFactory.JDBC_NETWORK_PROTOCOL),
        @Property(name = DataSourceFactory.JDBC_SERVER_NAME), @Property(name = DataSourceFactory.JDBC_PORT_NUMBER),
        @Property(name = DataSourceFactory.JDBC_DATABASE_NAME), @Property(name = DataSourceFactory.JDBC_USER),
        @Property(name = DataSourceFactory.JDBC_PASSWORD, passwordValue = ""),
        @Property(name = DataSourceFactory.JDBC_DATASOURCE_NAME), @Property(name = DataSourceFactory.JDBC_DESCRIPTION),
        @Property(name = DataSourceFactory.JDBC_ROLE_NAME),
        @Property(name = DSFConstants.PROP_CUSTOM_PROPERTIES, unbounded = PropertyUnbounded.ARRAY),
        @Property(name = DSFUtil.PROP_LOGIN_TIMEOUT, intValue = 0), @Property(name = "logService.target") })
@Component(name = "org.everit.osgi.jdbc.dsf.DataSource", metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
public class DataSourceComponent {

    @Reference(policy = ReferencePolicy.STATIC)
    private DataSourceFactory dataSourceFactory;

    private Map<String, Object> dataSourceFactoryProperties;

    @Reference(policy = ReferencePolicy.STATIC)
    private LogService logService;

    private ServiceRegistration<DataSource> serviceRegistration;

    @Activate
    public void activate(final BundleContext bundleContext, final Map<String, Object> componentProperties) {
        Properties jdbcProps = DSFUtil.collectDataSourceProperties(componentProperties);

        try {
            DataSource dataSource = dataSourceFactory.createDataSource(jdbcProps);

            Hashtable<String, Object> serviceProperties =
                    DSFUtil.collectDataSourceServiceProperties(componentProperties, dataSourceFactoryProperties);

            DSFUtil.initializeDataSource(dataSource, componentProperties, logService);

            serviceRegistration = bundleContext.registerService(DataSource.class, dataSource, serviceProperties);
        } catch (SQLException e) {
            throw new RuntimeException("Error during creating DataSource with properties: "
                    + componentProperties.toString(), e);
        }
    }

    public void bindDataSourceFactory(final DataSourceFactory dataSourceFactory,
            final Map<String, Object> serviceProperties) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataSourceFactoryProperties = serviceProperties;
    }

    public void bindLogService(final LogService logService) {
        this.logService = logService;
    }

    @Deactivate
    public void deActivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }
}
