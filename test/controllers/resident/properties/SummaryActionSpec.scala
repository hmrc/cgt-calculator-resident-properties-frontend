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

package controllers.resident.properties

import assets.MessageLookup.{SummaryPage => messages}
import common.Dates
import connectors.CalculatorConnector
import controllers.SummaryController
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.income._
import models.resident.properties._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future


class SummaryActionSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget
  (
    yourAnswersSummaryModel: YourAnswersSummaryModel,
    grossGain: BigDecimal,
    chargeableGainAnswers: ChargeableGainAnswers,
    chargeableGainResultModel: Option[ChargeableGainResultModel] = None,
    incomeAnswers: IncomeAnswersModel,
    totalGainAndTaxOwedModel: Option[TotalGainAndTaxOwedModel] = None,
    taxYearModel: Option[TaxYearModel]
  ): SummaryController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]

    when(mockCalculatorConnector.getPropertyGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(yourAnswersSummaryModel))

    when(mockCalculatorConnector.calculateRttPropertyGrossGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(grossGain))

    when(mockCalculatorConnector.getPropertyDeductionAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(chargeableGainAnswers))

    when(mockCalculatorConnector.calculateRttPropertyChargeableGain
    (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(chargeableGainResultModel)

    when(mockCalculatorConnector.getPropertyIncomeAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(incomeAnswers))

    when(mockCalculatorConnector.calculateRttPropertyTotalGainAndTax
    (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGainAndTaxOwedModel))

    when(mockCalculatorConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockCalculatorConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11100))))

    when(mockCalculatorConnector.getPropertyTotalCosts(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(BigDecimal(1000)))

    new SummaryController {
      override val calculatorConnector: CalculatorConnector = mockCalculatorConnector
    }
  }

  "Calling .summary from the SummaryController" when {

    "a negative total gain is returned" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(
        Dates.constructDate(12, 1, 2016),
        Some(3000),
        None,
        whoDidYouGiveItTo = Some("Other"),
        worthWhenGaveAway = Some(10000),
        10,
        Some(5000),
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        5,
        0,
        true,
        Some(false),
        true,
        Some(BigDecimal(5000)),
        Some("Bought"),
        Some(false)
      )

      lazy val chargeableGainAnswers = ChargeableGainAnswers(None, None, None, None, None, None, None)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -6000,
        chargeableGainAnswers, None, incomeAnswersModel,
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"has a link to '${controllers.routes.ReviewAnswersController.reviewGainAnswers()}'" in {
        doc.getElementById("back-link").attr("href") shouldBe controllers.routes.ReviewAnswersController.reviewGainAnswers().url
      }

    }

    "a negative taxable gain is returned with no brought forward losses" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        Some(3000),
        Some(500),
        whoDidYouGiveItTo = None,
        worthWhenGaveAway = None,
        10,
        Some(5000),
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        5,
        0,
        false,
        Some(true),
        false,
        None,
        Some("Bought"),
        Some(false)
      )

      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None, Some(PropertyLivedInModel(false)), None, None, None, None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 0, 11100, BigDecimal(0),
        BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        incomeAnswersModel,
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"has a link to '${controllers.routes.ReviewAnswersController.reviewDeductionsAnswers().url}'" in {
        doc.getElementById("back-link").attr("href") shouldBe controllers.routes.ReviewAnswersController.reviewDeductionsAnswers().url
      }
    }

    "a negative taxable gain is returned with brought forward losses" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        Some(3000),
        None,
        whoDidYouGiveItTo = Some("Other"),
        worthWhenGaveAway = Some(10000),
        10,
        Some(5000),
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        5,
        0,
        true,
        Some(false),
        true,
        Some(BigDecimal(5000)),
        Some("Bought"),
        Some(false)
      )

      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(true)), Some(LossesBroughtForwardValueModel(1000)),
        Some(PropertyLivedInModel(false)), None, None, None, None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 0, 11100, BigDecimal(0),
        BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        incomeAnswersModel,
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"has a link to '${controllers.routes.ReviewAnswersController.reviewDeductionsAnswers().url}'" in {
        doc.getElementById("back-link").attr("href") shouldBe controllers.routes.ReviewAnswersController.reviewDeductionsAnswers().url
      }
    }

    "a positive taxable gain is returned" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(
        Dates.constructDate(12, 1, 2016),
        Some(30000),
        None,
        whoDidYouGiveItTo = None,
        worthWhenGaveAway = None,
        0,
        Some(10000),
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        0,
        0,
        false,
        Some(false),
        false,
        None,
        Some("Bought"),
        Some(false)
      )

      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None,
        Some(PropertyLivedInModel(false)), None, None, None, None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(20000, 20000, 11100, 0, 11100, BigDecimal(0),
        BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
      lazy val incomeAnswersModel = IncomeAnswersModel(Some(CurrentIncomeModel(20000)), Some(PersonalAllowanceModel(10000)))
      lazy val totalGainAndTaxOwedModel = TotalGainAndTaxOwedModel(20000, 20000, 11100, 11100, 3600, 20000, 18,
        None, None, Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        incomeAnswersModel,
        Some(totalGainAndTaxOwedModel),
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"has a link to '${controllers.routes.ReviewAnswersController.reviewFinalAnswers().url}'" in {
        doc.getElementById("back-link").attr("href") shouldBe controllers.routes.ReviewAnswersController.reviewFinalAnswers().url
      }
    }
  }

  "Calling .summary while not eligible for PRR or Lettings Relief with a negative taxable gain" should {
    lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
      Some(3000),
      None,
      whoDidYouGiveItTo = Some("Other"),
      worthWhenGaveAway = Some(10000),
      10,
      Some(5000),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      5,
      0,
      true,
      Some(false),
      true,
      Some(BigDecimal(5000)),
      Some("Bought"),
      Some(false)
    )

    lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None,
      Some(PropertyLivedInModel(false)), None, None, None, None)
    lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 0, 11100, BigDecimal(0),
      BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val incomeAnswersModel = IncomeAnswersModel(None, None)
    lazy val target = setupTarget(
      yourAnswersSummaryModel,
      10000,
      chargeableGainAnswers,
      Some(chargeableGainResultModel),
      incomeAnswersModel,
      taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
    )
    lazy val result = target.summary()(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "not have GA metrics for prr" in {
      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
    }

    "not have GA metrics for lettings relief" in {
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
    }
  }

  "Calling .summary while eligible but not claiming PRR and Lettings Relief with a negative taxable gain" should {
    lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
      Some(3000),
      None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      10,
      Some(5000),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      5,
      0,
      false,
      Some(false),
      false,
      None,
      Some("Bought"),
      Some(false)
    )

    lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None, Some(PropertyLivedInModel(true)), Some(PrivateResidenceReliefModel(false)), None, None, None)
    lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 0, 11100, BigDecimal(0),
      BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val incomeAnswersModel = IncomeAnswersModel(None, None)
    lazy val target = setupTarget(
      yourAnswersSummaryModel,
      10000,
      chargeableGainAnswers,
      Some(chargeableGainResultModel),
      incomeAnswersModel,
      taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
    )
    lazy val result = target.summary()(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "not have GA metrics for prr" in {
      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 1
    }

    "not have GA metrics for lettings relief" in {
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
    }
  }

  "Calling .summary while eligible and claiming PRR but not Lettings Relief with a negative taxable gain" should {
    lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
      Some(3000),
      None,
      whoDidYouGiveItTo = Some("Other"),
      worthWhenGaveAway = Some(10000),
      10,
      Some(5000),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      5,
      0,
      true,
      Some(false),
      true,
      Some(BigDecimal(5000)),
      Some("Bought"),
      Some(false)
    )

    lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None, Some(PropertyLivedInModel(true)),
      Some(PrivateResidenceReliefModel(true)), Some(PrivateResidenceReliefValueModel(1000)),
      Some(LettingsReliefModel(false)), None)
    lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 0, 11100, BigDecimal(0),
      BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val incomeAnswersModel = IncomeAnswersModel(None, None)
    lazy val target = setupTarget(
      yourAnswersSummaryModel,
      10000,
      chargeableGainAnswers,
      Some(chargeableGainResultModel),
      incomeAnswersModel,
      taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
    )
    lazy val result = target.summary()(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "not have GA metrics for prr" in {
      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 1
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
    }

    "not have GA metrics for lettings relief" in {
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 1
    }
  }

  "Calling .summary while eligible and claiming PRR and Lettings Relief with a negative taxable gain" should {
    lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
      Some(3000),
      None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      10,
      Some(5000),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      5,
      0,
      false,
      Some(false),
      false,
      None,
      Some("Bought"),
      Some(false)
    )

    lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None, Some(PropertyLivedInModel(true)),
      Some(PrivateResidenceReliefModel(true)), Some(PrivateResidenceReliefValueModel(2000)), Some(LettingsReliefModel(true)),
      Some(LettingsReliefValueModel(1000)))
    lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 0, 11100, BigDecimal(0),
      BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val incomeAnswersModel = IncomeAnswersModel(None, None)
    lazy val target = setupTarget(
      yourAnswersSummaryModel,
      10000,
      chargeableGainAnswers,
      Some(chargeableGainResultModel),
      incomeAnswersModel,
      taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
    )
    lazy val result = target.summary()(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "not have GA metrics for prr" in {
      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 1
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
    }

    "not have GA metrics for lettings relief" in {
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 1
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
    }
  }

  "Calling .summary while not eligible for PRR or Lettings Relief with a positive taxable gain" should {
    lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
      Some(30000),
      None,
      whoDidYouGiveItTo = Some("Other"),
      worthWhenGaveAway = Some(10000),
      0,
      Some(10000),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      0,
      0,
      true,
      Some(false),
      true,
      Some(BigDecimal(5000)),
      Some("Bought"),
      Some(false)
    )

    lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None,
      Some(PropertyLivedInModel(false)), None, None, None, None)
    lazy val chargeableGainResultModel = ChargeableGainResultModel(20000, 20000, 11100, 0, 11100, BigDecimal(0),
      BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val incomeAnswersModel = IncomeAnswersModel(Some(CurrentIncomeModel(20000)), Some(PersonalAllowanceModel(10000)))
    lazy val totalGainAndTaxOwedModel = TotalGainAndTaxOwedModel(20000, 20000, 11100, 11100, 3600, 20000, 18,
      None, None, Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val target = setupTarget(
      yourAnswersSummaryModel,
      10000,
      chargeableGainAnswers,
      Some(chargeableGainResultModel),
      incomeAnswersModel,
      Some(totalGainAndTaxOwedModel),
      taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
    )
    lazy val result = target.summary()(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "not have GA metrics for prr" in {
      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
    }

    "not have GA metrics for lettings relief" in {
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
    }
  }

  "Calling .summary while eligible but not claiming PRR and Lettings Relief with a positive taxable gain" should {
    lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
      Some(30000),
      Some(500),
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      0,
      Some(10000),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      0,
      0,
      false,
      Some(true),
      false,
      None,
      Some("Bought"),
      Some(false)
    )

    lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None,
      Some(PropertyLivedInModel(true)), Some(PrivateResidenceReliefModel(false)), None, None, None)
    lazy val chargeableGainResultModel = ChargeableGainResultModel(20000, 20000, 11100, 0, 11100, BigDecimal(0),
      BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val incomeAnswersModel = IncomeAnswersModel(Some(CurrentIncomeModel(20000)), Some(PersonalAllowanceModel(10000)))
    lazy val totalGainAndTaxOwedModel = TotalGainAndTaxOwedModel(20000, 20000, 11100, 11100, 3600, 20000, 18,
      None, None, Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val target = setupTarget(
      yourAnswersSummaryModel,
      10000,
      chargeableGainAnswers,
      Some(chargeableGainResultModel),
      incomeAnswersModel,
      Some(totalGainAndTaxOwedModel),
      taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
    )
    lazy val result = target.summary()(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "not have GA metrics for prr" in {
      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 1
    }

    "not have GA metrics for lettings relief" in {
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
    }
  }

  "Calling .summary while eligible and claiming PRR but not Lettings Relief with a positive taxable gain" should {
    lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
      Some(30000),
      None,
      whoDidYouGiveItTo = Some("Other"),
      worthWhenGaveAway = Some(10000),
      0,
      Some(10000),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      0,
      0,
      true,
      Some(false),
      true,
      Some(BigDecimal(5000)),
      Some("Bought"),
      Some(false)
    )

    lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None,
      Some(PropertyLivedInModel(true)), Some(PrivateResidenceReliefModel(true)), Some(PrivateResidenceReliefValueModel(2000)),
      Some(LettingsReliefModel(false)), None)
    lazy val chargeableGainResultModel = ChargeableGainResultModel(20000, 20000, 11100, 0, 11100, BigDecimal(0),
      BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val incomeAnswersModel = IncomeAnswersModel(Some(CurrentIncomeModel(20000)), Some(PersonalAllowanceModel(10000)))
    lazy val totalGainAndTaxOwedModel = TotalGainAndTaxOwedModel(20000, 20000, 11100, 11100, 3600, 20000, 18,
      None, None, Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val target = setupTarget(
      yourAnswersSummaryModel,
      10000,
      chargeableGainAnswers,
      Some(chargeableGainResultModel),
      incomeAnswersModel,
      Some(totalGainAndTaxOwedModel),
      taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
    )
    lazy val result = target.summary()(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "not have GA metrics for prr" in {
      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 1
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
    }

    "not have GA metrics for lettings relief" in {
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 1
    }
  }

  "Calling .summary while eligible and claiming PRR and Lettings Relief with a positive taxable gain" should {
    lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
      Some(30000),
      Some(500),
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      0,
      Some(10000),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      0,
      0,
      false,
      Some(true),
      false,
      None,
      Some("Bought"),
      Some(false)
    )

    lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None,
      Some(PropertyLivedInModel(true)), Some(PrivateResidenceReliefModel(true)), Some(PrivateResidenceReliefValueModel(2000)),
      Some(LettingsReliefModel(true)), Some(LettingsReliefValueModel(1000)))
    lazy val chargeableGainResultModel = ChargeableGainResultModel(20000, 20000, 11100, 0, 11100, BigDecimal(0),
      BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val incomeAnswersModel = IncomeAnswersModel(Some(CurrentIncomeModel(20000)), Some(PersonalAllowanceModel(10000)))
    lazy val totalGainAndTaxOwedModel = TotalGainAndTaxOwedModel(20000, 20000, 11100, 11100, 3600, 20000, 18,
      None, None, Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
    lazy val target = setupTarget(
      yourAnswersSummaryModel,
      10000,
      chargeableGainAnswers,
      Some(chargeableGainResultModel),
      incomeAnswersModel,
      Some(totalGainAndTaxOwedModel),
      taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
    )
    lazy val result = target.summary()(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "not have GA metrics for prr" in {
      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 1
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
    }

    "not have GA metrics for lettings relief" in {
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 1
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
    }
  }


  "Calling .summary from the SummaryController with no session" should {

    lazy val result = SummaryController.summary(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
    }
  }
}
