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

import config.{AppConfig, ApplicationConfig}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.GainController
import org.mockito.ArgumentMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{NoTaxToPay => messages}
import common.KeystoreKeys.ResidentPropertyKeys
import models.resident.properties.gain.WhoDidYouGiveItToModel
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._

import scala.concurrent.Future

class NoTaxToPayActionSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper with MockitoSugar {

  def setupTarget(givenTo: String): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[WhoDidYouGiveItToModel](ArgumentMatchers.eq(ResidentPropertyKeys.whoDidYouGiveItTo))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(WhoDidYouGiveItToModel(givenTo))))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val config: AppConfig = ApplicationConfig
    }
  }

  "Calling .noTaxToPay" when {

    "A valid session is provided when gifted to charity" should {
      lazy val target = setupTarget("Charity")
      lazy val result = target.noTaxToPay(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        doc.title shouldEqual messages.title
      }

      "have text explaining why tax is not owed" in {
        doc.body().select("div#content p").text() shouldBe messages.charityText
      }
    }

    "A valid session is provided when gifted to a spouse" should {
      lazy val target = setupTarget("Spouse")
      lazy val result = target.noTaxToPay(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        doc.title shouldEqual messages.title
      }

      "have text explaining why tax is not owed" in {
        doc.body().select("div#content p").text() shouldBe messages.spouseText
      }
    }

    "An invalid session is provided" should {
      lazy val target = setupTarget("Other")
      lazy val result = target.noTaxToPay(fakeRequest)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }
  }
}
