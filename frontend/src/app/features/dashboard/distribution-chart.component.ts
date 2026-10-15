import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { AnalyticsApi } from '../../core/api/analytics-api.service';
import { formatNumber, formatUsdCompact } from '../../core/format';
import { ThemeService } from '../../core/ui/theme.service';
import { PanelComponent } from '../../shared/panel.component';
import { baseOptions, chartColors, registerCharts } from './chart-theme';

@Component({
  selector: 'app-distribution-chart',
  imports: [PanelComponent, BaseChartDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './distribution-chart.component.html',
  styleUrl: './chart.scss',
})
export class DistributionChartComponent {
  private readonly api = inject(AnalyticsApi);
  private readonly theme = inject(ThemeService);

  protected readonly buckets = rxResource({ stream: () => this.api.distribution() });
  protected readonly data = computed<ChartConfiguration<'bar'>['data']>(() => {
    const rows = this.buckets.hasValue() ? this.buckets.value() : [];
    return {
      labels: rows.map((b) => formatUsdCompact(b.fromUsd)),
      datasets: [{ label: 'Employees', data: rows.map((b) => b.count), backgroundColor: chartColors.primary, categoryPercentage: 1, barPercentage: 0.95 }],
    };
  });
  protected readonly options = computed<ChartConfiguration<'bar'>['options']>(() => ({
    ...baseOptions('Employees', 'Annual salary (USD, bucket start)', this.theme.dark()),
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          title: (items) => {
            const row = this.buckets.hasValue() ? this.buckets.value()[items[0]?.dataIndex ?? 0] : undefined;
            return row ? `${formatUsdCompact(row.fromUsd)} to ${formatUsdCompact(row.toUsd)}` : '';
          },
          label: (item) => `${formatNumber(item.parsed.y ?? 0)} employees`,
        },
      },
    },
  }));

  public constructor() {
    registerCharts();
  }
}
