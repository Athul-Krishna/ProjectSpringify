package com.athul.springify.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UtilsTest {

    @Autowired
    Utils utils;

    @BeforeEach
    void setUp() {
    }

    @Test
    void generateUserId() {
        String userId = utils.generateUserId(30);
        String userId2 = utils.generateUserId(30);

        assertNotNull(userId);
        assertNotNull(userId2);
        assertTrue(userId.length() == 30);
        assertTrue(!userId.equalsIgnoreCase(userId2));
    }

    @Test
    @Disabled
    void hasTokenExpired() {
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwOGdoWFk0Y2tiWldodzY4R1lhT2diTGpJVlpqNDIiLCJleHAiOjE2MTkwNjcxMTV9.NCLv14hpPSkVPmmfHlywhBrxlLJNTJ4TNLhWt1ESRjnFdPs7xv0_1Sg6MaIsv0DGRoqXOb7qOGy7JMUpl9kSQQ";
        boolean hasTokenExpired = Utils.hasTokenExpired(expiredToken);
        assertTrue(hasTokenExpired);
    }

    @Test
    void hasTokenNotExpired() {
        String token = utils.generateEmailVerificationToken("s0m3u53r1d");
        assertNotNull(token);
        boolean hasTokenExpired = Utils.hasTokenExpired(token);
        assertFalse(hasTokenExpired);
    }
}