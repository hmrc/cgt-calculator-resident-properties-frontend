/*
 * Copyright 2023 HM Revenue & Customs
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

import akka.actor.ActorSystem
import akka.stream.Materializer
import assets.MessageLookup.Resident.Properties.{ImprovementsView => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import controllers.{GainController, routes}
import models.resident.properties.gain.OwnerBeforeLegislationStartModel
import models.resident.properties.{ImprovementsModel, YourAnswersSummaryModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import common.{CommonPlaySpec,WithCommonFakeApplication}

import scala.concurrent.Future

class ImprovementsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  val summaryModel = mock[YourAnswersSummaryModel]

  def setupTarget(getData: Option[ImprovementsModel],
                  gainAnswers: YourAnswersSummaryModel,
                  totalGain: BigDecimal,
                  prrEnabled: Boolean = true,
                  ownerBeforeAprilNineteenEightyTwo: Boolean = false): GainController = {

    when(mockSessionCacheConnector.fetchAndGetFormData[ImprovementsModel](ArgumentMatchers.eq(keystoreKeys.improvements))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel]
      (ArgumentMatchers.eq(keystoreKeys.ownerBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(OwnerBeforeLegislationStartModel(ownerBeforeAprilNineteenEightyTwo))))

    when(mockSessionCacheService.getPropertyGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockCalcConnector.calculateRttPropertyGrossGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGain))

    when(mockSessionCacheConnector.saveFormData[ImprovementsModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    testingGainController
  }

  "Calling .improvements from the GainCalculationController" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None, summaryModel, BigDecimal(0))
      lazy val result = target.improvements(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the improvements view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(ImprovementsModel(1000)), summaryModel, BigDecimal(0))
      lazy val result = target.improvements(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Improvements view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "the property was owned before April 1982" should {
      lazy val target = setupTarget(Some(ImprovementsModel(1000)), summaryModel, BigDecimal(0), ownerBeforeAprilNineteenEightyTwo = true)
      lazy val result = target.improvements(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Improvements view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.titleBefore
      }
    }
  }

  "request has an invalid session" should {
    lazy val target = setupTarget(Some(ImprovementsModel(1000)), summaryModel, BigDecimal(0), ownerBeforeAprilNineteenEightyTwo = true)
    lazy val result = target.improvements(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
    }
  }

  "Calling .submitImprovements from the GainCalculationConroller" when {

    "a valid form is submitted with a zero gain result" should {
      lazy val target = setupTarget(None, summaryModel, BigDecimal(0))
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitImprovements(request.withMethod("POST"))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the review your answers page" in {
        redirectLocation(result).get shouldBe routes.ReviewAnswersController.reviewGainAnswers.url
      }
    }

    "a valid form is submitted with a negative gain result" should {
      lazy val target = setupTarget(None, summaryModel, BigDecimal(-1000))
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitImprovements(request.withMethod("POST"))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the review your answers page" in {
        redirectLocation(result).get shouldBe routes.ReviewAnswersController.reviewGainAnswers.url
      }
    }

    "a valid form is submitted with a positive gain result" should {
      lazy val target = setupTarget(None, summaryModel, BigDecimal(1000), false)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitImprovements(request.withMethod("POST"))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other-properties page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/property-lived-in")
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None, summaryModel, BigDecimal(1000))
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitImprovements(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the Improvements page" in {
        doc.title() shouldEqual s"Error: ${messages.title}"
      }
    }
  }
}
