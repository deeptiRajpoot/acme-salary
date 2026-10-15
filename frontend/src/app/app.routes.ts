import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Login | ACME Salary',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login.page').then((m) => m.LoginPage),
  },
  {
    path: '',
    canActivate: [authGuard],
    pathMatch: 'full',
    title: 'Pay overview | ACME Salary',
    loadComponent: () => import('./features/dashboard/dashboard.page').then((m) => m.DashboardPage),
  },
  {
    path: 'employees',
    canActivate: [authGuard],
    title: 'Employees | ACME Salary',
    loadComponent: () => import('./features/employees/employees.page').then((m) => m.EmployeesPage),
  },
  {
    path: 'employees/:id',
    canActivate: [authGuard],
    title: 'Employee | ACME Salary',
    loadComponent: () => import('./features/employees/employee-detail.page').then((m) => m.EmployeeDetailPage),
  },
  {
    path: 'import',
    canActivate: [authGuard],
    title: 'Import | ACME Salary',
    loadComponent: () => import('./features/import/import.page').then((m) => m.ImportPage),
  },
  {
    path: 'invites',
    title: 'Invite users | ACME Salary',
    canActivate: [authGuard],
    loadComponent: () => import('./features/invites/invites.page').then((m) => m.InvitesPage),
  },
  { path: '**', redirectTo: '' },
];
