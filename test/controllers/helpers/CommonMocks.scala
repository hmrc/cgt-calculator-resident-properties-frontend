/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.helpers

import config.AppConfig
import connectors.{CalculatorConnector, SessionCacheConnector}
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.WithFakeApplication

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait CommonMocks {
  self: MockitoSugar with WithFakeApplication =>

  val mockCalcConnector: CalculatorConnector = mock[CalculatorConnector]
  val mockSessionCacheConnector: SessionCacheConnector = mock[SessionCacheConnector]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockAppConfig: AppConfig = mock[AppConfig]
  val mockHttpClient: HttpClient = mock[HttpClient]

  lazy val stubComps: ControllerComponents = stubControllerComponents()

  private val messagesActionBuilder: MessagesActionBuilder = new DefaultMessagesActionBuilderImpl(stubBodyParser[AnyContent](), stubMessagesApi())
  private val cc = stubControllerComponents()

  val mockMessagesControllerComponents: MessagesControllerComponents = DefaultMessagesControllerComponents(
    messagesActionBuilder,
    DefaultActionBuilder(stubBodyParser[AnyContent]()),
    cc.parsers,
    fakeApplication.injector.instanceOf[MessagesApi],
    cc.langs,
    cc.fileMimeTypes,
    ExecutionContext.global
  )

  val controllerMocks: Seq[_] = Seq(
    mockCalcConnector
  )

  val connectorMocks: Seq[_] = Seq(
    mockSessionCacheConnector
  )

  val serviceMocks: Seq[_] = Seq(
    mockSessionCacheService
  )

  val otherMocks: Seq[_] = Seq(
    mockAppConfig,
    mockHttpClient,
    mockMessagesControllerComponents
  )

  val allMocks: Seq[_] = controllerMocks ++ connectorMocks ++ serviceMocks ++ otherMocks
}
