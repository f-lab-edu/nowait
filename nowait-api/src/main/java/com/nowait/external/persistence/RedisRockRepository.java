package com.nowait.external.persistence;

import com.nowait.domain.repository.LockRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRockRepository implements LockRepository {

    private static final String LOCK = "lock";
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(3);

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Boolean lock(String key) {
        return redisTemplate
            .opsForValue()
            .setIfAbsent(key, LOCK, LOCK_TIMEOUT);
    }

    @Override
    public Boolean unlock(String key) {
        return redisTemplate.delete(key);
    }
}
