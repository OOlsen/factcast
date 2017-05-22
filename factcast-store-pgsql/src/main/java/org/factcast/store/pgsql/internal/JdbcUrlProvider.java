package org.factcast.store.pgsql.internal;


/**
 *provides a jdbc connection string for a relational database 
 *
 */

public interface JdbcUrlProvider {

    String provide();
}
