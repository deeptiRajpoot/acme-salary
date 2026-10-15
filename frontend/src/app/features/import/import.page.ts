import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { finalize } from 'rxjs';
import { errorMessage } from '../../core/api/api-error';
import { ImportApi } from '../../core/api/import-api.service';
import { ImportResult } from '../../core/api/types';
import { NotifyService } from '../../core/ui/notify.service';
import { ImportResultComponent } from './import-result.component';

@Component({
  selector: 'app-import-page',
  imports: [MatButtonModule, MatCardModule, MatIconModule, ImportResultComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './import.page.html',
  styleUrl: './import.page.scss',
})
export class ImportPage {
  private readonly api = inject(ImportApi);
  private readonly notify = inject(NotifyService);

  protected readonly file = signal<File | null>(null);
  protected readonly dragging = signal(false);
  protected readonly uploading = signal(false);
  protected readonly result = signal<ImportResult | null>(null);
  protected readonly error = signal('');

  protected pick(files: FileList | null | undefined): void {
    const file = files?.[0];
    if (!file) return;
    this.file.set(file);
    this.result.set(null);
    this.error.set('');
  }

  protected onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragging.set(true);
  }

  protected onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragging.set(false);
    this.pick(event.dataTransfer?.files);
  }

  protected upload(): void {
    const file = this.file();
    if (!file) return;
    this.uploading.set(true);
    this.error.set('');
    this.api
      .importEmployees(file)
      .pipe(finalize(() => this.uploading.set(false)))
      .subscribe({
        next: (result) => {
          this.result.set(result);
          const msg = `Imported ${result.imported} of ${result.totalRows} rows`;
          if (result.failed > 0) this.notify.error(`${msg}; ${result.failed} failed`);
          else this.notify.success(msg);
        },
        error: (err: unknown) => {
          this.error.set(errorMessage(err, 'Import failed'));
          this.notify.error('Import failed');
        },
      });
  }
}
