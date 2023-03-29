
import com.typesafe.sbt.digest.Import.digest
import com.typesafe.sbt.web.Import.pipelineStages
import com.typesafe.sbt.web.Import.Assets
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}

lazy val appName = "cgt-calculator-resident-properties-frontend"
lazy val appDependencies : Seq[ModuleID] = ???
lazy val plugins : Seq[Plugins] = Seq(play.sbt.PlayScala)
lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;models\\.data\\..*;views.html.helpers.*;uk.gov.hmrc.BuildInfo;app.*;nr.*;res.*;prod.*;config.*;controllers.SessionCacheController",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin) ++ plugins : _*)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(scoverageSettings : _*)
  .settings(majorVersion := 1)
  .settings(playSettings : _*)
  .settings(PlayKeys.playDefaultPort := 9702)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scalaVersion := "2.13.8",
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    Global / lintUnusedKeysOnLoad := false,
    pipelineStages in Assets := Seq(digest),
    scalacOptions += "-P:silencer:pathFilters=routes;views;play.mvc.Http.Contexts",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.12" cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % "1.7.12" % Provided cross CrossVersion.full
    ),
    scalacOptions += "-feature",
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(integrationTestSettings())
  .settings(TwirlKeys.templateImports ++= Seq(
    "uk.gov.hmrc.govukfrontend.views.html.components._",
    "uk.gov.hmrc.hmrcfrontend.views.html.components._",
    "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
    "uk.gov.hmrc.govukfrontend.views.html.components.implicits._"
  ))
  .settings(
    isPublicArtefact := true
  )

fork in run := true