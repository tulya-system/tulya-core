package org.smallibs.tulya.actor.core;

import java.util.Optional;

public record ActorAddress(Optional<ActorAddress> parent, String name) {
    public static ActorAddress SYSTEM = new ActorAddress(Optional.empty(), "system");

    public boolean isParentOf(ActorAddress address) {
        return parent.map(p -> p.equals(address)).orElse(false);
    }

    public boolean isChildOf(ActorAddress address) {
        return address.isParentOf(this);
    }

    public boolean isAncestorOf(ActorAddress address) {
        return parent.map(p -> p.equals(address) || p.isParentOf(address)).orElse(false);
    }

    public boolean isDescendantOf(ActorAddress address) {
        return address.isAncestorOf(this);
    }

    @Override
    public String toString() {
        return parent.map(p -> p + "/" + name).orElse(name);
    }
}
