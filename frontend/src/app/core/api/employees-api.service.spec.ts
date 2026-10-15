import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { makeEmployee, makePage } from '../../testing/fixtures';
import { EmployeesApi } from './employees-api.service';

describe('EmployeesApi', () => {
  let api: EmployeesApi;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(EmployeesApi);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('sends paging, sort and every filter as query params', () => {
    api.search({ page: 2, size: 50, sort: 'salaryUsd,desc', q: 'ada', country: 'GB', department: 'Engineering', level: 'L5' }).subscribe();
    const req = controller.expectOne((r) => r.url === '/api/employees');
    expect(req.request.method).toBe('GET');
    expect(Object.fromEntries(req.request.params.keys().map((k) => [k, req.request.params.get(k)]))).toEqual({
      page: '2', size: '50', sort: 'salaryUsd,desc', q: 'ada', country: 'GB', department: 'Engineering', level: 'L5',
    });
    req.flush(makePage([]));
  });

  it('omits empty filters but keeps page 0', () => {
    api.search({ page: 0, size: 25, sort: 'lastName,asc', q: '', country: undefined }).subscribe();
    const req = controller.expectOne((r) => r.url === '/api/employees');
    expect(req.request.params.has('q')).toBe(false);
    expect(req.request.params.has('country')).toBe(false);
    expect(req.request.params.get('page')).toBe('0');
    req.flush(makePage([]));
  });

  it('builds an export URL honouring the current filters (no paging)', () => {
    expect(api.exportUrl({})).toBe('/api/export/employees');
    expect(api.exportUrl({ q: 'a b', country: 'DE', level: 'L2' })).toBe('/api/export/employees?q=a%20b&country=DE&level=L2');
  });

  it('posts a salary change to the salary endpoint', () => {
    api.changeSalary(7, { newSalary: 100, reason: 'promotion' }).subscribe();
    const req = controller.expectOne('/api/employees/7/salary');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ newSalary: 100, reason: 'promotion' });
    req.flush(makeEmployee({ id: 7 }));
  });

  it('uses PUT for profile updates and DELETE for removal', () => {
    api.update(3, { ...makeEmployee(), salary: 1 }).subscribe();
    controller.expectOne({ method: 'PUT', url: '/api/employees/3' }).flush(makeEmployee({ id: 3 }));
    api.remove(3).subscribe();
    const del = controller.expectOne({ method: 'DELETE', url: '/api/employees/3' });
    del.flush(null, { status: 204, statusText: 'No Content' });
  });
});
