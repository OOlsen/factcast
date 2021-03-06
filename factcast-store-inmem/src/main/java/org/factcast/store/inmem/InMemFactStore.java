/**
 * Copyright © 2018 Mercateo AG (http://www.mercateo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.factcast.store.inmem;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.factcast.core.Fact;
import org.factcast.core.spec.FactSpecMatcher;
import org.factcast.core.store.FactStore;
import org.factcast.core.subscription.Subscription;
import org.factcast.core.subscription.SubscriptionImpl;
import org.factcast.core.subscription.SubscriptionRequestTO;
import org.factcast.core.subscription.observer.FactObserver;
import org.springframework.beans.factory.DisposableBean;

import com.google.common.annotations.VisibleForTesting;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Eternally-growing InMem Implementation of a FactStore. USE FOR TESTING
 * PURPOSES ONLY
 *
 * @author uwe.schaefer@mercateo.com, joerg.adler@mercateo.com
 *
 */
@Deprecated
@Slf4j
public class InMemFactStore implements FactStore, DisposableBean {
    final AtomicLong highwaterMark = new AtomicLong(0);

    final LinkedHashMap<Long, Fact> store = new LinkedHashMap<>();

    final Set<UUID> ids = new HashSet<>();

    final CopyOnWriteArrayList<InMemFollower> activeFollowers = new CopyOnWriteArrayList<>();

    final ExecutorService executorService;

    @VisibleForTesting
    InMemFactStore(@NonNull ExecutorService es) {
        executorService = es;

        if (Package.getPackage("org.junit") == null) {

            log.warn("");
            log.warn(
                    "***********************************************************************************************************");
            log.warn(
                    "* You are using an inmem-impl of a FactStore. This implementation is for quick testing ONLY and will fail *");
            log.warn(
                    "*   with OOM if you load it with a significant amount of Facts.                                           *");
            log.warn(
                    "***********************************************************************************************************");
            log.warn("");
        }
    }

    public InMemFactStore() {
        this(Executors.newCachedThreadPool());
    }

    @RequiredArgsConstructor
    class AfterPredicate implements Predicate<Fact> {
        final UUID after;

        boolean flipSwitch = false;

        @Override
        public boolean test(Fact t) {
            if (flipSwitch) {
                return true;
            }

            flipSwitch = after.equals(t.id());
            return false;
        }
    }

    private class InMemFollower implements Predicate<Fact>, Consumer<Fact>, AutoCloseable {
        Predicate<Fact> matcher;

        final SubscriptionImpl<Fact> subscription;

        InMemFollower(SubscriptionRequestTO request, SubscriptionImpl<Fact> subscription) {
            this.subscription = subscription;
            Predicate<Fact> anyOf = FactSpecMatcher.matchesAnyOf(request.specs());
            if (request.startingAfter().isPresent()) {
                AfterPredicate afterPredicate = new AfterPredicate(request.startingAfter().get());
                matcher = f -> afterPredicate.test(f) && anyOf.test(f);
            } else {
                matcher = anyOf;
            }
        }

        @Override
        public void close() {
            synchronized (InMemFactStore.this) {
                activeFollowers.remove(this);
            }
        }

        @Override
        public boolean test(Fact f) {
            return matcher.test(f);
        }

        @Override
        public void accept(Fact t) {
            subscription.notifyElement(t);
        }

    }

    @Override
    public synchronized Optional<Fact> fetchById(@NonNull UUID id) {
        Stream<Entry<Long, Fact>> stream = store.entrySet().stream();
        return stream.filter(e -> e.getValue().id().equals(id)).findFirst().map(e -> e.getValue());
    }

    @Override
    public synchronized void publish(@NonNull List<? extends Fact> factsToPublish) {

        if (factsToPublish.stream().anyMatch(f -> ids.contains(f.id()))) {
            throw new IllegalArgumentException("duplicate ids - ids must be unique!");
        }

        factsToPublish.forEach(f -> {
            long ser = highwaterMark.incrementAndGet();

            Fact inMemFact = new InMemFact(ser, f);

            store.put(ser, inMemFact);
            ids.add(inMemFact.id());

            List<InMemFollower> subscribers = activeFollowers.stream()
                    .filter(s -> s.test(inMemFact))
                    .collect(Collectors.toList());
            subscribers.forEach(s -> s.accept(inMemFact));
        });
    }

    @Override
    public synchronized Subscription subscribe(SubscriptionRequestTO request,
            FactObserver observer) {

        SubscriptionImpl<Fact> subscription = SubscriptionImpl.on(observer);
        InMemFollower s = new InMemFollower(request, subscription);

        executorService.submit(() -> {

            // catchup
            if (!request.ephemeral()) {
                store.values().stream().filter(s).forEachOrdered(s);
            }

            if (request.continuous()) {
                activeFollowers.add(s);
            }

            subscription.notifyCatchup();

            // follow
            if (!request.continuous()) {
                subscription.notifyComplete();
            }

        });

        return subscription.onClose(s::close);
    }

    @Override
    public synchronized void destroy() throws Exception {
        executorService.shutdown();
    }

    @Override
    public OptionalLong serialOf(UUID l) {
        // hilariously inefficient
        for (Map.Entry<Long, Fact> e : store.entrySet()) {
            if (l.equals(e.getValue().id())) {
                return OptionalLong.of(e.getKey().longValue());
            }
        }
        return OptionalLong.empty();
    }

}
