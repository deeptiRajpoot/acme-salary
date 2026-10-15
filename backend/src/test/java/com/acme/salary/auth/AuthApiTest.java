package com.acme.salary.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class AuthApiTest {

    @Autowired
    MockMvc mvc;

    private ResultActions send(String path, String body, String bearer) throws Exception {
        var req = post(path).contentType(MediaType.APPLICATION_JSON).content(body);
        if (bearer != null) {
            req.header("Authorization", "Bearer " + bearer);
        }
        return mvc.perform(req);
    }

    private String hrToken() throws Exception {
        String res = send("/api/auth/signin", "{\"username\":\"hr\",\"password\":\"Hr@12345\"}", null)
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(res, "$.token");
    }

    private String invite(String bearer, String email, String role) throws Exception {
        String res = send("/api/auth/invites", "{\"email\":\"" + email + "\",\"role\":\"" + role + "\"}", bearer)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(res, "$.token");
    }

    private static String signupBody(String token, String username) {
        return "{\"inviteToken\":\"" + token + "\",\"fullName\":\"Ada Lovelace\",\"username\":\"" + username
                + "\",\"password\":\"Secret123\"}";
    }

    @Test
    void defaultHrCanSignIn() throws Exception {
        send("/api/auth/signin", "{\"username\":\"hr\",\"password\":\"Hr@12345\"}", null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.user.role").value("HR"));
    }

    @Test
    void defaultOwnerCanSignInAndInviteOwner() throws Exception {
        String res = send("/api/auth/signin", "{\"username\":\"owner\",\"password\":\"Acme@7878\"}", null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.user.role").value("OWNER")).andReturn().getResponse().getContentAsString();
        invite(JsonPath.read(res, "$.token"), "boss@x.com", "OWNER");
    }

    @Test
    void wrongPasswordIs401() throws Exception {
        send("/api/auth/signin", "{\"username\":\"hr\",\"password\":\"nope\"}", null).andExpect(status().isUnauthorized());
    }

    @Test
    void signUpWithoutInviteIs400() throws Exception {
        send("/api/auth/signup", signupBody("made-up", "ada"), null)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.inviteToken").exists());
    }

    @Test
    void inviteIsRequiredToBeSignedIn() throws Exception {
        send("/api/auth/invites", "{\"email\":\"a@x.com\",\"role\":\"HR\"}", null).andExpect(status().isUnauthorized());
    }

    @Test
    void invitedUserGetsEmailAndRoleFromInvite() throws Exception {
        String token = invite(hrToken(), "Ada@X.com", "HR");
        mvc.perform(get("/api/auth/invites/" + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@x.com")).andExpect(jsonPath("$.role").value("HR"));
        String res = send("/api/auth/signup", signupBody(token, "ada"), null)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.user.email").value("ada@x.com"))
                .andExpect(jsonPath("$.user.role").value("HR")).andReturn().getResponse().getContentAsString();
        String session = JsonPath.read(res, "$.token");
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + session)).andExpect(status().isOk());
        send("/api/auth/signout", "{}", session).andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + session)).andExpect(status().isUnauthorized());
    }

    @Test
    void inviteCanOnlyBeUsedOnce() throws Exception {
        String token = invite(hrToken(), "ada@x.com", "HR");
        send("/api/auth/signup", signupBody(token, "ada"), null).andExpect(status().isCreated());
        send("/api/auth/signup", signupBody(token, "ada2"), null).andExpect(status().isBadRequest());
        mvc.perform(get("/api/auth/invites/" + token)).andExpect(status().isNotFound());
    }

    @Test
    void hrCannotInviteOwner() throws Exception {
        send("/api/auth/invites", "{\"email\":\"boss@x.com\",\"role\":\"OWNER\"}", hrToken()).andExpect(status().isForbidden());
    }

    @Test
    void cannotInviteExistingEmail() throws Exception {
        send("/api/auth/invites", "{\"email\":\"hr@acme.com\",\"role\":\"HR\"}", hrToken())
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void shortPasswordIs400() throws Exception {
        String token = invite(hrToken(), "ada@x.com", "HR");
        send("/api/auth/signup", "{\"inviteToken\":\"" + token + "\",\"fullName\":\"A\",\"username\":\"abc\",\"password\":\"short\"}", null)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void meWithoutTokenIs401() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }
}
