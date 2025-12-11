package org.smallibs.tulya.actor.core;

import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.standard.Try;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public interface ActorReference<Protocol> {

    ActorAddress address();

    @SuppressWarnings("TypeParameterHidesVisibleType")
    <Protocol> Try<ActorReference<Protocol>> tryCreate(String name, BehaviorBuilder<Protocol> actor);

    @SuppressWarnings("TypeParameterHidesVisibleType")
    default <Protocol> ActorReference<Protocol> create(String name, BehaviorBuilder<Protocol> actor) throws Throwable {
        return tryCreate(name, actor).orElseThrow();
    }

    boolean tell(Protocol message);

    default <R> Promise<R> ask(BehaviorCall<Protocol, R> message) {
        var response = this.<R>responseHandler();
        tell(message.apply(response.solvable()));
        return response;
    }

    void dispose();

    <T> ResponseHandler<T> responseHandler();

    default void delay(Duration duration) {
        try {
            responseHandler().await(duration);
        } catch (TimeoutException e) {
            // Nominal case
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
