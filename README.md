# Scalding Workshop/Tutorial README

**Dean Wampler, Typesafe**<br/>
[dean@concurrentthought.com](mailto:dean@concurrentthought.com?subject=Question%20about%20your%20Scalding%20Workshop)<br/>
[@deanwampler](https://twitter.com/deanwampler)<br/>
[Typesafe](http://typesafe.com)

![Scalding logo](images/scalding-logo-small.png)

## About this Workshop/Tutorial

This session is a half-day tutorial on Scalding and its place in the Hadoop ecosystem. [Scalding](https://github.com/twitter/scalding) is a Scala API developed at Twitter for distributed data programming that uses the [Cascading](http://www.cascading.org/) Java API, which in turn sits on top of Hadoop's Java API. However, Scalding, through Cascading, also offers a *local* mode that makes it easy to run jobs without using the Hadoop libraries, for simpler testing and learning. We'll use this feature for most of this session.

## Getting Started

We use [sbt](http://www.scala-sbt.org/), the *de facto* Scala build tool, to resolve dependencies (such as the Scalding and Cascading jars), and to compile the one Hadoop example (but not the rest of the exercises...). You will need to install Git, Java, Scala, and sbt for this workshop, as we discuss next.

**Please do the following installation steps *before* the workshop!**

It helps to pick a work directory where you will install some of the packages. In what follows, we'll assume you're using `$HOME/fun` on Linux, Mac OSX, or Cygwin for Windows with the `bash` shell (or a similar shell) or you are using `C:\fun` on Windows.

### Git

You'll need [git](http://git-scm.com) to clone the workshop repository and optionally for other installs. See [Getting Started Installing Git](http://git-scm.com/book/en/Getting-Started-Installing-Git) for details.

### This Workshop

Once git is installed, [clone this workshop from GitHub](https://github.com/deanwampler/scalding-workshop). Use your favorite Git GUI or the command line. Using `bash`:

    cd $HOME/fun
    git clone git://github.com/deanwampler/scalding-workshop.git

On Windows:

    cd C:\fun
    git clone git://github.com/deanwampler/scalding-workshop.git

### Java v1.6 or Better

If it's not already installed, install Java from [java.com](http://www.java.com/en/download/help/download_options.xml).

### Scala v2.10.2 or v2.10.3

We'll use a build of Scalding for Scala v2.10.2. Install Scala following the instructions [here](http://www.scala-lang.org/downloads).

### SBT 

See the [website for sbt](http://www.scala-sbt.org/) for installation instructions. Actually, what you install is a driver Java program. The actual version of `sbt` used will be bootstrapped for the project...

## Setting Up The Project and a Sanity Check

Once you've completed these steps, we need to "bootstrap" the project with `sbt` and then run a "sanity check" script, our exercise 0.

The first of the following three commands changes to the root directory of the workshop. (We'll spend the whole session working in this directory.) The second command runs `sbt` to create an "assembly" (an all-inclusive jar file with all the dependent jars we need included - well, most of them...). Finally, the third and last command runs the sanity check script. We'll run it using a Scala script called `run` in the root directory of the project, which we'll use for all the exercises.

Using `bash` (assuming you installed the workshop in `$HOME/fun`): 

    cd $HOME/fun/scalding-workshop
    sbt assembly
    ./run scripts/SanityCheck0.scala

On Windows (assuming you installed the workshop in `C:\fun`):

    cd C:\fun\scalding-workshop
    sbt assembly
    scala run scripts/SanityCheck0.scala

The commands should run without error. If you get an error like `sbt not found` or `scala not found`, make sure these tools are on your command "path". 

The `sbt assembly` command first runs an `update` task, which downloads all the dependencies, using the specification in `project/Build.scala`. You'll see lots of messages as it tries different repositories. Note that these dependencies will be downloaded to your `$HOME/.ivy2` directory (on *nix systems). **This may take a while to run!!**

Next, the `assembly` task builds an all-inclusive "jar" (_Java ARchive_) file that includes all the dependencies, including Scalding and Hadoop. This jar file makes it easier to run Scalding scripts on Hadoop, because it simplifies working with dependency jars and the `CLASSPATH`. The output of `assembly` is `target/ScaldingWorkshop-X.Y.Z.jar`, where `X.Y.Z` will be the current version number for the workshop.

For completeness, note also that the version of `sbt` itself is specified in `project/build.properties`. There is also a `project/plugins.sbt` file that specifies some `sbt` plugins we use. 

Finally, the `run` Scala script takes a moment to compile the Scalding script and then run it. The output is written to `output/SanityCheck0.txt`. (What's in that file?)

If you have Ruby installed on your system, there is a port of `run` in Ruby called `run.rb`. To use it, just replace the `run` command above with `run.rb`, for the *nix `bash` shell, or for Windows, use `ruby run.rb` instead of `scala run`.
 
See the Appendix below for "optional installs", if you decide to use Scalding after the tutorial you'll want to install some of these packages.

## Next Steps

You can now start with the workshop itself. Go to the companion [Workshop page](https://github.com/deanwampler/scalding-workshop/blob/master/Workshop.html).

Note that there is a similar tutorial I wrote for Typesafe's [Activator](http://typesafe.com/activator) ecosystem of examples. See the [Scalding template](http://typesafe.com/activator/template/activator-scalding).

## Notes on Releases

### V0.4.0 

Moved to Scala v2.10.3 and Scalding v0.9.0rc4. Refined some of the exercises and added one that uses Scalding's newer "type-safe" API.

### V0.3.X 

Moved to Scala v2.10.2 and Scalding v0.8.6. Completely reworked the build process and the script running process. Refined many of the exercises.

### V0.2.1 

Added a file missing from distribution. Refined the run scripts to work better with different Java versions.

### V0.2 

Refined several exercises and fixed bugs. Added `Makefile` for building releases. (Since removed...)

### V0.1 

First release for the StrangeLoop 2012 workshop.


## For Further Information

See the [Scalding GitHub page](https://github.com/twitter/scalding) for more information about Scalding. The [wiki](https://github.com/twitter/scalding/wiki) is indispensable. The Scaladocs for Scalding are [here](http://twitter.github.io/scalding/).

I'm [Dean Wampler](mailto:dean@concurrentthought.com) from [Typesafe](http://typesafe.com). I prepared this workshop. Send me email with [questions about the workshop](mailto:dean@concurrentthought.com?subject=Question%20about%20your%20Scalding%20Workshop) or for [information about consulting and training](mailto:dean.wampler@typesafe.com?subject=Hiring%20Dean%20Wampler) on Scala, Scalding, the [Typesafe Reactive Platform](http://typesafe.com/platform), and other Hadoop and *Big Data* technologies.

Some of the data used in these exercises was obtained from [InfoChimps](http://infochimps.com).

**NOTE:** The first version of this workshop was written while I worked at Think Big Analytics. The original and now obsolete fork of the workshop is [here](https://github.com/ThinkBigAnalytics/scalding-workshop).

**Dean Wampler**<br/>
[dean@concurrentthought.com](mailto:dean@concurrentthought.com?subject=Question%20about%20your%20Scalding%20Workshop)<br/>
[@deanwampler](https://twitter.com/deanwampler)<br/>

## Appendix - Optional Installs

If you're serious about using Scalding, you should clone and build the Scalding repo itself. We'll talk briefly about it in the workshop, but it isn't required.

### Scalding from GitHub

Clone [Scalding from GitHub](https://github.com/twitter/scalding). Using `bash` and assuming you'll clone it into `$HOME/fun`:

    cd $HOME/fun
    git clone https://github.com/twitter/scalding.git

Windows is similar.

### Ruby v1.8.7 or v1.9.X

Ruby is used as a platform-independent language for driver scripts by Scalding (e.g., their `scripts/scald.rb`). See [ruby-lang.org](http://ruby-lang.org) for details on installing Ruby. Either version 1.8.7 or 1.9.X will work.

### Build Scalding

Build Scalding according to its [Getting Started](https://github.com/twitter/scalding/wiki/Getting-Started) page. By default, Twitter builds with Scala v2.9.3, but Scalding builds with 2.10.2 and the `project/Build.scala` file can be edited for this version. 

Edit `project/Build.scala`. Near the top, you'll see a line `scalaVersion := 2.9.2` and next to it, a commented line for version 2.10.0. Comment out the line with 2.9.2 and uncomment the 2.10.0 line, then change the last zero to "2" or "3". Save your changes.

Now, here is a synopsis of the build steps. Using `bash`: 

    cd $HOME/fun/scalding
    sbt update
    sbt assembly

On Windows:

    cd C:\fun\scalding
    sbt update
    sbt assembly

(The Getting Started page says to build the `test` target between `update` and `assembly`, but the later builds `test` itself.)

### Sanity Check

Once you've built Scalding, run the following command as a sanity check to ensure everything is setup properly. Using `bash`: 

    cd $HOME/fun/scalding
    scripts/scald.rb --local tutorial/Tutorial0.scala

On Windows:

    cd C:\fun\scalding
    ruby scripts\scald.rb --local tutorial/Tutorial0.scala

