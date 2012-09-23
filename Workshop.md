# Scalding Workshop

**StrangeLoop 2012**<br/>
**Dean Wampler, Think Big Analytics**<br/>
[dean@deanwampler.com](mailto:dean@deanwampler.com)<br/>
[@deanwampler](https://twitter.com/deanwampler)<br/>
[Hire Us!](http://thinkbiganalytics.com)

This workshop/tutorial takes you through the basic principles of writing data analysis applications with [Scalding](https://github.com/twitter/scalding), a Scala API that wraps [Cascading](http://www.cascading.org/).

These instructions walk you through a series of exercises. Note that most of the exercises have a corresponding Scalding script (Scala source file). We use a convention of adding a number suffix to the name to indicate the order of the exercises. Note that some of these exercises are adapted from the Tutorial examples that are part of the Scalding Github repo.

This document will explain many features of the Scalding and Cascading. The scripts themselves contain additional details. The Scalding and Cascading documentation has more information than we can cover here:

* [Cascading Documentation](http://www.cascading.org/documentation/), especially the [Cascading User Guide](http://www.cascading.org/documentation/) and the [Javadocs](http://docs.cascading.org/cascading/2.0/javadoc/).
* [Scalding Wiki](https://github.com/twitter/scalding/wiki).
* Scalding Scaladocs are not online, but they can be built from the [Scalding Repo](https://github.com/twitter/scalding). For convenience, we have included these files in the workshop as `api.zip`. Unzip the file and open the [index](api/index.html).
* [Movie Recommendations](http://blog.echen.me/2012/02/09/movie-recommendations-and-more-via-mapreduce-and-scalding/) is a fantastic blog post with detailed, non-trivial examples using Scalding.
* [Scalding Example Project](https://github.com/snowplow/scalding-example-project) is a full example designed to run on Hadoop, specifically on Amazon's EMR (Elastic MapReduce) platform.

## A Disclaimer...

I'm not a Scalding or Cascading expert. Feedback welcome! [Fork me](https://github.com/ThinkBigAnalytics/scalding-workshop).

# Basic Cascading Concepts

Let's start with a very brief synopsis of key Cascading concepts useful for understanding Scalding. Not all Cascading features are wrapped with Scalding APIs. In some cases, equivalent Scala idioms are used, even though the implementations may delegate to Cascading equivalents. 

See the Cascading User Guide for more details.

## Tuple

A common data structure in many programming languages, a tuple is a grouping of a fixed number of fields, where each element has a specific type, the types of the different fields can be different and the fields can have names. It is analogous to a SQL record, a `struct` in C, and an object in object-oriented languages.

## Tap 

A **Tap** is a data *source* (for reading) or *sink* (for writing), corresponding to a file on the local file system, *Hadoop Distributed File System* (HDFS), or Amazon S3. You instantiate an `Hfs` instance for Hadoop or S3 file systems, and a `FileTap` instance for local file system. There are also more specialized versions for particular scenarios, like specifying a "template" for file or directory naming conventions.

## Scheme

The **Scheme** encapsulates the file format. There are several supported natively by Cascading. The corresponding Java class names are used in the following subsections.

### TextLine

When read, each line of text in the file is returned, with no attempt to tokenize it into fields. The position, byte offset or line number, in the file for the line is also returned. In the Hadoop model of key-value data pairs, the offset is the key and the line is the value.

When written, tuple fields are serialized to text and separated by tabs.

Available for both local file systems and Hadoop.

### TextDelimited

Handles data where each line is a tuple with fields separated by delimiters, such as tabs and commas. Quoted strings with nested delimiters and files with column headers can be handled. Fields can be cast to primitive types.

Available for both local file systems and Hadoop.

### SequenceFile

A binary, Hadoop-only data format.

### WritableSequenceFile

A more efficient implementation of SequenceFile.

## Pipe

**Pipes** are units of processing through which tuples are streamed. They are composed into **Assemblies**. Pipes are provided to merge and join streams, split them into separate streams, group them, filter them, etc.

## Flow

A **Flow** is created whenever a Tap is connected to a Pipe. Flows can also be composed.

## Cascade

A **Cascade** joins flows and supports a model where a flow is only executed if the target output doesn't exist is older than the input data, analogous to build tools like `make`.

# The Workshop

Each section introduces one or more features for data manipulation, most of which are analogous to features found in SQL, Pig (the Hadoop *data flow* tool), and other systems.

## Sanity Check

First, the [README](README.html) tells you to run a `SanityCheck0.scala` Scalding script as a sanity check to verify that your environment is ready to go.

Using `bash`: 

		cd $HOME/fun/scalding-workshop
		./run.rb scripts/SanityCheck0.scala

On Windows:

		cd C:\fun\scalding-workshop
		ruby run.rb scripts/SanityCheck0.scala

From now on, we'll assume you are working in the `scalding-workshop` directory, unless otherwise noted. Also, we'll just show the `bash` versions of the subsequent `run.rb` commands. Finally, because we're lazy, we'll sometimes drop the `.scala` extension from script names when we discuss them in the text.

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
	  .project('line)
	  .write(out)

This expression is a sequence of Cascading [Pipes](http://docs.cascading.org/cascading/2.0/javadoc/cascading/pipe/Pipe.html). However, there is not `write` method defined on the `Pipe` class. Scalding uses Scala's *implicit conversion* feature to wrap `Pipe` with a Scalding-specific `com.twitter.scalding.RichPipe` type that provides most of the methods we'll actually use.

> There are also comments in this and other scripts about specific Scalding and Cascading features that we won't cover in these notes.

Run the script thusly:

		./run.rb scripts/Project1.scala
 
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

## CoGroups

CoGroups in Scalding are used internally to implement joins of two pipe assemblies. Clients can also use them to implement joins of three or more pipe assemblies, so-called *star joins*. You should always use the largest data stream as the first one in the join, because the Cascading implementation is optimized for this scenario. 

However, in this exercise, we'll do a four-way self-join of the data files for the four stocks symbols we provided, AAPL, INTC, GE, and IBM. 

For this script, the `--input` flag is used to specify the directory where the stocks files are located.

	run.rb scripts/StockCoGroup5.scala \
	  --input  data/stocks \
	  --output output/AAPL-INTC-GE-IBM.txt

When you look at the implementation, it is not obvious how to use the CoGroup feature. You could do pair-wise joins, which would be conceptually easier perhaps, but offer poor performance in a large MapReduce job, as each pair would require a separate MapReduce Job. The CoGroup feature tries to do as many joins at one as possible.

For comparison, here is the equivalent Hive join.

	SELECT a.ymd, a.symbol, a.price_close, b.symbol, b.price_close, 
	              c.symbol, c.price_close, d.symbol, d.price_close 
FROM stocks a 
JOIN stocks b ON a.ymd = b.ymd
JOIN stocks c ON a.ymd = c.ymd
JOIN stocks d ON a.ymd = d.ymd
   a.symbol = 'AAPL' AND 
   b.symbol = 'INTC' AND 
   c.symbol = 'GE'   AND 
   d.symbol = 'IBM'

Note that because `a.ymd` appears in all `ON` clauses, Hive will perform this four-way join in a single MapReduce job.

### Further Exploration

#### Star Joins, One Pair at a Time

Try implementing the same four-way join doing a sequence of pair-wise joins. Compare the complexity of the code and the performance of the join with the CoGroup implementation.

## Splitting a Pipe

This exercise shows how to split a data stream and use various features on the splits, including finding unique values.

	run.rb scripts/Twitter6.scala \
	  --input  data/twitter/tweets.tsv \
	  --uniques output/unique-languages.txt \
	  --count_star output/count-star.txt \
	  --count_star_limit output/count-star-limit.txt

The output in `output/unique-languages.txt` is the following:

	\N
	en
	es
	id
	ja
	ko
	pt
	ru

There are seven languages and an invalid value that looks vaguely like a null! These "languages" are actually from messages in the stream that aren't tweets, but the results of other user-invoked actions.

The output in `output/count-star.txt` is a single line with the value 1000, the same as the number of lines in the data file. Similarly, `output/count-star-limit.txt` should have 100, reflecting the limit to the first 100 lines.

Note that the implementations use `groupAll`, then count the elements in the single group, via the `GroupBuilder` object. (The `count` method requires that we specify a field. We arbitrarily picked `tweet_id`.) 

By the way, this is *exactly* how Pig implements `COUNT(*)`. For example:

	grouped = group tweets all;
	count = foreach grouped generate COUNT(tweets);

Here, `tweets` would be the equivalent of a Pipe, `grouped` is the name of a new Pipe created by the grouping. It effectively has one record with all tweet records in the grouping, `foreach ... generate` iterates through this single record and projects the `COUNT` the group contents (named `tweets` after the original relation).

Finally, note that we commented out the additional example using the `limit` feature. Unfortunately, there is a bug where running in local mode causes a *divide by zero* error. As we'll demonstrate later, this bug doesn't appear when running with Hadoop.

### Further Exploration

#### Debug Setting

Add the `debug` pipe to the pipe assembly. How does it change the console output?

#### Filter for Bad Languages

Add a filter that removes these "bad" records. **Hint:** You'll want to remove all tuples where the language value is `"""\N"""`. Without the triple quotes, you would have to write `"\\N"`.

## Compute NGrams

Let's return to the Shakespeare data to compute *context ngrams*, a common natural language processing technique, where we provide a prefix of words and find occurrences of the prefix followed by an additional word. The ranked most common `n` phrases are returned. 

	run.rb scripts/ContextNGrams7.scala \
	  --input  data/shakespeare/plays.txt \
	  --output output/context-ngrams.txt \
	  --ngram-prefix "I love" \
	  --count 10

Unfortunately, the data set isn't large enough to find a lot of examples.

### Further Exploration

#### Experiment with Different Prefixes

Try other prefixes of different lengths.

#### Try Using Shakespeare's Plays

The script hard-codes the Twitter schema, so we can ignore everything except the `text`. Create a variation that just reads the whole line as text, using `TextLine`. Try it on `data/shakespeare/plays.txt`.

#### NGram Detector

Context ngrams are special case of ngrams, where you just find the most common n-length phrases. Write a script to compute the most common ngrams. 

## Joining Pipes

Let's revisit the exercise to join stock and dividend records and generalize it to read in multiple sets of data, for different companies, and process them as one stream. A complication is that the data files don't contain the stock ("instrument") symbol, so we'll see another way to add data to tuples.

	run.rb scripts/StocksDividendsRevisited8.scala \
	  --stocks-root-path    data/stocks/ \
	  --dividends-root-path data/dividends/ \
	  --symbols AAPL,INTC,GE,IBM \
	  --output output/stocks-dividends-join.txt

# Matrix API

## Jaccard Similarity and Adjacency Matrices

*Adjacency matrices* are used to record the similarities between two things. For example, the "things" might be users who have rated movies and the *adjacency* might be how many movies they have reviewed in common. Higher adjacency numbers indicate more likely similarity of interests. Note that this simple representation says nothing about whether or not they both rated the movies in a similar way.

Once you have adjacency data, you need a *similarity measure* to determine how similar to things (e.g., people) really are. One is *Jaccard Similarity*:

![](images/JaccardSimilarity.png)

This is set notation; the size of the intersection of two sets over the size of the union. It can be generalized and is similar to the cosine of two vectors normalized by length. Note that the distance would be 1 - similarity.

Run the script this way on a small matrix:

	run.rb scripts/MatrixJaccardSimilarity9.scala \
	  --input data/matrix/graph.tsv \
	  --output output/jaccardSim.tsv

## Term Frequency-Inverse Document Frequency (TF*IDF)

TF*IDF is a widely used *Natural Language Processing* tool to analyze text. It's useful for indexing documents, e.g., for web search engines. Naively, you might calculate the *frequency* of words in a corpus of documents and assume that if a word appears more frequently in one document, then that document is probably a "definitive" place for that word, such as the way you search for web pages on a particular topic. Similarly, the most frequent words indicate the primary topics for a document.

There's a problem, though. Very common words, e.g., articles like "the", "a", etc. will appear very frequently, undermining results. So we want to remove them so how. Fortunately, they tend to appear frequently in *every* document, so you can reduce the ranking of a particular word if you *divide* its frequency in a given document by its frequency in *all* documents. That's the essence of TF*IDF.

For more information, see the [Wikipedia](http://en.wikipedia.org/wiki/Tf*idf) page.

Run the script this way on a small matrix:

	run.rb scripts/TfIdf10.scala \
 	  --input data/matrix/docBOW.tsv \
	  --output output/featSelectedMatrix.tsv \
	  --nWords 300

# Type-Safe API

So far, we have been using the more mature *Fields-Based API*, which emphasizes naming fields and uses a relatively dynamic approach to typing. This is consistent with Cascading's model.

There is now a newer, more experimental *Type-Safe API* that attempts to more fully exploit the type safety provided by Scala. We won't discuss it here, but refer you to the [Type-Safe API Wiki page](https://github.com/twitter/scalding/wiki/Type-safe-api-reference).

# Using Scalding with Hadoop

If you copy `data` to HDFS under your HDFS home directory:

	hadoop fs -cp data data

Then you can using the `scripts/scald.rb` script in the Scalding distribution to run any of our scripts as Hadoop jobs. For example, using the `Twitter6` exercise:

	cd $SCALDING_HOME
	scripts/scald.rb --hdfs --host localhost \
	  ../scalding-workshop/scripts/Twitter6.scala \
	  --input            data/twitter/tweets.tsv \
	  --uniques          output/unique-languages.txt \
	  --count_star       output/count-star.txt \
	  --count_star_limit output/count-star-limit.txt

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

* *A custom language* - A purpose-built language for a particular domain can optimize expressiveness for common scenarios.
* *Lazy evaluation* - you define the work flow, then Pig compiles, optimizes, and runs it when output is required. Scalding, following Scala, uses eager evaluation.
* *Describe* - The describe feature is very helpful when learning how each Pig statement defines a new schema.

#### Disadvantages

* *Not Turing complete* - You have to write extensions in other languages. By using Scala, Scalding lets you write everything in one language.
* *Slower* - At least for local jobs, Scalding (and Cascading) avoid Hadoop APIs more effectively and therefore run noticeably faster.

### Hive

Hive is ideal when your problem fits the SQL model for queries. It's less useful for complex transformations. Also, like Pig, extensions must be written in another language.

