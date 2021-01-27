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

package config

import connectors.{CalculatorConnector, CalculatorConnectorImpl, SessionCacheConnector, SessionCacheConnectorImpl}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

class ServicesBindings extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    bindDeps() ++ bindConnectors()
  }

  private def bindDeps() = Seq(
    bind(classOf[AppConfig]).to(classOf[ApplicationConfig]),
    bind(classOf[HttpClient]).to(classOf[DefaultHttpClient])
  )

  private def bindConnectors() = Seq(
    bind(classOf[CalculatorConnector]).to(classOf[CalculatorConnectorImpl]),
    bind(classOf[SessionCacheConnector]).to(classOf[SessionCacheConnectorImpl])
  )
}
