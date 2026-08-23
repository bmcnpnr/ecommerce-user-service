package com.ecommerce.user.contract;

import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

/**
 * Provider side of the contract api-gateway holds with user-service. The pact (copy in
 * {@code src/test/resources/pacts/}, see {@code ecommerce-platform/sync-pacts.sh}) has
 * two kinds of interaction, both replayed against the real application on H2:
 *
 * <ul>
 *   <li><b>HTTP</b> — the gateway validates the caller's JWT and forwards the request with
 *       {@code X-Username}, {@code X-User-Id} and {@code X-User-Role};
 *       {@code GET /api/v1/users/me} resolves the profile from {@code X-Username}. Verified
 *       over HTTP against the running server (security filter chain, controller, service, JPA).</li>
 *   <li><b>Message</b> — the JWT itself. The gateway decodes the claims this service signs
 *       into those headers, so the claim names, their types and the algorithm are the
 *       contract. The token is taken from the real login flow ({@link UserService#login})
 *       and handed to Pact decoded: {@code {"header": <JOSE header>, "payload": <claims>}}.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Provider("user-service")
@PactFolder("pacts")
class UserServiceProviderPactTest {

    private static final String ALICE_PASSWORD = "alice-password-1";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setTarget(PactVerificationContext context) {
        context.setTarget(context.getInteraction().isAsynchronousMessage()
                ? new MessageTestTarget()
                : new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    // ───────────────────────── provider states ─────────────────────────
    // State names are part of the contract: consumers reference them verbatim.

    /** Customer "alice" (id 42, alice@example.com, ACTIVE), able to log in. */
    @State("user alice exists")
    void userAliceExists() {
        jdbc.update("DELETE FROM app_users WHERE id = 42 OR username = 'alice'");
        jdbc.update("INSERT INTO app_users (id, username, email, password_hash, first_name, last_name, role, status, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                42L, "alice", "alice@example.com", passwordEncoder.encode(ALICE_PASSWORD), "Alice", "Example",
                "CUSTOMER", "ACTIVE", LocalDateTime.now());
    }

    // ───────────────────────── message producers ─────────────────────────
    // The annotation value must equal the consumer's expectsToReceive(...) description.

    /** The JWT the real login flow issues to alice, decoded into its JOSE header and claims. */
    @PactVerifyProvider("a JWT issued to alice at login")
    MessageAndMetadata jwtIssuedToAlice() {
        String token = userService.login(new LoginRequest("alice", ALICE_PASSWORD)).getToken();
        String[] segments = token.split("\\.");
        String decoded = "{\"header\":" + base64UrlDecode(segments[0]) + ",\"payload\":" + base64UrlDecode(segments[1]) + "}";
        return new MessageAndMetadata(decoded.getBytes(StandardCharsets.UTF_8), Map.of("contentType", "application/json"));
    }

    private static String base64UrlDecode(String segment) {
        return new String(Base64.getUrlDecoder().decode(segment), StandardCharsets.UTF_8);
    }
}
