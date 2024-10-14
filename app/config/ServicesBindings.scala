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

package config

import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment, inject}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider

class ServicesBindings extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    bindDeps()
  }

  private def bindDeps() = Seq(
    inject.bind(classOf[AppConfig]).to(classOf[ApplicationConfig]),
    inject.bind(classOf[HttpClientV2]).toProvider(classOf[HttpClientV2Provider])
  )
}
