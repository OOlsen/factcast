package org.factcast.store.pgsql.internal.cloud;

import org.factcast.store.pgsql.internal.JdbcUrlProvider;
import org.springframework.cloud.aws.jdbc.datasource.support.DatabaseType;

import com.amazonaws.services.rds.model.DBInstance;

import lombok.NonNull;

/**
 * provides a jdbc db url containing also the user credentials
 * </br>
 * e.g. <b>jdbc:pgsql://testhost:5432/testDB?username=testuser&password=testpw</b>
 *
 */

public class RdsJdbcUrlProvider implements JdbcUrlProvider {

    private final RdsInstanceInformationRetriever rds;

    private final String dbIdentifier;

    private final String pw;

    public RdsJdbcUrlProvider(@NonNull RdsInstanceInformationRetriever rds,
            @NonNull String dbIdentifier, @NonNull String pw) {
        this.rds = rds;
        this.dbIdentifier = dbIdentifier;
        this.pw = pw;
    }

    @Override
    public String provide() {

        DBInstance dbInstance = rds.getDbInstanceInfo(dbIdentifier);

        String databaseUrlForDatabase = new PgngIncludedStaticDataBaseSupport()
                .getDatabaseUrlForDatabase(DatabaseType.fromEngine(dbInstance.getEngine()),
                        dbInstance.getEndpoint().getAddress(), dbInstance.getEndpoint().getPort()
                                .intValue(), dbInstance.getDBName());

        return enrichUrlWithCredentials(databaseUrlForDatabase, dbInstance.getMasterUsername(), pw);

    }

    private String enrichUrlWithCredentials(String url, String userName, String pw) {

        return new StringBuilder(url).append("?user=").append(userName).append("&password=").append(
                pw).toString();
    }

}
