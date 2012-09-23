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

/**
 * This exercise shows how to split a data stream and use various features
 * on the splits, including finding unique values, one way to implement COUNT(*),
 * and the equivalent of a LIMIT clause.
 * You invoke the script like this:
 *   run.rb scripts/Twitter6.scala \
 *     --input  data/twitter/tweets.tsv \
 *     --uniques output/unique-languages.txt \
 *     --count_star output/count-star.txt \
 *     --count_star_limit output/count-star-limit.txt
 */

class Twitter6(args : Args) extends Job(args) {

  val twitterSchema = ('tweet_id, 'date, 'text, 'user_id, 'user_name, 'language)

  val tweets = Tsv(args("input"), twitterSchema)
      .read

  /*
   * Split the pipe in a stream were we find the unique languages.
   * The results are written to the output specified by "--uniques".
   */
  new RichPipe(tweets)
      .project('language)
      .unique('language)
      .write(Tsv(args("uniques")))

  /*
   * Another split used to implement "COUNT(*)".
   */
  new RichPipe(tweets)
      .groupAll { _.count('tweet_id).reducers(2) }
      .write(Tsv(args("count_star")))

  /*
   * Yet another split used to implement "LIMIT N".
   * Unfortunately, when running in local mode, a bug causes a 
   * divide by zero error.
   */
  new RichPipe(tweets)
      .limit(100)
      .groupAll { _.count('tweet_id).reducers(2) }
      .write(Tsv(args("count_star_limit")))
}
