package com.medchart.ehr.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import io.jsonwebtoken.ExpiredJwtException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", 86400000);
    }

    @Test
    void generateTokenFromUsername_producesValidJwtString() {
        String token = tokenProvider.generateTokenFromUsername("testuser");

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void getUsernameFromToken_extractsCorrectUsername() {
        String token = tokenProvider.generateTokenFromUsername("testuser");

        String username = tokenProvider.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    @Test
    void getExpirationDateFromToken_returnsFutureDate() {
        String token = tokenProvider.generateTokenFromUsername("testuser");

        Date expiration = tokenProvider.getExpirationDateFromToken(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_returnsTrueForValidTokenAndMatchingUser() {
        String token = tokenProvider.generateTokenFromUsername("testuser");
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());

        Boolean valid = tokenProvider.validateToken(token, userDetails);

        assertTrue(valid);
    }

    @Test
    void validateToken_throwsForExpiredToken() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        ReflectionTestUtils.setField(shortLivedProvider, "jwtExpirationInMs", -1000);

        String token = shortLivedProvider.generateTokenFromUsername("testuser");
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());

        // validateToken calls getUsernameFromToken which parses the expired JWT,
        // causing jjwt to throw ExpiredJwtException before the comparison
        assertThrows(ExpiredJwtException.class,
                () -> shortLivedProvider.validateToken(token, userDetails));
    }

    @Test
    void validateToken_returnsFalseForWrongUsername() {
        String token = tokenProvider.generateTokenFromUsername("testuser");
        UserDetails userDetails = new User("otheruser", "password", Collections.emptyList());

        Boolean valid = tokenProvider.validateToken(token, userDetails);

        assertFalse(valid);
    }
}
