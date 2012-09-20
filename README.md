# Scalding Workshop README

**StrangeLoop 2012**<br/>
**Dean Wampler**<br/>
[dean@deanwampler.com](mailto:dean@deanwampler.com)<br/>
[@deanwampler](https://twitter.com/deanwampler)<br/>

## About this Workshop

This workshop is a half-day tutorial on Scalding and its place in the Hadoop ecosystem. [Scalding](https://github.com/twitter/scalding) is a Scala API developed at Twitter for distributed data programming that uses the [Cascading](http://www.cascading.org/) Java API, which in turn sits on top of Hadoop's Java API. However, Scalding, through Cascading, also offers a *local* mode that makes it easy to run jobs without using the Hadoop libraries, for simpler testing and learning. We'll use this feature for most of this workshop.

## Getting Started

To keep the setup process as simple as possible, the workshop git repo contains a pre-built jar that bundles Scalding v0.7.3 for Scala v2.9.2 and other required jars, such as `Cascading`, `Hadoop` *core*, `Log4J`, etc. So, all you need to install is Java, Scala, Ruby, and this workshop.

It helps to pick a work directory where you will install some of the packages. In what follows, we'll assume you're using `$HOME/fun` on Linux, Mac OSX, or Cygwin for Windows with the `bash` shell (or a similar shell) or you are using `C:\fun` on Windows.

### Git

You'll need git to clone the workshop repository and optionally for other installs. See [here](http://git-scm.com/book/en/Getting-Started-Installing-Git) for details. As an alternative, you can download a workshop release from its Github repo, rather than clone it.

### This Workshop

Clone this [workshop from GitHub](https://github.com/thinkbiganalytics/scalding-workshop). Using `bash`:

    cd $HOME/fun
    git clone https://github.com/thinkbiganalytics/scalding-workshop

On Windows:

    cd C:\fun
    git clone https://github.com/thinkbiganalytics/scalding-workshop

Or, if you prefer, simply [download a release](https://github.com/thinkbiganalytics/scalding-workshop).

### Java v1.6 or Better

Install Java if necessary from [here](http://www.java.com/en/download/help/download_options.xml).

### Scala v2.9.2

Scalding uses Scala v2.9.2. Install it from [here](http://www.scala-lang.org/downloads).

### Ruby v1.8.7 or v1.9.X

Ruby is used as a platform-independent language for driver scripts by Scalding and we've followed the same convention. See [ruby-lang.org](http://ruby-lang.org) for details on installing Ruby. Either version 1.8.7 or 1.9.X will work.

## Sanity Check

Once you've completed these steps, run the following commands as a sanity check to ensure that everything is setup properly. Using `bash`: 

    cd $HOME/fun/scalding-workshop
    ./run.rb scripts/SanityCheck0.scala

On Windows:

    cd C:\fun\scalding-workshop
    ruby run.rb scripts/SanityCheck0.scala

The commands should run without error. Note that it takes a moment to compile the Scala script and run to completion. The output is written to `output/SanityCheck0.txt`. What's in that file?
 
## Optional Installs

If you're serious about using Scalding, you should clone and build the Scalding repo. We'll talk briefly about it in the workshop, but it isn't required.

### SBT v0.11

SBT is the *de facto* build tool for Scala. You'll need it to build Scalding. Follow these [installation instructions](https://github.com/harrah/xsbt/wiki/Getting-Started-Setup).

### Scalding from GitHub

Clone [Scalding from GitHub](https://github.com/twitter/scalding). Using `bash`:

    cd $HOME/fun
    git clone https://github.com/twitter/scalding.git

On Windows:

    cd C:\fun
    git clone https://github.com/thinkbiganalytics/scalding-workshop

### Build Scalding

Build Scalding according to its [Getting Started](https://github.com/twitter/scalding/wiki/Getting-Started) page. Here is a synopsis of the steps. Using `bash`: 

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

## Next Steps

The Workshop/Tutorial proper is described in the companion [Workshop document](https://github.com/deanwampler/scalding-workshop/blob/master/Workshop.html).

## For Further Information

See the [Scalding GitHub page](https://github.com/twitter/scalding) for more information about Scalding. The [wiki](https://github.com/twitter/scalding/wiki) is very useful.

[Dean Wampler](mailto:dean@deanwampler.com) from [Think Big Analytics](http://thinkbiganalytics.com) prepared this workshop. [Contact Dean](mailto:dean@deanwampler.com) with questions about the workshop. For information about consulting and training on Scalding and other Hadoop-related topics, [send us email](mailto:info@thinkbiganalytics.com).

Some of the data used in these exercises was obtained from [InfoChimps](http://infochimps.com).

**Dean Wampler**<br/>
[dean@deanwampler.com](mailto:dean@deanwampler.com)<br/>
[@deanwampler](https://twitter.com/deanwampler)<br/>

