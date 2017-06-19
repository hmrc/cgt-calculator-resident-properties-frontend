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

package controllers.resident.properties.DeductionsControllerSpec

import assets.MessageLookup.{LettingsRelief => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import config.AppConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.DeductionsController
import models.resident.properties.LettingsReliefModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class LettingsReliefActionSpec extends UnitSpec with OneAppPerSuite with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[LettingsReliefModel]): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAppConfig = mock[AppConfig]

    when(mockCalcConnector.fetchAndGetFormData[LettingsReliefModel](ArgumentMatchers.eq(keystoreKeys.lettingsRelief))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[LettingsReliefModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config = mockAppConfig
    }
  }

  "Calling .lettingsRelief from the resident DeductionsController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.lettingsRelief(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }

      "have a back link to the PRR value page" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief-value"
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(LettingsReliefModel(true)))
      lazy val result = target.lettingsRelief(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }

      "have a back link to PRR value page" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief-value"
      }
    }
  }

  "Calling .submitLettingsRelief from the DeductionsController" when {

    "a valid form 'Yes' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "Yes"))
      lazy val result = target.submitLettingsRelief(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the lettings relief value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/lettings-relief-value")
      }
    }

    "a valid form 'No' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "No"))
      lazy val result = target.submitLettingsRelief(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the brought forward losses page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DeductionsController.lossesBroughtForward().url)
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", ""))
      lazy val result = target.submitLettingsRelief(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the lettings-relief page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}
