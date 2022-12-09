/*
 * Copyright 2021 HM Revenue & Customs
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

import sbt._
import play.core.PlayVersion
import play.sbt.PlayImport._

object AppDependencies {

  val bootstrapVersion         = "5.24.0"
  val playFrontendVersion      = "0.88.0-play-28"
  val jsonJodaVersion          = "2.9.2"
  val govUKTemplateVersion     = "5.77.0-play-28"
  val playPartialsVersion      = "8.3.0-play-28"
  val httpCachingClientVersion = "9.6.0-play-28"
  val playLanguageVersion      = "5.3.0-play-28"
  val play2PdfVersion          = "1.10.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"   % playFrontendVersion,
    "com.typesafe.play" %% "play-json-joda"       % jsonJodaVersion,
    "uk.gov.hmrc"       %% "govuk-template"       % govUKTemplateVersion,
    "uk.gov.hmrc"       %% "play-partials"        % playPartialsVersion,
    "uk.gov.hmrc"       %% "http-caching-client"  % httpCachingClientVersion,
    "uk.gov.hmrc"       %% "play-language"        % playLanguageVersion,
    "it.innove"         %  "play2-pdf"            % play2PdfVersion exclude("com.typesafe.play","*")
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.vladsch.flexmark"    %  "flexmark-all"               % "0.35.10",
        "org.scalatestplus"       %% "scalatestplus-mockito"      % "1.0.0-M2",
        "org.scalatestplus"       %% "scalatestplus-scalacheck"   % "3.1.0.0-RC2",
        "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"  % "test",
        "org.mockito" %% "mockito-scala-scalatest" % "1.16.37" % "test"
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
