# Scalding Workshop

This workshop/tutorial takes you through the basic principles of writing data analysis applications with [Scalding](https://github.com/twitter/scalding), a Scala API that wraps [Cascading](http://www.cascading.org/).

These instructions walk you through a series of exercises. Note that most of the exercises have a corresponding Scalding script (Scala source file). We use a convention of adding a number suffix to the name to indicate the order of the exercises. Note that some of these exercises are adapted from the Tutorial examples that are part of the Scalding Github repo.

This document will explain many features of the Scalding and Cascading. The scripts themselves contain additional details. The Scalding and Cascading documentation has more information than we can cover here:

* [Cascading Documentation](http://www.cascading.org/documentation/), including the [Javadocs](http://docs.cascading.org/cascading/2.0/javadoc/).
* [Scalding Wiki](https://github.com/twitter/scalding/wiki).
* Scalding Scaladocs are not online, but they can be built from the [Scalding Repo](https://github.com/twitter/scalding). For convenience, we have included these files in the workshop as `api.zip`. Unzip the file and open the [index](api/index.html).

# Basic Operations

This first section covers the common data manipulation constructs that Scalding provides, which are analogous to features found in SQL and other systems.

## Sanity Check

First, the [README](README.html) tells you to run a `SanityCheck0.scala` Scalding script as a sanity check to verify that your environment is ready to go.

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
	
	./run.rb scripts/WordCount2.scala \
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

	.groupBy('count){ group => group.mkString('word -> 'words, "\t") }

The output lines will be extremely long at the beginning of the file, but very short at the end. This second `groupBy` regroups the `'word` and `'count` output from the previous pipe. It groups by count so we now have all the words with two occurrence on a line, followed by all the words with two occurrences, etc. At the end of the output, which words have the most occurrences?

#### Improve the Word Tokenization

You probably noticed that simply splitting on whitespace is not very good, as punctuation is not removed. Replace the expression `line.toLowerCase.split("\\s+")` with a call to a `tokenize` function. Implement `tokenize` to remove punctuation. An example implementation can be found in the [Scalding README](https://github.com/twitter/scalding).

#### Eliminate Blank Lines

The very first line in the output is an empty word and a count of approximately 49,000! These are blank lines in the text. The implementation removes all other whitespace, but as written, it still returns an empty word for blank lines. Adding a filter clause will remove these lines. We'll see how below, but you can search for that section now if you want to try it.

## Input Parsing

Let's do a similar `groupBy` operation, this time to compute the average of Apple's (AAPL) closing stock price year over year (so you'll know what entry points you missed...). Also, in this exercise we'll solve a common problem; the input data is in an unsupported format.

Oddly enough, while there is a built-in `Tsv` class for tab-separated values, there is no corresponding `Csv` class, so we'll handle that ourselves.

	./run.rb scripts/StockAverages3.scala \
		--input  data/stocks/AAPL.csv \
		--output output/AAPL-year-avg.txt

You should get the following output (the input data ends in early 2010):

	1984    25.578624999999995
	1985    20.19367588932806
	1986    32.46102766798416
	1987    53.8896837944664
	1988    41.540079051383415
	1989    41.65976190476193
	1990    37.562687747035575
	1991    52.49553359683798
	1992    54.80338582677166
	1993    41.02671936758894
	1994    34.08134920634922
	1995    40.54210317460316
	1996    24.91755905511811
	1997    17.96584980237154
	1998    30.56511904761905
	1999    57.7707142857143
	2000    71.7489285714286
	2001    20.219112903225806
	2002    19.139444444444454
	2003    18.5447619047619
	2004    35.52694444444441
	2005    52.401746031746065
	2006    70.81063745019917
	2007    128.2739043824701
	2008    141.9790118577075
	2009    146.81412698412706
	2010    204.7216

Note that as I write this, AAPL is currently trading at ~$700!

### Musical Interlude: Comparison with Hive and Pig

By the way, here's the same query written using *Hive*, assuming there exists a `stocks` table and we have to select for the stock symbol and exchange:

	SELECT year(s.ymd), avg(s.price_close) 
	FROM stocks s 
	WHERE s.symbol = 'AAPL' AND s.exchange = 'NASDAQ'
	GROUP BY year(s.ymd);

It's a little more compact, in part because we can handle all issues of record parsing, etc. when we set up Hive tables, etc. However, Scalding gives us more flexibility when our SQL dialect and built-in functions aren't flexible enough for our needs.

Here's what the corresponding *Pig* script looks like (see also `scripts/StockAverages3.pig`):

	aapl = load 'data/stocks/AAPL.csv' using PigStorage(',') as (
	  ymd:             chararray,
	  price_open:      float,
	  price_high:      float,
	  price_low:       float,
	  price_close:     float,
	  volume:          int,
	  price_adj_close: float);

	by_year = group aapl by SUBSTRING(ymd, 0, 4);

	year_avg = foreach by_year generate group, AVG(aapl.price_close);

	-- You always specify output directories:
	store year_avg into 'output/AAPL-year-avg-pig';

If you have *Pig* installed, you can run this script (from this directory) with the following command:

	pig -x local scripts/StockAverages3.pig

### Further Exploration

Try these additional "mini-exercises" to learn more.

#### Project Other Averages

Try projecting averages for one or more other fields.

#### Pig

If you have Pig installed, try the Pig script. Compare the performance of the Pig vs. Scalding script, but keep in mind that because we're running in local mode, the performance comparison won't mean as much as when you run in a Hadoop cluster.

#### Hive

If you have Hive installed, try the Hive query shown above. You'll need to create a table that uses the data files first. Compare the performance of the Hive vs. Scalding script, keeping in mind the caveats mentioned for Pig.


## Joins

Let's join stocks and dividend data. To join two data sources, you set up to pipe assemblies and use one of the join operations.

`scripts/StocksDividendsJoin4` performs an *inner join* of stock and dividend records. Let's invoke for Apple data (yes, although Apple only recently announced that it would pay a dividend, Apple paid dividends back in the late 80s and early 90s.):

	./run.rb scripts/StocksDividendsJoin4.scala \
	  --stocks data/stocks/AAPL.csv \
	  --dividends data/dividends/AAPL.csv \
	  --output output/AAPL-stocks-dividends-join.txt

Note that we need to input sources, we use flags `--stocks` and `--dividends` for them.

### Further Exploration

Try these additional "mini-exercises" to learn more.

#### Left Outer Join

Change `joinWithSmaller` to `leftJoinWithSmaller` to perform a left-outer join. (Also change the output file name to something else). You have to scroll a ways into file to find dividends. See also the next mini-exercise.

#### Filtering by Year

Sometimes you want to filter records, say to limit the output. Add the following filter clause to limit the records to 1988:

	.filter('symd){ ymd: String => ymd.startsWith("1988")}

Try moving it to different positions in the pipe assembly and see if the execution times change. However, the data set is small enough that you might not notice a difference.

#### Filtering Blank Lines from WordCount2

Recall in the `WordCount2` exercise that we had thousands of blank lines that got counted. Add a `filter` before the `groupBy` that keeps only those words whose lengths are greater than zero.

# Matrix API

# Fields-Based API

# Type-Safe API

# Using Scalding with Hadoop

If you copy `data` to HDFS under your HDFS home directory:

	hadoop fs -cp data data

Then you can using the `scripts/scald.rb` script in the Scalding distribution to run any of our scripts as Hadoop jobs. For example, using the `StocksDividendsJoin4` exercise:

	cd $SCALDING_HOME
	scripts/scald.rb --host your_hadoop_host \
		../scalding-workshop/scripts/StocksDividendsJoin4.scala \
		--stocks data/stocks --dividends data/dividends \
		--output AAPL_stocks_divs

On my laptop, I use `localhost` for `your_hadoop_host`.

# Conclusions

## Comparisons with Other Tools

It's interesting to contrast Scalding with other tools.

### Cascading

Because Scala is a *functional programming* language with excellent support for DSL creation, I would argue that using Scalding is much nicer than the Java-based Cascading itself.

### Cascalog

This Clojure dialect written by Nathan Marz also benefits from the functional nature and concision of Clojure. Nathan has also built in logic-programming features from Datalog.

### Pig

Pig has very similar capabilities, with notable advantages and disadvantages.

#### Advantages

* *A custom language* - A language customized for a particular purpose can optimize expressiveness for common scenarios.
* *Lazy evaluation* - you define the workflow, then Pig compiles, optimizes, and runs it when output is required. Scalding, following Scala, uses eager evaluation.
* *Describe* - The describe feature is very helpful when learning how each Pig statement defines a new schema.

#### Disadvantages

* *Not Turing complete* - You have to write extensions in other languages. By using Scala, Scalding lets you write everything in one language.
* *Slower* - At least for local jobs, Scalding (and Cascading) avoid Hadoop APIs more effectively and therefore run noticeably faster.

### Hive

Hive is ideal when your problem fits the SQL model for queries. It's less useful for complex transformations. Also, like Pig, extensions must be written in another language.

