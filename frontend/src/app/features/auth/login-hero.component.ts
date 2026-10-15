import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { AcmeLogoComponent } from '../../shared/acme-logo.component';

interface Feature {
  icon: string;
  label: string;
  tone: string;
}

/** Left-hand marketing panel of the login page. */
@Component({
  selector: 'app-login-hero',
  imports: [MatIconModule, AcmeLogoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './login-hero.component.html',
  styleUrl: './login-hero.component.scss',
})
export class LoginHeroComponent {
  protected readonly features: Feature[] = [
    { icon: 'calendar_month', label: 'Manage Payroll', tone: 'indigo' },
    { icon: 'groups', label: 'Track Employees', tone: 'blue' },
    { icon: 'description', label: 'Generate Reports', tone: 'green' },
    { icon: 'verified_user', label: 'Ensure Compliance', tone: 'purple' },
  ];
}
