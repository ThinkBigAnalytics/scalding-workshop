/*
Copyright 2013 Concurrent Thought, Inc.
Copyright 2012 Think Big Analytics, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import com.twitter.scalding._

/**
 * This exercise uses the same features as the previous exercise, but this time
 * we'll look at the year-over-year average of Apple's stock price (so you'll 
 * know which entry points you missed...).
 * You invoke the script like this:
 *   ./run scripts/StockAverages3a.scala \
 *     --input  data/stocks/AAPL.csv \
 *     --output output/AAPL-avg.txt
 */

class StockAverages3a(args : Args) extends Job(args) {

  val stockSchema = 
    ('ymd, 'price_open, 'price_high, 'price_low, 'price_close, 'volume, 'price_adj_close)

  /*
   * We read CSV input for the stock records. We'll just keep the year-month-day
   * and the "adjusted" closing price, which accounts for historical stock splits
   * and dividend payments to give you a better view of how much a stock has
   * appreciated.
   */
  new Csv(args("input"), fields = stockSchema)
    .read
    .project('ymd, 'price_adj_close)

  /*
   * A hack to let us pattern matching on the tuple, e.g., "(ymd, close)". 
   */
    .mapTo(('ymd, 'price_adj_close) -> ('year, 'closing_price)) { 
      tup: (String,String) =>
        val (ymd, close) = tup  // Extract the 1st, 2nd elements from the tuple.
        // TODO: Add exception handling logic in case the 
        // double conversion fails! (See StocksAverages3b ...)
        // A two-element tuple is returned. (Note the outer parentheses.)
        (toYear(ymd), close.toDouble)
    }

  /*
   * Finally, group by the year and average the closing price over each year.
   */
    .groupBy('year) {
      group => group.sizeAveStdev('closing_price -> ('size, 'average_close, 'std_dev))
    }

    .write(Tsv(args("output")))

  def toYear(date: String): Int = date.split("-")(0).toInt
}
