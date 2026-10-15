import { BarController, BarElement, CategoryScale, Chart, ChartOptions, Legend, LinearScale, LineController, LineElement, PointElement, Tooltip } from 'chart.js';

/** Colour-blind-safe palette: blue (primary) and orange (accent) differ in lightness as well as hue. */
export const chartColors = {
  primary: '#2a78d6',
  band: '#bcd6f5',
  accent: '#eb6834',
} as const;

/** Axis and grid colours that stay readable on the active surface. */
export function axisColors(dark: boolean): { grid: string; axis: string } {
  return dark ? { grid: '#3a3a38', axis: '#c9c8c4' } : { grid: '#e4e4e0', axis: '#52514e' };
}

let registered = false;

/** Registers only the Chart.js pieces we use, keeping the lazy dashboard chunk small. */
export function registerCharts(): void {
  if (registered) return;
  Chart.register(BarController, BarElement, LineController, LineElement, PointElement, CategoryScale, LinearScale, Tooltip, Legend);
  registered = true;
}

export function baseOptions(yTitle: string, xTitle?: string, dark = false): ChartOptions<'bar'> {
  const colors = axisColors(dark);
  return {
    responsive: true,
    maintainAspectRatio: false,
    animation: false,
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: colors.axis },
        title: { display: !!xTitle, text: xTitle ?? '', color: colors.axis },
      },
      y: {
        grid: { color: colors.grid },
        ticks: { color: colors.axis },
        title: { display: true, text: yTitle, color: colors.axis },
      },
    },
  };
}
