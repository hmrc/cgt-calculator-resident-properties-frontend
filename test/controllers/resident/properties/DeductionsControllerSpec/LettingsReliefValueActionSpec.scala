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

package controllers.DeductionsControllerSpec

import assets.MessageLookup.{LettingsReliefValue => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.DeductionsController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.DeductionsControllerSpec.DeductionsControllerBaseSpec
import models.resident.properties.{LettingsReliefValueModel, PrivateResidenceReliefValueModel, YourAnswersSummaryModel}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._

import scala.concurrent.Future

class LettingsReliefValueActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with DeductionsControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(
                   getData: Option[LettingsReliefValueModel],
                   prrValue: Option[PrivateResidenceReliefValueModel],
                   totalGain: BigDecimal
                 ): DeductionsController = {

    when(mockSessionCacheService.fetchAndGetFormData[LettingsReliefValueModel](ArgumentMatchers.eq(keystoreKeys.lettingsReliefValue))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.fetchAndGetFormData[PrivateResidenceReliefValueModel](ArgumentMatchers.eq(keystoreKeys.prrValue))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(prrValue))

    when(mockSessionCacheService.saveFormData[LettingsReliefValueModel](ArgumentMatchers.any(), ArgumentMatchers.any())
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    when(mockSessionCacheService.getPropertyGainAnswers(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[YourAnswersSummaryModel]))

    when(mockCalcConnector.calculateRttPropertyGrossGain(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGain))

    testingDeductionsController
  }

  "Calling .lettingsReliefValue from the resident DeductionsController" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None, Some(PrivateResidenceReliefValueModel(1000)), 10000)
      lazy val result = target.lettingsReliefValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the lettings relief value view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(LettingsReliefValueModel(1000)), Some(PrivateResidenceReliefValueModel(1000)), 2500)
      lazy val result = target.lettingsReliefValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Lettings Relief Value view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }
  }

  "request has an invalid session" should {

    lazy val target = setupTarget(None, Some(PrivateResidenceReliefValueModel(1000)), 1500)
    lazy val result = target.lettingsReliefValue(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
    }
  }

  "Calling .submitLettingsReliefValue from the GainCalculationController" when {

    "a valid form is submitted" should {
      lazy val target = setupTarget(None, Some(PrivateResidenceReliefValueModel(10000)), 100000)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitLettingsReliefValue(request.withMethod("POST"))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the brought forward losses page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DeductionsController.lossesBroughtForward.url)
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None, Some(PrivateResidenceReliefValueModel(200)), 1000000)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "10000"))
      lazy val result = target.submitLettingsReliefValue(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the Lettings Relief Value page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}
