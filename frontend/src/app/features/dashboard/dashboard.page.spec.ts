import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ApiError } from '../../core/api/api-error';
import { AnalyticsApi } from '../../core/api/analytics-api.service';
import { DashboardPage } from './dashboard.page';

const SUMMARY = {
  headcount: 10000, totalPayrollUsd: 950_000_000, meanUsd: 95000, medianUsd: 88000, p25Usd: 60000, p75Usd: 120000, p90Usd: 150000,
  countries: 12, departments: 8,
};

function mockApi(overrides: Partial<Record<keyof AnalyticsApi, unknown>> = {}): void {
  const api = {
    summary: () => of(SUMMARY),
    groups: () => of([{ group: 'US', headcount: 10, minUsd: 1, p25Usd: 60000, medianUsd: 90000, p75Usd: 110000, p90Usd: 1, maxUsd: 2, meanUsd: 3 }]),
    distribution: () => of([{ fromUsd: 30000, toUsd: 40000, count: 120 }]),
    payGap: () =>
      of({
        overallGapPercent: 4.2,
        cohorts: [{ department: 'Engineering', level: 'L5', medianFemaleUsd: 100000, medianMaleUsd: 110000, gapPercent: 9.1, femaleCount: 5, maleCount: 8 }],
      }),
    outliers: () => of([{ employeeId: 42, name: 'Grace Hopper', country: 'US', department: 'Sales', level: 'L3', salaryUsd: 300000, peerMedianUsd: 90000, zScore: 3.4 }]),
    ...overrides,
  };
  TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: AnalyticsApi, useValue: api }] });
}

describe('DashboardPage', () => {
  beforeEach(() => {
    // jsdom has no canvas; Chart.js copes with a null context but we keep it quiet.
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(null);
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
  });

  afterEach(() => vi.restoreAllMocks());

  it('renders KPIs, pay equity and outliers from the API data', async () => {
    mockApi();
    const fixture = TestBed.createComponent(DashboardPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    const kpis = Array.from(el.querySelectorAll('[data-testid=kpi]')).map((k) => k.textContent ?? '');
    expect(kpis).toHaveLength(4);
    expect(kpis[0]).toContain('10,000');
    expect(kpis[1]).toContain('$950M');
    expect(kpis[2]).toContain('$88,000');
    expect(kpis[3]).toContain('$150,000');

    expect(el.querySelector('[data-testid=overall-gap]')?.textContent).toContain('4.2%');
    expect(el.textContent).toContain("Women's median pay is 4.2% below men's");
    const link = el.querySelector<HTMLAnchorElement>('a[href="/employees/42"]');
    expect(link?.textContent).toContain('Grace Hopper');
  });

  it('shows an error with Retry on a failing panel while the others still render', async () => {
    let calls = 0;
    mockApi({
      summary: () => {
        calls += 1;
        return calls === 1 ? throwError(() => new ApiError(500, 'Summary unavailable')) : of(SUMMARY);
      },
    });
    const fixture = TestBed.createComponent(DashboardPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    const alert = el.querySelector('[role=alert]');
    expect(alert?.textContent).toContain('Summary unavailable');
    expect(el.querySelector('[data-testid=overall-gap]')).not.toBeNull();

    Array.from(alert!.querySelectorAll('button')).find((b) => b.textContent?.includes('Retry'))!.click();
    await fixture.whenStable();
    expect(el.querySelectorAll('[data-testid=kpi]')).toHaveLength(4);
    expect(el.querySelector('[role=alert]')).toBeNull();
  });
});
