package com.tezish.demo.services.token;

import com.tezish.demo.dto.response.JWTTokenResponse;
import com.tezish.demo.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;

    @Value("${redis.password}")
    private String redisPassword;

    private JedisPool jedisPool;

    private final JwtUtil jwtUtil;

    public TokenServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostConstruct
    public void init() {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort, 5000, redisPassword);
    }


    @Override
    public JWTTokenResponse saveUserTokensToRedis(UserDetails userDetails) {
        String redisKey = "tokens:" + userDetails.getUsername(); // username is email

        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> tokenData = new HashMap<>();
            var jwtToken = jwtUtil.generateJwtToken(userDetails);
            tokenData.put("accessToken", jwtToken);
            tokenData.put("accessTokenExpired", "false");
            tokenData.put("accessTokenRevoked", "false");

            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            tokenData.put("refreshToken", refreshToken);
            tokenData.put("refreshTokenExpired", "false");
            tokenData.put("refreshTokenRevoked", "false");

//            // Check existing tokens and remove them if present
//            if (jedis.exists(redisKey)) {
//                jedis.del(redisKey);
//            }

            jedis.hset(redisKey, tokenData);

            return JWTTokenResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (Exception e) {
            log.error("Failed to save user tokens to Redis: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void revokeAllUserTokensInRedis(UserDetails userDetails) {
        String redisKey = "tokens:" + userDetails.getUsername(); // username is email

        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> userTokens = jedis.hgetAll(redisKey);
            if (!userTokens.isEmpty()) {
                Map<String, String> revokedData = new HashMap<>();
                revokedData.put("accessTokenRevoked", "true");
                revokedData.put("accessTokenExpired", "true");
                revokedData.put("refreshTokenRevoked", "true");
                revokedData.put("refreshTokenExpired", "true");

                jedis.hset(redisKey, revokedData);
            }
        } catch (Exception e) {
            log.error("Failed to revoke user tokens in Redis: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, String> getUserTokensFromRedis(UserDetails user) {
        String redisKey = "tokens:" + user.getUsername(); // username is email

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(redisKey);
        } catch (Exception e) {
            log.error("Failed to get user tokens from Redis: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
