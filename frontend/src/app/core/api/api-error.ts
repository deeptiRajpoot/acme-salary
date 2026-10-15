/** RFC 7807 problem body as returned by the API. */
export interface ProblemBody {
  title?: string;
  status?: number;
  detail?: string;
  errors?: Record<string, string>;
}

export class ApiError extends Error {
  public constructor(
    public readonly status: number,
    message: string,
    public readonly fieldErrors: Record<string, string> = {},
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toFieldErrors(value: unknown): Record<string, string> {
  if (!isRecord(value)) return {};
  const out: Record<string, string> = {};
  for (const [key, msg] of Object.entries(value)) {
    if (typeof msg === 'string') out[key] = msg;
  }
  return out;
}

/** Builds an ApiError from an HTTP status and a (possibly non-JSON) response body. */
export function parseProblem(status: number, body: unknown, statusText = ''): ApiError {
  if (status === 0) return new ApiError(0, 'Cannot reach the server. Check your connection and retry.');
  const problem: ProblemBody = isRecord(body) ? (body as ProblemBody) : {};
  const message =
    (typeof problem.detail === 'string' && problem.detail) ||
    (typeof problem.title === 'string' && problem.title) ||
    statusText ||
    `Request failed (${status})`;
  return new ApiError(typeof problem.status === 'number' ? problem.status : status, message, toFieldErrors(problem.errors));
}

export function errorMessage(err: unknown, fallback = 'Something went wrong'): string {
  return err instanceof Error && err.message ? err.message : fallback;
}
