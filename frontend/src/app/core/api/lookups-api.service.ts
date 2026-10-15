import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { API_BASE } from './employees-api.service';
import { Lookups } from './types';

@Injectable({ providedIn: 'root' })
export class LookupsApi {
  private readonly http = inject(HttpClient);
  private cache$?: Observable<Lookups>;

  /** Cached for the session; filter values change rarely. */
  public lookups(): Observable<Lookups> {
    this.cache$ ??= this.http.get<Lookups>(`${API_BASE}/lookups`).pipe(shareReplay({ bufferSize: 1, refCount: false }));
    return this.cache$;
  }
}
