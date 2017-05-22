package org.factcast.store.pgsql.internal.cloud;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;

import lombok.NonNull;

/**
 * retrieve rds db instance information from the remote aws rds service for a
 * given db identifier
 *
 */

public class RdsInstanceInformationRetriever {

    private final AmazonRDS rds;

    public RdsInstanceInformationRetriever(@NonNull AmazonRDS rds) {
        this.rds = rds;
    }

    /**
     * If there is no matching dbInstance, the call will fail with a DBInstanceNotFoundException. 
     * 
     * @param dbIdentifier
     * @return DBInstance
     */

    public DBInstance getDbInstanceInfo(@NonNull String dbIdentifier) {
        DescribeDBInstancesRequest describeDBInstancesRequest = new DescribeDBInstancesRequest()
                .withDBInstanceIdentifier(dbIdentifier);
        DescribeDBInstancesResult describeDBInstances = rds.describeDBInstances(
                describeDBInstancesRequest);

        DBInstance dbInstance = describeDBInstances.getDBInstances().get(0);
        return dbInstance;
    }
}
