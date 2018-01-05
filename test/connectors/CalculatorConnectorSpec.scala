/*
 * Copyright 2018 HM Revenue & Customs
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

package connectors

import java.time.LocalDate
import java.util.UUID

import common.{Dates, KeystoreKeys}
import models.resident
import models.resident.properties.{ChargeableGainAnswers, YourAnswersSummaryModel}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import services.SessionCacheService
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.http.logging.SessionId

class CalculatorConnectorSpec extends UnitSpec with MockitoSugar {

  val mockHttp: HttpGet = mock[HttpGet]
  val sessionId = UUID.randomUUID.toString

  object TargetCalculatorConnector extends CalculatorConnector {
    override val http = mockHttp
    override val serviceUrl = "dummy"
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  "Calling .getPropertyTotalCosts" should {

    implicit val hc = mock[HeaderCarrier]

    val gainAnswers = YourAnswersSummaryModel(
      disposalDate = Dates.constructDate(10, 10, 2018),
      disposalValue = Some(200000),
      worthWhenSoldForLess = None,
      whoDidYouGiveItTo = Some("Other"),
      worthWhenGaveAway = Some(10000),
      disposalCosts = 10000,
      acquisitionValue = Some(100000),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      acquisitionCosts = 10000,
      improvements = 30000,
      givenAway = true,
      sellForLess = Some(false),
      ownerBeforeLegislationStart = true,
      valueBeforeLegislationStart = Some(5000),
      howBecameOwner = Some("Bought"),
      boughtForLessThanWorth = Some(false)
    )

    when(mockHttp.GET[BigDecimal](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(BigDecimal(1000.0)))

    lazy val result = TargetCalculatorConnector.getPropertyTotalCosts(gainAnswers)

    "return 1000" in {
      await(result) shouldBe 1000
    }
  }

  "Calling .getMinimumDate" should {
    def mockDate(result: Future[DateTime]): OngoingStubbing[Future[DateTime]] =
      when(mockHttp.GET[DateTime](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(result)

    "return a DateTime which matches the returned LocalDate" in {
      mockDate(Future.successful(DateTime.parse("2015-06-04")))
      await(TargetCalculatorConnector.getMinimumDate()) shouldBe LocalDate.parse("2015-06-04")
    }

    "return a failure if one occurs" in {
      mockDate(Future.failed(new Exception("error message")))
      the[Exception] thrownBy await(TargetCalculatorConnector.getMinimumDate()) should have message "error message"
    }
  }
}
