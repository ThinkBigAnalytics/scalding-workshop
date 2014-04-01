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
 * This exercise extends the previous analysis of Apple's stock price, 
 * but we'll improve the error handling. See the comment marked "ERROR HANDLING".
 * Note that we use a different input file where we added some malformed lines
 * and we have a new argument for error output:
 *   ./run scripts/StockAverages3b.scala \
 *     --input  data/stocks/AAPL-with-errors.csv \
 *     --output output/AAPL-avg.txt \ 
 *     --errors output/AAPL-errors.txt
 */

class StockAverages3b(args : Args) extends Job(args) {

  val stockSchema = 
    ('ymd, 'price_open, 'price_high, 'price_low, 'price_close, 'volume, 'price_adj_close)

  /*
   * We read CSV input for the stock records. We'll just keep the year-month-day
   * and the "adjusted" closing price, which accounts for historical stock splits
   * and dividend payments to give you a better view of how much a stock has
   * appreciated.
   */
  val csv = new Csv(args("input"), fields = stockSchema)
    .read
    .project('ymd, 'price_adj_close)

  /*
   * Unfortunately, we have to pass a single tuple argument to the anonymous function. 
   * It would be nice if we could use "(ymd: String, close: String)" as the argument
   * list. Note that you reference the Nth field in a tuple with the "_N" method
   * (it's not zero-indexed).
   * ERROR HANDLING: Now we add exception handling logic in case the year or the
   * double conversion fails. We add an additional field, a marker for "bad" fields
   * which we'll use to split the stream into good and bad records.
   * There is one other change we've made. Now we assign the intermediate Pipes
   * to values ("csv" and "mapped"), which we need for splitting the stream.
   */
   val mapped = csv
    .mapTo(('ymd, 'price_adj_close) -> ('year, 'closing_price, 'bad)) { 
      tup: (String,String) =>
        val (ymd, closeStr) = tup  // Extract the 1st, 2nd elements from the tuple.
        // First, try extracting the year and converting to an integer:
        val (year, bad1) = try {  // Return the year and a "bad" flag
          (toYear(ymd), 0)        // Bad flag is 0...
        } catch {               // ... unless an exception is thrown
          case nfe: java.lang.NumberFormatException => (0, 1)
        }
        // Now try convertin the closeStr to a double.
        val (close, bad2) = try {
          (closeStr.toDouble, 0)
        } catch {
          case nfe: java.lang.NumberFormatException => (-1.0, 1)
        }
        (year, close, bad1+bad2)  // bad > 0 if one or both failed.
    }

  /*
   * Now split the data stream. The "good" data has the "bad" flag set to 0,
   * while it's 1 or 2 for the data with malformed dates and/or doubles.
   * Then we discard the "bad" field.
   */
  val good = mapped
    .filter('bad)( (bad:Int) => bad == 0)
    .discard('bad)             // no longer needed
  val bad = mapped
    .filter('bad)( (bad:Int) => bad != 0)
    .discard('bad)             // no longer needed

  /*
   * Finally, group the good records by the year and average the closing price
   * over each year. Then write the results. For the bad records, just write 'em.
   */
  good
    .groupBy('year) {
      group => group.sizeAveStdev('closing_price -> ('size, 'average_close, 'std_dev))
    }
    .write(Tsv(args("output")))

  bad.write(Tsv(args("errors")))

  def toYear(date: String): Int = date.split("-")(0).toInt
}
