import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiError, parseProblem } from './api-error';
import { errorInterceptor } from './error.interceptor';

describe('parseProblem', () => {
  it('reads detail and the field errors map from an RFC 7807 body', () => {
    const err = parseProblem(400, { title: 'Bad Request', status: 400, detail: 'Validation failed', errors: { email: 'must be valid' } });
    expect(err).toBeInstanceOf(ApiError);
    expect(err.status).toBe(400);
    expect(err.message).toBe('Validation failed');
    expect(err.fieldErrors).toEqual({ email: 'must be valid' });
  });

  it('falls back to title, then status text, for sparse bodies', () => {
    expect(parseProblem(404, { title: 'Not Found' }).message).toBe('Not Found');
    expect(parseProblem(500, 'oops', 'Server Error').message).toBe('Server Error');
    expect(parseProblem(502, null).message).toBe('Request failed (502)');
  });

  it('ignores non-string field error values', () => {
    expect(parseProblem(400, { errors: { a: 'x', b: 3 } }).fieldErrors).toEqual({ a: 'x' });
  });

  it('reports a network failure (status 0) readably', () => {
    expect(parseProblem(0, null).message).toContain('Cannot reach the server');
  });
});

describe('errorInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([errorInterceptor])), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('turns a problem+json response into an ApiError', () => {
    let caught: unknown;
    http.get('/api/x').subscribe({ error: (e: unknown) => (caught = e) });
    controller
      .expectOne('/api/x')
      .flush({ status: 400, detail: 'Invalid', errors: { salary: 'must be positive' } }, { status: 400, statusText: 'Bad Request' });
    expect(caught).toBeInstanceOf(ApiError);
    expect((caught as ApiError).fieldErrors['salary']).toBe('must be positive');
    expect((caught as ApiError).status).toBe(400);
  });

  it('passes successful responses through untouched', () => {
    let body: unknown;
    http.get('/api/x').subscribe((b) => (body = b));
    controller.expectOne('/api/x').flush({ ok: true });
    expect(body).toEqual({ ok: true });
  });
});
