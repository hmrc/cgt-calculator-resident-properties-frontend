/*
 * Copyright 2016 HM Revenue & Customs
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

  val bootstrapVersion        = "1.5.0"
  val jsonJodaVersion         = "2.7.4"
  val govUKTemplateVersion    = "5.52.0-play-26"
  val playUiVersion           = "8.8.0-play-26"
  val playPartialsVersion     = "6.9.0-play-26"
  val httpCachingVersion      = "9.0.0-play-26"
  val mongoCachingVersion     = "6.8.0-play-26"
  val playLanguageVersion     = "4.2.0-play-26"
  val play2PdfVersion         = "1.9.1"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-play-26"    % bootstrapVersion,
    "com.typesafe.play" %% "play-json-joda"       % jsonJodaVersion,
    "uk.gov.hmrc"       %% "govuk-template"       % govUKTemplateVersion,
    "uk.gov.hmrc"       %% "play-ui"              % playUiVersion,
    "uk.gov.hmrc"       %% "play-partials"        % playPartialsVersion,
    "uk.gov.hmrc"       %% "http-caching-client"  % httpCachingVersion,
    "uk.gov.hmrc"       %% "mongo-caching"        % mongoCachingVersion,
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
        "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-26" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % scope,
        "org.mockito" % "mockito-core" % "3.3.1" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapVersion % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
