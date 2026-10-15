import { HttpParams } from '@angular/common/http';

export type QueryValue = string | number | undefined | null;

/** Builds HttpParams, skipping undefined, null and empty values. */
export function toParams(query: Record<string, QueryValue>): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== null && value !== '') params = params.set(key, String(value));
  }
  return params;
}
