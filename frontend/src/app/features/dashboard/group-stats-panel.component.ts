import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatTableModule } from '@angular/material/table';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { AnalyticsApi } from '../../core/api/analytics-api.service';
import { GroupDimension, GroupStats } from '../../core/api/types';
import { formatUsd, formatUsdCompact } from '../../core/format';
import { UsdPipe } from '../../core/format.pipes';
import { ThemeService } from '../../core/ui/theme.service';
import { PanelComponent } from '../../shared/panel.component';
import { axisColors, baseOptions, chartColors, registerCharts } from './chart-theme';

type ViewMode = 'chart' | 'table';

function sortRows(rows: GroupStats[], dim: GroupDimension): GroupStats[] {
  const sorted = [...rows];
  if (dim === 'level')
    return sorted.sort((a, b) => a.group.localeCompare(b.group, undefined, { numeric: true }));
  return sorted.sort((a, b) => b.medianUsd - a.medianUsd);
}

@Component({
  selector: 'app-group-stats-panel',
  imports: [PanelComponent, BaseChartDirective, MatButtonToggleModule, MatTableModule, UsdPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './group-stats-panel.component.html',
  styleUrl: './chart.scss',
})
export class GroupStatsPanelComponent {
  private readonly api = inject(AnalyticsApi);
  private readonly theme = inject(ThemeService);

  protected readonly dimensions: GroupDimension[] = ['country', 'department', 'level'];
  protected readonly columns = ['group', 'headcount', 'p25', 'median', 'p75'];
  protected readonly dim = signal<GroupDimension>('country');
  protected readonly view = signal<ViewMode>('chart');
  protected readonly stats = rxResource({
    params: () => this.dim(),
    stream: ({ params }) => this.api.groups(params),
  });
  protected readonly rows = computed(() =>
    this.stats.hasValue() ? sortRows(this.stats.value(), this.dim()) : [],
  );
  protected readonly ariaLabel = computed(
    () => `Median salary with P25 to P75 range by ${this.dim()}`,
  );
  protected readonly data = computed<ChartConfiguration<'bar'>['data']>(() => {
    const rows = this.rows();
    return {
      labels: rows.map((r) => r.group),
      datasets: [
        {
          type: 'bar',
          label: 'P25 to P75',
          data: rows.map((r) => [r.p25Usd, r.p75Usd]),
          backgroundColor: chartColors.band,
          borderRadius: 4,
          barThickness: 22,
          order: 2,
        },
        {
          type: 'line',
          label: 'Median',
          data: rows.map((r) => r.medianUsd),
          showLine: false,
          order: 1,
          pointBackgroundColor: chartColors.accent,
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointRadius: 6,
          pointHoverRadius: 8,
        },
      ],
    } as ChartConfiguration<'bar'>['data'];
  });
  protected readonly options = computed<ChartConfiguration<'bar'>['options']>(() => {
    const dark = this.theme.dark();
    const base = baseOptions('Annual salary (USD)', undefined, dark);
    return {
      ...base,
      scales: {
        ...base.scales,
        y: {
          ...base.scales?.['y'],
          ticks: { color: axisColors(dark).axis, callback: (v) => formatUsdCompact(Number(v)) },
        },
      },
      plugins: {
        legend: { position: 'top', labels: { usePointStyle: true, color: axisColors(dark).axis } },
        tooltip: {
          callbacks: {
            label: (item) => {
              const raw = item.raw as number | [number, number];
              const value = Array.isArray(raw)
                ? `${formatUsd(raw[0])} to ${formatUsd(raw[1])}`
                : formatUsd(raw);
              return `${item.dataset.label}: ${value}`;
            },
          },
        },
      },
    };
  });

  public constructor() {
    registerCharts();
  }

  protected setDim(dim: GroupDimension): void {
    this.dim.set(dim);
  }

  protected setView(view: ViewMode): void {
    this.view.set(view);
  }
}
