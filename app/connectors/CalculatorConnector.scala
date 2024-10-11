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
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CalculatorConnector @Inject()(val servicesConfig: ServicesConfig,
                                    val http: HttpClientV2)(implicit val ec: ExecutionContext) {
  val headers: (String, String) = "Accept" -> "application/vnd.hmrc.1.0+json"
  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val serviceUrl: String = servicesConfig.baseUrl("capital-gains-calculator")

  def getMinimumDate()(implicit hc: HeaderCarrier): Future[LocalDate] = {
    http.get(url"$serviceUrl/capital-gains-calculator/minimum-date")
      .transform(_.addHttpHeaders(headers))
      .execute[LocalDate]
  }

  def getFullAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.get(url"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear")
      .transform(_.addHttpHeaders(headers))
      .execute[Option[BigDecimal]]
  }

  def getPartialAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.get(url"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-partial-aea?taxYear=$taxYear")
      .transform(_.addHttpHeaders(headers))
      .execute[Option[BigDecimal]]
  }

  def getPA(taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false,
            isEligibleMarriageAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    val param1 = {
      if (isEligibleBlindPersonsAllowance) s"&isEligibleBlindPersonsAllowance=true"
      else ""
    }
    val param2 = {
      if (isEligibleMarriageAllowance) s"&isEligibleMarriageAllowance=true"
      else ""
    }

    http.get(url"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=$taxYear+$param1+$param2")
      .transform(_.addHttpHeaders(headers))
      .execute[Option[BigDecimal]]
  }

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    http.get(url"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear")
      .transform(_.addHttpHeaders(headers))
      .execute[Option[TaxYearModel]]
  }


  //Rtt property calculation methods
  def calculateRttPropertyGrossGain(input: YourAnswersSummaryModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    val totalGainReqStr = propertyConstructor.CalculateRequestConstructor.totalGainRequestString(input)
    http.get(url"$serviceUrl/capital-gains-calculator/calculate-total-gain?$totalGainReqStr")
      .transform(_.addHttpHeaders(headers))
      .execute[BigDecimal]
  }

  def calculateRttPropertyChargeableGain(totalGainInput: YourAnswersSummaryModel,
                                         chargeableGainInput: ChargeableGainAnswers,
                                         maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
    val totalGainReqStr = propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput)
    val chargeableGainReqStr = propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)
    http.get(url"$serviceUrl/capital-gains-calculator/calculate-chargeable-gain?$totalGainReqStr+$chargeableGainReqStr")
      .transform(_.addHttpHeaders(headers))
      .execute[Option[ChargeableGainResultModel]]
  }

  def calculateRttPropertyTotalGainAndTax(totalGainInput: YourAnswersSummaryModel,
                                          chargeableGainInput: ChargeableGainAnswers,
                                          maxAEA: BigDecimal,
                                          incomeAnswers: IncomeAnswersModel)(implicit hc: HeaderCarrier): Future[Option[TotalGainAndTaxOwedModel]] = {
    val totalGainReqStr = propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput)
    val chargeableGainReqStr = propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)
    val incomeAnsReqStr = propertyConstructor.CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)
    http.get(url"$serviceUrl/capital-gains-calculator/calculate-resident-capital-gains-tax?$totalGainReqStr+$chargeableGainReqStr+$incomeAnsReqStr")
      .transform(_.addHttpHeaders(headers))
      .execute[Option[TotalGainAndTaxOwedModel]]
  }

  def getPropertyTotalCosts(input: YourAnswersSummaryModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    val totalGainReqStr = propertyConstructor.CalculateRequestConstructor.totalGainRequestString(input)
    http.get(url"$serviceUrl/capital-gains-calculator/calculate-total-costs?$totalGainReqStr")
      .transform(_.addHttpHeaders(headers))
      .execute[BigDecimal]
  }
}
