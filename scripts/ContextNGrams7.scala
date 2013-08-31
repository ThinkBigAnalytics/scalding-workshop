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

This script was adapted from the tutorial/Tutorial[2-4].scala scripts that
come with the Scalding distribution, which is subject to the same Apache License.
*/

import com.twitter.scalding._

/**
 * This exercise revisits the Twitter data to demonstrate fold and reduce operations.
 * You invoke the script like this:
 *   run.rb scripts/ContextNGrams7.scala \
 *     --input  data/shakespeare/plays.txt \
 *     --output output/context-ngrams.txt \
 *     --ngram-prefix "I love" \
 *     --count 10
 */

class ContextNGrams7(args : Args) extends Job(args) {
  
  val ngramPrefix = args.list("ngram-prefix").mkString(" ")
  val numberOfNGrams = args.getOrElse("count", "10").toInt
  val ngramRE = (ngramPrefix + """\s+(\w+)""").r

  // Used to sort (phrase,count) by count, descending.
  val countReverseComparator = (tuple1:(String,Int), tuple2:(String,Int)) => tuple1._2 > tuple2._2
      
  // This flow adds a debug step, which writes the records to the console.
  val lines = TextLine(args("input"))
    .read
    .flatMap('line -> 'ngram) { text: String => ngramRE.findAllIn(text).toIterable }
    .discard('offset, 'line)
    .groupBy('ngram) { _.size('count) }
    .groupAll { _.sortWithTake[(String,Int)](('ngram,'count) -> 'sorted_ngrams, numberOfNGrams)(countReverseComparator) }
    .debug
    .write(Tsv(args("output")))
}