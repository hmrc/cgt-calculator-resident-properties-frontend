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

import play.core.PlayVersion
import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  val bootstrapVersion         = "7.22.0"
  val playFrontendVersion      = "7.23.0-play-28"
  val playPartialsVersion      = "8.4.0-play-28"
  val httpCachingClientVersion = "10.0.0-play-28"
  val play2PdfVersion          = "1.11.0"
  val hmrcMongoVersion         = "1.3.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"           % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"   % playFrontendVersion,
    "uk.gov.hmrc"       %% "play-partials"        % playPartialsVersion,
    "it.innove"         %  "play2-pdf"            % play2PdfVersion exclude("com.typesafe.play","*")
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc.mongo"       %%  "hmrc-mongo-test-play-28" % hmrcMongoVersion    % scope,
        "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % bootstrapVersion    % scope,
        "org.pegdown"             %  "pegdown"                  % "1.6.0"             % scope,
        "org.jsoup"               %  "jsoup"                    % "1.16.1"            % scope,
        "com.typesafe.play"       %% "play-test"                % PlayVersion.current % scope,
        "com.vladsch.flexmark"    %  "flexmark-all"             % "0.35.10"           % scope,
        "org.scalatestplus"       %% "scalatestplus-mockito"    % "1.0.0-M2"          % scope,
        "org.scalatestplus"       %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"       % scope,
        "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"             % scope,
        "org.mockito"             %% "mockito-scala-scalatest"  % "1.17.14"           % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
