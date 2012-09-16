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

This script was adapted from the Tutorial0.scala script distributed with
the Scalding distribution.
*/
import com.twitter.scalding._

/**
 * This script functions as a sanity check that everything is setup properly.
 * From the project root directory, run this command:
 *   ruby run.rb tutorial/SanityCheck0.scala
 * It should run without error. The output is written to
 *   output/SanityCheck_0.txt
 * What's in that file?
 */


/**
 * Scalding's scripts/scald.rb script ASSUMES that the class name MATCHES
 * the file name (i.e., like the Java convention) AND no package is used. 
 * We'll follow these conventions here, even though we aren't using scald.rb.
 *
 * Scalding jobs are a subclass of com.twitter.scalding.Job. 
 * The constructor takes a com.twitter.scalding.Args object, which may be
 * ignored.
 */
class SanityCheck_0(args : Args) extends Job(args) {

  /**
   * com.twitter.scalding.Source is the parent type for all
   * input and output data sources. Scalding bundles two implementations:
   *   TextLine:  Read in each line with no attempt at parsing it.
   *   Tsv:       Tab-seperated values.
   */
  val in  = TextLine("tutorial/SanityCheck_0.scala")
  val out = TextLine("output/SanityCheck_0.txt")

  /**
   * Use the simplest of Cascading pipelines; just pipe the input to the output.
   */
  in.read.write(out)

  /**
   * By the way, if you look at the docs for Pipe, you won't find write there. That's
   * because it's actually defined on com.twitter.scalding.RichPipe. Most of the methods
   * we call on Pipes will actually be found on RichPipe; in typical scala style,
   * the conversion between them is implicit.
   */
}
