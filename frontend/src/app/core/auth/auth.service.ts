import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, of, tap } from 'rxjs';

export type Designation = 'HR' | 'OWNER';

export interface UserProfile {
  id: number;
  fullName: string;
  username: string;
  email: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  expiresAt: string;
  user: UserProfile;
}

export interface SignInPayload {
  username: string;
  password: string;
  rememberMe: boolean;
}

export interface SignUpPayload {
  inviteToken: string;
  fullName: string;
  username: string;
  password: string;
}

const STORAGE_KEY = 'acme.auth';

function readFrom(store: Storage): AuthResponse | null {
  try {
    const raw = store.getItem(STORAGE_KEY);
    const parsed = raw ? (JSON.parse(raw) as AuthResponse) : null;
    return parsed && Date.parse(parsed.expiresAt) > Date.now() ? parsed : null;
  } catch {
    return null; // storage unavailable or corrupt: treat as signed out
  }
}

function readStored(): AuthResponse | null {
  return readFrom(localStorage) ?? readFrom(sessionStorage);
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly session = signal<AuthResponse | null>(readStored());

  public readonly user = computed(() => this.session()?.user ?? null);
  public readonly token = computed(() => this.session()?.token ?? null);
  public readonly isAuthenticated = computed(() => this.session() !== null);

  public signIn(payload: SignInPayload): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/signin', payload).pipe(tap((res) => this.start(res, payload.rememberMe)));
  }

  public signUp(payload: SignUpPayload): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/signup', payload).pipe(tap((res) => this.start(res, false)));
  }

  /** Revokes the server session (best effort) and always clears local state. */
  public signOut(): void {
    this.http.post<void>('/api/auth/signout', {}).pipe(catchError(() => of(null))).subscribe();
    this.clear();
    void this.router.navigate(['/login']);
  }

  /** Called when the API rejects the token (expired or revoked). */
  public expire(): void {
    this.clear();
    void this.router.navigate(['/login']);
  }

  private start(res: AuthResponse, persist: boolean): void {
    this.clear();
    try {
      (persist ? localStorage : sessionStorage).setItem(STORAGE_KEY, JSON.stringify(res));
    } catch {
      // storage unavailable: session lives in memory only
    }
    this.session.set(res);
  }

  private clear(): void {
    for (const store of [localStorage, sessionStorage]) {
      try {
        store.removeItem(STORAGE_KEY);
      } catch {
        // ignore
      }
    }
    this.session.set(null);
  }
}
