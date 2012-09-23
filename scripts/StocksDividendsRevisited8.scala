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
import workshop.Csv

/**
 * This exercise returns to the original join exercise and generalizes 
 * it to allow multiple stocks-dividends pairs, for different symbols to 
 * be joined.
 * You invoke the script like this:
 *   run.rb scripts/StocksDividendsRevisited8.scala \
 *     --stocks-root-path    data/stocks/ \
 *     --dividends-root-path data/dividends/ \
 *     --symbols AAPL,INTC,GE,IBM \
 *     --output output/stocks-dividends-join.txt
 */

class StocksDividendsRevisited8(args : Args) extends Job(args) {

  /*
   * The stocks and dividends schemas we saw before. Note that the symbol
   * is missing, because it's not in the data files. We'll fix that by
   * adding the symbol. 
   */
  val stocksSchema = 
    ('symd, 'price_open, 'price_high, 'price_low, 'price_close, 'volume, 'price_adj_close)
  val dividendsSchema = ('dymd, 'dividend)

  /*
   * Read CSV input for one company's stocks or dividends and add in the symbol... 
   * Yea, we're using "symbol" to mean both the company symbol and the Scala symbol type.
   */
  def startPipe(rootPath: String, symbol: String, beforeSchema: Fields, afterSchema: Fields) = 
    new Csv(rootPath+"/"+symbol+".csv", beforeSchema)
      .read
      .write(Tsv("output/dump1"))
      .map(beforeSchema -> afterSchema) { before: Fields => (symbol, before)}
      .flatten[String]((Symbol(symbol), beforeSchema) -> afterSchema)

  val symbols = args("symbols").split(",").toList
  val stocksRoot = args("stocks-root-path")
  val dividendsRoot = args("dividends-root-path")
  
  val stocksPipes = symbols.map { symbol => 
          new Csv(stocksRoot+"/"+symbol+".csv", stocksSchema)
          .read
          .mapTo(stocksSchema -> ('symd, 'ssymbol, 'price_close)) { 
            record: (String,String,String,String,String,String,String) => (symbol, record._1, record._5) 
          } 
      }
      .reduceLeft( _ ++ _ )

  val dividendsPipe = symbols.map { symbol => 
          new Csv(dividendsRoot+"/"+symbol+".csv", dividendsSchema)
          .read
          .mapTo(dividendsSchema -> ('dymd, 'dsymbol, 'dividend)) { 
            record: (String,String) => (symbol, record._1, record._2) 
          }
      }
      .reduceLeft( _ ++ _ )

  /*
   * Inner join like before. Use the "tiny" variant that attempts to replicate the
   * dividend table to all nodes for faster joining.
   * Then suppress the extra ymd and specify the remaining fields in the desired order.
   */
  stocksPipes
    .joinWithTiny(('ssymbol, 'symd) -> ('dsymbol, 'dymd), dividendsPipe)
    .project('symd, 'ssymbol, 'price_close, 'dividend)  
    .write(Tsv(args("output")))
}
