aapl = load 'data/stocks/AAPL.csv' using PigStorage(',') as (
  date:            chararray,
  price_open:      float,
  price_high:      float,
  price_low:       float,
  price_close:     float,
  volume:          int,
  price_adj_close: float);

by_year = group aapl by SUBSTRING(date, 0, 4);

year_avg = foreach by_year generate group, AVG(aapl.price_close);

-- You always specify output directories:
store year_avg into 'output/AAPL-year-avg-pig';
