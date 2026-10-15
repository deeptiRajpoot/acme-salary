import { ChangeDetectionStrategy, Component, DestroyRef, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { EMPTY, catchError, filter, finalize, switchMap } from 'rxjs';
import { InviteApi, InvitePreview, designationLabel } from '../../core/auth/invite-api.service';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/api/api-error';
import { AcmeLogoComponent } from '../../shared/acme-logo.component';
import { FieldErrorComponent } from '../../shared/field-error.component';
import { applyServerErrors } from '../../shared/server-errors';
import { ThemeToggleComponent } from '../../shared/theme-toggle.component';
import { LoginHeroComponent } from './login-hero.component';

type Mode = 'signin' | 'signup';

@Component({
  selector: 'app-login-page',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    FieldErrorComponent,
    AcmeLogoComponent,
    LoginHeroComponent,
    ThemeToggleComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './login.page.html',
  styleUrl: './login.page.scss',
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly invites = inject(InviteApi);
  private readonly destroyRef = inject(DestroyRef);

  /** Invite token from the link (`/login?invite=...`); bound from the query string. */
  public readonly invite = input<string>();

  protected readonly mode = signal<Mode>('signin');
  protected readonly busy = signal(false);
  protected readonly formError = signal('');
  protected readonly showPassword = signal(false);
  protected readonly preview = signal<InvitePreview | null>(null);
  protected readonly designation = designationLabel;

  protected readonly signInForm = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    rememberMe: new FormControl(false, { nonNullable: true }),
  });
  protected readonly signUpForm = new FormGroup({
    fullName: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(100)] }),
    username: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(3), Validators.pattern(/^[A-Za-z0-9._-]+$/)] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8)] }),
  });

  public constructor() {
    toObservable(this.invite)
      .pipe(
        filter((token): token is string => !!token),
        switchMap((token) => this.invites.preview(token).pipe(catchError((err: unknown) => this.inviteFailed(err)))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((preview) => {
        this.preview.set(preview);
        this.setMode('signup');
      });
  }

  protected setMode(mode: Mode): void {
    this.mode.set(mode);
    this.formError.set('');
  }

  protected togglePassword(): void {
    this.showPassword.update((v) => !v);
  }

  protected submit(): void {
    const form = this.mode() === 'signin' ? this.signInForm : this.signUpForm;
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.formError.set('');
    const request =
      this.mode() === 'signin' ? this.auth.signIn(this.signInForm.getRawValue()) : this.auth.signUp({ ...this.signUpForm.getRawValue(), inviteToken: this.invite() ?? '' });
    request
      .pipe(
        finalize(() => this.busy.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => void this.router.navigate(['/']),
        error: (err: unknown) => this.formError.set(applyServerErrors(form, err)),
      });
  }

  private inviteFailed(err: unknown) {
    this.formError.set(errorMessage(err, 'This invite link is invalid'));
    return EMPTY;
  }
}
