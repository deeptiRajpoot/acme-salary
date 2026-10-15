import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { ApiError } from '../../core/api/api-error';
import { EmployeesApi } from '../../core/api/employees-api.service';
import { NotifyService } from '../../core/ui/notify.service';
import { makeEmployee } from '../../testing/fixtures';
import { SalaryChangeDialogComponent } from './salary-change.dialog';

describe('SalaryChangeDialogComponent', () => {
  const employee = makeEmployee({ id: 9 });
  const changeSalary = vi.fn();
  const close = vi.fn();
  const notify = { success: vi.fn(), error: vi.fn() };

  function setup() {
    TestBed.configureTestingModule({
      providers: [
        { provide: EmployeesApi, useValue: { changeSalary } },
        { provide: NotifyService, useValue: notify },
        { provide: MatDialogRef, useValue: { close } },
        { provide: MAT_DIALOG_DATA, useValue: { employee } },
      ],
    });
    const fixture = TestBed.createComponent(SalaryChangeDialogComponent);
    const el = fixture.nativeElement as HTMLElement;
    const fill = (salary: string, reason: string): void => {
      const s = el.querySelector<HTMLInputElement>('[data-testid=new-salary]')!;
      const r = el.querySelector<HTMLTextAreaElement>('[data-testid=reason]')!;
      s.value = salary;
      s.dispatchEvent(new Event('input'));
      r.value = reason;
      r.dispatchEvent(new Event('input'));
    };
    const submit = async (): Promise<void> => {
      el.querySelector<HTMLButtonElement>('[data-testid=submit]')!.click();
      await fixture.whenStable();
    };
    return { fixture, el, fill, submit };
  }

  beforeEach(() => {
    changeSalary.mockReset();
    close.mockReset();
    notify.success.mockReset();
    notify.error.mockReset();
  });

  it('rejects a non-positive amount and a too-short reason without calling the API', async () => {
    const { fixture, el, fill, submit } = setup();
    await fixture.whenStable();
    fill('0', 'ab');
    await submit();
    expect(changeSalary).not.toHaveBeenCalled();
    expect(el.textContent).toContain('Must be greater than 0');
    expect(el.textContent).toContain('Enter at least 3 characters');
  });

  it('submits trimmed values and closes with the updated employee on success', async () => {
    const updated = makeEmployee({ id: 9, salary: 100000 });
    changeSalary.mockReturnValue(of(updated));
    const { fixture, fill, submit } = setup();
    await fixture.whenStable();
    fill('100000', '  Annual review ');
    await submit();
    expect(changeSalary).toHaveBeenCalledWith(9, { newSalary: 100000, reason: 'Annual review' });
    expect(notify.success).toHaveBeenCalled();
    expect(close).toHaveBeenCalledWith(updated);
  });

  it('keeps the dialog open and shows server field errors on a 400', async () => {
    changeSalary.mockReturnValue(throwError(() => new ApiError(400, 'Validation failed', { reason: 'reason is too vague' })));
    const { fixture, el, fill, submit } = setup();
    await fixture.whenStable();
    fill('120000', 'okay then');
    await submit();
    expect(close).not.toHaveBeenCalled();
    expect(notify.error).toHaveBeenCalled();
    expect(el.textContent).toContain('reason is too vague');
    expect(el.querySelector('[data-testid=form-error]')?.textContent).toContain('Please fix the highlighted fields');
  });

  it('shows a general message when the server error is not field specific', async () => {
    changeSalary.mockReturnValue(throwError(() => new ApiError(404, 'Employee 9 not found')));
    const { fixture, el, fill, submit } = setup();
    await fixture.whenStable();
    fill('120000', 'okay then');
    await submit();
    expect(el.querySelector('[data-testid=form-error]')?.textContent).toContain('Employee 9 not found');
  });
});
