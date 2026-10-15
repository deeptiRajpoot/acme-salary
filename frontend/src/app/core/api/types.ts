import type { components } from './schema';

type Schemas = components['schemas'];

export type Employee = Schemas['Employee'];
export type EmployeeInput = Schemas['EmployeeInput'];
export type EmployeePage = Schemas['EmployeePage'];
export type SalaryChangeInput = Schemas['SalaryChangeInput'];
export type SalaryHistoryEntry = Schemas['SalaryHistoryEntry'];
export type Summary = Schemas['Summary'];
export type GroupStats = Schemas['GroupStats'];
export type Bucket = Schemas['Bucket'];
export type Outlier = Schemas['Outlier'];
export type PayGap = Schemas['PayGap'];
/** The contract marks cohort fields optional, but the API always sends them. */
export type PayGapCohort = Required<PayGap['cohorts'][number]>;
export type ImportResult = Schemas['ImportResult'];
export type Lookups = Schemas['Lookups'];
export type Country = Required<Lookups['countries'][number]>;
export type Gender = Employee['gender'];

export type GroupDimension = 'country' | 'department' | 'level';

export interface EmployeeFilters {
  q?: string;
  country?: string;
  department?: string;
  level?: string;
}

export interface EmployeeQuery extends EmployeeFilters {
  page: number;
  size: number;
  sort: string;
}
