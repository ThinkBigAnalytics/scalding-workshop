# Scalding Workshop

This workshop/tutorial takes you through the basic principles of writing data analysis applications with [Scalding](https://github.com/twitter/scalding), a Scala API that wraps [Cascading](http://www.cascading.org/).

These instructions walk you through a series of exercises. Note that most of the exercises have a corresponding Scalding script (Scala source file). We use a convention of adding a number suffix to the name to indicate the order of the exercises. Note that some of these exercises are adapted from the Tutorial examples that are part of the Scalding Github repo.

This document will explain many features of the Scalding and Cascading. The scripts themselves contain additional details. The Scalding and Cascading documentation has more information than we can cover here:

* [Cascading Documentation](http://www.cascading.org/documentation/), including the [Javadocs](http://docs.cascading.org/cascading/2.0/javadoc/).
* [Scalding Wiki](https://github.com/twitter/scalding/wiki).
* Scalding Scaladocs are not online, but they can be built from the [Scalding Repo](https://github.com/twitter/scalding). For convenience, we have included these files in the workshop as `api.zip`. Unzip the file and open the [index](api/index.html).

## Sanity Check

The [README](README.html) tells you to run a `SanityCheck0.scala` Scalding script as a sanity check to verify that your environment is ready to go.

Using `bash`: 

    cd $HOME/fun/scalding-workshop
    ./run.rb scripts/SanityCheck0.scala

On Windows:

    cd C:\fun\scalding-workshop
    ruby run.rb scripts/SanityCheck0.scala

From now on, we'll assume you are working in the `scalding-workshop` directory, unless otherwise noted. Also, we'll just show the `bash` versions of the subsequent `run.rb` commands. Finally, because we're lazy, we'll sometimes drop the `.scala` extension from script names when we discuss them.

Run these commands again and verify that they run without error. The output is written to `output/SanityCheck0.txt`. What's in that file?

It contains the contents of `SanityCheck0.scala`, but each line is now numbered.

> By default, when you create a new field in a **pipeline**, Cascading adds the field to the existing fields. All the fields together constitute a **tuple**.

Loading the file added the line number as an additional field.

## Projecting Fields

When you write a SQL `SELECT` statement like the following, you are *projecting* out the fields/columns or calculated values that you want, and discarding the rest of the fields. 

	SELECT name, age FROM employees;

Scalding also has a `project` method for the same purpose. Let's modify `SanityCheck0` to project out just the line we read from the file, discarding the line number. `Scripts/Project1.scala` has this change near the end of the file:

	in
      .read
      .project('line')
      .write(out)

This expression is a sequence of Cascading [Pipes](http://docs.cascading.org/cascading/2.0/javadoc/cascading/pipe/Pipe.html). However, there is not `write` method defined on the `Pipe` class. Scalding uses Scala's *implicit conversion* feature to wrap `Pipe` with a Scalding-specific `com.twitter.scalding.RichPipe` type that provides most of the methods we'll actually use.

> There are also comments in this and other scripts about specific Scalding and Cascading features that we won't cover in these notes.
 
Now, if you look at the output in `output/Project1.txt`, you'll see just the original lines from `scripts/Project1.scala`.


## FlatMap and GroupBy - Implementing Word Count

This exercise introduces several new concepts and implements the famous *hello world!* of Hadoop programming: *word count*. In word count, a corpus of documents is read, the content is tokenized into words, and the total count for each word over the entire corpus is computed.

First, we'll use two new invocation command options:

* `--input file` specifies the input file.
* `--output file` specifies the output file.

> Unlike Hadoop's HDFS API, Hive, and Pig, when you run using `--local` mode, you can't specify a directory for the input, where all files will be read, or for the output, where one or more files will be written. You have to specify input and output files.

Run the script like this, where have wrapped lines and used `\\` in to indicate the line breaks:
	
	./run.rb script/WordCount2.scala \
	  --input  data/shakespeare/plays.txt \
	  --output output/shakespeare-wc.txt

The output should be identical to the contents of `data/shakespeare-wc/simple/wc.txt`. Using the `bash diff` command (or a similar command for Windows), should show no differences:

	diff data/shakespeare-wc/simple/wc.txt output/shakespeare-wc.txt

The script uses two new data transformation features to compute the word count.

### Further Exploration

Try this additional "mini-exercise" to explore what Scalding and Cascading are doing.

#### Project the 'num Field 

Instead of projecting out `'line`, project out `'num`, the line number. (The output is boring, but now you know the name of this field!)

### flatMap

When you apply a `map` operation to a collection, each element is passed to a function that returns a new element, perhaps of a completely different type. For example, mapping a collection of integers to a collection of their string representations. A crucial feature of `map` is that the process is *one-to-one*. Each input element has a corresponding output element and the sizes of the input and output collections are the same.

The `flatMap` operation is similar, but now the output of the function called for each element is a collection of zero or more new elements. These output collections from each function call are *flattened* into a single collection. So, a crucial difference compared to `map` is that the process is *one-to-many*, where *many* could be zero!

`WordCount2` uses `flatMap` to convert each line of input text into many words:

	.flatMap('line -> 'word){ line : String => line.toLowerCase.split("\\s+")}

A bit of Scala syntax; there are *two* argument lists passed to `flatMap`. The first, `('line -> 'word)` specifies the field(s) in the tuple to pass to the mapping function, shown on the left-hand side of the arrow-like `->`, and it names the output field(s) the function will return, the single `'word` in this case.

The second function argument list is `{ line : String => line.toLowerCase.split("\\s+")}`. Scala lets you substitute curly braces `{...}` for parentheses `(...)` for function argument lists, which is most useful when the content of the "block-like" structure is a single *function literal* (a.k.a. *anonymous function*). 

The `line : String` is the argument list passed to the anonymous function, a single parameter named `line` of type `String`. On the right-hand side of the arrow-like `=>` is the body of the anonymous function. In this case it converts `line` to lower case and splits it on whitespace into an array of words.


### groupBy

Once we have a stream of individual words, we want to count the occurrences of each word. To do that, we need to group together all occurrences of the same words. The `groupBy` operation is used by `WordCount2` to do this. 

	.groupBy('word){group => group.size('count)}

The calling syntax is similar to `flatMap`. The first argument list specifies one or more fields to group over, forming the "key". The second argument is a function literal. It takes a single argument of type `com.twitter.scalding.GroupBuilder` that gives us a hook to the constructed group of words so we can compute what we need from it. In this case, all we care about is the size of the group, which we'll name `'count`.

### Further Exploration

Try these additional "mini-exercises" to explore what Scalding and Cascading are doing.

#### Remove the groupBy

Comment out the `groupBy` line so that the raw results of `flatMap` are written to the output instead of the word count output. Note the fields that are written. 

You'll see the line number, the whole line, and an individual word from the line. Note that the line number and line are repeated for each word in the line.

#### Group Again by Count

Now restore the `groupBy` line, and after it, add this line:

    .groupBy('count) { group => group.mkString('word -> 'words, "\t") }

The output lines will be extremely long at the beginning of the file, but very short at the end. This second `groupBy` regroups the `'word` and `'count` output from the previous pipe. It groups by count so we now have all the words with two occurrence on a line, followed by all the words with two occurrences, etc. At the end of the output, which words have the most occurrences?
