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

package controllers.helpers

import common.WithCommonFakeApplication
import config.AppConfig
import connectors.CalculatorConnector
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SessionCacheService
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext

trait CommonMocks {
  self: MockitoSugar & WithCommonFakeApplication =>

  val mockCalcConnector: CalculatorConnector = mock[CalculatorConnector]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockAppConfig: AppConfig = mock[AppConfig]
  val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
  val mockServiceConfig: ServicesConfig = mock[ServicesConfig]
  implicit val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]

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
    ec
  )

  val sessionCacheMocks: Seq[Any] = Seq(
    mockSessionCacheService
  )


  val controllerMocks: Seq[Any] = Seq(
    mockCalcConnector
  )


  val serviceMocks: Seq[Any] = Seq(
    mockSessionCacheService
  )

  val otherMocks: Seq[Any] = Seq(
    mockAppConfig,
    mockHttpClient,
    mockMessagesControllerComponents
  )

  val allMocks: Seq[Any] = controllerMocks ++ sessionCacheMocks ++ serviceMocks ++ otherMocks
}
