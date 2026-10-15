import { ChangeDetectionStrategy, Component, DestroyRef, effect, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Employee, EmployeeInput, Gender, Lookups } from '../../core/api/types';
import { FieldErrorComponent } from '../../shared/field-error.component';
import { applyServerErrors } from '../../shared/server-errors';
import { positive } from '../../shared/validators';

const NAME_MAX = 100;

/** Profile form used by both the "Add employee" dialog and the detail page. */
@Component({
  selector: 'app-employee-form',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, FieldErrorComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.scss',
})
export class EmployeeFormComponent {
  public readonly initial = input<Employee>();
  public readonly lookups = input<Lookups>();
  /** Salary and currency are read-only: they change only through the salary endpoint. */
  public readonly salaryLocked = input(false);
  public readonly saving = input(false);
  public readonly submitLabel = input('Save');
  /** The last failed save; field errors are mapped onto the controls. */
  public readonly failure = input<unknown>(null);
  public readonly save = output<EmployeeInput>();

  protected readonly formError = signal('');
  protected readonly genders: { value: Gender; label: string }[] = [
    { value: 'F', label: 'Female' },
    { value: 'M', label: 'Male' },
    { value: 'X', label: 'Another / undisclosed' },
  ];
  protected readonly form = new FormGroup({
    firstName: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(NAME_MAX)] }),
    lastName: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(NAME_MAX)] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    country: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    department: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    level: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    jobTitle: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    gender: new FormControl<Gender>('F', { nonNullable: true, validators: [Validators.required] }),
    hireDate: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    salary: new FormControl<number | null>(null, { validators: [Validators.required, positive] }),
    currency: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  public constructor() {
    effect(() => {
      const employee = this.initial();
      if (employee) this.form.reset(employee);
    });
    effect(() => {
      const method = this.salaryLocked() ? 'disable' : 'enable';
      this.form.controls.salary[method]();
      this.form.controls.currency[method]();
    });
    effect(() => {
      const failure = this.failure();
      this.formError.set(failure ? applyServerErrors(this.form, failure) : '');
    });
    this.form.controls.country.valueChanges.pipe(takeUntilDestroyed(inject(DestroyRef))).subscribe((code) => this.suggestCurrency(code));
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    this.save.emit({ ...raw, salary: Number(raw.salary) });
  }

  private suggestCurrency(code: string): void {
    if (this.salaryLocked() || this.form.controls.currency.dirty) return;
    const match = this.lookups()?.countries.find((c) => c.code === code);
    if (match?.currency) this.form.controls.currency.setValue(match.currency);
  }
}
