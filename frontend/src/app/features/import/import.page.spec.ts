import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ApiError } from '../../core/api/api-error';
import { ImportApi } from '../../core/api/import-api.service';
import { ImportResult } from '../../core/api/types';
import { NotifyService } from '../../core/ui/notify.service';
import { ImportResultComponent } from './import-result.component';
import { ImportPage } from './import.page';

const RESULT: ImportResult = {
  totalRows: 5,
  imported: 3,
  failed: 2,
  errors: [
    { row: 3, message: 'email already exists' },
    { row: 5, message: 'salary must be positive' },
  ],
};

describe('ImportResultComponent', () => {
  it('renders the summary and one row per error', async () => {
    const fixture = TestBed.createComponent(ImportResultComponent);
    fixture.componentRef.setInput('result', RESULT);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('[data-testid=imported]')?.textContent).toContain('3');
    expect(el.querySelector('[data-testid=failed]')?.textContent).toContain('2');
    const rows = el.querySelectorAll('[data-testid=error-row]');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('email already exists');
  });

  it('hides the error table when everything imported', async () => {
    const fixture = TestBed.createComponent(ImportResultComponent);
    fixture.componentRef.setInput('result', { totalRows: 2, imported: 2, failed: 0, errors: [] });
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelector('[data-testid=error-row]')).toBeNull();
  });
});

describe('ImportPage', () => {
  const importEmployees = vi.fn();
  const notify = { success: vi.fn(), error: vi.fn() };

  function setup() {
    TestBed.configureTestingModule({
      providers: [
        { provide: ImportApi, useValue: { importEmployees } },
        { provide: NotifyService, useValue: notify },
      ],
    });
    const fixture = TestBed.createComponent(ImportPage);
    const el = fixture.nativeElement as HTMLElement;
    const choose = async (file: File): Promise<void> => {
      const input = el.querySelector<HTMLInputElement>('[data-testid=file-input]')!;
      Object.defineProperty(input, 'files', { value: [file], configurable: true });
      input.dispatchEvent(new Event('change'));
      await fixture.whenStable();
    };
    const upload = async (): Promise<void> => {
      el.querySelector<HTMLButtonElement>('[data-testid=upload]')!.click();
      await fixture.whenStable();
    };
    return { fixture, el, choose, upload };
  }

  beforeEach(() => {
    importEmployees.mockReset();
    notify.success.mockReset();
    notify.error.mockReset();
  });

  it('keeps Upload disabled until a file is chosen, then uploads and shows the result', async () => {
    importEmployees.mockReturnValue(of(RESULT));
    const { fixture, el, choose, upload } = setup();
    await fixture.whenStable();
    expect(el.querySelector<HTMLButtonElement>('[data-testid=upload]')!.disabled).toBe(true);

    const file = new File(['a,b'], 'people.csv', { type: 'text/csv' });
    await choose(file);
    expect(el.querySelector('[data-testid=file-name]')?.textContent).toContain('people.csv');
    await upload();

    expect(importEmployees).toHaveBeenCalledWith(file);
    expect(el.querySelectorAll('[data-testid=error-row]').length).toBe(2);
    expect(notify.error).toHaveBeenCalledWith(expect.stringContaining('2 failed'));
  });

  it('shows the server message when the upload is rejected', async () => {
    importEmployees.mockReturnValue(throwError(() => new ApiError(400, 'CSV is missing columns: salary')));
    const { fixture, el, choose, upload } = setup();
    await fixture.whenStable();
    await choose(new File(['x'], 'bad.csv'));
    await upload();
    expect(el.querySelector('[data-testid=import-error]')?.textContent).toContain('missing columns');
    expect(el.querySelector('[data-testid=import-result]')).toBeNull();
  });
});
