package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.ActorRuntime;

import java.util.concurrent.ExecutorService;

public class ActorRuntimeImpl implements ActorRuntime {

    private final ExecutorService executor;

    public ActorRuntimeImpl(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void perform(Runnable runnable) {
        this.executor.execute(runnable);
    }
}
