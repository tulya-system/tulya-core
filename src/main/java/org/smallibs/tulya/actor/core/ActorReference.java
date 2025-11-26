package org.smallibs.tulya.actor.core;

import org.smallibs.tulya.standard.Try;

public interface ActorReference<Protocol> {

    ActorAddress address();

    <AnotherProtocol> Try<ActorReference<AnotherProtocol>> create(String name, BehaviorBuilder<AnotherProtocol> actor);

    boolean tell(Protocol message);

    void dispose();

    <T> ResponseHandler<T> reponseHandler();

}
