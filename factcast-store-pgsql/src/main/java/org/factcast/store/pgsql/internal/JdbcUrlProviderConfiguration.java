package org.factcast.store.pgsql.internal;

import org.factcast.store.pgsql.internal.cloud.RdsInstanceInformationRetriever;
import org.factcast.store.pgsql.internal.cloud.RdsJdbcUrlProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.amazonaws.services.rds.AmazonRDS;

/**
 * jdbc url provider configuration class which will always try to rise exactly
 * one jdbc url provider </br>
 * 
 * use <b>cloud.aws.db.rds.conf.enable=true</b> to use the rds remote call based
 * configuration, omit the parameter or set it to false to use the jdbc string
 * based configuration
 *
 */

public class JdbcUrlProviderConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "cloud.aws.db.rds.conf", name = "enable", havingValue = "false", matchIfMissing = true)
    JdbcUrlProvider jdbcUrlProvider(@Value("${spring.datasource.url}") String url) {

        return new JdbcUrlProvider() {

            @Override
            public String provide() {
                return url;
            }
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "cloud.aws.db.rds.conf", name = "enable", havingValue = "true")
    JdbcUrlProvider jdbcRdsUrlProvider(AmazonRDS amazonRds,
            @Value("${cloud.aws.db.identifier}") String dbIdentifier,
            @Value("${cloud.aws.db.password}") String dbPassword) {

        return new RdsJdbcUrlProvider(new RdsInstanceInformationRetriever(amazonRds), dbIdentifier,
                dbPassword);
    }

}
