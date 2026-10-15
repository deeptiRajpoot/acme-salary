import { AbstractControl, ValidationErrors } from '@angular/forms';

/** Strictly positive number (the API requires exclusiveMinimum 0). */
export function positive(control: AbstractControl): ValidationErrors | null {
  const value = control.value as number | string | null;
  if (value === null || value === '') return null;
  return Number(value) > 0 ? null : { positive: true };
}

/** At least `min` non-whitespace characters. */
export function trimmedMinLength(min: number) {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = typeof control.value === 'string' ? control.value.trim() : '';
    return value.length >= min ? null : { minlength: { requiredLength: min, actualLength: value.length } };
  };
}
