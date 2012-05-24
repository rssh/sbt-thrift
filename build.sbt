
sbtPlugin := true

organization := "atd"

name := "sbt-thrift"

version := "0.4"

externalResolvers ++= Seq("Local Maven Repository" at "file:///home/rssh/.m2/repository/")

libraryDependencies += "com.twitter" % "scrooge" % "3.0.0-SNAPSHOT"
  
publishTo := Some(Resolver.file("bigtoast.github.com", file(Path.userHome + "/Projects/Destroyer/bigtoast.github.com/repo")))

