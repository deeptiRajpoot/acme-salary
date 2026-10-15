import { ChangeDetectionStrategy, Component } from '@angular/core';
import { DistributionChartComponent } from './distribution-chart.component';
import { GroupStatsPanelComponent } from './group-stats-panel.component';
import { KpiCardsComponent } from './kpi-cards.component';
import { OutliersPanelComponent } from './outliers-panel.component';
import { PayEquityPanelComponent } from './pay-equity-panel.component';

@Component({
  selector: 'app-dashboard-page',
  imports: [KpiCardsComponent, DistributionChartComponent, GroupStatsPanelComponent, PayEquityPanelComponent, OutliersPanelComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="page-header">
      <div>
        <h1>How do we pay people?</h1>
        <p>Distribution, pay equity and outliers across the organisation</p>
      </div>
    </header>
    <div class="grid">
      <app-kpi-cards class="span-2" />
      <div class="span-2 panels">
        <app-distribution-chart />
        <app-group-stats-panel />
        <app-pay-equity-panel />
        <app-outliers-panel />
      </div>
    </div>
  `,
  styles: `
    .grid { display: grid; grid-template-columns: 1fr; gap: 16px; }
    .panels { display: grid; grid-template-columns: repeat(auto-fit, minmax(min(100%, 520px), 1fr)); grid-auto-rows: 1fr; gap: 16px; }
    .span-2 { grid-column: 1 / -1; }
  `,
})
export class DashboardPage {}
