package com.klp.hub.inventory.infrastructure.lock;

public interface DistributedLockManager {

    boolean tryLock(String key);

    void releaseLock(String key);
}
