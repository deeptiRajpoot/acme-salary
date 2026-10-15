import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** ACME wordmark: gradient "A" mark plus the name. */
@Component({
  selector: 'app-acme-logo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="logo" [class.small]="small()">
      <svg viewBox="0 0 64 56" aria-hidden="true">
        <defs>
          <linearGradient id="acmeLogoGrad" x1="0" y1="1" x2="1" y2="0">
            <stop offset="0" stop-color="#6a8bff" />
            <stop offset="1" stop-color="#2d3fb5" />
          </linearGradient>
        </defs>
        <path d="M32 2 L62 54 H46 L32 28 L18 54 H2 Z" fill="url(#acmeLogoGrad)" />
        <path d="M32 28 L40 42 H24 Z" fill="#ffffff" fill-opacity=".35" />
      </svg>
      <span class="name">ACME</span>
    </span>
  `,
  styles: `
    .logo { display: inline-flex; align-items: center; gap: 12px; color: var(--mat-sys-on-surface); }
    .logo svg { width: 64px; height: 56px; }
    .name { font-size: 3rem; font-weight: 800; letter-spacing: 1px; line-height: 1; }
    .small svg { width: 44px; height: 38px; }
    .small .name { font-size: 2.1rem; }
  `,
})
export class AcmeLogoComponent {
  public readonly small = input(false);
}
