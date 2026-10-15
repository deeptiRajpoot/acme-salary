const LOCALE = 'en-US';

export function formatNumber(n: number): string {
  return new Intl.NumberFormat(LOCALE).format(n);
}

export function formatCurrency(n: number, currency: string): string {
  try {
    return new Intl.NumberFormat(LOCALE, { style: 'currency', currency, maximumFractionDigits: 0 }).format(n);
  } catch {
    return `${formatNumber(Math.round(n))} ${currency}`;
  }
}

export function formatUsd(n: number): string {
  return formatCurrency(n, 'USD');
}

/** Compact USD for axes and KPI cards: $1.2M, $85K. */
export function formatUsdCompact(n: number): string {
  return new Intl.NumberFormat(LOCALE, {
    style: 'currency',
    currency: 'USD',
    notation: 'compact',
    minimumFractionDigits: 0,
    maximumFractionDigits: 1,
  }).format(n);
}

export function formatPercent(n: number, digits = 1): string {
  return `${n.toFixed(digits)}%`;
}

export function formatDateTime(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString(LOCALE, { dateStyle: 'medium', timeStyle: 'short' });
}
