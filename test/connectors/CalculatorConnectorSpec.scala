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

import com.github.tomakehurst.wiremock.client.WireMock._
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
      val req = "/capital-gains-calculator/minimum-date"
      val date = "\"2015-04-05\"\n"
      val expectedResponse = LocalDate.parse("2015-04-05")

      stubFor(get(urlPathEqualTo(req))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(date)
        )
      )

      val result = await(connector.getMinimumDate())
      await(result) shouldBe expectedResponse
    }

   "return a failure if one occurs" in {
      wireMockServer.stop()
      (the[Exception] thrownBy await(connector.getMinimumDate())).getMessage  should include ("Connection refused")
      wireMockServer.start()
    }
  }

  "Calling .getFullAEA" should{

    "return Some(BigDecimal(11100)) when taxYear = 2017" in {
      val req = "/capital-gains-calculator/tax-rates-and-bands/max-full-aea"
      val expectedResponse = Some(BigDecimal(11100))
      stubFor(get(urlPathEqualTo(req))
        .withQueryParam("taxYear", equalTo("2017"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(expectedResponse.get.toString)
        )
      )

      val result = connector.getFullAEA(2017)
      await(result) shouldBe expectedResponse
    }



    "return None when taxYear = 0" in {
      val req = "/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=0"
      val expectedResponse = "\"This tax year is not valid\""

      stubFor(get(urlPathEqualTo(req))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(expectedResponse)
        )
      )

      val result = connector.getFullAEA(0)
      await(result) shouldBe None
    }
  }


  "Calling .getPA" should{


    "return Some(BigDecimal(13290)) when isEligibleBlindPersonsAllowance = true and taxYear = 2017" in {
      val req = "/capital-gains-calculator/tax-rates-and-bands/max-pa"
      val expectedResponse = Some(BigDecimal(13290))
      stubFor(get(urlPathEqualTo(req))
        .withQueryParam("taxYear", equalTo("2017"))
        .withQueryParam("isEligibleBlindPersonsAllowance", equalTo("true"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(expectedResponse.get.toString)
        )
      )

      val result = connector.getPA(taxYear = 2017, isEligibleBlindPersonsAllowance = true)
      await(result) shouldBe expectedResponse
    }

    "return Some(BigDecimal(11000)) when taxYear=2017" in {
      val req = "/capital-gains-calculator/tax-rates-and-bands/max-pa"
      val expectedResponse = Some(BigDecimal(11000))
      stubFor(get(urlPathEqualTo(req))
        .withQueryParam("taxYear", equalTo("2017"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(expectedResponse.get.toString)
        )
      )

      val result = connector.getPA(taxYear = 2017)
      await(result) shouldBe expectedResponse
    }

    "return None when taxYear = 2 and isEligibleBlindPersonsAllowance = true" in {

      val req = "/capital-gains-calculator/tax-rates-and-bands/max-pa"
      val expectedResponse = "\"This tax year is not valid\""

      stubFor(get(urlPathEqualTo(req))
        .withQueryParam("taxYear", equalTo("2"))
        .withQueryParam("isEligibleBlindPersonsAllowance", equalTo("true"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(expectedResponse)
        )
      )

      val result = connector.getPA(taxYear = 2017, isEligibleBlindPersonsAllowance = true)
      await(result) shouldBe None
    }

    "return None when taxYear = 2 and isEligibleBlindPersonsAllowance = false" in {
      val req = "/capital-gains-calculator/tax-rates-and-bands/max-pa"
      val expectedResponse = "\"This tax year is not valid\""
      val expectedResponseBigDecimal = None

      stubFor(get(urlPathEqualTo(req))
        .withQueryParam("taxYear", equalTo("2"))
        .withQueryParam("isEligibleBlindPersonsAllowance", equalTo("false"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(expectedResponse)
        )
      )

      val result = await(connector.getPA(taxYear = 2, isEligibleBlindPersonsAllowance = false))
      result shouldBe expectedResponseBigDecimal
    }

  }

  "Calling .getTaxYear" should{

    "return Some(TaxYearModel)" in {
      val req = "/capital-gains-calculator/tax-year"
      val model = TaxYearModel(taxYearSupplied = "2017/18", isValidYear = true, calculationTaxYear = "testCalcTaxYear")
      val modelJson = Json.toJson(model).toString()

      stubFor(get(urlPathEqualTo(req))
        .withQueryParam("date", equalTo("2017-01-01"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(modelJson)
        )
      )

      val result = await(connector.getTaxYear("2017-01-01"))
      result shouldBe Some(model)
    }

    "return None when taxYear = 3" in {

      val expectedResult = "Cannot parse input as LocalDate: For input string: \"3\""
      val req = "/capital-gains-calculator/tax-year?date=3"

      stubFor(get(urlPathEqualTo(req))
        .willReturn(aResponse()
          .withStatus(400)
          .withBody(expectedResult)
        )
      )

      val result = await(connector.getTaxYear("3"))
      result shouldBe None
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

  "return BigDecimal(400.0)" in {
    val req = "/capital-gains-calculator/calculate-total-gain"
    val expectedResult = BigDecimal(400.0)
    val expectedResultJson = Json.toJson(expectedResult).toString()

    stubFor(get(urlPathEqualTo(req))
      .withQueryParam("disposalValue", equalTo("10000.0"))
      .withQueryParam("disposalDate", equalTo("2018-10-10"))
      .withQueryParam("improvements", equalTo("30000.0"))
      .withQueryParam("acquisitionValue", equalTo("5000.0"))
      .withQueryParam("disposalCosts", equalTo("10000.0"))
      .withQueryParam("acquisitionCosts", equalTo("10000.0"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(expectedResultJson)
      )
    )

    val result = await(connector.calculateRttPropertyGrossGain(testYourAnswersSummaryModel))
    result shouldBe expectedResult
  }

  "Calling .calculateRttPropertyChargeableGain" should {
    "return ChargeableGainResultModel" in {

      val req = "/capital-gains-calculator/calculate-chargeable-gain"
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
      val expectedResultJson = Json.toJson(expectedResult).toString()

      stubFor(get(urlPathEqualTo(req))
        .withQueryParam("disposalValue", equalTo("10000.0"))
        .withQueryParam("disposalDate", equalTo("2018-10-10"))
        .withQueryParam("improvements", equalTo("30000.0"))
        .withQueryParam("acquisitionValue", equalTo("5000.0"))
        .withQueryParam("disposalCosts", equalTo("10000.0"))
        .withQueryParam("acquisitionCosts", equalTo("10000.0"))
        .withQueryParam("annualExemptAmount", equalTo("123.45"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(expectedResultJson)
        )
      )

      val result = await(connector.calculateRttPropertyChargeableGain(testYourAnswersSummaryModel, testChargeableGainAnswersModel, BigDecimal(123.45)))
      result shouldBe expectedResult
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

      val req = "/capital-gains-calculator/calculate-resident-capital-gains-tax"

      val expectedResultJson = Json.toJson(expectedResult).toString()

      stubFor(get(urlPathEqualTo(req))
        .withQueryParam("disposalValue", equalTo("10000.0"))
        .withQueryParam("disposalDate", equalTo("2018-10-10"))
        .withQueryParam("improvements", equalTo("30000.0"))
        .withQueryParam("acquisitionValue", equalTo("5000.0"))
        .withQueryParam("disposalCosts", equalTo("10000.0"))
        .withQueryParam("acquisitionCosts", equalTo("10000.0"))
        .withQueryParam("annualExemptAmount", equalTo("123.45"))
        .withQueryParam("previousIncome", equalTo("999.0"))
        .withQueryParam("personalAllowance", equalTo("999.0"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(expectedResultJson)
        )
      )

      val result = await(connector.calculateRttPropertyTotalGainAndTax(testYourAnswersSummaryModel,
                                                                  testChargeableGainAnswersModel,
                                                                  BigDecimal(123.45),
                                                                  testIncomeAnswersModel))

      result shouldBe expectedResult
    }
  }

  "Calling .getPropertyTotalCosts" should {


    "return BigDecimal(1000.0)" in {
      val req = "/capital-gains-calculator/calculate-total-costs"
      val expectedResult = BigDecimal(1000.0)


      stubFor(get(urlPathEqualTo(req))
        .withQueryParam("disposalValue",equalTo("10000.0"))
        .withQueryParam("disposalDate",equalTo("2018-10-10"))
        .withQueryParam("improvements",equalTo("30000.0"))
        .withQueryParam("acquisitionValue",equalTo("5000.0"))
        .withQueryParam("disposalCosts",equalTo("10000.0"))
        .withQueryParam("acquisitionCosts",equalTo("10000.0"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(expectedResult.toString())
        )
      )

      val result = connector.getPropertyTotalCosts(testYourAnswersSummaryModel)

      await(result) shouldBe expectedResult
    }
  }


}
