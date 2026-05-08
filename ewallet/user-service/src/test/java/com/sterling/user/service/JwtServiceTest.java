package com.sterling.user.service;

import com.sterling.user.entity.Role;
import com.sterling.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JWT Service Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private User mockUser;

    // A valid Base64-encoded 256-bit key for testing
    private static final String TEST_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        mockUser = User.builder()
                .id(1L)
                .fullName("Simranpreet Singh")
                .email("simran@sterling.com")
                .password("encodedPassword")
                .phone("9876543210")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("generateToken: Returns non-null JWT string")
    void generateToken_ReturnsToken() {
        String token = jwtService.generateToken(mockUser);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("extractUsername: Returns correct email from token")
    void extractUsername_ReturnsEmail() {
        String token = jwtService.generateToken(mockUser);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("simran@sterling.com");
    }

    @Test
    @DisplayName("isTokenValid: Returns true for fresh token")
    void isTokenValid_ReturnsTrueForFreshToken() {
        String token = jwtService.generateToken(mockUser);
        assertThat(jwtService.isTokenValid(token, mockUser)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: Returns false for token belonging to different user")
    void isTokenValid_ReturnsFalseForWrongUser() {
        String token = jwtService.generateToken(mockUser);

        User otherUser = User.builder()
                .email("other@sterling.com")
                .password("encodedPwd")
                .role(Role.USER)
                .enabled(true)
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    @DisplayName("generateToken: Two tokens for same user have different signatures (non-deterministic)")
    void generateToken_TwoTokensAreDifferent() throws InterruptedException {
        String token1 = jwtService.generateToken(mockUser);
        Thread.sleep(10); // small delay to ensure different issuedAt
        String token2 = jwtService.generateToken(mockUser);
        // iat differs, so the compact string differs
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("getExpirationTime: Returns configured value")
    void getExpirationTime_ReturnsConfiguredValue() {
        assertThat(jwtService.getExpirationTime()).isEqualTo(86400000L);
    }
}
