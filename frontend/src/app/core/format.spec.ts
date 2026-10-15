import { formatCurrency, formatNumber, formatPercent, formatUsd, formatUsdCompact } from './format';
import { MoneyPipe, PercentPipe, UsdPipe } from './format.pipes';

describe('format', () => {
  it('formats numbers with grouping', () => {
    expect(formatNumber(10000)).toBe('10,000');
  });

  it('formats currencies without decimals', () => {
    expect(formatUsd(123456.7)).toBe('$123,457');
    expect(formatCurrency(95000, 'GBP')).toBe('£95,000');
  });

  it('falls back gracefully for an unknown currency code', () => {
    expect(formatCurrency(1234.4, 'zz')).toContain('1,234');
  });

  it('formats compact USD for axes and KPI cards', () => {
    expect(formatUsdCompact(1_250_000)).toBe('$1.3M');
    expect(formatUsdCompact(85_000)).toBe('$85K');
  });

  it('formats percentages', () => {
    expect(formatPercent(7.456)).toBe('7.5%');
    expect(formatPercent(-2, 0)).toBe('-2%');
  });
});

describe('pipes', () => {
  it('delegate to the format helpers', () => {
    expect(new UsdPipe().transform(1000)).toBe('$1,000');
    expect(new UsdPipe().transform(85_000, true)).toBe('$85K');
    expect(new MoneyPipe().transform(5000, 'EUR')).toBe('€5,000');
    expect(new PercentPipe().transform(3.14159, 2)).toBe('3.14%');
  });
});
