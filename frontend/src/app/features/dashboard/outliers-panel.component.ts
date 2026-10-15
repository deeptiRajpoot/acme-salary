import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { AnalyticsApi } from '../../core/api/analytics-api.service';
import { UsdPipe } from '../../core/format.pipes';
import { PanelComponent } from '../../shared/panel.component';

interface OutlierRow {
  employeeId: number;
  name: string;
  peerGroup: string;
  salaryUsd: number;
  peerMedianUsd: number;
  z: string;
}

@Component({
  selector: 'app-outliers-panel',
  imports: [PanelComponent, MatTableModule, RouterLink, UsdPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './outliers-panel.component.html',
  styleUrl: './chart.scss',
})
export class OutliersPanelComponent {
  private readonly api = inject(AnalyticsApi);

  protected readonly columns = ['name', 'peers', 'salary', 'median', 'z'];
  protected readonly outliers = rxResource({ stream: () => this.api.outliers() });
  protected readonly rows = computed<OutlierRow[]>(() =>
    this.outliers.hasValue()
      ? this.outliers.value().map((o) => ({
          employeeId: o.employeeId,
          name: o.name,
          peerGroup: `${o.country} / ${o.department} / ${o.level}`,
          salaryUsd: o.salaryUsd,
          peerMedianUsd: o.peerMedianUsd,
          z: `${o.zScore > 0 ? '+' : ''}${o.zScore.toFixed(1)} ${o.zScore > 0 ? '(above)' : '(below)'}`,
        }))
      : [],
  );

  protected trackRow(_: number, row: OutlierRow): number {
    return row.employeeId;
  }
}
