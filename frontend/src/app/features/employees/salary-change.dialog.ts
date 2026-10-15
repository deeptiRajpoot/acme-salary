import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';
import { EmployeesApi } from '../../core/api/employees-api.service';
import { Employee } from '../../core/api/types';
import { formatCurrency } from '../../core/format';
import { NotifyService } from '../../core/ui/notify.service';
import { FieldErrorComponent } from '../../shared/field-error.component';
import { applyServerErrors } from '../../shared/server-errors';
import { positive, trimmedMinLength } from '../../shared/validators';

export interface SalaryChangeDialogData {
  employee: Employee;
}

const REASON_MIN = 3;

@Component({
  selector: 'app-salary-change-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, FieldErrorComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>Change salary</h2>
    <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
      <mat-dialog-content class="content">
        <p class="muted">
          {{ employee.firstName }} {{ employee.lastName }}: current salary {{ current }}. A history entry is recorded.
        </p>
        <mat-form-field class="full-width">
          <mat-label>New annual salary ({{ employee.currency }})</mat-label>
          <input matInput type="number" min="0" step="any" formControlName="newSalary" data-testid="new-salary" />
          <mat-error appFieldError [control]="form.controls.newSalary" />
        </mat-form-field>
        <mat-form-field class="full-width">
          <mat-label>Reason</mat-label>
          <textarea matInput rows="3" formControlName="reason" data-testid="reason"></textarea>
          <mat-error appFieldError [control]="form.controls.reason" />
        </mat-form-field>
        @if (formError()) {
          <p class="form-error" role="alert" data-testid="form-error">{{ formError() }}</p>
        }
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button mat-flat-button type="submit" [disabled]="saving()" data-testid="submit">{{ saving() ? 'Saving...' : 'Save salary' }}</button>
      </mat-dialog-actions>
    </form>
  `,
  styles: `
    .content { min-width: min(440px, 80vw); }
    .form-error { color: var(--mat-sys-error); margin: 0; }
  `,
})
export class SalaryChangeDialogComponent {
  private readonly api = inject(EmployeesApi);
  private readonly notify = inject(NotifyService);
  private readonly ref = inject<MatDialogRef<SalaryChangeDialogComponent, Employee>>(MatDialogRef);

  protected readonly employee = inject<SalaryChangeDialogData>(MAT_DIALOG_DATA).employee;
  protected readonly current = formatCurrency(this.employee.salary, this.employee.currency);
  protected readonly saving = signal(false);
  protected readonly formError = signal('');
  protected readonly form = new FormGroup({
    newSalary: new FormControl<number | null>(null, { validators: [Validators.required, positive] }),
    reason: new FormControl('', { nonNullable: true, validators: [Validators.required, trimmedMinLength(REASON_MIN)] }),
  });

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const { newSalary, reason } = this.form.getRawValue();
    this.saving.set(true);
    this.formError.set('');
    this.api
      .changeSalary(this.employee.id, { newSalary: Number(newSalary), reason: reason.trim() })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (updated) => {
          this.notify.success('Salary updated');
          this.ref.close(updated);
        },
        error: (err: unknown) => {
          this.formError.set(applyServerErrors(this.form, err));
          this.notify.error('Salary was not changed');
        },
      });
  }
}
