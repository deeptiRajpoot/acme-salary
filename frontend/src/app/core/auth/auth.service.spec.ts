import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';
import { AuthService } from './auth.service';

const RESPONSE = {
  token: 't1',
  expiresAt: new Date(Date.now() + 3_600_000).toISOString(),
  user: { id: 1, fullName: 'HR Manager', username: 'hr', email: 'hr@acme.com', role: 'HR' },
};

describe('AuthService', () => {
  let auth: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: 'login', children: [] }])] });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  it('starts signed out', () => {
    expect(auth.isAuthenticated()).toBe(false);
  });

  it('signs in and keeps the token for the tab only unless remembered', () => {
    auth.signIn({ username: 'hr', password: 'x', rememberMe: false }).subscribe();
    http.expectOne('/api/auth/signin').flush(RESPONSE);
    expect(auth.user()?.role).toBe('HR');
    expect(sessionStorage.getItem('acme.auth')).not.toBeNull();
    expect(localStorage.getItem('acme.auth')).toBeNull();
  });

  it('persists the session when remember me is on', () => {
    auth.signIn({ username: 'hr', password: 'x', rememberMe: true }).subscribe();
    http.expectOne('/api/auth/signin').flush(RESPONSE);
    expect(localStorage.getItem('acme.auth')).not.toBeNull();
  });

  it('clears state on expire', () => {
    auth.signUp({ inviteToken: 'tok', fullName: 'A', username: 'abc', password: 'Secret123' }).subscribe();
    http.expectOne('/api/auth/signup').flush(RESPONSE);
    auth.expire();
    expect(auth.isAuthenticated()).toBe(false);
  });
});
