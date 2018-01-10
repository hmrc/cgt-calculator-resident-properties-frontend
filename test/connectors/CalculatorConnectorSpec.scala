/*
 * Copyright 2018 HM Revenue & Customs
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

package connectors

import java.time.LocalDate
import java.util.UUID

import common.Dates
import models.resident.{IncomeAnswersModel, TaxYearModel}
import models.resident.properties.{ChargeableGainAnswers, YourAnswersSummaryModel}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import constructors.resident.{properties => propertyConstructor}
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.http.logging.SessionId

class CalculatorConnectorSpec extends UnitSpec with MockitoSugar {

  val mockHttp: HttpGet = mock[HttpGet]
  val sessionId = UUID.randomUUID.toString

  object TargetCalculatorConnector extends CalculatorConnector {
    override val http = mockHttp
    override val serviceUrl = "capital-gains-calculator"
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  "Calling .getMinimumDate" should {
    def mockDate(result: Future[DateTime]): OngoingStubbing[Future[DateTime]] =
      when(mockHttp.GET[DateTime](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(result)

    "return a DateTime which matches the returned LocalDate" in {
      mockDate(Future.successful(DateTime.parse("2015-06-04")))
      await(TargetCalculatorConnector.getMinimumDate()) shouldBe LocalDate.parse("2015-06-04")
    }

    "return a failure if one occurs" in {
      mockDate(Future.failed(new Exception("error message")))
      the[Exception] thrownBy await(TargetCalculatorConnector.getMinimumDate()) should have message "error message"
    }
  }

  "Calling .getFullAEA" should{
    "return Some(BigDecimal(100.0))" in{
      when(mockHttp.GET[Option[BigDecimal]](ArgumentMatchers.contains("taxYear=2017"))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(BigDecimal(100.0))))

      val result = TargetCalculatorConnector.getFullAEA(2017)
      await(result) shouldBe Some(BigDecimal(100.0))
    }

    "return None" in{
      when(mockHttp.GET[Option[BigDecimal]](ArgumentMatchers.contains("taxYear=0"))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(None))

      val result = TargetCalculatorConnector.getFullAEA(0)
      await(result) shouldBe None
    }
  }

  "Calling .getPartialAEA" should{
    "return Some(BigDecimal(200.0))" in{
      when(mockHttp.GET[Option[BigDecimal]](ArgumentMatchers.contains("taxYear=2017"))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(BigDecimal(200.0))))

      val result = TargetCalculatorConnector.getPartialAEA(2017)
      await(result) shouldBe Some(BigDecimal(200.0))
    }

    "return None" in{
      when(mockHttp.GET[Option[BigDecimal]](ArgumentMatchers.contains("taxYear=1"))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(None))

      val result = TargetCalculatorConnector.getPartialAEA(1)
      await(result) shouldBe None
    }
  }

  "Calling .getPA" should{
    "return Some(BigDecimal(300.0)) when isEligibleBlindPersonsAllowance = true" in{
      val req = "capital-gains-calculator/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=2017&isEligibleBlindPersonsAllowance=true"
      when(mockHttp.GET[Option[BigDecimal]](ArgumentMatchers.eq(req))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(BigDecimal(300.0))))

      val result = TargetCalculatorConnector.getPA(2017,isEligibleBlindPersonsAllowance = true)
      await(result) shouldBe Some(BigDecimal(300.0))
    }

    "return Some(BigDecimal(350.0)) when isEligibleBlindPersonsAllowance = false" in{
      val req = "capital-gains-calculator/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=2017"
      when(mockHttp.GET[Option[BigDecimal]](ArgumentMatchers.eq(req))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(BigDecimal(350.0))))

      val result = TargetCalculatorConnector.getPA(2017,isEligibleBlindPersonsAllowance = false)
      await(result) shouldBe Some(BigDecimal(350.0))
    }

    "return None" in{
      when(mockHttp.GET[Option[BigDecimal]](ArgumentMatchers.contains("taxYear=2"))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(None))

      val result = TargetCalculatorConnector.getPA(2, isEligibleBlindPersonsAllowance = true)
      await(result) shouldBe None
    }
  }

  "Calling .getTaxYear" should{
    "return Some(TaxYearModel)" in{
      val model = TaxYearModel(taxYearSupplied = "testYearSupplied", isValidYear = true, calculationTaxYear = "testCalcTaxYear")

      when(mockHttp.GET[Option[TaxYearModel]](ArgumentMatchers.contains("date=2017"))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(model)))

      val result = TargetCalculatorConnector.getTaxYear("2017")
      await(result) shouldBe Some(model)
    }

    "return None" in{
      when(mockHttp.GET[Option[TaxYearModel]](ArgumentMatchers.contains("date=3"))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(None))

      val result = TargetCalculatorConnector.getTaxYear("3")
      await(result) shouldBe None
    }
  }

  val testYourAnswersSummaryModel = YourAnswersSummaryModel(
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

  val testChargeableGainAnswersModel = ChargeableGainAnswers(
    broughtForwardModel = None,
    broughtForwardValueModel = None,
    propertyLivedInModel = None,
    privateResidenceReliefModel = None,
    privateResidenceReliefValueModel = None,
    lettingsReliefModel = None,
    lettingsReliefValueModel = None)

  val testCurrentIncomeModel = CurrentIncomeModel(BigDecimal(999))
  val testPersonalAllowanceModel = PersonalAllowanceModel(BigDecimal(999))

  val testIncomeAnswersModel = IncomeAnswersModel(
    currentIncomeModel = Some(testCurrentIncomeModel),
    personalAllowanceModel = Some(testPersonalAllowanceModel)
  )

  "Calling .calculateRttPropertyGrossGain" should{
    "return BigDecimal(400.0)" in{
      val req = s"capital-gains-calculator/capital-gains-calculator/calculate-total-gain" +
        s"${propertyConstructor.CalculateRequestConstructor.totalGainRequestString(testYourAnswersSummaryModel)}"

      when(mockHttp.GET[BigDecimal](ArgumentMatchers.eq(req))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(BigDecimal(400.0)))

      val result = TargetCalculatorConnector.calculateRttPropertyGrossGain(testYourAnswersSummaryModel)
      await(result) shouldBe BigDecimal(400.0)
    }
  }

  "Calling .calculateRttPropertyChargeableGain" should{
    "return BigDecimal(500.0)" in{
      when(mockHttp.GET[BigDecimal](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(BigDecimal(500.0)))

      val result = TargetCalculatorConnector.calculateRttPropertyChargeableGain(testYourAnswersSummaryModel, testChargeableGainAnswersModel, BigDecimal(123.45))
      await(result) shouldBe BigDecimal(500.0)
    }
  }

  "Calling .calculateRttPropertyTotalGainAndTax" should{
    "return BigDecimal(600.0)" in{
      when(mockHttp.GET[BigDecimal](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(BigDecimal(600.0)))

      val result = TargetCalculatorConnector.calculateRttPropertyTotalGainAndTax(testYourAnswersSummaryModel,
                                                                                 testChargeableGainAnswersModel,
                                                                                 BigDecimal(123.45),
                                                                                 testIncomeAnswersModel)
      await(result) shouldBe BigDecimal(600.0)
    }
  }

  "Calling .getPropertyTotalCosts" should {
    lazy val result = TargetCalculatorConnector.getPropertyTotalCosts(testYourAnswersSummaryModel)

    "return 1000" in {
      when(mockHttp.GET[BigDecimal](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(BigDecimal(1000.0)))

      await(result) shouldBe 1000
    }
  }


}
