import play.sbt.PlayImport.PlayKeys.{playInteractionMode, playMonitoredFiles}
import play.sbt.PlayInteractionMode
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{FileSystems, Files, Paths}
import sbt.complete.Parsers.spaceDelimited
import scala.collection.JavaConverters._
import scala.sys.process.Process
import scala.sys.process._
import sbt.Tests.{SubProcess, Group}


// ------------------------------------------------------------------------------------------------
// Functions
// ------------------------------------------------------------------------------------------------

def log(msg: String): Unit = {
  println(s"[sbt log] $msg")
}

// ------------------------------------------------------------------------------------------------
// Task Keys
// ------------------------------------------------------------------------------------------------

lazy val runBackend = taskKey[Unit]("Run Backend")
lazy val versionGenerate = taskKey[Int]("Add version_metadata.json file")
lazy val installFrontendDependencies = taskKey[Unit]("Install frontend dependencies via npm")

lazy val frontendBuild = taskKey[Unit]("Build production version of frontend code.")
val cleanFrontend = taskKey[Int]("Clean frontend")

lazy val frontendTests = taskKey[Unit]("Run UI tests when testing application.")

lazy val releaseModulesLocally = taskKey[Int]("Release modules locally")
lazy val downloadThirdPartyDeps = taskKey[Int]("Downloading thirdparty dependencies")

lazy val  devSpaceReload = taskKey[Int]("DevSpace reload")


// ------------------------------------------------------------------------------------------------
// Main build.sbt script
// ------------------------------------------------------------------------------------------------

name := """playreact"""


version := "1.0-SNAPSHOT"

val commonSettings = Seq(
  scalaVersion := "3.3.3"
)

// javacOptions ++= Seq("-source", "17", "-target", "17")
// Prevents websockets from being closed by the server
  //PlayKeys.devSettings += "play.server.websocket.periodic-keep-alive-mode" -> "pong"
ThisBuild/publish/skip := true

ThisBuild/ Test/javaOptions ++= Seq(
  "-Dconfig.resource=application.test.conf"
)
// This is for dev-mode server. In dev-mode, the play server is started before the files are compiled.
// Hence, the application files are not available in the path. For prod, It is in reference.conf file.
PlayKeys.devSettings += "play.pekko.dev-mode.pekko.coordinated-shutdown.phases.service-requests-done.timeout" -> "150s"

// add the classpath to be managed
//Compile / managedClasspath += baseDirectory.value / "target/scala-2.13/"

version := sys.process.Process("cat version.txt").lineStream_!.head

Global / onChangedBuildSource := IgnoreSourceChanges

lazy val root = (project in file(".")).enablePlugins(PlayScala)
.enablePlugins(UniversalPlugin,DockerPlugin,GraalVMNativeImagePlugin)

ThisBuild/scalaVersion := "3.3.3"  // pac4j and nimbusds libraries need to be upgraded together.
//   "org.pac4j" %% "play-pac4j" % "11.0.0-PLAY2.8",
//   "org.pac4j" % "pac4j-oauth" % "5.7.7" exclude("commons-io" , "commons-io"),
//   "org.pac4j" % "pac4j-oidc" % "5.7.7"  exclude("commons-io" , "commons-io"),

routesGenerator := InjectedRoutesGenerator
generateReverseRouter:= false

generateJsReverseRouter := false

libraryDependencies ++= Seq( guice,ws,jdbc,evolutions,logback,ehcache,filters,openId,
"com.nimbusds" % "nimbus-jose-jwt" % "9.37.2",
"com.nimbusds" % "oauth2-oidc-sdk" % "10.1",
"com.github.pureconfig" %% "pureconfig-core" % "0.17.9",
"com.github.pureconfig" %% "pureconfig-generic-scala3" % "0.17.9",
//"io.github.iltotore" %% "iron-pureconfig" % "3.0.0"
"org.playframework" %% "play-mailer" % "10.1.0",
"org.playframework" %% "play-mailer-guice" % "10.1.0"

)

libraryDependencies ++= Seq(
   "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
   "qa.hedgehog" %% "hedgehog-sbt" % "0.12.0" % Test,
   //https://hedgehog.qa/
"qa.hedgehog" %% "hedgehog-core" % "0.12.0" % Test

)



versionGenerate := {
 log("version_metadata.json Generated")
  val versionFile = baseDirectory.value / "version_metadata.json"
  //val version:Int = version.value
  val json = s"""{"version": "$version"}"""
  0
}


downloadThirdPartyDeps := {
  log("Downloading third-party dependencies...")
  val status = Process("wget -Nqi thirdparty-dependencies.txt -P /opt/third-party -c", baseDirectory.value / "support").!
  status
}

//https://www.devspace.sh/
//When you use Def.sequential to chain multiple tasks, the final result will be the type of the last task in the sequence.
devSpaceReload := Def.sequential(
  (Universal / packageBin),
  Def.task { Process("devspace run extract-archive").! }
).value


//----------------------------------------------------------------------------------
// OpenAPI tasks
//----------------------------------------------------------------------------------

lazy val openApiBundle = taskKey[Unit]("Running bundle on openapi spec")
openApiBundle := {
  val rc = Process("./openapi_bundle.sh", baseDirectory.value / "scripts").!
  if (rc != 0) {
    throw new RuntimeException("openapi bundle failed!!!")
  }
}


lazy val openApiFormat = taskKey[Unit]("Format openapi files")
openApiFormat / fileInputs += baseDirectory.value.toGlob /
    "src/main/resources/openapi" / ** / "[!_]*.yaml"
openApiFormat := {
  import java.nio.file.Path
  def formatFile(file: Path): Unit = {
    log(s"formatting api file $file")
    val rc = Process(s"./openapi_format.sh $file", baseDirectory.value / "scripts").!
    if (rc != 0) {
      throw new RuntimeException("openapi format failed!!!")
    }
  }
  val changes = openApiFormat.inputFileChanges
  val changedFiles = (changes.created ++ changes.modified).toSet
  changedFiles.foreach(formatFile)

}

lazy val openApiLint = taskKey[Unit]("Running lint on openapi spec")
openApiLint := {
  val rc = Process("./openapi_lint.sh", baseDirectory.value / "scripts").!
  if (rc != 0) {
    throw new RuntimeException("openapi lint failed!!!")
  }
}

lazy val cleanV2ServerStubs = taskKey[Int]("Clean v2 server stubs")
cleanV2ServerStubs := {
  log("Cleaning Openapi v2 server stubs...")
  Process("rm -rf openapi", target.value) !
  val openapiDir = baseDirectory.value / "src/main/resources/openapi"
  Process("rm -f ../openapi.yaml ../openapi_public.yaml", openapiDir) !
}


//-------------------------------------------------------------------------------------------------
// Run settings
//-------------------------------------------------------------------------------------------------

 // Add UI Run hook to run UI alongside with API.
 //   (Compile / run) is an input task
 //   (Compile / run).toTask("") is a task
 
runBackend := {
  val runBackendTask: Def.Initialize[Task[Unit]] = (Compile / run).toTask("")
  val curState = state.value
  val newState = Project.extract(curState).appendWithoutSession(
    Vector(PlayKeys.playRunHooks += UIRunHook(baseDirectory.value / "ui")),
    curState
  )
  Project.extract(newState).runInputTask((Compile / run),"", newState)
}



//-------------------------------------------------------------------------------------------------
// Test settings
//-------------------------------------------------------------------------------------------------

Global / concurrentRestrictions := Seq(Tags.limitAll(16))

val testParallelForks = SettingKey[Int]("testParallelForks",
  "Number of parallel forked JVMs, running tests")
testParallelForks := 4
val testShardSize = SettingKey[Int]("testShardSize",
  "Number of test classes, executed by each forked JVM")
testShardSize := 30

Global / concurrentRestrictions += Tags.limit(Tags.ForkedTestGroup, testParallelForks.value)

def partitionTests(tests: Seq[TestDefinition], shardSize: Int): Seq[Group] =
  tests.sortWith(_.name.hashCode() < _.name.hashCode()).grouped(shardSize).zipWithIndex map {
    case (tests, index) =>
      val options = ForkOptions().withRunJVMOptions(Vector(
        "-Xmx2g", "-XX:MaxMetaspaceSize=600m", "-XX:MetaspaceSize=200m",
        "-Dconfig.resource=application.test.conf"
      ))
      Group("testGroup" + index, tests, SubProcess(options))
  } toSeq


Test / parallelExecution := true
Test / fork := true
Test / testGrouping := partitionTests((Test / definedTests).value, testShardSize.value)


Test / javaOptions += "-Dconfig.resource=application.test.conf"
testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-q", "-a")
testOptions += Tests.Filter(s =>
  !s.contains("tasks.local")
)

lazy val testLocal = inputKey[Unit]("Runs local provider tests")
lazy val testFast = inputKey[Unit]("Runs quick tests")
lazy val testUpgradeRetry = inputKey[Unit]("Runs retry tests")

def localTestSuiteFilter(name: String): Boolean = (name startsWith "tasks.local")
def quickTestSuiteFilter(name: String): Boolean =
  !(name.startsWith("tasks.local") ||
    name.startsWith("tasks.upgrade"))

def upgradeRetryTestSuiteFilter(name: String): Boolean = (name startsWith "tasks.upgrade")



// Skip auto-recompile of code in dev mode if AUTO_RELOAD=false
//lazy val autoReload = getBoolEnvVar("AUTO_RELOAD")

lazy val autoReload = true
playMonitoredFiles := { if (autoReload) playMonitoredFiles.value: @sbtUnchecked else Seq() }


val swaggerGen: TaskKey[Unit] = taskKey[Unit](
  "generate swagger.json"
)

val swaggerGenTest: TaskKey[Unit] = taskKey[Unit](
  "test generate swagger.json"
)

swaggerGen := Def.taskDyn {
      // Consider generating this only in managedResources
      val swaggerJson = (root / Compile / resourceDirectory).value / "swagger.json"
      val swaggerStrictJson = (root / Compile / resourceDirectory).value / "swagger-strict.json"
      val swaggerAllJson = (root / Compile / resourceDirectory).value / "swagger-all.json"
      Def.sequential(
        (Test / runMain )
          .toTask(s"controllers.SwaggerGenTest $swaggerJson"),
        // swagger-strict.json excludes deprecated apis
        // For ex use '--exclude_deprecated 2.15.0.0' to drop APIs deprecated before a version
        // or use '--exclude_deprecated 24m' to drop APIs deprecated before 2 years
        // or use '--exclude_deprecated 2020-12-21' (YYYY-MM-DD format) to drop since date
        // or use '--exclude_deprecated all' to drop all deprecated APIs
        (Test / runMain )
          .toTask(s"controllers.SwaggerGenTest $swaggerStrictJson --exclude_deprecated all"),
        (Test / runMain )
          .toTask(s"controllers.SwaggerGenTest $swaggerAllJson --exclude_internal none")
      )
    }.value

    swaggerGenTest := {
        val _ =(root / Test / testOnly).toTask(s" controllers.ApiTest").value
        (Test / testOnly).toTask(s"controllers.SwaggerGenTest").value
    }
  


commands += Command.command("swaggerGen") { state =>
  "swagger/swaggerGen" ::
  "swagger/swaggerGenTest" ::
  "swaggerGenClients" ::
  "swaggerCompileClients" ::
  state
}

val grafanaGen: TaskKey[Unit] = taskKey[Unit](
  "generate dashboard.json"
)

grafanaGen := Def.taskDyn {
  val file = (Compile / resourceDirectory).value / "metric" / "Dashboard.json"
  Def.sequential(
    (Test / runMain)
      .toTask(s" controllers.GrafanaGenTest $file")
  )
}.value


//-----------------------------------------------------------------------------------
// UI Build Tasks
//  UI Build Tasks like clean node modules, npm install and npm run build
//-----------------------------------------------------------------------------------

// Execution status success.
val Success = 0

// Execution status failure.
val Error = 1

// Delete node_modules directory in the given path. Return 0 if success.
def cleanNodeModules(implicit dir: File): Int = Process("rm -rf node_modules", dir)!

// Execute `npm ci` command to install all node module dependencies. Return 0 if success.
def runNpmInstall(implicit dir: File): Int =
  if (cleanNodeModules != 0) throw new Exception("node_modules not cleaned up")
  //        if (!(base / "ui" / "node_modules").exists()
  else {
    println("node version: " + Process("node" :: "--version" :: Nil).lineStream_!.head)
    println("npm version: " + Process("npm" :: "--version" :: Nil).lineStream_!.head)
    println("npm config get: " + Process("npm" :: "config" :: "get" :: Nil).lineStream_!.head)
    println("npm cache verify: " + Process("npm" :: "cache" :: "verify" :: Nil).lineStream_!.head)
    Process("npm" :: "ci" :: "--legacy-peer-deps" :: Nil, dir).!
  }

// Execute `npm run build` command to build the production build of the UI code. Return 0 if success.
def runNpmBuild(implicit dir: File): Int =
  Process("npm run build-and-copy:prod", dir)!

def npmRunTest(implicit dir: File): Int =
  Process("npm run test", dir)!

clean := (clean dependsOn cleanFrontend).value

cleanFrontend := {
  log("Cleaning Frontend...")
  val status = Process("rm -rf node_modules dist", baseDirectory.value / "ui").!
  status
}

installFrontendDependencies := {
  log("Installing Frontend dependencies...")
  implicit val uiSource = baseDirectory.value / "ui"
  if (runNpmInstall != 0) throw new Exception("npm install failed")
}

frontendBuild := {
  implicit val uiSource = baseDirectory.value / "ui"
  if (runNpmBuild != 0) throw new Exception("UI Build crashed.")
}

frontendTests := {
  implicit val uiSource = baseDirectory.value / "ui"
  if (npmRunTest != 0) throw new Exception("UI Tests crashed.")
}

frontendBuild:= frontendBuild.dependsOn(installFrontendDependencies).value

//Test/test := (Test/test).dependsOn(frontendTests).value
 /// Execute frontend prod build task prior to play dist execution.
// If task A depends on task B, then task B will be executed first.

 dist := (dist dependsOn frontendBuild).value

// // Execute frontend prod build task prior to play stage execution.
 stage := (stage dependsOn frontendBuild).value

// // Execute frontend test task prior to play test execution.
//test := ((Test / test) dependsOn `ui-test`).value


//----------------------------------------------------------------------------------
// Packaging tasks
//----------------------------------------------------------------------------------
//Make SBT packaging depend on the UI build hook.

//target/universal/<project-name>.txz
Universal / packageXzTarball := (Universal / packageXzTarball).dependsOn(frontendBuild,versionGenerate).value

//target/universal/<project-name>.tgz
Universal / packageZipTarball := (Universal / packageZipTarball).dependsOn(frontendBuild,versionGenerate).value

// Being used by DevSpace tool to build an archive without building the UI
Universal / packageBin := (Universal / packageBin).dependsOn(versionGenerate).value

//Universal / packageBin
//javaAgents += "io.kamon" % "kanela-agent" % "1.0.18"


//openApiInputSpec := "openapi.yaml"
//openApiConfigFile := "config.yaml"


Docker / mappings := (Universal / mappings).value
//Universal / packageZipTarball / mappings += file("README") -> "README"


ThisBuild / semanticdbEnabled := true

ThisBuild / run / fork := true

ThisBuild / test / fork := true
