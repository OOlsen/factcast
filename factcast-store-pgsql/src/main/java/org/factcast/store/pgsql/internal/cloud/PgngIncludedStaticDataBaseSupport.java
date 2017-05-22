package org.factcast.store.pgsql.internal.cloud;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.aws.jdbc.datasource.support.DatabaseType;
import org.springframework.cloud.aws.jdbc.datasource.support.StaticDatabasePlatformSupport;

/**
 * This class is necessary to change the postgresql driver and schema when we
 * resolve the db information with rds. The other mappings are copied from
 * {@link org.springframework.cloud.aws.jdbc.datasource.support.StaticDatabasePlatformSupport}
 * 
 *
 */

public class PgngIncludedStaticDataBaseSupport extends StaticDatabasePlatformSupport {

    private static final String JDBC_SCHEME_NAME = "jdbc:";

    @Override
    protected Map<DatabaseType, String> getSchemeNames() {
        HashMap<DatabaseType, String> schemeNamesMappings = new HashMap<>();
        schemeNamesMappings.put(DatabaseType.MYSQL, JDBC_SCHEME_NAME + "mysql");
        schemeNamesMappings.put(DatabaseType.ORACLE, JDBC_SCHEME_NAME + "oracle:thin");
        schemeNamesMappings.put(DatabaseType.SQLSERVER, JDBC_SCHEME_NAME + "jtds:sqlserver");
        schemeNamesMappings.put(DatabaseType.POSTGRES, JDBC_SCHEME_NAME + "pgsql");
        schemeNamesMappings.put(DatabaseType.MARIA, JDBC_SCHEME_NAME + "mariadb");
        schemeNamesMappings.put(DatabaseType.AURORA, schemeNamesMappings.get(DatabaseType.MYSQL));
        return Collections.unmodifiableMap(schemeNamesMappings);
    }

    @Override
    protected Map<DatabaseType, String> getDriverClassNameMappings() {
        HashMap<DatabaseType, String> driverClassNameMappings = new HashMap<>();
        driverClassNameMappings.put(DatabaseType.MYSQL, "com.mysql.jdbc.Driver");
        driverClassNameMappings.put(DatabaseType.ORACLE, "oracle.jdbc.OracleDriver");
        driverClassNameMappings.put(DatabaseType.SQLSERVER, "net.sourceforge.jtds.jdbc.Driver");
        driverClassNameMappings.put(DatabaseType.POSTGRES, "com.impossibl.postgres.jdbc.PGDriver");
        driverClassNameMappings.put(DatabaseType.MARIA, "org.mariadb.jdbc.Driver");
        driverClassNameMappings.put(DatabaseType.AURORA, driverClassNameMappings.get(
                DatabaseType.MYSQL));
        return Collections.unmodifiableMap(driverClassNameMappings);
    }

}
