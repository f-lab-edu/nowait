package com.nowait.domain.repository;

public interface LockRepository {

    Boolean lock(String key);

    Boolean unlock(String key);
}
