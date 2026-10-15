import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  const root = (): HTMLElement => TestBed.inject(DOCUMENT).documentElement;

  beforeEach(() => {
    localStorage.clear();
    root().classList.remove('dark', 'light');
  });

  it('toggles the html class and remembers the choice', () => {
    const theme = TestBed.inject(ThemeService);
    theme.dark.set(false);
    TestBed.tick();
    expect(root().classList.contains('light')).toBe(true);
    theme.toggle();
    TestBed.tick();
    expect(root().classList.contains('dark')).toBe(true);
    expect(localStorage.getItem('acme.theme')).toBe('dark');
  });
});
