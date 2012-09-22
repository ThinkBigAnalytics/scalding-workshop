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
*/

import com.twitter.scalding._
import workshop.Csv

/**
 * This exercise explores joining two data sets, stocks and dividends.
 */

class StocksDividendsJoin4(args : Args) extends Job(args) {

  /*
   * All the fields in the stocks and dividends records. 
   * Because we're going to join these data streams, it's convenient to use
   * unique names across both schema. We just need unique names for "ymd", 
   * e.g., 'symd for stocks.ymd and 'dymd for dividends.ymd.
   * Note that for these particular data samples, the symbol and exchange
   * are not in the data.
   */
  val stockSchema = 
    ('symd, 'price_open, 'price_high, 'price_low, 'price_close, 'volume, 'price_adj_close)
  val dividendSchema = ('dymd, 'dividend)

  /*
   * We read CSV input for the stocks and dividends. 
   */
  val stocksPipe = new Csv(args("stocks"), stockSchema)
    .read
    .project('symd, 'price_close)

  val dividendsPipe = new Csv(args("dividends"), dividendSchema)
    .read

  /*
   * Inner join!
   * Then suppress the extra ymd and specify the remaining fields in the desired order.
   */
  stocksPipe
    .joinWithSmaller('symd -> 'dymd, dividendsPipe)
    .project('symd, 'price_close, 'dividend)  
    .write(Tsv(args("output")))
}
