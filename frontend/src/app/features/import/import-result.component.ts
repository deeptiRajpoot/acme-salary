import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { ImportResult } from '../../core/api/types';
import { NumberFormatPipe } from '../../core/format.pipes';

@Component({
  selector: 'app-import-result',
  imports: [MatCardModule, MatTableModule, NumberFormatPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section aria-label="Import result" data-testid="import-result">
      <div class="stats">
        <mat-card appearance="outlined"><mat-card-content><div class="label">Rows read</div><div class="value" data-testid="total">{{ result().totalRows | num }}</div></mat-card-content></mat-card>
        <mat-card appearance="outlined"><mat-card-content><div class="label">Imported</div><div class="value ok" data-testid="imported">{{ result().imported | num }}</div></mat-card-content></mat-card>
        <mat-card appearance="outlined"><mat-card-content><div class="label">Failed</div><div class="value" [class.bad]="result().failed > 0" data-testid="failed">{{ result().failed | num }}</div></mat-card-content></mat-card>
      </div>
      @if (result().errors.length > 0) {
        <h2 class="errors-title">Rows that were not imported</h2>
        <div class="scroll">
          <table mat-table [dataSource]="result().errors" [trackBy]="trackRow" aria-label="Row errors" class="full-width">
            <ng-container matColumnDef="row">
              <th mat-header-cell *matHeaderCellDef>Row</th>
              <td mat-cell *matCellDef="let e">{{ e.row }}</td>
            </ng-container>
            <ng-container matColumnDef="message">
              <th mat-header-cell *matHeaderCellDef>Problem</th>
              <td mat-cell *matCellDef="let e">{{ e.message }}</td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="columns; sticky: true"></tr>
            <tr mat-row *matRowDef="let row; columns: columns" data-testid="error-row"></tr>
          </table>
        </div>
      }
    </section>
  `,
  styles: `
    .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 16px; margin-bottom: 16px; }
    .label { font-size: 0.8rem; text-transform: uppercase; color: var(--mat-sys-on-surface-variant); }
    .value { font-size: 1.8rem; font-weight: 600; }
    .ok { color: light-dark(#1b5e20, #81c784); }
    .bad { color: light-dark(#b3261e, #f2b8b5); }
    .errors-title { font-size: 1.05rem; }
    .scroll { max-height: 360px; overflow: auto; }
  `,
})
export class ImportResultComponent {
  public readonly result = input.required<ImportResult>();
  protected readonly columns = ['row', 'message'];

  protected trackRow(index: number, error: ImportResult['errors'][number]): string {
    return `${error.row}-${index}`;
  }
}
