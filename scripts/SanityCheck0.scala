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

This script was adapted from the tutorial/Tutorial0.scala script that
comes with the Scalding distribution, which is subject to the same Apache License.
*/

import com.twitter.scalding._

/**
 * This script functions as a sanity check that everything is setup properly.
 * From the project root directory, run the following command. For bash users
 * (including Windows users with Cygwin):
 *   ./run tutorial/SanityCheck0.scala
 * For Windows users (without Cygwin):
 *   scala ./run tutorial/SanityCheck0.scala
 * It should run without error. The output is written to
 *   output/SanityCheck0.txt
 * What's in that file?
 */

class SanityCheck0(args : Args) extends Job(args) {

  /**
   * TextLine: Read in each line with no attempt at parsing it, and write out 
   * records as lines of text.
   */
  val in  = TextLine("scripts/SanityCheck0.scala")
  val out = TextLine("output/SanityCheck0.txt")

  /**
   * Just pipe the input to the output.
   */
  in.read.write(out)
}
