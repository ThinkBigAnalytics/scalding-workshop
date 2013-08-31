/*
Copyright 2013 Concurrent Thought, Inc.

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
 *   1. It's located in src/main/scala so sbt finds it, compiles it, and includes
 *      the class file in the assembly.
 *   2. It has its own main routine.
 *   3. It configures the number of reducers to prevent an excess number being
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
 *   ../scalding/scripts/scald.rb --hdfs --host localhost \
 *     scripts/HadoopTwitter11.scala \
 *     --input  data/twitter/tweets.tsv \
 *     --uniques output/unique-languages \
 *     --count-star output/count-star \
 *     --count-star-limit output/count-star-limit
 * If you're on a real cluster, replace "localhost" with the correct name.
 * The output directory will also be in your HDFS home directory. Note that our
 * three output directories for --uniques, --count-star and --count-start-limit
 * drop the ".txt" suffix. In local mode, they will still be files, BUT in Hadoop,
 * they will be directories.
 * If you edit this file, you'll need to rebuild the assembly jar:
 *   sbt assembly
 */

class HadoopTwitter11(args : Args) extends Job(args) {

  val twitterSchema = ('tweet_id, 'date, 'text, 'user_id, 'user_name, 'language)

  val tweets = Tsv(args("input"), twitterSchema)
      .read
      .filter('language) { l:String => l != "\\N"}

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
      .groupAll { _.size('tweet_id).reducers(2) }
      .write(Tsv(args("count-star")))

  /*
   * Yet another split used to implement "LIMIT N".
   */
  new RichPipe(tweets)
      .limit(100)
      .groupAll { _.size('tweet_id).reducers(2) }
      .write(Tsv(args("count-star-100")))
}
