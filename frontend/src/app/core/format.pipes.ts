import { Pipe, PipeTransform } from '@angular/core';
import { formatCurrency, formatDateTime, formatNumber, formatPercent, formatUsd, formatUsdCompact } from './format';

@Pipe({ name: 'usd' })
export class UsdPipe implements PipeTransform {
  public transform(value: number, compact = false): string {
    return compact ? formatUsdCompact(value) : formatUsd(value);
  }
}

@Pipe({ name: 'money' })
export class MoneyPipe implements PipeTransform {
  public transform(value: number, currency: string): string {
    return formatCurrency(value, currency);
  }
}

@Pipe({ name: 'pct' })
export class PercentPipe implements PipeTransform {
  public transform(value: number, digits = 1): string {
    return formatPercent(value, digits);
  }
}

@Pipe({ name: 'num' })
export class NumberFormatPipe implements PipeTransform {
  public transform(value: number): string {
    return formatNumber(value);
  }
}

@Pipe({ name: 'dateTime' })
export class DateTimePipe implements PipeTransform {
  public transform(value: string): string {
    return formatDateTime(value);
  }
}
