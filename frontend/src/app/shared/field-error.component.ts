import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl } from '@angular/forms';
import { map, startWith, switchMap } from 'rxjs';

export function messageFor(control: AbstractControl): string {
  const errors = control.errors;
  if (!errors) return '';
  if (typeof errors['server'] === 'string') return errors['server'];
  if (errors['required']) return 'This field is required';
  if (errors['email']) return 'Enter a valid email address';
  if (errors['positive'] || errors['min']) return 'Must be greater than 0';
  if (errors['minlength']) return `Enter at least ${(errors['minlength'] as { requiredLength: number }).requiredLength} characters`;
  return 'Invalid value';
}

/** Renders the first validation message of a control; use as `<mat-error appFieldError [control]="...">`. */
@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'mat-error[appFieldError]',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: '{{ message() }}',
})
export class FieldErrorComponent {
  public readonly control = input.required<AbstractControl>();

  protected readonly message = toSignal(
    toObservable(this.control).pipe(
      switchMap((c) => c.events.pipe(startWith(null), map(() => messageFor(c)))),
    ),
    { initialValue: '' },
  );
}
