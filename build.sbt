name := "scalding-workshop"

version := "1.0"

scalaVersion := "2.9.2"

// Adapted from scalding's build.sbt

resolvers += "Concurrent Maven Repo" at "http://conjars.org/repo"

libraryDependencies += "cascading" % "cascading-core" % "2.0.2"

libraryDependencies += "cascading" % "cascading-local" % "2.0.2"

libraryDependencies += "cascading" % "cascading-hadoop" % "2.0.2"

libraryDependencies += "cascading.kryo" % "cascading.kryo" % "0.4.4"

libraryDependencies += "com.twitter" % "meat-locker" % "0.3.0"

libraryDependencies += "com.twitter" % "maple" % "0.2.2"

libraryDependencies += "commons-lang" % "commons-lang" % "2.4"

libraryDependencies += "org.scala-tools.testing" % "specs_2.8.1" % "1.6.6" % "test"

// Scalding itself!

libraryDependencies += "com.twitter" % "scalding_2.9.2" % "0.7.3"

