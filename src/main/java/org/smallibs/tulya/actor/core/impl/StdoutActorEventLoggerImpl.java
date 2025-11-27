package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.ActorAddress;
import org.smallibs.tulya.actor.core.ActorEventLogger;

import java.time.Instant;
import java.util.Optional;

public class StdoutActorEventLoggerImpl implements ActorEventLogger {

    @Override
    public void log(Optional<ActorAddress> address, ActorAddress destination, Event event) {
        var nameSource = (address.map(ActorAddress::toString).orElse("") + " ".repeat(16)).substring(0, 16);
        var nameDestination = (destination + " ".repeat(16)).substring(0, 16);
        var threadId = (Thread.currentThread().threadId() + " ".repeat(8)).substring(0, 8);
        var date = (Instant.now().toString() + " ".repeat(19)).substring(0, 19);

        System.out.printf("%s | %s | %s ~> %s | %s%n", date, threadId, nameSource, nameDestination, event);
    }
}
