package com.acme.salary.auth;

import com.acme.salary.auth.AuthDtos.AuthResponse;
import com.acme.salary.auth.AuthDtos.InvitePreview;
import com.acme.salary.auth.AuthDtos.InviteRequest;
import com.acme.salary.auth.AuthDtos.InviteResponse;
import com.acme.salary.auth.AuthDtos.SignInRequest;
import com.acme.salary.auth.AuthDtos.SignUpRequest;
import com.acme.salary.auth.AuthDtos.UserProfile;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER = "Bearer ";

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse invite(@RequestHeader(value = "Authorization", required = false) String header,
                                 @Valid @RequestBody InviteRequest req) {
        return auth.createInvite(currentUser(header), req);
    }

    @GetMapping("/invites/{token}")
    public InvitePreview previewInvite(@PathVariable String token) {
        return auth.previewInvite(token);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signUp(@Valid @RequestBody SignUpRequest req) {
        return auth.signUp(req);
    }

    @PostMapping("/signin")
    public AuthResponse signIn(@Valid @RequestBody SignInRequest req) {
        return auth.signIn(req);
    }

    @GetMapping("/me")
    public UserProfile me(@RequestHeader(value = "Authorization", required = false) String header) {
        return currentUser(header);
    }

    @PostMapping("/signout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signOut(@RequestHeader(value = "Authorization", required = false) String header) {
        auth.signOut(token(header));
    }

    private UserProfile currentUser(String header) {
        return auth.authenticate(token(header)).orElseThrow(() -> new BadCredentialsException("Session expired, please sign in again"));
    }

    private static String token(String header) {
        if (header == null || !header.startsWith(BEARER)) {
            throw new BadCredentialsException("Missing bearer token");
        }
        return header.substring(BEARER.length()).trim();
    }
}
