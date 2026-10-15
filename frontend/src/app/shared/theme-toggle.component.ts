import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ThemeService } from '../core/ui/theme.service';

@Component({
  selector: 'app-theme-toggle',
  imports: [MatButtonModule, MatIconModule, MatTooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button mat-icon-button type="button" [matTooltip]="theme.dark() ? 'Switch to light mode' : 'Switch to dark mode'" [attr.aria-label]="theme.dark() ? 'Switch to light mode' : 'Switch to dark mode'" [attr.aria-pressed]="theme.dark()" (click)="theme.toggle()">
      <mat-icon>{{ theme.dark() ? 'light_mode' : 'dark_mode' }}</mat-icon>
    </button>
  `,
})
export class ThemeToggleComponent {
  protected readonly theme = inject(ThemeService);
}
