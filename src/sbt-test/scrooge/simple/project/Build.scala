import sbt._
import Keys._
import atd.sbtthrift.ThriftPlugin._


object MyBuild extends Build
{

  override def projects = Seq(root);

  lazy val root = Project("root", file("."),
                          settings = Defaults.defaultSettings ++
                                     mySettings)

  lazy val mySettings = Seq(
     name:="scrooge-test",
     organization:="ua-gradsoft",
     version:="0.1",
     externalResolvers ++= Seq("Local Maven Repository" at "file:///home/rssh/.m2/repository/",
                               "Twitter Repository" at "http://maven.twttr.com/"
                              )
  ) ++ thriftSettings ++ Seq(
     (thriftJavaEnabled in thriftConfig) := false,
     (thriftScalaEnabled in thriftConfig) := true,
     libraryDependencies ++= Seq(
                              "org.apache.thrift" % "libthrift" % "0.8.0",
                              "com.twitter" %% "scrooge-runtime" % "3.0.0-SNAPSHOT",
                              "com.twitter" % "util-core" % "5.0.3",
                              // ostrich and finaglr needed only for compiling.
                              // TODO: fill bug report to twitter.
                              "com.twitter" %% "ostrich" % "7.0.0",
                              "com.twitter" %% "finagle-core" % "4.0.2",
                              "com.twitter" %% "finagle-thrift" % "4.0.2",
                              "com.twitter" %% "finagle-ostrich4" % "4.0.2"
                             )
  )

}

// vim: set ts=4 sw=4 et:
