package net.rasanovum.becareful.warden;

public interface WardenStunAccess {
    boolean beCareful$isStunned();

    void beCareful$stun();

    void beCareful$beginKeyDeath();

    int beCareful$keyDeathDuration();

    long beCareful$keyDeathStartedAt();
}
