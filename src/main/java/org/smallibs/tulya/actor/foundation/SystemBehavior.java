package org.smallibs.tulya.actor.foundation;

import org.smallibs.tulya.actor.core.ActorReference;
import org.smallibs.tulya.actor.core.Behavior;
import org.smallibs.tulya.standard.Unit;

public record SystemBehavior(ActorReference<Unit> self) implements Behavior<Unit> {
    @Override
    public void tell(Unit message) {
        // Do nothing for the moment
    }
}
