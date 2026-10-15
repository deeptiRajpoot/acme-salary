import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { MatPaginatorHarness } from '@angular/material/paginator/testing';
import { MatSelectHarness } from '@angular/material/select/testing';
import { MatSortHarness } from '@angular/material/sort/testing';
import { provideRouter, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { EmployeesApi } from '../../core/api/employees-api.service';
import { LookupsApi } from '../../core/api/lookups-api.service';
import { EmployeePage, EmployeeQuery } from '../../core/api/types';
import { LOOKUPS, makeEmployee, makePage } from '../../testing/fixtures';
import { EmployeesPage, SEARCH_DEBOUNCE_MS } from './employees.page';

const ROWS = [
  makeEmployee({ id: 1, firstName: 'Ada', lastName: 'Lovelace' }),
  makeEmployee({ id: 2, firstName: 'Grace', lastName: 'Hopper', country: 'US', currency: 'USD', salary: 185000, salaryUsd: 185000 }),
];

describe('EmployeesPage', () => {
  let fixture: ComponentFixture<EmployeesPage>;
  let loader: HarnessLoader;
  const search = vi.fn<(q: EmployeeQuery) => Observable<EmployeePage>>(() => of(makePage(ROWS, 60)));
  const exportUrl = vi.fn(() => '/api/export/employees');

  const lastQuery = (): EmployeeQuery => search.mock.calls[search.mock.calls.length - 1][0];

  beforeEach(async () => {
    search.mockClear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: EmployeesApi, useValue: { search, exportUrl } },
        { provide: LookupsApi, useValue: { lookups: () => of(LOOKUPS) } },
      ],
    });
    fixture = TestBed.createComponent(EmployeesPage);
    loader = TestbedHarnessEnvironment.loader(fixture);
    await fixture.whenStable();
  });

  it('renders rows with local currency and USD salary', () => {
    const rows = (fixture.nativeElement as HTMLElement).querySelectorAll('[data-testid=row]');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('Ada Lovelace');
    expect(rows[0].textContent).toContain('£95,000');
    expect(rows[1].textContent).toContain('$185,000');
    expect(search).toHaveBeenCalledWith({ page: 0, size: 25, sort: 'lastName,asc' });
  });

  it('applies a country filter and resets to the first page', async () => {
    const paginator = await loader.getHarness(MatPaginatorHarness);
    await paginator.goToNextPage();
    expect(lastQuery().page).toBe(1);

    const select = await loader.getHarness(MatSelectHarness.with({ selector: '[data-testid=filter-country]' }));
    await select.open();
    await select.clickOptions({ text: /Germany/ });
    expect(lastQuery()).toMatchObject({ country: 'DE', page: 0 });
  });

  it('requests the next page and a new page size', async () => {
    const paginator = await loader.getHarness(MatPaginatorHarness);
    await paginator.goToNextPage();
    expect(lastQuery()).toMatchObject({ page: 1, size: 25 });
    await paginator.setPageSize(50);
    expect(lastQuery()).toMatchObject({ page: 0, size: 50 });
  });

  it('sorts server-side by the clicked column and toggles direction', async () => {
    const sort = await loader.getHarness(MatSortHarness);
    const [header] = await sort.getSortHeaders({ label: 'Salary (USD)' });
    await header.click();
    expect(lastQuery().sort).toBe('salaryUsd,asc');
    await header.click();
    expect(lastQuery().sort).toBe('salaryUsd,desc');
  });

  it('debounces the search box before calling the API', async () => {
    vi.useFakeTimers();
    try {
      const input = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>('[data-testid=search]');
      const before = search.mock.calls.length;
      input!.value = '  ada ';
      input!.dispatchEvent(new Event('input'));
      vi.advanceTimersByTime(SEARCH_DEBOUNCE_MS - 50);
      expect(search.mock.calls.length).toBe(before);
      vi.advanceTimersByTime(60);
    } finally {
      vi.useRealTimers();
    }
    await fixture.whenStable();
    expect(lastQuery()).toMatchObject({ q: 'ada', page: 0 });
  });

  it('exposes the export link and navigates to the detail page on row click', async () => {
    const link = (fixture.nativeElement as HTMLElement).querySelector<HTMLAnchorElement>('[data-testid=export]');
    expect(link?.getAttribute('href')).toBe('/api/export/employees');
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    (fixture.nativeElement as HTMLElement).querySelector<HTMLElement>('[data-testid=row]')!.click();
    expect(navigate).toHaveBeenCalledWith(['/employees', 1]);
  });
});
