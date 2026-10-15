import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE } from './employees-api.service';
import { ImportResult } from './types';

@Injectable({ providedIn: 'root' })
export class ImportApi {
  private readonly http = inject(HttpClient);

  public importEmployees(file: File): Observable<ImportResult> {
    const form = new FormData();
    form.append('file', file, file.name);
    return this.http.post<ImportResult>(`${API_BASE}/import/employees`, form);
  }
}
