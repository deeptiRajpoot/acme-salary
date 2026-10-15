import { Employee, EmployeePage, Lookups } from '../core/api/types';

export function makeEmployee(overrides: Partial<Employee> = {}): Employee {
  return {
    id: 1,
    firstName: 'Ada',
    lastName: 'Lovelace',
    email: 'ada@acme.example',
    country: 'GB',
    department: 'Engineering',
    level: 'L5',
    jobTitle: 'Staff Engineer',
    gender: 'F',
    hireDate: '2021-03-15',
    salary: 95000,
    currency: 'GBP',
    salaryUsd: 120650,
    ...overrides,
  };
}

export function makePage(content: Employee[], totalElements = content.length): EmployeePage {
  return { content, page: 0, size: 25, totalElements, totalPages: Math.ceil(totalElements / 25) };
}

export const LOOKUPS: Lookups = {
  countries: [
    { code: 'GB', name: 'United Kingdom', currency: 'GBP' },
    { code: 'DE', name: 'Germany', currency: 'EUR' },
  ],
  departments: ['Engineering', 'Sales'],
  levels: ['L1', 'L2', 'L5'],
  currencies: ['GBP', 'EUR', 'USD'],
};
