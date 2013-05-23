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
    lazy val fxmlFiles = TaskKey[Seq[File]]("scalafxml-fxml-files")
    lazy val fileFilter = SettingKey[Pattern]("fxml-file-filter")
  }

  import ScalaFxmlKeys._

  lazy val scalafxmlSettings: Seq[Project.Setting[_]] = inConfig(Compile)(baseScalafxmlSettings)
  lazy val baseScalafxmlSettings: Seq[Project.Setting[_]] = Seq(
    fileFilter := """.*\.fxml$""".r.pattern,
    fxmlFiles <<= (resourceDirectories in Compile, fileFilter) map { (rDirs, filter) =>
      (rDirs ** new PatternFilter(filter)).get
    },
    sourceManaged in scalafxml <<= sourceManaged / "sbt-scalaxb",
    sourceGenerators in Compile <+= (sourceManaged in Compile, resourceDirectories in Compile, fxmlFiles, streams) map { (dir, res, files, s) =>
      val bindingResults:Seq[File] = for (file <- files) yield {
        val file2:File = managed(Source fromFile file) { fxmlFile =>
          val outputFile = dir / "scala" / (file.getName + ".scala")
          outputFile.delete
          outputFile.getParentFile.mkdirs
          println("Save to " + outputFile)
          managed(new PrintWriter(outputFile, "utf-8")) { f =>
            val fxmlSource = fxmlFile.mkString
            val pars = parse(fxmlSource)
            val sim = xmlToElement(pars)
            f.write(generateScalaSource("FxmlFiles", file.getName.split('.').head.capitalize, parseImports(fxmlSource), sim))
          }
          outputFile
        }
        file2
      }
      bindingResults.toSeq
    },
    clean in scalafxml <<= (sourceManaged in scalafxml) map { (outDir) =>
      IO.delete((outDir ** "*").get)
      IO.createDirectory(outDir)
    }
  )



}