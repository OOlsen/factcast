package org.factcast.store.pgsql.internal;

import javax.inject.Inject;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.google.common.base.Supplier;
import com.impossibl.postgres.api.jdbc.PGConnection;

import lombok.SneakyThrows;

/**
 * Supplies a PGConnection directly from an un-pooled DataSource
 * 
 * @author uwe.schaefer@mercateo.com
 *
 */
class EnvironmentPGConnectionSupplier implements Supplier<PGConnection> {

    DriverManagerDataSource dataSource;

    @Inject
    public EnvironmentPGConnectionSupplier(JdbcUrlProvider urlprovider) {
        this.dataSource = new DriverManagerDataSource();
        dataSource.setUrl(urlprovider.provide());
    }

    @Override
    @SneakyThrows
    public PGConnection get() {
        return (PGConnection) dataSource.getConnection();
    }

}
