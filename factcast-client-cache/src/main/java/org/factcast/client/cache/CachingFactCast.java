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
package org.factcast.client.cache;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

import org.factcast.core.Fact;
import org.factcast.core.FactCast;
import org.factcast.core.subscription.Subscription;
import org.factcast.core.subscription.SubscriptionRequest;
import org.factcast.core.subscription.observer.FactObserver;
import org.factcast.core.subscription.observer.IdObserver;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Integrates local cache for Facts.
 * 
 * Note that this Wrapper does not necessarily make your consumers faster, as
 * the local caching might involve IO. Local caching is a good strategy where,
 * the same Fact is read very often by either restarting Consumers using
 * non-snapshot transient state, or where there are many local consumers that
 * share interest in the same Facts.
 * 
 * @author uwe.schaefer@mercateo.com
 *
 */

@Slf4j
@RequiredArgsConstructor
public class CachingFactCast implements FactCast {

    final FactCast delegate;

    final CachingFactLookup lookup;

    @Override
    public void publish(@NonNull List<? extends Fact> factsToPublish) {
        delegate.publish(factsToPublish);
    }

    @Override
    public Subscription subscribeToIds(@NonNull SubscriptionRequest req,
            @NonNull IdObserver observer) {
        return delegate.subscribeToIds(req, observer);
    }

    @Override
    public Subscription subscribeToFacts(@NonNull SubscriptionRequest req,
            @NonNull FactObserver observer) {

        log.debug("changing Fact Subscription to Id subscription for caching single Fact lookups");

        return subscribeToIds(req, new IdObserver() {

            @Override
            public void onNext(UUID f) {
                fetchById(f).ifPresent(observer::onNext);
            }

            @Override
            public void onCatchup() {
                observer.onCatchup();
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }
        });
    }

    @Override
    public Optional<Fact> fetchById(UUID id) {
        return lookup.lookup(id);
    }

    @Override
    public OptionalLong serialOf(@NonNull UUID ids) {
        return delegate.serialOf(ids);
    }

}
