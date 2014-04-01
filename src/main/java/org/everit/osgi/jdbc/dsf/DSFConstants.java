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
package org.everit.osgi.jdbc.dsf;

public final class DSFConstants {

    public static final String COMPONENT_NAME_DATASOURCE = "org.everit.osgi.jdbc.dsf.DataSource";

    public static final String COMPONENT_NAME_XA_DATASOURCE = "org.everit.osgi.jdbc.dsf.XADataSource";

    /**
     * Configuration property name of custom JDBC properties String array.
     */
    public static final String PROP_CUSTOM_PROPERTIES = "customProperties";

    private DSFConstants() {
    }
}
