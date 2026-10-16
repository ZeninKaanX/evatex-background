package com.videomenu;

/** One-way session circuit breaker. VM-fatal errors deliberately propagate. */
public final class RecoveryCircuit {
    private volatile Throwable failure;
    public synchronized boolean trip(Throwable cause) {
        if (!(cause instanceof RuntimeException || cause instanceof LinkageError)) return false;
        if (failure != null) return false;
        failure = cause; return true;
    }
    public boolean suspended() { return failure != null; }
    public Throwable failure() { return failure; }
}
