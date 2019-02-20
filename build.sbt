//
// Algorithmia algorithm build file
//

name := "TopX"

organization := "ons"

// Allow version to be overwritten with "-DalgoVersion=XXX"
version := System.getProperty("algo.version", "1.0-SNAPSHOT")

scalaVersion := "2.11.8"

mainClass in Compile := Some("uk.gov.ons.algorithmia.Main")

val repoUrl = System.getProperty("repo.url", "http://git.algorithmia.com")

resolvers += "Maven Central" at "http://repo1.maven.org/maven2/org/"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.algorithmia" % "algorithmia-client" % "1.0.+",
  "com.algorithmia" % "algorithmia-extras" % "1.0.+",
  "com.google.code.gson" % "gson" % "2.5"
)

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.1" % "test"

resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"

// Scala 2.11
libraryDependencies += "MrPowers" % "spark-fast-tests" % "0.17.1-s_2.11"

retrieveManaged := true

// Don't convert name to lowercase
normalizedName := name.value
