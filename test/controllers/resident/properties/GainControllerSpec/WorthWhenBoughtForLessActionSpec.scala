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

package controllers.GainControllerSpec

import common.KeystoreKeys.{ResidentPropertyKeys => keyStoreKeys}
import config.{AppConfig, ApplicationConfig}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.GainController
import models.resident.properties.WorthWhenBoughtForLessModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.Resident.Properties.{WorthWhenBoughtForLess => messages}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import scala.concurrent.Future

class WorthWhenBoughtForLessActionSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[WorthWhenBoughtForLessModel]): GainController= {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[WorthWhenBoughtForLessModel](ArgumentMatchers.eq(keyStoreKeys.worthWhenBoughtForLess))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[WorthWhenBoughtForLessModel](ArgumentMatchers.any(), ArgumentMatchers.any())
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val config: AppConfig = ApplicationConfig
    }
  }

  "Calling .sellOrGiveAway action" when {

    "request has a valid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenBoughtForLess(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        doc.title shouldEqual messages.question
      }
    }

    "request has a valid session with existing data" should {
      lazy val target = setupTarget(Some(WorthWhenBoughtForLessModel(100)))
      lazy val result = target.worthWhenBoughtForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.question
      }
    }

    "request has an invalid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenBoughtForLess(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }
  }

  "Calling .submitSellOrGiveAway action" when {

    "a valid form with the answer '100' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitWorthWhenBoughtForLess(fakeRequestToPOSTWithSession(("amount", "100")))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the acquisition-costs page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/acquisition-costs")
      }
    }

    "an invalid form with no answer is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitWorthWhenBoughtForLess(fakeRequestToPOSTWithSession(("amount", "")))
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the page" in {
        doc.title shouldEqual messages.question
      }

      "raise an error on the page" in {
        doc.body.select("#amount-error-summary").size shouldBe 1
      }
    }
  }

}
