
package atd.sbtthrift

import sbt._
import classpath._
import Process._
import Keys._

object ThriftPlugin extends Plugin {

  val thriftConfig = config("thrift")

  val thrift = SettingKey[String]("thrift", "thrift executable")
  val thriftSourceDir = SettingKey[File]("source-directory", "Source directory for thrift files. Defaults to src/main/thrift")
  val thriftGenerate = TaskKey[Seq[File]]("generate-java", "Generate java sources from thrift files")
  val thriftOutputDir = SettingKey[File]("output-directory", "Directory where the java files should be placed. Defaults to sourceManaged")
  val thriftJavaOptions = SettingKey[Seq[String]]("thrift-java-options", "additional options for java thrift generation")
  val thriftJavaEnabled = SettingKey[Boolean]("java-enabled", "java generation is enabled. Default - yes")
  val thriftGenerateJs = TaskKey[Seq[File]]("generate-js","Generate javascript sources from thrift files")
  val thriftJsOutputDir = SettingKey[File]("js-output-directory","Direcotry where generated javsacript files should be placed. default target/thrift-js")
  val thriftJsOptions = SettingKey[Seq[String]]("thrift-js-options", "additional options for js thrift generation")
  val thriftJsEnabled = SettingKey[Boolean]("js-enabled", "javascript generation is enabled. Default - no")


  val thriftGenerateScala = TaskKey[Seq[File]]("generate-scala", "Generate scala sources from thrift files")
  val thriftScalaOptions = SettingKey[Seq[String]]("thrift-scala-options", "additional options for scala thrift generation")
  val thriftScalaOutputDir = SettingKey[File]("scala-output-directory","Directory where generated scala files should be placed. default target/thrift-scala")
  val thriftScalaEnabled = SettingKey[Boolean]("scala-enabled", "scala generation is enabled. Default - no")

  lazy val thriftSettings :Seq[Setting[_]] = inConfig(thriftConfig)(Seq[Setting[_]](

  thrift := "thrift",

  thriftSourceDir <<= (sourceDirectory in Compile){ _ / "thrift"},

  thriftOutputDir <<= (sourceManaged in Compile).identity,

  thriftJavaEnabled := true,

  thriftJavaOptions := Seq[String](),

  thriftJsOutputDir := new File("target/thrift-js"),

  thriftGenerate <<= (streams, thriftSourceDir, thriftOutputDir, 
                      thrift, thriftJavaOptions, thriftJavaEnabled) map { 
       (out, sdir, odir, tbin, opts, enabled ) =>
        if (enabled) {
          compileThrift(sdir,odir,tbin,"java",opts,out.log);
        }else{
          Seq[File]()
        }
  },

  thriftJsEnabled := false,

  thriftJsOptions := Seq[String](),

  thriftGenerateJs <<= (streams, thriftSourceDir, thriftJsOutputDir, 
                        thrift, thriftJsOptions, thriftJsEnabled) map { 
        ( out, sdir, odir, tbin, opts, enabled ) =>
        if (enabled) {
          compileThrift(sdir,odir,tbin,"js",opts,out.log);
        } else {
          Seq[File]()
        }
    },

  thriftScalaOutputDir := new File("target/thrift-scala"),

  thriftScalaOptions := Seq(),

  thriftGenerateScala <<= (streams, thriftSourceDir, thriftScalaOutputDir, 
                                    thriftScalaOptions, thriftScalaEnabled) map { 
        ( out, sdir, odir, opts, enabled ) =>
        if (enabled) {
          //System.err.println("will generate scala (todo) ");
          val fullOpts = Seq[String]("-l","scala","-i",sdir.toString,
                                              "-d",odir.toString
                                ) ++ opts ++
                                (sdir ** "*.thrift").get.toSeq map (_.toString)
          // TODO: eliminate dependency ?
          //scroogeClass.invoke(null,run,opts.toArray);
          com.twitter.scrooge.Main.main(fullOpts.toArray);
          (odir ** "*.scala").get.toSeq
          //Seq[File]()
        } else {
          Seq[File]()
        }
   },


  managedClasspath <<= (classpathTypes, update) map { (cpt, up) =>
    Classpaths.managedJars(thriftConfig, cpt, up)
  },

  (libraryDependencies in Compile)  <++= (thriftScalaEnabled,
                                                 thriftJavaEnabled) { 
           (scalaEnabled, javaEnabled) =>
                  (if (scalaEnabled) {
                       System.err.println("libaryDependencies in compile\n");
                       Seq("com.twitter" %% "scrooge" % "3.0.0-SNAPSHOT" % "provided",
                           "com.twitter" %% "scrooge-runtime" % "3.0.0-SNAPSHOT"
                          )
                     } else
                         Seq()
                    ) ++ (if (scalaEnabled || javaEnabled) {
                          Some("org.apache.thrift" % "libthrift" % "0.8.0")
                         } else {
                          None
                         })
  },

  //(managedJars in Compile) <++ = (

  (managedResourceDirectories in Compile) <++= (thriftJsOutputDir, thriftJsEnabled) {
      (out, enabled) => if (enabled) {
                              Seq(out)
                        } else {
                              Seq()
                        }
  }

  )) ++ Seq(
    (sourceGenerators in Compile) <+= thriftGenerate in thriftConfig,
    sourceGenerators in Compile <+= thriftGenerateScala in thriftConfig,
    resourceGenerators in Compile <+= thriftGenerateJs in thriftConfig,
    ivyConfigurations += thriftConfig
  )

  def compileThrift(sourceDir: File, 
                    outputDir: File, 
                    thriftBin: String, 
                    language: String, 
                    options: Seq[String],
                    logger: Logger):Seq[File] =
  {
    val schemas = (sourceDir ** "*.thrift").get
    outputDir.mkdirs()
    logger.info("Compiling %d thrift files to %s in %s".format(schemas.size, language, outputDir))
    schemas.foreach { schema =>
      val cmd = "%s -gen %s -o %s %s".format(thriftBin, 
                                        language + options.mkString(":",",",""),
                                        outputDir, schema)
      logger.info("Compiling schema with command: %s" format cmd)
      <x>{cmd}</x> !
    }
    (outputDir ** "*.%s".format(language)).get.toSeq
  }


 }
