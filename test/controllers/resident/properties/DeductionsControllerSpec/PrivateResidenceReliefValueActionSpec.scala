/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.DeductionsControllerSpec

import assets.MessageLookup.{PrivateResidenceReliefValue => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import config.AppConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.DeductionsController
import models.resident.properties.{PrivateResidenceReliefValueModel, YourAnswersSummaryModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class PrivateResidenceReliefValueActionSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[PrivateResidenceReliefValueModel], totalGain: BigDecimal): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAppConfig = mock[AppConfig]

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefValueModel](ArgumentMatchers.eq(keystoreKeys.prrValue))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[PrivateResidenceReliefValueModel](ArgumentMatchers.any(), ArgumentMatchers.any())
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    when(mockCalcConnector.getPropertyGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[YourAnswersSummaryModel]))

    when(mockCalcConnector.calculateRttPropertyGrossGain(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(totalGain))

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config = mockAppConfig
    }
  }

  "Calling .privateResidenceReliefValue from the resident DeductionsController" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None, 10000)
      lazy val result = target.privateResidenceReliefValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the reliefs value view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(PrivateResidenceReliefValueModel(1000)), 2500)
      lazy val result = target.privateResidenceReliefValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Reliefs Value view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }
  }

  "request has an invalid session" should {

    lazy val target = setupTarget(None, 1500)
    lazy val result = target.privateResidenceReliefValue(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "redirect you to the session timeout page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
    }
  }

  "Calling .submitPrivateResidenceReliefValue from the GainCalculationController" when {

    "a valid form is submitted" should {
      lazy val target = setupTarget(None, 10000)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitPrivateResidenceReliefValue(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the lettings relief page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DeductionsController.lettingsRelief().url)
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None, 1000000)
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitPrivateResidenceReliefValue(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the Reliefs Value page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}
