import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class NotifyService {
  private readonly snackBar = inject(MatSnackBar);

  public success(message: string): void {
    this.snackBar.open(message, 'Dismiss', { duration: 4000, panelClass: 'snack-success' });
  }

  public error(message: string): void {
    this.snackBar.open(message, 'Dismiss', { duration: 8000, panelClass: 'snack-error' });
  }
}
