import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from './employees-api.service';
import { toParams } from './query';
import { Bucket, GroupDimension, GroupStats, Outlier, PayGap, Summary } from './types';

@Injectable({ providedIn: 'root' })
export class AnalyticsApi {
  private readonly http = inject(HttpClient);

  public summary(): Observable<Summary> {
    return this.http.get<Summary>(`${API_BASE}/analytics/summary`);
  }

  public groups(dimension: GroupDimension): Observable<GroupStats[]> {
    return this.http.get<GroupStats[]>(`${API_BASE}/analytics/by-${dimension}`);
  }

  public distribution(buckets = 20): Observable<Bucket[]> {
    return this.http.get<Bucket[]>(`${API_BASE}/analytics/distribution`, { params: toParams({ buckets }) });
  }

  public payGap(): Observable<PayGap> {
    return this.http.get<PayGap>(`${API_BASE}/analytics/pay-gap`);
  }

  public outliers(limit = 20): Observable<Outlier[]> {
    return this.http.get<Outlier[]>(`${API_BASE}/analytics/outliers`, { params: toParams({ limit }) });
  }
}
