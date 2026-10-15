import { DOCUMENT } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { finalize } from 'rxjs';
import { errorMessage } from '../../core/api/api-error';
import { AuthService, Designation } from '../../core/auth/auth.service';
import { InviteApi, InviteResponse, designationLabel } from '../../core/auth/invite-api.service';
import { NotifyService } from '../../core/ui/notify.service';
import { FieldErrorComponent } from '../../shared/field-error.component';
import { applyServerErrors } from '../../shared/server-errors';

/** Sign-up is invite-only: HR and Owners create single-use links here. */
@Component({
  selector: 'app-invites-page',
  imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule, FieldErrorComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './invites.page.html',
  styleUrl: './invites.page.scss',
})
export class InvitesPage {
  private readonly api = inject(InviteApi);
  private readonly notify = inject(NotifyService);
  private readonly origin = inject(DOCUMENT).location.origin;
  private readonly destroyRef = inject(DestroyRef);
  private readonly auth = inject(AuthService);

  protected readonly busy = signal(false);
  protected readonly formError = signal('');
  protected readonly created = signal<InviteResponse | null>(null);
  protected readonly link = computed(() => {
    const invite = this.created();
    return invite ? `${this.origin}/login?invite=${invite.token}` : '';
  });
  protected readonly designation = designationLabel;
  /** Only an Owner can invite another Owner. */
  protected readonly designations: { value: Designation; label: string }[] =
    this.auth.user()?.role === 'OWNER'
      ? [{ value: 'HR', label: 'HR' }, { value: 'OWNER', label: 'Owner' }]
      : [{ value: 'HR', label: 'HR' }];
  protected readonly form = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    role: new FormControl<Designation>('HR', { nonNullable: true, validators: [Validators.required] }),
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.formError.set('');
    this.api
      .create(this.form.getRawValue())
      .pipe(
        finalize(() => this.busy.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (invite) => this.created.set(invite),
        error: (err: unknown) => this.formError.set(applyServerErrors(this.form, err)),
      });
  }

  protected copy(): void {
    navigator.clipboard.writeText(this.link()).then(
      () => this.notify.success('Invite link copied'),
      (err: unknown) => this.notify.error(errorMessage(err, 'Could not copy, select the link and copy it manually')),
    );
  }
}
