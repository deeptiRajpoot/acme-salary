import { FormGroup } from '@angular/forms';
import { ApiError } from '../core/api/api-error';

/**
 * Applies the problem `errors` map (field -> message) to matching controls and returns a
 * form-level message for anything that is not field-specific.
 */
export function applyServerErrors(form: FormGroup, err: unknown): string {
  if (!(err instanceof ApiError)) return 'Unexpected error';
  const entries = Object.entries(err.fieldErrors);
  let unmatched = entries.length === 0;
  for (const [field, message] of entries) {
    const control = form.get(field);
    if (!control) {
      unmatched = true;
      continue;
    }
    control.setErrors({ server: message });
    control.markAsTouched();
  }
  return unmatched ? err.message : 'Please fix the highlighted fields';
}
