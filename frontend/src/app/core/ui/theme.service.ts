import { DOCUMENT } from '@angular/common';
import { Injectable, RendererFactory2, effect, inject, signal } from '@angular/core';

const STORAGE_KEY = 'acme.theme';

function initialDark(doc: Document): boolean {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) return saved === 'dark';
  } catch {
    // storage unavailable: fall back to the OS preference
  }
  return doc.defaultView?.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
}

/** Light/dark switch: a class on <html> drives `color-scheme`, which the Material theme follows. */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly doc = inject(DOCUMENT);
  private readonly renderer = inject(RendererFactory2).createRenderer(null, null);

  public readonly dark = signal(initialDark(this.doc));

  public constructor() {
    effect(() => {
      const dark = this.dark();
      const root = this.doc.documentElement;
      this.renderer.removeClass(root, dark ? 'light' : 'dark');
      this.renderer.addClass(root, dark ? 'dark' : 'light');
      try {
        localStorage.setItem(STORAGE_KEY, dark ? 'dark' : 'light');
      } catch {
        // storage unavailable: the choice lasts for this tab only
      }
    });
  }

  public toggle(): void {
    this.dark.update((v) => !v);
  }
}
