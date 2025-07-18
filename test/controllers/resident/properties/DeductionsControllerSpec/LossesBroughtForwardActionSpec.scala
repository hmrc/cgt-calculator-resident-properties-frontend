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

import assets.MessageLookup.{LossesBroughtForward => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.DeductionsControllerSpec.DeductionsControllerBaseSpec
import controllers.{DeductionsController, routes}
import models.resident._
import models.resident.properties.{ChargeableGainAnswers, LettingsReliefModel, PropertyLivedInModel, YourAnswersSummaryModel}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._

import scala.concurrent.Future

class LossesBroughtForwardActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper
  with CommonMocks with MockitoSugar with DeductionsControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  val gainModel = mock[YourAnswersSummaryModel]
  val summaryModel = mock[ChargeableGainAnswers]
  val chargeableGainModel = mock[ChargeableGainResultModel]

  def setupTarget(lossesBroughtForwardData: Option[LossesBroughtForwardModel],
                  gainAnswers: YourAnswersSummaryModel,
                  chargeableGainAnswers: ChargeableGainAnswers,
                  chargeableGain: ChargeableGainResultModel,
                  disposalDate: Option[DisposalDateModel],
                  taxYear: Option[TaxYearModel],
                  propertyLivedInModel: Option[PropertyLivedInModel] = Some(PropertyLivedInModel(false)),
                  privateResidenceReliefModel: Option[PrivateResidenceReliefModel] = None,
                  lettingsReliefModel: Option[LettingsReliefModel] = None
                 ): DeductionsController = {
    when(mockSessionCacheService.fetchAndGetFormData[LossesBroughtForwardModel](ArgumentMatchers.eq(keystoreKeys.lossesBroughtForward))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(lossesBroughtForwardData))

    when(mockSessionCacheService.getPropertyGainAnswers(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockSessionCacheService.getPropertyDeductionAnswers(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(chargeableGainAnswers))

    when(mockCalcConnector.calculateRttPropertyChargeableGain(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(chargeableGain)))

    when(mockSessionCacheService.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.eq(keystoreKeys.disposalDate))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(disposalDate))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYear))

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(6000))))

    when(mockSessionCacheService.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(keystoreKeys.propertyLivedIn))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(propertyLivedInModel))

    when(mockSessionCacheService.fetchAndGetFormData[PrivateResidenceReliefModel](ArgumentMatchers.eq(keystoreKeys.privateResidenceRelief))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(privateResidenceReliefModel))

    when(mockSessionCacheService.fetchAndGetFormData[LettingsReliefModel](ArgumentMatchers.eq(keystoreKeys.lettingsRelief))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(lettingsReliefModel))

    when(mockSessionCacheService.saveFormData[LossesBroughtForwardValueModel]
      (ArgumentMatchers.eq(keystoreKeys.lossesBroughtForward),ArgumentMatchers.any())
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    testingDeductionsController
  }

  "Calling .lossesBroughtForward from the resident DeductionsController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None, gainModel,
        summaryModel, chargeableGainModel, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with " in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title("2015 to 2016")}" in {
        doc.title shouldEqual messages.title("2015 to 2016")
      }
    }

    "request has no session" should {

      lazy val target = setupTarget(None, gainModel, summaryModel, chargeableGainModel, None, None)
      lazy val result = target.lossesBroughtForward(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }
    }

    "the property was not lived in" should {
      lazy val target = setupTarget(None, gainModel,
        summaryModel, chargeableGainModel, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have a back link with the address ${controllers.routes.DeductionsController.propertyLivedIn.url}" in {
        doc.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "the property was lived in but reliefs were not claimed" should {
      lazy val target = setupTarget(None, gainModel,
        summaryModel, chargeableGainModel, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")),
        Some(PropertyLivedInModel(true)), Some(PrivateResidenceReliefModel(false)))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have a back link with the address ${controllers.routes.DeductionsController.privateResidenceRelief.url}" in {
        doc.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "the property was lived in with PRR but lettings reliefs was not claimed" should {
      lazy val target = setupTarget(None, gainModel,
        summaryModel, chargeableGainModel, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")),
        Some(PropertyLivedInModel(true)), Some(PrivateResidenceReliefModel(true)), Some(LettingsReliefModel(false)))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have a back link with the address ${controllers.routes.DeductionsController.lettingsRelief.url}" in {
        doc.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "the property was lived in with PRR and lettings reliefs" should {
      lazy val target = setupTarget(None, gainModel,
        summaryModel, chargeableGainModel, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")),
        Some(PropertyLivedInModel(true)), Some(PrivateResidenceReliefModel(true)), Some(LettingsReliefModel(true)))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have a back link with the address ${controllers.routes.DeductionsController.lettingsReliefValue.url}" in {
        doc.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }
  }

  "Calling .submitLossesBroughtForward from the DeductionsController" when {

    "a valid form 'No' and chargeable gain is £1000" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), gainModel,
        summaryModel, ChargeableGainResultModel(1000, 1000, 0, 0, 0, BigDecimal(0), BigDecimal(0), Some(BigDecimal(0)),
          Some(BigDecimal(0)), 0, 0), Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request.withMethod("POST"))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the current income page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/current-income")
      }
    }

    "a valid form 'No' and chargeable gain is zero" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), gainModel,
        summaryModel, ChargeableGainResultModel(1000, 0, 0, 0, 1000, BigDecimal(0), BigDecimal(0), Some(BigDecimal(0)),
          Some(BigDecimal(0)), 0, 0), Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request.withMethod("POST"))

      "return a 303" in {

        status(result) shouldBe 303
      }

      "redirect to the review your answers page" in {
        redirectLocation(result).get shouldBe routes.ReviewAnswersController.reviewDeductionsAnswers.url
      }
    }

    "a valid form 'No' and has a positive chargeable gain of £1,000" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), gainModel,
        summaryModel, ChargeableGainResultModel(1000, -1000, 0, 0, 2000, BigDecimal(0), BigDecimal(0), Some(BigDecimal(0)),
          Some(BigDecimal(0)), 0, 0), Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request.withMethod("POST"))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the review answers page" in {
        redirectLocation(result).get shouldBe routes.ReviewAnswersController.reviewDeductionsAnswers.url
      }
    }

    "a valid form 'Yes'" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), gainModel, summaryModel,
        ChargeableGainResultModel(0, 0, 0, 0, 0, BigDecimal(0), BigDecimal(0), Some(BigDecimal(0)),
          Some(BigDecimal(0)), 0, 0), Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "Yes"))
      lazy val result = target.submitLossesBroughtForward(request.withMethod("POST"))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the losses brought forward value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/losses-brought-forward-value")
      }
    }

    "an invalid form is submitted" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), gainModel,
        summaryModel, chargeableGainModel, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015 to 2016", true, "2015 to 2016")))
      lazy val request = fakeRequestToPOSTWithSession(("option", ""))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the brought forward losses page" in {
        Jsoup.parse(bodyOf(result)).title() shouldEqual s"Error: ${messages.title("2015 to 2016")}"      }
    }
  }
}
