import { BreakpointObserver } from '@angular/cdk/layout';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { map } from 'rxjs';
import { ThemeToggleComponent } from './shared/theme-toggle.component';
import { AuthService } from './core/auth/auth.service';

interface NavItem {
  path: string;
  label: string;
  icon: string;
  exact: boolean;
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule, MatButtonModule, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly auth = inject(AuthService);
  protected readonly nav: NavItem[] = [
    { path: '/', label: 'Pay overview', icon: 'insights', exact: true },
    { path: '/employees', label: 'Employees', icon: 'groups', exact: false },
    { path: '/import', label: 'Import CSV', icon: 'upload_file', exact: false },
    { path: '/invites', label: 'Invite users', icon: 'person_add', exact: false },
  ];
  protected readonly narrow = toSignal(
    inject(BreakpointObserver)
      .observe('(max-width: 959px)')
      .pipe(map((state) => state.matches)),
    { initialValue: false },
  );
}
