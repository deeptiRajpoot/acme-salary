package com.acme.salary.auth;

import com.acme.salary.auth.AuthDtos.UserProfile;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {

    /** A user row including its password hash. */
    public record Credentials(UserProfile profile, String passwordHash) {
    }

    /** An invite row; `usedAt` is null until it has been redeemed. */
    public record InviteRow(long id, String email, String role, String expiresAt, String usedAt) {
    }

    private static final String COLUMNS = "id, full_name, username, email, role, password_hash";
    private static final RowMapper<Credentials> MAPPER = (rs, i) -> new Credentials(
            new UserProfile(rs.getLong("id"), rs.getString("full_name"), rs.getString("username"),
                    rs.getString("email"), rs.getString("role")),
            rs.getString("password_hash"));

    private final JdbcTemplate jdbc;

    public AuthRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean usernameExists(String username) {
        return count("SELECT COUNT(*) FROM app_user WHERE username = ?", username) > 0;
    }

    public boolean emailExists(String email) {
        return count("SELECT COUNT(*) FROM app_user WHERE email = ?", email) > 0;
    }

    public long userCount() {
        return count("SELECT COUNT(*) FROM app_user");
    }

    public long insertUser(String fullName, String username, String email, String passwordHash, String role, String createdAt) {
        jdbc.update("INSERT INTO app_user (full_name, username, email, password_hash, role, created_at) VALUES (?,?,?,?,?,?)",
                fullName, username, email, passwordHash, role, createdAt);
        Long id = jdbc.queryForObject("SELECT last_insert_rowid()", Long.class);
        return id == null ? 0 : id;
    }

    /** Matches on username or email, case-insensitively. */
    public Optional<Credentials> findByLogin(String login) {
        return jdbc.query("SELECT " + COLUMNS + " FROM app_user WHERE username = ? OR email = ?", MAPPER, login, login)
                .stream().findFirst();
    }

    public void insertSession(String tokenHash, long userId, String expiresAt) {
        jdbc.update("INSERT INTO auth_session (token_hash, user_id, expires_at) VALUES (?,?,?)", tokenHash, userId, expiresAt);
    }

    /** Returns the user owning a non-expired session; `now` is ISO-8601 UTC so text comparison is chronological. */
    public Optional<UserProfile> findBySession(String tokenHash, String now) {
        return jdbc.query("SELECT u." + COLUMNS.replace(", ", ", u.") + " FROM auth_session s JOIN app_user u ON u.id = s.user_id "
                + "WHERE s.token_hash = ? AND s.expires_at > ?", MAPPER, tokenHash, now)
                .stream().findFirst().map(Credentials::profile);
    }

    public void deleteSession(String tokenHash) {
        jdbc.update("DELETE FROM auth_session WHERE token_hash = ?", tokenHash);
    }

    public void deleteExpiredSessions(String now) {
        jdbc.update("DELETE FROM auth_session WHERE expires_at <= ?", now);
    }

    private long count(String sql, Object... args) {
        Long n = jdbc.queryForObject(sql, Long.class, args);
        return n == null ? 0 : n;
    }

    public void insertInvite(String tokenHash, String email, String role, long createdBy, String expiresAt) {
        jdbc.update("INSERT INTO invite (token_hash, email, role, created_by, expires_at) VALUES (?,?,?,?,?)",
                tokenHash, email, role, createdBy, expiresAt);
    }

    public Optional<InviteRow> findInvite(String tokenHash) {
        return jdbc.query("SELECT id, email, role, expires_at, used_at FROM invite WHERE token_hash = ?",
                (rs, i) -> new InviteRow(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)),
                tokenHash).stream().findFirst();
    }

    /** Atomically claims an unused invite; false when someone else redeemed it first. */
    public boolean markInviteUsed(long id, String now) {
        return jdbc.update("UPDATE invite SET used_at = ? WHERE id = ? AND used_at IS NULL", now, id) == 1;
    }
}
