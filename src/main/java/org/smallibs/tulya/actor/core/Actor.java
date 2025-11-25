package org.smallibs.tulya.actor.core;

public interface Actor<Protocol> {

    boolean tell(Extended<Protocol> message);

}
