// This SBT build file is only used to resolve the dependencies we need.

import sbt._
import sbt.Keys._

object BuildSettings {

  val Organization  = "com.concurrentthought"
  val Version       = "0.3.0"
  val Description   = "Scalding Workshop"
  val ScalaVersion  = "2.10.2"
  val ScalacOptions = Seq("-deprecation", "-unchecked", "-encoding", "utf8")

  val basicSettings = Defaults.defaultSettings ++ Seq (
    organization  := Organization,
    version       := Version,
    description   := Description,
    scalaVersion  := ScalaVersion,
    scalacOptions := ScalacOptions,
    shellPrompt   := ShellPrompt.Prompt
  )

  // sbt-assembly settings for building a fat jar that includes all dependencies.
  // This is useful for running Hadoop jobs, but not needed for local script testing.
  // Adapted from https://github.com/snowplow/scalding-example-project
  import sbtassembly.Plugin._
  import AssemblyKeys._
  lazy val sbtAssemblySettings = assemblySettings ++ Seq(

    // Slightly cleaner jar name
    jarName in assembly <<= (name, version) { (name, version) => name + "-" + version + ".jar" },
    
    // Drop these jars, most of which are dependencies of dependencies...
    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      val excludes = Set(
        "scala-compiler.jar",
        "jsp-api-2.1-6.1.14.jar",
        "jsp-2.1-6.1.14.jar",
        "jasper-compiler-5.5.12.jar",
        "minlog-1.2.jar", // Otherwise causes conflicts with Kyro (which bundles it)
        "janino-2.5.16.jar", // Janino includes a broken signature, and is not needed anyway
        "commons-beanutils-core-1.8.0.jar", // Clash with each other and with commons-collections
        "commons-beanutils-1.7.0.jar"
      ) 
      cp filter { jar => excludes(jar.data.getName) }
    },
    
    mergeStrategy in assembly <<= (mergeStrategy in assembly) {
      (old) => {
        case "project.clj" => MergeStrategy.discard // Leiningen build files
        case x => old(x)
      }
    }
  )

  lazy val buildSettings = basicSettings ++ sbtAssemblySettings

  // unmanagedBase <<= baseDirectory { base => base / "lib" }
}

// Shell prompt which show the current project,
// git branch and build version
object ShellPrompt {
  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }
  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
  )

  val Prompt = {
    (state: State) => {
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (
        currProject, currBranch, BuildSettings.Version
      )
    }
  }
}

object Resolvers {
  val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  val sonatype =  "Sonatype Release" at "https://oss.sonatype.org/content/repositories/releases"
  val mvnrepository = "MVN Repo" at "http://mvnrepository.com/artifact"
  val conjars  = "Concurrent Maven Repo" at "http://conjars.org/repo"
  val clojars  = "Clojars Repo" at "http://clojars.org/repo"
  val twitterMaven = "Twitter Maven" at "http://maven.twttr.com"
  
  val allResolvers = Seq(typesafe, sonatype, mvnrepository, conjars, clojars, twitterMaven)

}

object Dependency {
  object Version {
    val Scalding   = "0.8.11"
    val Algebird   = "0.2.0"
    val Bijection  = "0.5.2"
    val Hadoop     = "1.1.2"
    val ScalaTest  = "1.9.1"
    val ScalaCheck = "1.10.1"
  }

  // ---- Application dependencies ----

  // Include the Scala compiler itself for reification and evaluation of expressions. 
  val scalaCompiler = "org.scala-lang"  %  "scala-compiler" % BuildSettings.ScalaVersion
  
  val scalding_args  = "com.twitter"     %% "scalding-args"  % Version.Scalding
  val scalding_core  = "com.twitter"     %% "scalding-core"  % Version.Scalding
  val scalding_date  = "com.twitter"     %% "scalding-date"  % Version.Scalding

  val algebird_core  = "com.twitter"     %% "algebird-core"  % Version.Algebird
  val algebird_util  = "com.twitter"     %% "algebird-util"  % Version.Algebird
  val bijection_core = "com.twitter"     %% "bijection-core" % Version.Bijection

  val hadoop_core    = "org.apache.hadoop"  % "hadoop-core"  % Version.Hadoop
  
  // ---- Test dependencies ----

  val scalaTest   = "org.scalatest"    %%  "scalatest"   %  Version.ScalaTest  %  "test"
  val scalaCheck  = "org.scalacheck"   %%  "scalacheck"  %  Version.ScalaCheck %  "test"
}

object Dependencies {
  import Dependency._

  val scaldingWorkshop = Seq(
    scalaCompiler, scalding_args, scalding_core, scalding_date, 
    algebird_core, algebird_util, bijection_core) //, hadoop_core)
    // scalaTest, scalaCheck)
}

object ScaldingWorkshopBuild extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  lazy val scaldingWorkshop = Project(
    id = "ScaldingWorkshop",
    base = file("."),
    settings = buildSettings ++ Seq(
      resolvers := allResolvers,
      libraryDependencies ++= Dependencies.scaldingWorkshop))
}

