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
 * This exercise introduces several new concepts and implements the famous
 * "hello world!" of Hadoop programming: Word Count.
 * In Word Count, a corpus of documents is read, the content is tokenized into
 * words, and the total count for each word over the entire corpus is computed.
 * Several new features are introduced in this exercise:
 *   --input/--output options for specifying the input and output.
 *   flatMap, which is discussed in the Workshop document.
 *   groupBy, which is also discussed in the Workshop document.
 * You invoke the script like this:
 *   ./run scripts/WordCount2.scala \
 *     --input  data/shakespeare/plays.txt \
 *     --output output/shakespeare-wc.txt
 * The output should be identical to the contents of data/shakespeare-wc/simple/wc.txt.
 */

class WordCount2(args : Args) extends Job(args) {

  // Tokenize into words by by splitting on whitespace.
  val tokenizerRegex = """\s+"""
  
  /*
   * Note we don't bother capturing the input and output objects in vals.
   * Read the file specified by the --input argument and process each line
   * by trimming leading and trailing whitespace, converting to lower case,
   * then tokenizing it into words.
   * The first argument list to flatMap specifies that we pass the 'line field
   * to the anonymous function on each call and each word in the returned 
   * collection of words is given the name 'word.
   */
  TextLine(args("input"))
    .read
    .flatMap('line -> 'word) {
      line : String => line.trim.toLowerCase.split(tokenizerRegex) 
    }

  /*
   * At this point we have a stream of words in the pipeline. To count 
   * occurrences of the same word, we need to group the words together.
   * The groupBy operation does this. The first argument list to groupBy
   * specifies the fields to group over as the key. In this case, we only
   * use the 'word field. 
   * The anonymous function is passed a object of type com.twitter.scalding.GroupBuilder.
   * All we need to do is compute the size of the group and we give it an 
   * optional name, 'count.
   */
    .groupBy('word){ group => group.size('count) }

  /**
   * Don't we need to project out just the word and count? No, unlike most 
   * operations, groupBy has eliminated everything but these two fields.
   */
    .write(Tsv(args("output")))
}
