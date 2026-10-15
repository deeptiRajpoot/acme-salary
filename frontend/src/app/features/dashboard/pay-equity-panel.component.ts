import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatTableModule } from '@angular/material/table';
import { AnalyticsApi } from '../../core/api/analytics-api.service';
import { formatPercent } from '../../core/format';
import { PercentPipe, UsdPipe } from '../../core/format.pipes';
import { PanelComponent } from '../../shared/panel.component';

const WORST_COHORTS = 8;
const WARN_THRESHOLD = 5;

@Component({
  selector: 'app-pay-equity-panel',
  imports: [PanelComponent, MatTableModule, UsdPipe, PercentPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pay-equity-panel.component.html',
  styleUrl: './pay-equity-panel.component.scss',
})
export class PayEquityPanelComponent {
  private readonly api = inject(AnalyticsApi);

  protected readonly columns = ['cohort', 'female', 'male', 'gap'];
  protected readonly gap = rxResource({ stream: () => this.api.payGap() });
  /** Positive gap = women paid less: (median M - median F) / median M. */
  protected readonly overall = computed(() => (this.gap.hasValue() ? this.gap.value().overallGapPercent : 0));
  protected readonly worst = computed(() =>
    this.gap.hasValue() ? [...this.gap.value().cohorts].sort((a, b) => (b.gapPercent ?? 0) - (a.gapPercent ?? 0)).slice(0, WORST_COHORTS) : [],
  );
  protected readonly isWarning = computed(() => this.overall() > WARN_THRESHOLD);
  protected readonly summary = computed(() =>
    this.overall() > 0
      ? `Women's median pay is ${formatPercent(this.overall())} below men's in like-for-like cohorts.`
      : `No adverse gap for women in like-for-like cohorts (${formatPercent(this.overall())}).`,
  );
}
