package org.smallibs.tulya.actor.core;

import org.smallibs.tulya.standard.Try;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public interface ActorReference<Protocol> {

    ActorAddress address();

    @SuppressWarnings("TypeParameterHidesVisibleType")
    <Protocol> Try<ActorReference<Protocol>> create(String name, BehaviorBuilder<Protocol> actor);

    boolean tell(Protocol message);

    void dispose();

    <T> ResponseHandler<T> reponseHandler();

    default void delay(Duration duration) {
        try {
            reponseHandler().await(duration);
        } catch (TimeoutException e) {
            // Nominal case
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
