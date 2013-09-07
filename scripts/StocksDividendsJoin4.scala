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
 * This exercise explores joining two data sets, stocks and dividends.
 * You invoke the script like this:
 *   ./run scripts/StocksDividendsJoin4.scala \
 *     --stocks    data/stocks/IBM.csv \
 *     --dividends data/dividends/IBM.csv \
 *     --output output/IBM-stocks-dividends-join.txt
 * You can also try AAPL, GE, and INTC.
 */

class StocksDividendsJoin4(args : Args) extends Job(args) {

  /*
   * All the fields in the stocks and dividends records. 
   * Because we're going to join these data streams, it's convenient to use
   * unique names across both schema. We just need unique names for "ymd", 
   * e.g., 'symd for stocks.ymd and 'dymd for dividends.ymd.
   * Note that for these particular data samples, the symbol and exchange
   * are not in the data.
   * Finally, we previously used Tuples to declare schemas like these, but here
   * we use two alternatives, a List and an Enumeration. The tuple is the most
   * compact syntax, BUT it's limited to 22 fields. Some data sets can have
   * more than 22 fields!
   */
  val stockSchema = 
    List ('symd, 'price_open, 'price_high, 'price_low, 'price_close, 'volume, 'price_adj_close)
  object dividendsSchema extends Enumeration {
    val dymd, dividend = Value
  }
  import dividendsSchema._    // Note that the import is required

  /*
   * We read CSV input for the stocks and dividends. 
   */
  val stocksPipe = new Csv(args("stocks"), fields = stockSchema)
    .read
    .project('symd, 'price_close)

  val dividendsPipe = new Csv(args("dividends"), fields = dividendsSchema)
    .read

  /*
   * Inner join! Use the "tiny" variant that attempts to replicate the dividend table
   * to all nodes for faster joining.
   * Then suppress the extra ymd and specify the remaining fields in the desired order.
   */
  stocksPipe
    .joinWithTiny('symd -> 'dymd, dividendsPipe)
    .project('symd, 'price_close, 'dividend)  
    .write(Tsv(args("output")))
}
