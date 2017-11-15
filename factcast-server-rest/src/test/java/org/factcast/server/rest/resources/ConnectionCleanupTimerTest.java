package org.factcast.server.rest.resources;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.Duration;
import java.util.Timer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class ConnectionCleanupTimerTest {

    @Mock
    private FactsObserver observer;

    Timer timer = Mockito.spy(new Timer());

    ConnectionCleanupTimer uut;

    @Before
    public void prepare() {
        initMocks(this);

        uut = new ConnectionCleanupTimer(timer, Duration.ofMillis(10), Duration.ofMillis(100),
                observer);
    }

    @SuppressWarnings("boxing")
    @Test
    public void test_connectionClosed() {

        // when
        uut.start();

        // then
        verify(observer, timeout(1000)).unsubscribe();

        verify(timer).cancel();

    }

    @SuppressWarnings("boxing")
    @Test
    public void test_connectionOpen() {

        // given
        when(observer.isConnectionAlive()).thenReturn(true);

        // when
        uut.start();

        // then
        verify(observer, timeout(1000)).isConnectionAlive();

        verifyNoMoreInteractions(observer);

        verifyZeroInteractions(timer);

    }

}
