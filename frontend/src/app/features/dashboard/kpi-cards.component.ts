import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { AnalyticsApi } from '../../core/api/analytics-api.service';
import { formatNumber, formatUsd, formatUsdCompact } from '../../core/format';
import { PanelComponent } from '../../shared/panel.component';

interface Kpi {
  label: string;
  value: string;
  hint: string;
}

@Component({
  selector: 'app-kpi-cards',
  imports: [PanelComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './kpi-cards.component.html',
  styleUrl: './kpi-cards.component.scss',
})
export class KpiCardsComponent {
  private readonly api = inject(AnalyticsApi);

  protected readonly summary = rxResource({ stream: () => this.api.summary() });
  protected readonly kpis = computed<Kpi[]>(() => {
    if (!this.summary.hasValue()) return [];
    const s = this.summary.value();
    return [
      { label: 'Headcount', value: formatNumber(s.headcount), hint: `${s.countries} countries, ${s.departments} departments` },
      { label: 'Total payroll (USD)', value: formatUsdCompact(s.totalPayrollUsd), hint: formatUsd(s.totalPayrollUsd) },
      { label: 'Median salary', value: formatUsd(s.medianUsd), hint: `P25 ${formatUsd(s.p25Usd)} / P75 ${formatUsd(s.p75Usd)}` },
      { label: 'P90 salary', value: formatUsd(s.p90Usd), hint: `Mean ${formatUsd(s.meanUsd)}` },
    ];
  });
}
