import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { rxResource, toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { Router, RouterLink } from '@angular/router';
import { filter, finalize, switchMap } from 'rxjs';
import { errorMessage } from '../../core/api/api-error';
import { EmployeesApi } from '../../core/api/employees-api.service';
import { LookupsApi } from '../../core/api/lookups-api.service';
import { Employee, EmployeeInput } from '../../core/api/types';
import { MoneyPipe, UsdPipe } from '../../core/format.pipes';
import { NotifyService } from '../../core/ui/notify.service';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/confirm-dialog.component';
import { PanelComponent } from '../../shared/panel.component';
import { EmployeeFormComponent } from './employee-form.component';
import { SalaryChangeDialogComponent, SalaryChangeDialogData } from './salary-change.dialog';
import { SalaryHistoryComponent } from './salary-history.component';

@Component({
  selector: 'app-employee-detail-page',
  imports: [
    RouterLink, MatButtonModule, MatCardModule, MatIconModule, PanelComponent, EmployeeFormComponent,
    SalaryHistoryComponent, MoneyPipe, UsdPipe,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './employee-detail.page.html',
  styleUrl: './employee-detail.page.scss',
})
export class EmployeeDetailPage {
  private readonly api = inject(EmployeesApi);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly notify = inject(NotifyService);

  /** Bound from the `:id` route param. */
  public readonly id = input.required<string>();

  protected readonly lookups = toSignal(inject(LookupsApi).lookups());
  protected readonly employee = rxResource({ params: () => Number(this.id()), stream: ({ params }) => this.api.get(params) });
  protected readonly history = rxResource({ params: () => Number(this.id()), stream: ({ params }) => this.api.salaryHistory(params) });
  protected readonly saving = signal(false);
  protected readonly failure = signal<unknown>(null);
  protected readonly loadError = computed(() => (this.employee.error() ? errorMessage(this.employee.error(), 'Could not load employee') : ''));
  protected readonly entries = computed(() => (this.history.hasValue() ? this.history.value() : []));

  protected save(input: EmployeeInput): void {
    const current = this.employee.hasValue() ? this.employee.value() : undefined;
    if (!current) return;
    this.saving.set(true);
    this.failure.set(null);
    // PUT requires the salary: send it unchanged. Salary changes go through the salary endpoint.
    this.api
      .update(current.id, { ...input, salary: current.salary, currency: current.currency })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (updated) => {
          this.employee.set(updated);
          this.notify.success('Profile saved');
        },
        error: (err: unknown) => {
          this.failure.set(err);
          this.notify.error('Could not save profile');
        },
      });
  }

  protected changeSalary(employee: Employee): void {
    this.dialog
      .open<SalaryChangeDialogComponent, SalaryChangeDialogData, Employee>(SalaryChangeDialogComponent, { data: { employee } })
      .afterClosed()
      .subscribe((updated) => {
        if (!updated) return;
        this.employee.set(updated);
        this.history.reload();
      });
  }

  protected remove(employee: Employee): void {
    const data: ConfirmDialogData = {
      title: 'Delete employee',
      message: `Delete ${employee.firstName} ${employee.lastName}? This cannot be undone.`,
      confirmLabel: 'Delete',
    };
    this.dialog
      .open<ConfirmDialogComponent, ConfirmDialogData, boolean>(ConfirmDialogComponent, { data })
      .afterClosed()
      .pipe(
        filter(Boolean),
        switchMap(() => this.api.remove(employee.id)),
      )
      .subscribe({
        next: () => {
          this.notify.success('Employee deleted');
          void this.router.navigate(['/employees']);
        },
        error: (err: unknown) => this.notify.error(errorMessage(err, 'Delete failed')),
      });
  }
}
