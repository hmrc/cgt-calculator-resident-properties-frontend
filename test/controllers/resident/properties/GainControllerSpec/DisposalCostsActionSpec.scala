/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.resident.properties.GainControllerSpec

import akka.actor.ActorSystem
import akka.stream.Materializer
import assets.MessageLookup.{DisposalCosts => messages, Resident => commonMessages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import controllers.GainController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import models.resident.properties.SellOrGiveAwayModel
import models.resident.{DisposalCostsModel, SellForLessModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import common.{CommonPlaySpec,WithCommonFakeApplication}

import scala.concurrent.Future

class DisposalCostsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(
                   disposalCostsData: Option[DisposalCostsModel] = None,
                   sellOrGiveAwayData: Option[SellOrGiveAwayModel] = None,
                   sellForLessData: Option[SellForLessModel] = None
                 ): GainController = {

    when(mockSessionCacheConnector.fetchAndGetFormData[SellOrGiveAwayModel](ArgumentMatchers.eq(keystoreKeys.sellOrGiveAway))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(sellOrGiveAwayData))

    when(mockSessionCacheConnector.fetchAndGetFormData[SellForLessModel](ArgumentMatchers.eq(keystoreKeys.sellForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(sellForLessData))

    when(mockSessionCacheConnector.fetchAndGetFormData[DisposalCostsModel](ArgumentMatchers.eq(keystoreKeys.disposalCosts))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(disposalCostsData))

    when(mockSessionCacheConnector.saveFormData[DisposalCostsModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    testingGainController
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
        doc.title shouldBe s"${messages.title} - ${commonMessages.homeText} - GOV.UK"
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
      lazy val result = target.submitDisposalCosts(request.withMethod("POST"))

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
