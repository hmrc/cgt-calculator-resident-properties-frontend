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

import assets.MessageLookup.{LossesBroughtForward => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import config.AppConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.{DeductionsController, routes}
import models.resident._
import models.resident.properties.{ChargeableGainAnswers, LettingsReliefModel, PropertyLivedInModel, YourAnswersSummaryModel}
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class LossesBroughtForwardActionSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper with MockitoSugar {

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

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[LossesBroughtForwardModel](ArgumentMatchers.eq(keystoreKeys.lossesBroughtForward))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(lossesBroughtForwardData))

    when(mockCalcConnector.getPropertyGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockCalcConnector.getPropertyDeductionAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(chargeableGainAnswers))

    when(mockCalcConnector.calculateRttPropertyChargeableGain(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(chargeableGain)))

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.eq(keystoreKeys.disposalDate))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(disposalDate)

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(taxYear)

    when(mockCalcConnector.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(keystoreKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(propertyLivedInModel)

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](ArgumentMatchers.eq(keystoreKeys.privateResidenceRelief))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(privateResidenceReliefModel)

    when(mockCalcConnector.fetchAndGetFormData[LettingsReliefModel](ArgumentMatchers.eq(keystoreKeys.lettingsRelief))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(lettingsReliefModel)

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config = mock[AppConfig]
    }
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

      s"return a title of ${messages.title("2015/16")}" in {
        doc.title shouldEqual messages.title("2015/16")
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

      s"have a back link with the address ${controllers.routes.DeductionsController.propertyLivedIn().url}" in {
        doc.select("#back-link").attr("href") shouldEqual controllers.routes.DeductionsController.propertyLivedIn().url
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

      s"have a back link with the address ${controllers.routes.DeductionsController.privateResidenceRelief().url}" in {
        doc.select("#back-link").attr("href") shouldEqual controllers.routes.DeductionsController.privateResidenceRelief().url
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

      s"have a back link with the address ${controllers.routes.DeductionsController.lettingsRelief().url}" in {
        doc.select("#back-link").attr("href") shouldEqual controllers.routes.DeductionsController.lettingsRelief().url
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

      s"have a back link with the address ${controllers.routes.DeductionsController.lettingsReliefValue().url}" in {
        doc.select("#back-link").attr("href") shouldEqual controllers.routes.DeductionsController.lettingsReliefValue().url
      }
    }
  }

  "Calling .submitLossesBroughtForward from the DeductionsController" when {

    "a valid form 'No' and chargeable gain is £1000" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), gainModel,
        summaryModel, ChargeableGainResultModel(1000, 1000, 0, 0, 0, BigDecimal(0), BigDecimal(0), Some(BigDecimal(0)),
          Some(BigDecimal(0)), 0, 0), Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request)

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
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the review your answers page" in {
        redirectLocation(result).get shouldBe routes.ReviewAnswersController.reviewDeductionsAnswers().url
      }
    }

    "a valid form 'No' and has a positive chargeable gain of £1,000" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), gainModel,
        summaryModel, ChargeableGainResultModel(1000, -1000, 0, 0, 2000, BigDecimal(0), BigDecimal(0), Some(BigDecimal(0)),
          Some(BigDecimal(0)), 0, 0), Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the review answers page" in {
        redirectLocation(result).get shouldBe routes.ReviewAnswersController.reviewDeductionsAnswers().url
      }
    }

    "a valid form 'Yes'" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), gainModel, summaryModel,
        ChargeableGainResultModel(0, 0, 0, 0, 0, BigDecimal(0), BigDecimal(0), Some(BigDecimal(0)),
          Some(BigDecimal(0)), 0, 0), Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", "Yes"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the losses brought forward value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/losses-brought-forward-value")
      }
    }

    "an invalid form is submitted" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), gainModel,
        summaryModel, chargeableGainModel, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("option", ""))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the brought forward losses page" in {
        Jsoup.parse(bodyOf(result)).title() shouldEqual messages.title("2015/16")
      }
    }
  }
}
