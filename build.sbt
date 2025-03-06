/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

lazy val appName = "cgt-calculator-resident-properties-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(CodeCoverageSettings.settings *)
  .settings(majorVersion := 1)
  .settings(PlayKeys.playDefaultPort := 9702)
  .settings(
      scalacOptions += "-Wconf:src=routes/.*:s",
      scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s"
  )
  .settings(
    onLoadMessage := "",
    scalaVersion := "2.13.12",
    libraryDependencies ++= AppDependencies(),
    scalacOptions += "-feature",
    Test / testOptions -= Tests.Argument("-o", "-u", "target/test-reports", "-h", "target/test-reports/html-report"),
    // Suppress successful events in Scalatest in standard output (-o)
    // Options described here: https://www.scalatest.org/user_guide/using_scalatest_with_sbt
    Test / testOptions += Tests.Argument(
        TestFrameworks.ScalaTest,
        "-oNCHPQR",
        "-u", "target/test-reports",
        "-h", "target/test-reports/html-report")
  )
