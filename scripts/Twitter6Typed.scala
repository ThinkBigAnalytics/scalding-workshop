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
 * This exercise is port of Twitter6 to the type-safe API.
 * You invoke the script like this:
 *   ./run scripts/Twitter6Typed.scala \
 *     --input  data/twitter/tweets.tsv \
 *     --uniques output/unique-languages.txt \
 *     --count-star output/count-star.txt \
 *     --first-100 output/first-100.txt
 */

class Twitter6Typed(args : Args) extends Job(args) {

  import TDsl._   // Import the type-safe "DSL"

  val twitterSchema = ('tweet_id, 'date, 'text, 'user_id, 'user_name, 'language)
  
  // I show more type annotations here to make it more clear what's different
  // about this API. Also, you'll notice that argument lists and the anonymous
  // functions we pass to operators are different. We don't have the ('a -> 'b)
  // mappings anymore. With the typed API, when we pass a "schema" argument,
  // Scalding knows it's a tuple of the same fields as "twitterSchema" with the
  // types of the tuple elements corresponding to the type argument given to
  // TypedPipe (or derivatives) here.
  // I recommend you compare this code to Twitter6.scala.

  // Construct a TypedPipe from a TypedTSV source.
  // Note that no ('language) argument list is passed to filter now.
  val tweets: TypedPipe[(Long,String,String,Long,String,String)] = 
    TypedTsv[(Long,String,String,Long,String,String)](
      args("input"), twitterSchema)
        .filter(schema => schema._6 != "\\N")   

  /*
   * Split the pipe in a stream were we find the unique languages.
   * The results are written to the output specified by "--uniques".
   * We don't need to declare a uniques val, but it shows the type
   * of this pipe.
   */
  val uniques: TypedPipe[(String)] =   // Create a new typed pipe...
    tweets.fork                        // by forking the existing pipe.
      .map(schema => schema._6)        // No ('a -> 'b) arg. list

  uniques
      .distinct                        // was "unique" in the field API
      .write(TypedTsv[(String)](args("uniques")))

  /*
   * Another split used to implement "COUNT(*)".
   * We don't split the sequence of function calls this time.
   * This logic is quite different than the implementation in Twitter6.scala.
   * the groupAll function doesn't take a function argument to apply to the
   * group. Instead, the pipeline is just a sequence of "1" values that we
   * group into a single group, using groupAll, then we sum those 1s.
   * Note the kind of output TypedTsv we're writing.
   */
  tweets.fork
      .map(schema => 1)    // Return a 1. We'll sum all the 1s after grouping.
      .groupAll
      .sum
      .values              // Discard the key from the groupAll which is "()"
      .write(TypedTsv[(Int)](args("count-star")))

  /*
   * Yet another split used to implement "LIMIT N". This time, instead of just
   * counting 100 lines, we write the first 100 lines. 
   */
  tweets.fork
      .limit(100)
      .write(TypedTsv[(Long,String,String,Long,String,String)](args("first-100")))
}
