/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.ReportControllerSpec

import assets.MessageLookup.{SummaryPage => messages}
import common.Dates
import controllers.ReportController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import it.innove.play.pdf.PdfGenerator
import javax.inject.Inject
import models.resident._
import models.resident.properties._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.RequestHeader
import play.api.test.Helpers._
import common.{CommonPlaySpec, WithCommonFakeApplication}
import views.html.calculation.resident.properties.report.{deductionsSummaryReport, finalSummaryReport, gainSummaryReport}

import scala.concurrent.Future

class DeductionsSummaryActionSpec @Inject()(val pdfGenerator: PdfGenerator) extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar with CommonMocks {

  def setupTarget
  (
    yourAnswersSummaryModel: YourAnswersSummaryModel,
    grossGain: BigDecimal,
    chargeableGainAnswers: ChargeableGainAnswers,
    chargeableGainResultModel: Option[ChargeableGainResultModel] = None,
    taxYearModel: Option[TaxYearModel]
  ): ReportController = {

    when(mockSessionCacheService.getPropertyGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(yourAnswersSummaryModel))

    when(mockCalcConnector.calculateRttPropertyGrossGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(grossGain))

    when(mockSessionCacheService.getPropertyDeductionAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(chargeableGainAnswers))

    when(mockCalcConnector.calculateRttPropertyChargeableGain(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
    (ArgumentMatchers.any()))
      .thenReturn(chargeableGainResultModel)

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11100))))

    when(mockCalcConnector.getPropertyTotalCosts(ArgumentMatchers.any())(ArgumentMatchers.any()))
    .thenReturn(Future.successful(BigDecimal(10000)))

    new ReportController(fakeApplication.configuration, mockCalcConnector, mockSessionCacheService, mockMessagesControllerComponents,
      fakeApplication.injector.instanceOf[deductionsSummaryReport],
      fakeApplication.injector.instanceOf[gainSummaryReport],
      fakeApplication.injector.instanceOf[finalSummaryReport],
      pdfGenerator) {
      override def host(implicit request: RequestHeader): String = "http://localhost:9977/"
    }
  }

  "Calling .deductionReport from the ReportController" when {

    "a 0 gain is returned" should {
      lazy val gainAnswers = YourAnswersSummaryModel(
        disposalDate = Dates.constructDate(10, 10, 2018),
        disposalValue = Some(200000),
        worthWhenSoldForLess = None,
        whoDidYouGiveItTo = Some("Other"),
        worthWhenGaveAway = Some(10000),
        disposalCosts = 10000,
        acquisitionValue = Some(100000),
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        acquisitionCosts = 10000,
        improvements = 30000,
        givenAway = true,
        sellForLess = Some(false),
        ownerBeforeLegislationStart = true,
        valueBeforeLegislationStart = Some(5000),
        howBecameOwner = Some("Bought"),
        boughtForLessThanWorth = Some(false)
      )

      lazy val deductionAnswers = ChargeableGainAnswers(
        Some(LossesBroughtForwardModel(true)),
        Some(LossesBroughtForwardValueModel(10000)),
        Some(PropertyLivedInModel(false)),
        None,
        None,
        None,
        None
      )
      lazy val results = ChargeableGainResultModel(BigDecimal(50000),
        BigDecimal(-11000),
        BigDecimal(0),
        BigDecimal(11000),
        BigDecimal(71000),
        BigDecimal(0),
        BigDecimal(0),
        Some(BigDecimal(0)),
        Some(BigDecimal(0)),
        0,
        0
      )

      lazy val target = setupTarget(
        gainAnswers,
        0,
        deductionAnswers,
        Some(results),
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.deductionsReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "return a pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "return the pdf with a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }

    "a carried forward loss is returned with an invalid tax year" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(
        disposalDate = Dates.constructDate(12, 1, 2016),
        disposalValue = Some(30000),
        worthWhenSoldForLess = None,
        whoDidYouGiveItTo = None,
        worthWhenGaveAway = None,
        disposalCosts = 0,
        acquisitionValue = Some(10000),
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        acquisitionCosts = 0,
        improvements = 0,
        givenAway = false,
        sellForLess = Some(false),
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart =  None,
        howBecameOwner = Some("Bought"),
        boughtForLessThanWorth = Some(false)
      )

      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None,
        Some(PropertyLivedInModel(false)), None, None, None, None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(20000, 20000, 11100, 0, 11100,
        BigDecimal(0), BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        taxYearModel = Some(TaxYearModel("2013/2014", false, "2015/16"))
      )
      lazy val result = target.deductionsReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "return a pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "return the pdf with a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }
  }
}
