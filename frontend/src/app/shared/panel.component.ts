import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { ResourceRef } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { errorMessage } from '../core/api/api-error';

type PanelState = 'loading' | 'error' | 'ready';

/** Card with a title, a loading skeleton and an error + retry state driven by a resource. */
@Component({
  selector: 'app-panel',
  imports: [MatCardModule, MatButtonModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './panel.component.html',
  styleUrl: './panel.component.scss',
})
export class PanelComponent {
  public readonly heading = input.required<string>();
  public readonly subtitle = input<string>('');
  public readonly resource = input.required<ResourceRef<unknown>>();
  public readonly skeletonHeight = input(200);
  public readonly retry = output<void>();

  protected readonly state = computed<PanelState>(() => {
    const res = this.resource();
    if (res.status() === 'error') return 'error';
    return res.isLoading() ? 'loading' : 'ready';
  });
  protected readonly message = computed(() => errorMessage(this.resource().error(), 'Could not load this panel'));
  protected readonly compact = computed(() => this.skeletonHeight() < 150);

  protected onRetry(): void {
    this.resource().reload();
    this.retry.emit();
  }
}
