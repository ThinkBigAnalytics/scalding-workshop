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
import cascading.tuple.Fields
import cascading.tuple.Fields._
import workshop.Csv

/**
 * This exercise uses the CoGroup feature. CoGroups in Cascading are really the basis
 * for joins, where two or more pipes are joined and grouped by join keys. Here we
 * use it joining more than two pipes, since the join methods only support two-way
 * joins.
 * We'll join four stocks together by date, showing their closing price:
 *   AAPL, INTC, GE, and IBM
 * You invoke the script like this:
 *   run.rb scripts/StockCoGroup5.scala \
 *     --input  data/stocks \
 *     --output output/AAPL-INTC-GE-IBM.txt
 */

class StockCoGroup5(args : Args) extends Job(args) {

  val stockSchema = 
    ('ymd, 'price_open, 'price_high, 'price_low, 'price_close, 'volume, 'price_adj_close)

  val stockNames = "AAPL" :: "INTC" :: "GE" :: "IBM" :: Nil

  /*
   * The "input" is the root folder "stocks" where the files are found.
   * The complexity of having four streams would be eliminated if we had all the
   * stock records in the same file, but then we would need the symbols in each record.
   */
  val stocksDir = args("input")

  def rename(oldName: Symbol, suffix: String) = Symbol(oldName.name + "_" + suffix)

  /*
   * This function is used to construct the Tap for each stock data source,
   * then project out the fields we want before the join, but we have to rename
   * the fields before we join, so we have no name collisions.
   */
  def startStockPipe(name: String) = 
    new Csv(stocksDir + "/" + name + ".csv", stockSchema)
      .read
      .project(('ymd, 'price_close))
      .rename(('ymd, 'price_close) -> (rename('ymd, name), rename('price_close, name)))

  /*
   * The following is equivalent to ... stockName.map(name: String => startStockPipe(name))
   */
  val stocks = stockNames.map(startStockPipe(_))
  // val stocks = stockNames.map(symbol => startStockPipe(symbol))

  /*
   * Finally, cogroup the other 3 stocks by the ymd, project out just the fields we want,
   * and insert the stock symbol into the tuples. (We don't have to; we 
   * could just remember the order of the fields...) Scalding doesn't support
   * Cascading's "insert" operator, so we use a mapTo instead.
   */
  stocks.head.coGroupBy('ymd_AAPL, InnerJoinMode) { 
    // Hacky...
    _.coGroup('ymd_INTC, stocks.tail.head)
     .coGroup('ymd_GE,   stocks.tail.tail.head)
     .coGroup('ymd_IBM,  stocks.tail.tail.tail.head)
  }
    .mapTo(('ymd_AAPL, 'ymd_INTC, 'ymd_GE, 'ymd_IBM, 
            'price_close_AAPL, 'price_close_INTC, 'price_close_GE, 'price_close_IBM) ->
           ('ymd, 'aapl, 'aapl_close, 'intc, 'intc_close, 'ge, 'ge_close, 'ibm, 'ibm_close)) {

      tuple: (String, String, String, String, String, String, String, String) => 
      (tuple._1, "AAPL", tuple._5, "INTC", tuple._6, "GE", tuple._7, "IBM", tuple._8)
    }
    .write(Tsv(args("output")))
}
