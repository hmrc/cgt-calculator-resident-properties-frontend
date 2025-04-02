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

package connectors

import com.typesafe.config.ConfigFactory
import common.{CommonPlaySpec, Dates}
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.properties.{ChargeableGainAnswers, YourAnswersSummaryModel}
import models.resident.{ChargeableGainResultModel, IncomeAnswersModel, TaxYearModel, TotalGainAndTaxOwedModel}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport
import util.WireMockMethods

import java.time.LocalDate
import scala.Option.option2Iterable
import scala.concurrent.ExecutionContext



class CalculatorConnectorSpec extends CommonPlaySpec with MockitoSugar with WireMockSupport with WireMockMethods with GuiceOneAppPerSuite{




  private val config = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |      capital-gains-calculator {
         |      host     = $wireMockHost
         |      port     = $wireMockPort
         |    }
         |  }
         |}
         |
         |""".stripMargin
    )
  )



  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(config).build()

  val connector: CalculatorConnector = app.injector.instanceOf[CalculatorConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]


  "Calling .getMinimumDate" must {

    "return a DateTime which matches the returned LocalDate" in {

      val expectedResult = LocalDate.parse("2015-06-04")
      when(
        GET,
        "/capital-gains-calculator/minimum-date"
      ).thenReturn(Status.OK,expectedResult)
      await(connector.getMinimumDate()) shouldBe expectedResult
    }

   "return a failure if one occurs" in {
      wireMockServer.stop()
      (the[Exception] thrownBy await(connector.getMinimumDate())).getMessage  should include ("Connection refused")
      wireMockServer.start()
    }
  }

  "Calling .getFullAEA" should{

    "return Some(BigDecimal(100.0))" in{
      val expectedResult = Some(BigDecimal(100.0))
      when(
        GET,
        "/capital-gains-calculator/tax-rates-and-bands/max-full-aea",
      ).thenReturn(Status.OK,expectedResult)

      val result = await(connector.getFullAEA(2017))
      result shouldBe expectedResult
    }


    "return None" in {
      when(
        GET,
        "/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=0",
      ).thenReturn(Status.OK,None)
      val result = connector.getFullAEA(0)
      await(result.value) shouldBe None
    }

  }

  "Calling .getPartialAEA" should{
    "return Some(BigDecimal(200.0))" in{
      val expectedResult = Some(BigDecimal(200.0))

      when(
        GET,
        "/capital-gains-calculator/tax-rates-and-bands/max-partial-aea"
      ).thenReturn(Status.OK,expectedResult)

      val result = connector.getPartialAEA(2017)
      await(result) shouldBe expectedResult
    }

    "return None" in {
      when(
        GET,
        "/capital-gains-calculator/tax-rates-and-bands/max-partial-aea?taxYear=1"
      ).thenReturn(Status.OK,None)

      val result = connector.getPartialAEA(1)
      await(result.value) shouldBe None
    }
  }

  "Calling .getPA" should{
    "return Some(BigDecimal(300.0)) when isEligibleBlindPersonsAllowance = true" in{
      val req = "/capital-gains-calculator/tax-rates-and-bands/max-pa"
      val expectedResult = Some(BigDecimal(300.0))
      when(
        GET,
        req
      ).thenReturn(Status.OK,expectedResult)

      val result = connector.getPA(2017,isEligibleBlindPersonsAllowance = true)
      await(result) shouldBe expectedResult
    }

    "return Some(BigDecimal(350.0)) when isEligibleBlindPersonsAllowance = false" in{
      val req = "/capital-gains-calculator/tax-rates-and-bands/max-pa"
      val expectedResult = Some(BigDecimal(350.0))
      when(
        GET,
        req
      ).thenReturn(Status.OK,expectedResult)

      val result = connector.getPA(2017,isEligibleBlindPersonsAllowance = false)
      await(result) shouldBe expectedResult
    }

    "return None when isEligibleMarriageAllowance = true" in {
      when(
        GET,
        "/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=2"
      ).thenReturn(Status.OK,None)

      val result = connector.getPA(2, isEligibleMarriageAllowance= true)
      await(result.value) shouldBe None
    }

    "return None when isEligibleMarriageAllowance = false" in {
      when(
        GET,
        "/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=2"
      ).thenReturn(Status.OK,None)

      val result = connector.getPA(2, isEligibleMarriageAllowance = false)
      await(result.value) shouldBe None
    }
  }

  "Calling .getTaxYear" should{
    "return Some(TaxYearModel)" in{
      val model = TaxYearModel(taxYearSupplied = "2017/18", isValidYear = true, calculationTaxYear = "testCalcTaxYear")

      when(
        GET,
        "/capital-gains-calculator/tax-year"
      ).thenReturn(Status.OK,Some(model))


      val result = connector.getTaxYear("2017")
      await(result) shouldBe Some(model)
    }

    "return None" in {

      when(
        GET,
        "/capital-gains-calculator/tax-year"
      ).thenReturn(Status.OK,None)


      val result = connector.getTaxYear("3")
      await(result.value) shouldBe None
    }
  }

  val testYourAnswersSummaryModel: YourAnswersSummaryModel = YourAnswersSummaryModel(
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

  val testChargeableGainAnswersModel: ChargeableGainAnswers = ChargeableGainAnswers(
    broughtForwardModel = None,
    broughtForwardValueModel = None,
    propertyLivedInModel = None,
    privateResidenceReliefModel = None,
    privateResidenceReliefValueModel = None,
    lettingsReliefModel = None,
    lettingsReliefValueModel = None)

  val testCurrentIncomeModel: CurrentIncomeModel = CurrentIncomeModel(BigDecimal(999))
  val testPersonalAllowanceModel: PersonalAllowanceModel = PersonalAllowanceModel(BigDecimal(999))

  val testIncomeAnswersModel: IncomeAnswersModel = IncomeAnswersModel(
    currentIncomeModel = Some(testCurrentIncomeModel),
    personalAllowanceModel = Some(testPersonalAllowanceModel)
  )

  "Calling .calculateRttPropertyGrossGain" should{
    "return BigDecimal(400.0)" in{


      val expectedResult = BigDecimal(400.0)
      when(
        GET,
        "/capital-gains-calculator/calculate-total-gain"
      ).thenReturn(Status.OK,expectedResult)

      val result = connector.calculateRttPropertyGrossGain(testYourAnswersSummaryModel)
      await(result) shouldBe expectedResult
    }
  }

  "Calling .calculateRttPropertyChargeableGain" should {
    "return ChargeableGainResultModel" in{

      val expectedResult = Some(ChargeableGainResultModel(
        gain = BigDecimal(-45000),
        chargeableGain=BigDecimal(-45000),
        aeaUsed = BigDecimal(0),
        aeaRemaining = BigDecimal(124),
        deductions= BigDecimal(-45000),
        allowableLossesRemaining= BigDecimal(0),
        broughtForwardLossesRemaining= BigDecimal(0),
        lettingReliefsUsed = Option[BigDecimal](45000),
        prrUsed= Option[BigDecimal](0),
        broughtForwardLossesUsed= BigDecimal(0),
        allowableLossesUsed= BigDecimal(0)
      ))

      when(
        GET,
        "/capital-gains-calculator/calculate-chargeable-gain"
      ).thenReturn(Status.OK,expectedResult)


      val result = connector.calculateRttPropertyChargeableGain(testYourAnswersSummaryModel, testChargeableGainAnswersModel, BigDecimal(123.45))
      await(result) shouldBe expectedResult
    }
  }

  "Calling .calculateRttPropertyTotalGainAndTax" should {
    "return TotalGainAndTaxOwedModel" in{

      val expectedResult = Some(TotalGainAndTaxOwedModel(
        gain = BigDecimal(-45000),
        chargeableGain=BigDecimal(-45000),
        aeaUsed = BigDecimal(0),
        deductions= BigDecimal(-45000),
        taxOwed = -8100,
        firstBand= 0,
        firstRate= 0,
        lettingReliefsUsed = Option[BigDecimal](45000),
        prrUsed= Option[BigDecimal](0),
        broughtForwardLossesUsed= BigDecimal(0),
        allowableLossesUsed= BigDecimal(0),
        baseRateTotal = -8100,
        secondBand = Option[BigDecimal](0),
        secondRate = Option[Int](0)
      ))


      when(
        GET,
        "/capital-gains-calculator/calculate-resident-capital-gains-tax",
      ).thenReturn(Status.OK,expectedResult)

      val result = connector.calculateRttPropertyTotalGainAndTax(testYourAnswersSummaryModel,
                                                                                 testChargeableGainAnswersModel,
                                                                                 BigDecimal(123.45),
                                                                                 testIncomeAnswersModel)
      await(result) shouldBe expectedResult
    }
  }

  "Calling .getPropertyTotalCosts" should {


    "return 1000" in {
      val expectedResult = 1000

      when(
        GET,
        "/capital-gains-calculator/calculate-total-costs"
      ).thenReturn(Status.OK,expectedResult)

      val result = connector.getPropertyTotalCosts(testYourAnswersSummaryModel)

      await(result) shouldBe expectedResult
    }
  }


}
