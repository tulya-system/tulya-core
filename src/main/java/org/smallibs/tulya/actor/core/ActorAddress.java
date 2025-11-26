package org.smallibs.tulya.actor.core;

import java.util.Optional;

public record ActorAddress(Optional<ActorAddress> parent, String name) {
    public ActorAddress child(String name) {
        return new ActorAddress(Optional.of(this), name);
    }

    public boolean isParentOf(ActorAddress address) {
        return address.parent.map(parent -> parent.equals(this)).orElse(false);
    }

    public boolean isChildOf(ActorAddress address) {
        return address.isParentOf(this);
    }

    public boolean isAncestorOf(ActorAddress address) {
        return address.parent.map(parent -> parent.equals(this) || this.isAncestorOf(parent)).orElse(false);
    }

    public boolean isDescendantOf(ActorAddress address) {
        return address.isAncestorOf(this);
    }

    @Override
    public String toString() {
        return parent.map(p -> p + "/" + name).orElse(name);
    }

    public static class Companion {
        public static ActorAddress address(String name) {
            return new ActorAddress(Optional.empty(), name);
        }
    }
}
