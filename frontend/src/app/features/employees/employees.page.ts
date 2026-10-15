import { ChangeDetectionStrategy, Component, computed, inject, linkedSignal, signal } from '@angular/core';
import { rxResource, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';
import { errorMessage } from '../../core/api/api-error';
import { EmployeesApi } from '../../core/api/employees-api.service';
import { LookupsApi } from '../../core/api/lookups-api.service';
import { Employee, EmployeePage, EmployeeQuery } from '../../core/api/types';
import { MoneyPipe, UsdPipe } from '../../core/format.pipes';
import { CreateEmployeeDialogComponent } from './create-employee.dialog';

const DEFAULT_SORT: Sort = { active: 'lastName', direction: 'asc' };
const DEFAULT_PAGE_SIZE = 25;
const EMPTY_PAGE: EmployeePage = { content: [], page: 0, size: 25, totalElements: 0, totalPages: 0 };
export const SEARCH_DEBOUNCE_MS = 300;

@Component({
  selector: 'app-employees-page',
  imports: [
    ReactiveFormsModule, MatTableModule, MatSortModule, MatPaginatorModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule, MatProgressBarModule, MoneyPipe, UsdPipe,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './employees.page.html',
  styleUrl: './employees.page.scss',
})
export class EmployeesPage {
  private readonly api = inject(EmployeesApi);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['lastName', 'email', 'country', 'department', 'level', 'jobTitle', 'hireDate', 'salary', 'salaryUsd'];
  protected readonly pageSizes = [DEFAULT_PAGE_SIZE, 50, 100];
  protected readonly lookups = toSignal(inject(LookupsApi).lookups(), { initialValue: undefined });
  protected readonly filterForm = new FormGroup({
    q: new FormControl('', { nonNullable: true }),
    country: new FormControl('', { nonNullable: true }),
    department: new FormControl('', { nonNullable: true }),
    level: new FormControl('', { nonNullable: true }),
  });

  private readonly q = toSignal(
    this.filterForm.controls.q.valueChanges.pipe(
      debounceTime(SEARCH_DEBOUNCE_MS),
      map((v) => v.trim()),
      distinctUntilChanged(),
    ),
    { initialValue: '' },
  );
  private readonly country = toSignal(this.filterForm.controls.country.valueChanges, { initialValue: '' });
  private readonly department = toSignal(this.filterForm.controls.department.valueChanges, { initialValue: '' });
  private readonly level = toSignal(this.filterForm.controls.level.valueChanges, { initialValue: '' });
  protected readonly sort = signal<Sort>(DEFAULT_SORT);
  protected readonly size = signal(DEFAULT_PAGE_SIZE);

  private readonly filters = computed(() => ({
    q: this.q() || undefined,
    country: this.country() || undefined,
    department: this.department() || undefined,
    level: this.level() || undefined,
  }));
  /** Any change to filters, sort or page size sends the user back to the first page. */
  protected readonly pageIndex = linkedSignal<unknown, number>({
    source: () => [this.filters(), this.sort(), this.size()],
    computation: () => 0,
  });
  protected readonly query = computed<EmployeeQuery>(() => ({
    ...this.filters(),
    page: this.pageIndex(),
    size: this.size(),
    sort: `${this.sort().active},${this.sort().direction || 'asc'}`,
  }));
  protected readonly exportUrl = computed(() => this.api.exportUrl(this.filters()));
  protected readonly result = rxResource({ params: () => this.query(), stream: ({ params }) => this.api.search(params) });
  /** Keeps the previous page on screen while the next one loads. */
  protected readonly current = linkedSignal<EmployeePage | undefined, EmployeePage>({
    source: () => (this.result.hasValue() ? this.result.value() : undefined),
    computation: (next, prev) => next ?? prev?.value ?? EMPTY_PAGE,
  });
  protected readonly errorText = computed(() => (this.result.error() ? errorMessage(this.result.error()) : ''));
  protected readonly empty = computed(() => !this.result.isLoading() && this.current().content.length === 0);

  protected onSort(sort: Sort): void {
    this.sort.set(sort.direction ? sort : DEFAULT_SORT);
  }

  protected onPage(event: PageEvent): void {
    if (event.pageSize !== this.size()) this.size.set(event.pageSize);
    else this.pageIndex.set(event.pageIndex);
  }

  protected open(employee: Employee): void {
    void this.router.navigate(['/employees', employee.id]);
  }

  protected add(): void {
    this.dialog.open<CreateEmployeeDialogComponent, void, Employee>(CreateEmployeeDialogComponent, { width: '720px', maxWidth: '95vw' })
      .afterClosed()
      .subscribe((created) => {
        if (created) this.result.reload();
      });
  }

  protected trackId(_: number, row: Employee): number {
    return row.id;
  }
}
