package org.factcast.store.pgsql.internal.cloud;

import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.amazonaws.services.rds.AmazonRDS;

public class RdsInstanceInformationRetrieverTest {

    RdsInstanceInformationRetriever uut;

    @Mock
    private AmazonRDS rds;

    @Before
    public void prepare() {

        initMocks(this);
        uut = new RdsInstanceInformationRetriever(rds);
    }

    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void test_constructor() {

        new RdsInstanceInformationRetriever(null);
    }

    @Test(expected = NullPointerException.class)
    public void test_getDbInstanceInfo() {

        uut.getDbInstanceInfo(null);
    }

}
