INSERT INTO country (code, name, currency) VALUES
  ('US', 'United States', 'USD'),
  ('IN', 'India', 'INR'),
  ('GB', 'United Kingdom', 'GBP'),
  ('DE', 'Germany', 'EUR'),
  ('CA', 'Canada', 'CAD'),
  ('AU', 'Australia', 'AUD'),
  ('SG', 'Singapore', 'SGD'),
  ('BR', 'Brazil', 'BRL'),
  ('JP', 'Japan', 'JPY'),
  ('FR', 'France', 'EUR');

INSERT INTO department (name) VALUES
  ('Engineering'), ('Product'), ('Sales'), ('Marketing'),
  ('Finance'), ('Human Resources'), ('Operations'), ('Customer Support');

INSERT INTO job_level (code, sort_order) VALUES
  ('L1', 1), ('L2', 2), ('L3', 3), ('L4', 4), ('L5', 5), ('L6', 6), ('L7', 7);

INSERT INTO currency_rate (currency, rate_to_usd) VALUES
  ('USD', 1.0), ('EUR', 1.08), ('GBP', 1.27), ('CAD', 0.74), ('AUD', 0.66),
  ('SGD', 0.74), ('BRL', 0.20), ('JPY', 0.0067), ('INR', 0.012);
