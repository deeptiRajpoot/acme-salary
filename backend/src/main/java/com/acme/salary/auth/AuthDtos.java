package com.acme.salary.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record SignInRequest(@NotBlank String username, @NotBlank String password, boolean rememberMe) {
    }

    /** Email and designation come from the invite, never from the caller. */
    public record SignUpRequest(
            @NotBlank String inviteToken,
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "[A-Za-z0-9._-]*", message = "Use letters, digits, dot, dash or underscore") String username,
            @NotBlank @Size(min = 8, max = 100) String password) {
    }

    public record InviteRequest(
            @NotBlank @Email @Size(max = 200) String email,
            @NotBlank @Pattern(regexp = "HR|OWNER", message = "Designation must be HR or Owner") String role) {
    }

    public record InviteResponse(String token, String email, String role, String expiresAt) {
    }

    public record InvitePreview(String email, String role) {
    }

    public record UserProfile(long id, String fullName, String username, String email, String role) {
    }

    public record AuthResponse(String token, String expiresAt, UserProfile user) {
    }
}
