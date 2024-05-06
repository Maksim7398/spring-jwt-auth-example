package com.example.springjwtauthexample.events;

import com.example.springjwtauthexample.entity.RefreshToken;
import com.example.springjwtauthexample.exception.RefreshTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisExpirationEvent {

    @EventListener
    public void handleRedisKeyExpiredEvent(RedisKeyExpiredEvent<RefreshToken> event) {
        RefreshToken expiredRefreshToken = (RefreshToken) event.getValue();

        if (expiredRefreshToken == null) {
            throw new RefreshTokenException("Refresh token is null in handleRedisKeyExpiredEvent function: ");
        }

        log.info("Refresh token with key = {} has expired RefreshToken is: {}",
                expiredRefreshToken.getId(), expiredRefreshToken.getToken());

    }
}
