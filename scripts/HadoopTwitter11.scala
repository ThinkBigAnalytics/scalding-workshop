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
 * This exercise adapts Twitter6 for Hadoop execution:
 *   1. It runs the third pipe with the limit clause that crashes in local mode.
 *   2. It configures the number of reducers to prevent an excess number being
 *      invoked.
 * To run it, you need to have the Scalding distribution installed, e.g., cloned
 * form GitHub, and built. Then you use the "scripts/scald.rb" script in the 
 * distribution. You also need Hadoop installed and running using 
 * pseudo-distributed mode or local mode your laptop.
 * For example, let's assume the Scalding distribution directory shares the same 
 * parent as the workshop directory and you're running the Hadoop services in
 * pseudo-distributed mode. Run the following commands, starting in the workshop
 * directory:
 *   # Put the workshop data directory in your HDFS home directory:
 *   hadoop fs -put data data
 *   ../scalding/scripts/scald.rb --hdfs-local --host localhost \
 *     scripts/HadoopTwitter11.scala \
 *     --input  data/twitter/tweets.tsv \
 *     --uniques output/unique-languages.txt \
 *     --count_star output/count-star.txt \
 *     --count_star_limit output/count-star-limit.txt
 * If you're on a real cluster, replace "localhost" with the correct name.
 * The output directory will also be in your HDFS home directory.
 */

class HadoopTwitter11(args : Args) extends Job(args) {

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
      .groupAll { _.count('tweet_id) }
      .write(Tsv(args("count_star")))

  /*
   * Yet another split used to implement "LIMIT N".
   * Unfortunately, when running in local mode, a bug causes a 
   * divide by zero error.
   */
  new RichPipe(tweets)
      .limit(100)
      .groupAll { _.count('tweet_id) }
      .write(Tsv(args("count_star_limit")))
}
