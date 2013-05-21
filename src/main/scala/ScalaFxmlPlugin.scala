package scalafxmlplugin

import sbt._
import Keys._
import java.io.{PrintWriter, FileInputStream, InputStreamReader, BufferedReader}
import java.util.regex.Pattern
import scala.io.Source
import com.github.nuriaion.ScalaFxml._

object ScalaFxmlPlugin extends sbt.Plugin {

  def managed[C <: Closeable, R](closeable: C)(f: C => R) =
    try { f(closeable) } finally { closeable.close }

  type Closeable = { def close(): Unit }

  import Keys._

  // configuration points, like the built in `version`, `libraryDependencies`, or `compile`
  // by implementing Plugin, these are automatically imported in a user's `build.sbt`
  val newTask = TaskKey[Unit]("new-task")
  val newSetting = SettingKey[String]("new-setting")

  // a group of settings ready to be added to a Project
  // to automatically add them, do
  val newSettings = Seq(
    newSetting := "test",
    newTask <<= newSetting map {
      str => println(str)
    }
  )

  // alternatively, by overriding `settings`, they could be automatically added to a Project
  // overridde val settings = Seq(...)

  override lazy val settings = Seq(commands += myCommand)

  lazy val myCommand =
    Command.command("hello") {
      (state: State) =>
        println("Hihuhuhu!")
        newSetting map {
          str => println(str)
        }
        state
    }

  /*resourceGenerators in Compile <+=
    (resourceManaged in Compile, name, version) map {
      (dir, n, v) =>
        val file = dir / "demo" / "myapp.properties"
        val contents = "name=%s\nversion=%s".format(n, v)
        IO.write(file, contents)
        Seq(file)
    }*/

  //sourceGenerators in Compile <+= <your Task[Seq[File]] here>


  //def makeSomeSources(base: File): Seq[File],
  /*sourceGenerators in Compile <+= sourceManaged in Compile map { outDir: File =>
    makeSomeSources(outDir / "demo")
  }*/

  sourceGenerators in Compile <+= sourceManaged in Compile map { dir =>
    val file = dir / "demo" / "Test.scala"
    IO.write(file, """object Test extends App { println("Hi") }""")
    Seq(file)
  }

  object ScalaFxmlKeys {
    lazy val scalafxml = TaskKey[Seq[File]]("scalafxml")
    //lazy val generate = TaskKey[Seq[File]]("generate")
    lazy val generate2 = TaskKey[Seq[File]]("generate2")
    lazy val fxmlSource = SettingKey[File]("scalafxml-source")
    lazy val packageDir = SettingKey[File]("scalafxml-package-dir")
    lazy val fxmlFiles = TaskKey[Seq[File]]("scalafxml-fxml-files")
    lazy val fileFilter = SettingKey[Pattern]("fxml-file-filter")
  }

  import ScalaFxmlKeys._

  object ScalaFxmlCompile {
    def apply(sources: Seq[File], outDir: File): Seq[File] = {
      println("compile")
      println("sources:" + sources)
      sources map {s:File =>
        println(s)
        val fxmlSource = scala.io.Source.fromFile(s, "utf-8").mkString
        println(fxmlSource)
        println(s.getName)
        val scalaFile: File = (outDir / (s.getName + ".scala"))
        val scalaFileWriter = new PrintWriter(scalaFile)

        import com.github.nuriaion.ScalaFxml._
        val pars = parse(fxmlSource)
        val sim = xmlToElement(pars)        
        val scalaSource = elementToString("Sculu", sim)
        println(scalaSource)

        scalaFileWriter.write(scalaSource)
        scalaFileWriter.close
        println("scalaFile: " + scalaFile)
        scalaFile
      }
    }
  }

  lazy val scalafxmlSettings: Seq[Project.Setting[_]] = inConfig(Compile)(baseScalafxmlSettings)
  lazy val baseScalafxmlSettings: Seq[Project.Setting[_]] = Seq(
    fileFilter := """.*\.fxml$""".r.pattern,
    fxmlFiles <<= (resourceDirectories in Compile, fileFilter) map { (rDirs, filter) =>
      (rDirs ** new PatternFilter(filter)).get
    },
    generate2 <<= (fxmlFiles, sourceManaged in Compile, streams) map {(files, dir, s) =>
      println("generate2")
      val bindingResults:Seq[File] = for (file <- files) yield {
        val file2:File = managed(Source fromFile file) { fxmlFile =>
          val outputFile = dir / "scala" / (/*file.getName + */"222" + ".scala")
          outputFile.delete
          outputFile.getParentFile.mkdirs
          println("Save to " + outputFile)
          s.log.debug("Save to " + outputFile)
          managed(new PrintWriter(outputFile, "utf-8")) { f =>
            val pars = parse(fxmlFile.mkString)
            val sim = xmlToElement(pars)
            f.write(elementToString("Schubiud", sim))
          }
          outputFile
        }
        file2
      }
      println("Res: " + bindingResults.toSeq)
      bindingResults.toSeq
    },
    /*sourceGenerators in Compile <<= (fxmlFiles, sourceManaged in Compile, streams) map {(files, dir, s) =>
      println("generate2")
      val bindingResults:Seq[File] = for (file <- files) yield {
        val file2:File = managed(Source fromFile file) { fxmlFile =>
          val outputFile = dir / "scala" / (/*file.getName + */"222" + ".scala")
          outputFile.delete
          outputFile.getParentFile.mkdirs
          println("Save to " + outputFile)
          s.log.debug("Save to " + outputFile)
          managed(new PrintWriter(outputFile, "utf-8")) { f =>
            val pars = parse(fxmlFile.mkString)
            val sim = xmlToElement(pars)
            f.write(elementToString("Schubiud", sim))
          }
          outputFile
        }
        file2
      }
      println("Res: " + bindingResults.toSeq)
      bindingResults.toSeq
    },*/
    sourceGenerators in Compile <+= (sourceManaged in Compile, resourceDirectories in Compile, fxmlFiles, streams) map { (dir, res, files, s) =>
      val file = dir / "demo" / "Test.scala"
      IO.write(file, """object Test extends App { println("Hi") }""")

      println("generate2")
      val bindingResults:Seq[File] = for (file <- files) yield {
        val file2:File = managed(Source fromFile file) { fxmlFile =>
          val outputFile = dir / "scala" / (/*file.getName + */"222" + ".scala")
          outputFile.delete
          outputFile.getParentFile.mkdirs
          println("Save to " + outputFile)
          s.log.debug("Save to " + outputFile)
          managed(new PrintWriter(outputFile, "utf-8")) { f =>
            val pars = parse(fxmlFile.mkString)
            val sim = xmlToElement(pars)
            f.write(elementToString("Schubiud", sim))
          }
          outputFile
        }
        file2
      }
      println("Res: " + bindingResults.toSeq)
      bindingResults.toSeq

      Seq(file)
      bindingResults.toSeq
    },
    scalafxml <<= generate2 in scalafxml,
    /*generate in scalafxml <<= (sources in scalafxml, sourceManaged in scalafxml) map { (sources, outDir) =>
      //IO.delete((outDir ** "*").get)
      IO.createDirectory(outDir)
      println("HAHAHA!!!")
      println(sources)
      println(sourceManaged)
      ScalaFxmlCompile(sources, outDir)
    },*/
    sourceManaged in scalafxml <<= sourceManaged / "sbt-scalaxb",
    sources in scalafxml <<= (fxmlSource in scalafxml) map { (fxml) =>
      (fxml ** "*.fxml").get.sorted},
    fxmlSource in scalafxml <<= (sourceDirectory) { (src) =>
      src / "resources" },
    clean in scalafxml <<= (sourceManaged in scalafxml) map { (outDir) =>
      IO.delete((outDir ** "*").get)
      IO.createDirectory(outDir)
    }//,
    /*sourceGenerators in Compile <+= sourceManaged in Compile map { dir =>
      val file = dir / "demo" / "Test.scala"
      println("HUHUSDFKJHSDLéFKJH")
      IO.write(file, """object Test extends App { println("Hi") }""")
      Seq(file)
    },
    sourceGenerators in Compile <<= (sourceManaged in scalafxml, sourceDirectory in Compile) map { (outDir, sourceDirectory) =>
      val sources = ((sourceDirectory / "resources") ** "*.fxml").get.sorted
      //IO.delete((outDir ** "*").get)
      IO.createDirectory(outDir)
      println("GENHAHAHAH!!!")
      println(sources)
      println(sourceManaged)
      ScalaFxmlCompile(sources, outDir)
    }*/
  )



}