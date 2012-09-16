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

> By default, when you create a new field in a **pipeline**, Cascading adds the field to the existing fields.

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


