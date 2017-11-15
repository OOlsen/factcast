package org.factcast.server.rest.resources;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionCleanupTimer extends Timer {

    private static final Duration TEN_SECONDS = Duration.ofSeconds(10); 
    @NonNull
    private final FactsObserver observer;

    void start() {

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                if (!observer.isConnectionAlive()) {
                    observer.unsubscribe();
                    this.cancel();
                    log.debug("unsubscribe and close cleanup timer");
                }

            }
        };
        this.schedule(task, TEN_SECONDS.toMillis(), TEN_SECONDS.toMillis());

    }

    public ConnectionCleanupTimer(@NonNull FactsObserver observer) {
        super(true);
        this.observer = observer;
    }

}
