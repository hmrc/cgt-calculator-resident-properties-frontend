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

import assets.MessageLookup.{AcquisitionCosts => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import config.AppConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.GainController
import models.resident.AcquisitionCostsModel
import models.resident.properties.{BoughtForLessThanWorthModel, HowBecameOwnerModel}
import models.resident.properties.gain.OwnerBeforeLegislationStartModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AcquisitionCostsActionSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[AcquisitionCostsModel],
                  ownerBefore: Option[OwnerBeforeLegislationStartModel] = None,
                  howBecameOwner: Option[HowBecameOwnerModel] = None,
                  boughtForLess: Option[BoughtForLessThanWorthModel] = None
                 ): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionCostsModel](ArgumentMatchers.eq(keystoreKeys.acquisitionCosts))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](ArgumentMatchers.eq(keystoreKeys.ownerBeforeLegislationStart))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(ownerBefore))

    when(mockCalcConnector.fetchAndGetFormData[HowBecameOwnerModel](ArgumentMatchers.eq(keystoreKeys.howBecameOwner))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(howBecameOwner))

    when(mockCalcConnector.fetchAndGetFormData[BoughtForLessThanWorthModel](ArgumentMatchers.eq(keystoreKeys.boughtForLessThanWorth))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(boughtForLess))

    when(mockCalcConnector.saveFormData[AcquisitionCostsModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config: AppConfig = mock[AppConfig]
    }
  }

  "Calling .acquisitionCosts from the GainCalculationController" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None)
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Acquisition Costs view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(AcquisitionCostsModel(1000)))
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Acquisition Costs view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "the origin page was valueBeforeLegislationStart" should {
      lazy val target = setupTarget(None, Some(OwnerBeforeLegislationStartModel(true)))
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "have a link to valueBeforeLegislationStart" in {
        doc.select("a#back-link").attr("href") shouldBe controllers.routes.GainController.valueBeforeLegislationStart().url
      }
    }

    "the origin page was worthWhenInherited" should {
      lazy val target = setupTarget(None, Some(OwnerBeforeLegislationStartModel(false)), Some(HowBecameOwnerModel("Inherited")))
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "have a link to worthWhenInherited" in {
        doc.select("a#back-link").attr("href") shouldBe controllers.routes.GainController.worthWhenInherited().url
      }
    }

    "the origin page was worthWhenGifted" should {
      lazy val target = setupTarget(None, Some(OwnerBeforeLegislationStartModel(false)), Some(HowBecameOwnerModel("Gifted")))
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "have a link to worthWhenGifted" in {
        doc.select("a#back-link").attr("href") shouldBe controllers.routes.GainController.worthWhenGifted().url
      }
    }

    "the origin page was worthWhenBoughtForLess" should {
      lazy val target = setupTarget(None, Some(OwnerBeforeLegislationStartModel(false)), Some(HowBecameOwnerModel("Bought")), Some(BoughtForLessThanWorthModel(true)))
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "have a link to worthWhenBoughtForLess" in {
        doc.select("a#back-link").attr("href") shouldBe controllers.routes.GainController.worthWhenBoughtForLess().url
      }
    }

    "the origin page was acquisitionValue" should {
      lazy val target = setupTarget(None, Some(OwnerBeforeLegislationStartModel(false)), Some(HowBecameOwnerModel("Bought")), Some(BoughtForLessThanWorthModel(false)))
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "have a link to worthWhenBoughtForLess" in {
        doc.select("a#back-link").attr("href") shouldBe controllers.routes.GainController.acquisitionValue().url
      }
    }
  }

  "request has an invalid session" should {

    lazy val result = GainController.acquisitionCosts(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
    }
  }

  "Calling .submitAcquisitionCosts from the GainCalculationConroller" when {

    "a valid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitAcquisitionCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the improvements page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/improvements")
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitAcquisitionCosts(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the acquisition costs page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }

}
