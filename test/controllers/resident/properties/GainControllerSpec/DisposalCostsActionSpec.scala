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

import assets.MessageLookup.{DisposalCosts => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import config.AppConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.GainController
import models.resident.DisposalCostsModel
import models.resident.properties.SellOrGiveAwayModel
import models.resident.SellForLessModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class DisposalCostsActionSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper with MockitoSugar {

  def setupTarget(
                   disposalCostsData: Option[DisposalCostsModel] = None,
                   sellOrGiveAwayData: Option[SellOrGiveAwayModel] = None,
                   sellForLessData: Option[SellForLessModel] = None
                 ): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[SellOrGiveAwayModel](ArgumentMatchers.eq(keystoreKeys.sellOrGiveAway))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(sellOrGiveAwayData))

    when(mockCalcConnector.fetchAndGetFormData[SellForLessModel](ArgumentMatchers.eq(keystoreKeys.sellForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(sellForLessData))

    when(mockCalcConnector.fetchAndGetFormData[DisposalCostsModel](ArgumentMatchers.eq(keystoreKeys.disposalCosts))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(disposalCostsData))

    when(mockCalcConnector.saveFormData[DisposalCostsModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config: AppConfig = mock[AppConfig]
    }
  }

  "Calling .disposalCosts from the GainCalculationController with session" when {

    "supplied with no pre-existing stored data and given away" should {

      lazy val target = setupTarget(None, Some(SellOrGiveAwayModel(true)))
      lazy val result = target.disposalCosts(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Disposal Costs view" in {
        doc.title shouldBe messages.title
      }

      s"have a back link to '${controllers.routes.GainController.disposalValue().url}'" in {
        doc.getElementById("back-link").attr("href") shouldBe controllers.routes.GainController.worthWhenGaveAway().url
      }
    }

    "supplied with pre-existing stored data and sold less for market value" should {

      lazy val target = setupTarget(
        Some(DisposalCostsModel(100.99)),
        Some(SellOrGiveAwayModel(false)),
        Some(SellForLessModel(true))
      )
      lazy val result = target.disposalCosts(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "have the amount 100.99 pre-populated into the input field" in {
        doc.getElementById("amount").attr("value") shouldBe "100.99"
      }

      s"have a back link to '${controllers.routes.GainController.worthWhenSoldForLess().url}'" in {
        doc.getElementById("back-link").attr("href") shouldBe controllers.routes.GainController.worthWhenSoldForLess().url
      }
    }

    "supplied with pre-existing stored data and sold for market value or more" should {

      lazy val target = setupTarget(
        Some(DisposalCostsModel(100.99)),
        Some(SellOrGiveAwayModel(false)),
        Some(SellForLessModel(false))
      )
      lazy val result = target.disposalCosts(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "have the amount 100.99 pre-populated into the input field" in {
        doc.getElementById("amount").attr("value") shouldBe "100.99"
      }

      s"have a back link to '${controllers.routes.GainController.disposalValue().url}'" in {
        doc.getElementById("back-link").attr("href") shouldBe controllers.routes.GainController.disposalValue().url
      }
    }
  }

  "Calling .disposalCosts from the GainCalculationController with no session" should {

    lazy val target = setupTarget()
    lazy val result = target.disposalCosts(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
    }
  }

  "calling .submitDisposalCosts from the GainCalculationController" when {

    "given a valid form should" should {

      lazy val target = setupTarget(
        Some(DisposalCostsModel(100.99)),
        Some(SellOrGiveAwayModel(false)),
        Some(SellForLessModel(true))
      )
      lazy val request = fakeRequestToPOSTWithSession(("amount", "100.99"))
      lazy val result = target.submitDisposalCosts(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      s"redirect to '${controllers.routes.GainController.ownerBeforeLegislationStart().url}'" in {
        redirectLocation(result).get shouldBe controllers.routes.GainController.ownerBeforeLegislationStart().url
      }
    }

    "given an invalid form" should {

      lazy val target = setupTarget(
        Some(DisposalCostsModel(100.99)),
        Some(SellOrGiveAwayModel(true))
      )
      lazy val request = fakeRequestToPOSTWithSession(("amount", "-100"))
      lazy val result = target.submitDisposalCosts(request)

      "return a status of 400" in {
        status(result) shouldBe 400
      }
    }
  }
}
