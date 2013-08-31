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
import org.apache.hadoop.util.ToolRunner
import org.apache.hadoop.conf.Configuration

/**
 * Entry point for Scalding jobs on Hadoop.
 * This is an alternative to using Scalding's own scald.rb script.
 * Code adapted from com.twitter.scalding.Tool
 */
object Main {
    def main(args: Array[String]) {
        ToolRunner.run(new Configuration, new Tool, args);
    }
}

