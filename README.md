# Scalding Workshop README

**StrangeLoop 2012**<br/>
**Dean Wampler**<br/>
[dean@deanwampler.com](mailto:dean@deanwampler.com)<br/>
[@deanwampler](https://twitter.com/deanwampler)<br/>

## About this Workshop

This workshop is a half-day tutorial on Scalding and its place in the Hadoop ecosystem. [Scalding](https://github.com/twitter/scalding) is a Scala API developed at Twitter for distributed data programming that uses the [Cascading](http://www.cascading.org/) Java API, which in turn sits on top of Hadoop's Java API. However, Scalding, through Cascading, also offers a *local* mode that makes it easy to run jobs without using the Hadoop libraries, for simpler testing and learning. We'll use that feature for most of this workshop.

## Getting Started

You need to install several software packages first. It helps to pick a work directory where you will install some of the packages. In what follows, we'll assume you're using `$HOME/fun` on Linux or Mac OSX with `bash` (or a similar shell) or `C:\fun` on Windows.

### This Workshop

Clone this [workshop from GitHub](https://github.com/thinkbiganalytics/scalding-workshop). Using `bash`:

    cd $HOME/fun
    git clone https://github.com/thinkbiganalytics/scalding-workshop

On Windows:

    cd C:\fun
    git clone https://github.com/thinkbiganalytics/scalding-workshop

### Scalding from GitHub

Clone [Scalding from GitHub](https://github.com/twitter/scalding). Using `bash`:

    cd $HOME/fun
    git clone https://github.com/twitter/scalding.git

On Windows:

    cd C:\fun
    git clone https://github.com/thinkbiganalytics/scalding-workshop

### Java v1.6 or Better

Install Java if necessary from [here](http://www.java.com/en/download/help/download_options.xml).

### Scala v2.9.2

Scalding uses Scala v2.9.2. Install it from [here](http://www.scala-lang.org/downloads).

### SBT v0.11

SBT is the *de facto* build tool for Scala. We included SBT in the workshop you cloned above. If you plan to use Scala or Scalding after this workshop, you should install SBT yourself, following these [installation instructions](https://github.com/harrah/xsbt/wiki/Getting-Started-Setup).

### Ruby v1.8.7 or v1.9.X

See [ruby-lang.org](http://ruby-lang.org) for details on installing Ruby, which is used by Scalding driver's scripts. Either version 1.8.7 or 1.9.X will work.

### Build Scalding

Build Scalding according to its [Getting Started](https://github.com/twitter/scalding/wiki/Getting-Started) page. Here is a synopsis of the steps. Note that we'll assume that you'll use the SBT command that was installed in the `$HOME/fun/scalding-workshop` directory. Using `bash`: 

    cd $HOME/fun/scalding
    ../scalding-workshop/sbt.sh update
    ../scalding-workshop/sbt.sh test
    ../scalding-workshop/sbt.sh assembly

On Windows:

    cd C:\fun\scalding
    ..\scalding-workshop\sbt.bat update
    ..\scalding-workshop\sbt.bat test
    ..\scalding-workshop\sbt.bat assembly

## Sanity Test

Once you've built Scalding, run the following command as a sanity check to ensure everything is setup properly. Using `bash`: 

    cd $HOME/fun/scalding
    scripts/scald.rb --local tutorial/Tutorial0.scala

On Windows:

    cd C:\fun\scalding
    ruby scripts\scald.rb --local tutorial/Tutorial0.scala
  
## For Further Information

See the [Scalding GitHub page](https://github.com/twitter/scalding) for more information about Scalding. The [wiki](https://github.com/twitter/scalding/wiki) is very useful.

[Dean Wampler](mailto:dean@deanwampler.com) from [Think Big Analytics](http://thinkbiganalytics.com) prepared this tutorial. [Contact Dean](mailto:dean@deanwampler.com) with questions about the tutorial. For information about consulting and training on Scalding and other Hadoop-related topics, [send us email](mailto:info@thinkbiganalytics.com).

**Dean Wampler**<br/>
[dean@deanwampler.com](mailto:dean@deanwampler.com)<br/>
[@deanwampler](https://twitter.com/deanwampler)<br/>

