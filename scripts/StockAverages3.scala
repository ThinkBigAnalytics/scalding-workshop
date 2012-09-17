/*
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

This script was adapted from the tutorial/Tutorial[2-4].scala scripts that
come with the Scalding distribution, which is subject to the same Apache License.
*/

import com.twitter.scalding._

/**
 * This exercise uses the same features as the previous exercise, but this time
 * we'll look at the year-over-year average of Apple's stock price (so you'll 
 * know which entry points you missed...).
 */

class StockAverages3(args : Args) extends Job(args) {

  // All the fields in the stock records. For this particular data sample,
  // the symbol and exchange are not in the data.
  val stockSchema = 
    ('ymd, 'price_open, 'price_high, 'price_low, 'price_close, 'volume, 'price_adj_close)

  /*
   * We read CSV input for the stock records. However, Cascading and Scalding DON'T 
   * have built-in support for CSV, so we read in each line as a whole, then split
   * it using flatMap into the desired fields.
   */
  TextLine(args("input"))
    .read
    .mapTo('line -> ('year, 'closing_price)) { line : String => 
      val fields = line.split(",")
      val yr     = year(fields(0))
      val close  = java.lang.Double.parseDouble(fields(4))
      (yr, close)
    }

  /*
   * Finally, group by the year and average the closing price over each year.
   */
    .groupBy('year) {group => group.average('closing_price -> 'average_close)}

    .write(Tsv(args("output")))

  def year(date: String): Int = Integer.parseInt(date.split("-")(0))
}
