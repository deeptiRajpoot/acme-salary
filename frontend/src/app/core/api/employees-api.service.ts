import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Employee, EmployeeFilters, EmployeeInput, EmployeePage, EmployeeQuery, SalaryChangeInput, SalaryHistoryEntry } from './types';
import { toParams } from './query';

export const API_BASE = '/api';

@Injectable({ providedIn: 'root' })
export class EmployeesApi {
  private readonly http = inject(HttpClient);

  public search(query: EmployeeQuery): Observable<EmployeePage> {
    return this.http.get<EmployeePage>(`${API_BASE}/employees`, {
      params: toParams({
        page: query.page,
        size: query.size,
        sort: query.sort,
        q: query.q,
        country: query.country,
        department: query.department,
        level: query.level,
      }),
    });
  }

  public get(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${API_BASE}/employees/${id}`);
  }

  public create(input: EmployeeInput): Observable<Employee> {
    return this.http.post<Employee>(`${API_BASE}/employees`, input);
  }

  public update(id: number, input: EmployeeInput): Observable<Employee> {
    return this.http.put<Employee>(`${API_BASE}/employees/${id}`, input);
  }

  public remove(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/employees/${id}`);
  }

  public changeSalary(id: number, input: SalaryChangeInput): Observable<Employee> {
    return this.http.post<Employee>(`${API_BASE}/employees/${id}/salary`, input);
  }

  public salaryHistory(id: number): Observable<SalaryHistoryEntry[]> {
    return this.http.get<SalaryHistoryEntry[]>(`${API_BASE}/employees/${id}/salary-history`);
  }

  /** Download URL for the CSV export; honours the same filters as the list (no paging). */
  public exportUrl(filters: EmployeeFilters): string {
    const qs = toParams({ q: filters.q, country: filters.country, department: filters.department, level: filters.level }).toString();
    return `${API_BASE}/export/employees${qs ? `?${qs}` : ''}`;
  }
}
