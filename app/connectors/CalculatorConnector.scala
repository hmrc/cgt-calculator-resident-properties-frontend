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

import constructors.resident.{properties => propertyConstructor}
import models.resident._
import models.resident.properties._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CalculatorConnector @Inject()(val servicesConfig: ServicesConfig,
                          val http: HttpClient)(implicit val ec: ExecutionContext) {

  val serviceUrl: String = servicesConfig.baseUrl("capital-gains-calculator")

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val headers = Seq("Accept" -> "application/vnd.hmrc.1.0+json")

  def getMinimumDate()(implicit hc : HeaderCarrier): Future[LocalDate] = {
    http.GET[LocalDate](s"$serviceUrl/capital-gains-calculator/minimum-date", headers = headers)
  }

  def getFullAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear", headers = headers)
  }

  def getPartialAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-partial-aea?taxYear=$taxYear", headers = headers)
  }

  def getPA(taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false,
            isEligibleMarriageAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=$taxYear" +
      s"${
        if (isEligibleBlindPersonsAllowance) s"&isEligibleBlindPersonsAllowance=true"
        else ""
      }" +
      s"${
        if (isEligibleMarriageAllowance) s"&isEligibleMarriageAllowance=true"
        else ""
      }"
      , headers = headers)
  }

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    http.GET[Option[TaxYearModel]](s"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear", headers = headers)
  }


  //Rtt property calculation methods
  def calculateRttPropertyGrossGain(input: YourAnswersSummaryModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/calculate-total-gain" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(input)
      , headers = headers)
  }

  def calculateRttPropertyChargeableGain(totalGainInput: YourAnswersSummaryModel,
                                         chargeableGainInput: ChargeableGainAnswers,
                                         maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
    http.GET[Option[ChargeableGainResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-chargeable-gain" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)
      , headers = headers)
  }

  def calculateRttPropertyTotalGainAndTax(totalGainInput: YourAnswersSummaryModel,
                                          chargeableGainInput: ChargeableGainAnswers,
                                          maxAEA: BigDecimal,
                                          incomeAnswers: IncomeAnswersModel)(implicit hc: HeaderCarrier): Future[Option[TotalGainAndTaxOwedModel]] = {
    http.GET[Option[TotalGainAndTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/calculate-resident-capital-gains-tax" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA) +
      propertyConstructor.CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)
      , headers = headers)
  }

  def getPropertyTotalCosts(input: YourAnswersSummaryModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/calculate-total-costs" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(input)
      , headers = headers)
  }
}
