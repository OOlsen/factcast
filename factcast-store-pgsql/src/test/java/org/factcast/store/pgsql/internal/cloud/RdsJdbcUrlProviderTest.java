package org.factcast.store.pgsql.internal.cloud;

import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.Endpoint;

public class RdsJdbcUrlProviderTest {

    RdsJdbcUrlProvider uut;

    @Mock
    private RdsInstanceInformationRetriever rds;

    private String dbIdentifier = "testDbIdentifier";

    private String pw = "testPw";

    @Before
    public void prepare() {

        initMocks(this);
        uut = new RdsJdbcUrlProvider(rds, dbIdentifier, pw);
    }

    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void test_Constructor() throws Exception {
        new RdsInstanceInformationRetriever(null);
    }

    @Test
    public void test_createPostgresUrl() {

        // given
        DBInstance value = mock(DBInstance.class);
        Endpoint endpoint = new Endpoint().withAddress("testHost").withPort(new Integer(45454));
        when(value.getEndpoint()).thenReturn(endpoint);
        when(value.getEngine()).thenReturn("POSTGRESQL");
        when(value.getDBName()).thenReturn("testDbName");
        when(value.getMasterUsername()).thenReturn("testUser");
        when(rds.getDbInstanceInfo(dbIdentifier)).thenReturn(value);

        // when
        String url = uut.provide();

        // then
        assertEquals("jdbc:pgsql://testHost:45454/testDbName?user=testUser&password=testPw", url);

    }

    @Test(expected = IllegalStateException.class)
    public void test_unsuppDbEngine() {

        // given
        DBInstance value = mock(DBInstance.class);
        Endpoint endpoint = new Endpoint().withAddress("testHost").withPort(new Integer(45454));
        when(value.getEndpoint()).thenReturn(endpoint);
        when(value.getEngine()).thenReturn("FanyNewDBEngine");
        when(value.getDBName()).thenReturn("testDbName");
        when(value.getMasterUsername()).thenReturn("testUser");
        when(rds.getDbInstanceInfo(dbIdentifier)).thenReturn(value);

        // when
        uut.provide();

        // then throw exception
        failBecauseExceptionWasNotThrown(IllegalStateException.class);

    }
}
