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

  val bootstrapVersion         = "8.4.0"
  val playVersion               = "play-28"
  val playPartialsVersion      = s"8.4.0-$playVersion"
  val hmrcMongoVersion         = "1.3.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"           % hmrcMongoVersion,
    "uk.gov.hmrc"       %% s"bootstrap-frontend-$playVersion"   % bootstrapVersion,
    "uk.gov.hmrc"       %% s"play-frontend-hmrc-$playVersion"   % "8.5.0",
    "uk.gov.hmrc"       %% "play-partials"                      % playPartialsVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc.mongo"       %%  s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion    % scope,
        "uk.gov.hmrc"             %% s"bootstrap-test-$playVersion"   % bootstrapVersion    % scope,
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
