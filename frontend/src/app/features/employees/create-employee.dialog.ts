import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { EmployeesApi } from '../../core/api/employees-api.service';
import { LookupsApi } from '../../core/api/lookups-api.service';
import { Employee, EmployeeInput } from '../../core/api/types';
import { NotifyService } from '../../core/ui/notify.service';
import { EmployeeFormComponent } from './employee-form.component';

@Component({
  selector: 'app-create-employee-dialog',
  imports: [MatDialogModule, MatButtonModule, EmployeeFormComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>Add employee</h2>
    <mat-dialog-content>
      <app-employee-form [lookups]="lookups()" [saving]="saving()" [failure]="failure()" submitLabel="Create employee" (save)="create($event)">
        <button formActions mat-button type="button" mat-dialog-close>Cancel</button>
      </app-employee-form>
    </mat-dialog-content>
  `,
  styles: `
    mat-dialog-content { max-height: 90vh; }
  `,
})
export class CreateEmployeeDialogComponent {
  private readonly api = inject(EmployeesApi);
  private readonly notify = inject(NotifyService);
  private readonly ref = inject<MatDialogRef<CreateEmployeeDialogComponent, Employee>>(MatDialogRef);

  protected readonly lookups = toSignal(inject(LookupsApi).lookups());
  protected readonly saving = signal(false);
  protected readonly failure = signal<unknown>(null);

  protected create(input: EmployeeInput): void {
    this.saving.set(true);
    this.failure.set(null);
    this.api
      .create(input)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (employee) => {
          this.notify.success(`Added ${employee.firstName} ${employee.lastName}`);
          this.ref.close(employee);
        },
        error: (err: unknown) => {
          this.failure.set(err);
          this.notify.error('Could not add employee');
        },
      });
  }
}
