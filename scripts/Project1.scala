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

This script was adapted from the tutorial/Tutorial1.scala script that
comes with the Scalding distribution, which is subject to the same Apache License.
*/

import com.twitter.scalding._

/**
 * Scalding's scripts/scald.rb script ASSUMES that the class name MATCHES
 * the file name (i.e., like the Java convention) AND no package is used. 
 * We'll follow these conventions here, even though we aren't using scald.rb.
 *
 * Scalding jobs are a subclass of com.twitter.scalding.Job. 
 * The constructor takes a com.twitter.scalding.Args object, which may be
 * ignored.
 */
class Project1(args : Args) extends Job(args) {

  /**
   * com.twitter.scalding.Source is the parent type for all
   * input and output data sources. Scalding bundles two implementations:
   *   TextLine:  Read in each line with no attempt at parsing it.
   *   Tsv:       Tab-seperated values.
   */
  val in  = TextLine("scripts/Project1.scala")
  val out = TextLine("output/Project1.txt")

  /**
   * Here is the change relative to SanityCheck0.scala. We added a projection 
   * to the Cascading pipeline that keeps just the 'line field (the original
   * input), discarding the line number added when we read the file. Note that
   * 'line is a Scala "symbol" (an interned String). For comparision, Ruby writes
   * symbols with a colon prefix, e.g., :line.
   * We also put the individual steps on separate lines, for better clarity.
   */
  in
    .read
    .project('line) // Use 'offset to project the line #
    .write(out)
}
