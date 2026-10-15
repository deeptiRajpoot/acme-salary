package com.acme.salary.auth;

import com.acme.salary.auth.AuthDtos.AuthResponse;
import com.acme.salary.auth.AuthDtos.InvitePreview;
import com.acme.salary.auth.AuthDtos.InviteRequest;
import com.acme.salary.auth.AuthDtos.InviteResponse;
import com.acme.salary.auth.AuthDtos.SignInRequest;
import com.acme.salary.auth.AuthDtos.SignUpRequest;
import com.acme.salary.auth.AuthDtos.UserProfile;
import com.acme.salary.auth.AuthRepository.Credentials;
import com.acme.salary.auth.AuthRepository.InviteRow;
import com.acme.salary.common.NotFoundException;
import com.acme.salary.common.ValidationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    public static final String ROLE_HR = "HR";
    public static final String ROLE_OWNER = "OWNER";

    private static final Duration SESSION_TTL = Duration.ofHours(8);
    private static final Duration INVITE_TTL = Duration.ofDays(7);
    private static final String INVALID_INVITE = "This invite link is invalid, expired or already used";
    private static final Duration REMEMBER_TTL = Duration.ofDays(30);
    private static final String BAD_CREDENTIALS = "Invalid username or password";

    private final AuthRepository repo;
    private final PasswordHasher hasher;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public AuthService(AuthRepository repo, PasswordHasher hasher, Clock clock) {
        this.repo = repo;
        this.hasher = hasher;
        this.clock = clock;
    }

    /** Creates the invited account (email and designation fixed by the invite) and signs it in. */
    @Transactional
    public AuthResponse signUp(SignUpRequest req) {
        InviteRow invite = validInvite(req.inviteToken())
                .orElseThrow(() -> new ValidationException("inviteToken", INVALID_INVITE));
        String username = req.username().trim();
        if (repo.usernameExists(username)) {
            throw new ValidationException("username", "Username is already taken");
        }
        if (repo.emailExists(invite.email())) {
            throw new ValidationException("inviteToken", "This email already has an account");
        }
        if (!repo.markInviteUsed(invite.id(), now().toString())) {
            throw new ValidationException("inviteToken", INVALID_INVITE);
        }
        String fullName = req.fullName().trim();
        long id = repo.insertUser(fullName, username, invite.email(), hasher.hash(req.password()), invite.role(), now().toString());
        return openSession(new UserProfile(id, fullName, username, invite.email(), invite.role()), false);
    }

    /** Only an OWNER may invite another OWNER; HR may invite HR. */
    public InviteResponse createInvite(UserProfile inviter, InviteRequest req) {
        if (ROLE_OWNER.equals(req.role()) && !ROLE_OWNER.equals(inviter.role())) {
            throw new ForbiddenException("Only an Owner can invite another Owner");
        }
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (repo.emailExists(email)) {
            throw new ValidationException("email", "Email is already registered");
        }
        byte[] raw = new byte[32];
        random.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        Instant expires = now().plus(INVITE_TTL);
        repo.insertInvite(sha256(token), email, req.role(), inviter.id(), expires.toString());
        return new InviteResponse(token, email, req.role(), expires.toString());
    }

    public InvitePreview previewInvite(String token) {
        InviteRow invite = validInvite(token).orElseThrow(() -> new NotFoundException(INVALID_INVITE));
        return new InvitePreview(invite.email(), invite.role());
    }

    private Optional<InviteRow> validInvite(String token) {
        return repo.findInvite(sha256(token))
                .filter(i -> i.usedAt() == null && i.expiresAt().compareTo(now().toString()) > 0);
    }

    public AuthResponse signIn(SignInRequest req) {
        Optional<Credentials> found = repo.findByLogin(req.username().trim());
        // Same message for unknown user and wrong password so usernames cannot be probed.
        if (found.isEmpty() || !hasher.matches(req.password(), found.get().passwordHash())) {
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }
        return openSession(found.get().profile(), req.rememberMe());
    }

    public Optional<UserProfile> authenticate(String token) {
        return repo.findBySession(sha256(token), now().toString());
    }

    public void signOut(String token) {
        repo.deleteSession(sha256(token));
    }

    /** Creates the user only when no account with that username exists yet. */
    public void ensureUser(String fullName, String username, String email, String password, String role) {
        if (!repo.usernameExists(username)) {
            repo.insertUser(fullName, username, email, hasher.hash(password), role, now().toString());
        }
    }

    private AuthResponse openSession(UserProfile user, boolean remember) {
        repo.deleteExpiredSessions(now().toString());
        byte[] raw = new byte[32];
        random.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        Instant expires = now().plus(remember ? REMEMBER_TTL : SESSION_TTL);
        repo.insertSession(sha256(token), user.id(), expires.toString());
        return new AuthResponse(token, expires.toString(), user);
    }

    private Instant now() {
        return clock.instant();
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
