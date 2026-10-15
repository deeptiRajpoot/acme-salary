import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Designation } from './auth.service';

export interface InvitePreview {
  email: string;
  role: Designation;
}

export interface InviteRequest {
  email: string;
  role: Designation;
}

export interface InviteResponse extends InvitePreview {
  token: string;
  expiresAt: string;
}

export function designationLabel(role: string): string {
  return role === 'OWNER' ? 'Owner' : 'HR';
}

@Injectable({ providedIn: 'root' })
export class InviteApi {
  private readonly http = inject(HttpClient);

  public create(body: InviteRequest): Observable<InviteResponse> {
    return this.http.post<InviteResponse>('/api/auth/invites', body);
  }

  public preview(token: string): Observable<InvitePreview> {
    return this.http.get<InvitePreview>(`/api/auth/invites/${encodeURIComponent(token)}`);
  }
}
