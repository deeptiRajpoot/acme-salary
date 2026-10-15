import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { SalaryHistoryEntry } from '../../core/api/types';
import { DateTimePipe, MoneyPipe } from '../../core/format.pipes';

@Component({
  selector: 'app-salary-history',
  imports: [MatTableModule, MoneyPipe, DateTimePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (entries().length === 0) {
      <p class="muted">No salary changes recorded.</p>
    } @else {
      <div class="scroll">
        <table mat-table [dataSource]="entries()" [trackBy]="trackId" aria-label="Salary history" class="full-width">
          <ng-container matColumnDef="changedAt">
            <th mat-header-cell *matHeaderCellDef>When</th>
            <td mat-cell *matCellDef="let h">{{ h.changedAt | dateTime }}</td>
          </ng-container>
          <ng-container matColumnDef="change">
            <th mat-header-cell *matHeaderCellDef class="num">Change</th>
            <td mat-cell *matCellDef="let h" class="num">
              {{ h.oldSalary === null ? 'Initial' : (h.oldSalary | money: h.currency) }}
              &rarr; {{ h.newSalary | money: h.currency }}
            </td>
          </ng-container>
          <ng-container matColumnDef="reason">
            <th mat-header-cell *matHeaderCellDef>Reason</th>
            <td mat-cell *matCellDef="let h">{{ h.reason }}</td>
          </ng-container>
          <ng-container matColumnDef="changedBy">
            <th mat-header-cell *matHeaderCellDef>By</th>
            <td mat-cell *matCellDef="let h">{{ h.changedBy }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      </div>
    }
  `,
  styles: `.scroll { overflow-x: auto; }`,
})
export class SalaryHistoryComponent {
  public readonly entries = input.required<SalaryHistoryEntry[]>();
  protected readonly columns = ['changedAt', 'change', 'reason', 'changedBy'];

  protected trackId(_: number, entry: SalaryHistoryEntry): number {
    return entry.id;
  }
}
