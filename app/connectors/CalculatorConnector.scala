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

package connectors

import java.time.LocalDate

import common.Dates._
import common.KeystoreKeys.ResidentPropertyKeys
import config.{CalculatorSessionCache, WSHttp}
import constructors.resident.{properties => propertyConstructor}
import models.resident._
import models.resident.properties._
import models.resident.properties.gain.{OwnerBeforeLegislationStartModel, WhoDidYouGiveItToModel, WorthWhenGiftedModel}
import org.asynchttpclient.exception.RemotelyClosedException
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Format
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.exceptions.ApplicationException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpResponse}

object CalculatorConnector extends CalculatorConnector with ServicesConfig {
  override val sessionCache = CalculatorSessionCache
  override val http = WSHttp
  override val serviceUrl = baseUrl("capital-gains-calculator")
}

trait CalculatorConnector {

  val sessionCache: SessionCache
  val http: HttpGet
  val serviceUrl: String
  val homeLink = controllers.routes.GainController.disposalDate().url


  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

  def saveFormData[T](key: String, data: T)(implicit hc: HeaderCarrier, formats: Format[T]): Future[CacheMap] = {
    sessionCache.cache(key, data).recoverWith{
      case e: Exception => Logger.warn(s"Keystore failed to save data: $data to this key: $key with message: ${e.getMessage}", e)
        throw e
    }
  }

  def fetchAndGetFormData[T](key: String)(implicit hc: HeaderCarrier, formats: Format[T]): Future[Option[T]] = {
    sessionCache.fetchAndGetEntry(key).recoverWith{
      case e: RemotelyClosedException => Logger.warn(s"Remotely closed exception from keystore on fetch: ${e.getMessage}", e)
        throw e
    }
  }

  def getMinimumDate()(implicit hc : HeaderCarrier): Future[LocalDate] = {
    http.GET[DateTime](s"$serviceUrl/capital-gains-calculator/minimum-date").map { date =>
      LocalDate.of(date.getYear, date.getMonthOfYear, date.getDayOfMonth)
    }
  }

  def getFullAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear")
  }

  def getPartialAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-partial-aea?taxYear=$taxYear")
  }

  def getPA(taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=$taxYear" +
      s"${
        if (isEligibleBlindPersonsAllowance) s"&isEligibleBlindPersonsAllowance=true"
        else ""
      }"
    )
  }

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    http.GET[Option[TaxYearModel]](s"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear")
  }

  def clearKeystore(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    sessionCache.remove()
  }

  //Rtt property calculation methods
  def calculateRttPropertyGrossGain(input: YourAnswersSummaryModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/calculate-total-gain" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(input)
    )
  }

  def calculateRttPropertyChargeableGain(totalGainInput: YourAnswersSummaryModel,
                                         chargeableGainInput: ChargeableGainAnswers,
                                         maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
    http.GET[Option[ChargeableGainResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-chargeable-gain" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)

    )
  }

  def calculateRttPropertyTotalGainAndTax(totalGainInput: YourAnswersSummaryModel,
                                          chargeableGainInput: ChargeableGainAnswers,
                                          maxAEA: BigDecimal,
                                          incomeAnswers: IncomeAnswersModel)(implicit hc: HeaderCarrier): Future[Option[TotalGainAndTaxOwedModel]] = {
    http.GET[Option[TotalGainAndTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/calculate-resident-capital-gains-tax" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA) +
      propertyConstructor.CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)
    )
  }

  //scalastyle:off
  def getPropertyGainAnswers(implicit hc: HeaderCarrier): Future[YourAnswersSummaryModel] = {
    val disposalDate = fetchAndGetFormData[DisposalDateModel](ResidentPropertyKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))

    //This is a proposed alternate method of writing the map without needing the case statement, need a judgement on whether
    //to use this method or older ones. Fold automatically handles the None/Some cases without matching manually
    val disposalValue = fetchAndGetFormData[DisposalValueModel](ResidentPropertyKeys.disposalValue)
      .map(_.fold[Option[BigDecimal]](None)(input => Some(input.amount)))

    val worthWhenSoldForLess = fetchAndGetFormData[WorthWhenSoldForLessModel](ResidentPropertyKeys.worthWhenSoldForLess).map(_.map(_.amount))

    val whoDidYouGiveItTo = fetchAndGetFormData[WhoDidYouGiveItToModel](ResidentPropertyKeys.whoDidYouGiveItTo).map(_.map(_.option))

    val worthWhenGaveAway = fetchAndGetFormData[WorthWhenGaveAwayModel](ResidentPropertyKeys.worthWhenGaveAway).map(_.map(_.amount))

    val acquisitionValue = fetchAndGetFormData[AcquisitionValueModel](ResidentPropertyKeys.acquisitionValue).map(_.map(_.amount))

    val worthWhenInherited = fetchAndGetFormData[WorthWhenInheritedModel](ResidentPropertyKeys.worthWhenInherited).map(_.map(_.amount))

    val worthWhenGifted = fetchAndGetFormData[WorthWhenGiftedModel](ResidentPropertyKeys.worthWhenGifted).map(_.map(_.amount))

    val worthWhenBoughtForLess = fetchAndGetFormData[WorthWhenBoughtForLessModel](ResidentPropertyKeys.worthWhenBoughtForLess).map(_.map(_.amount))

    val acquisitionCosts = fetchAndGetFormData[AcquisitionCostsModel](ResidentPropertyKeys.acquisitionCosts).map(_.get.amount)

    val disposalCosts = fetchAndGetFormData[DisposalCostsModel](ResidentPropertyKeys.disposalCosts).map(_.get.amount)

    val improvements = fetchAndGetFormData[ImprovementsModel](ResidentPropertyKeys.improvements).map(_.get.amount)

    val givenAway = fetchAndGetFormData[SellOrGiveAwayModel](ResidentPropertyKeys.sellOrGiveAway).map(_.get.givenAway)

    val sellForLess = fetchAndGetFormData[SellForLessModel](ResidentPropertyKeys.sellForLess).map(_.map(_.sellForLess))

    val ownerBeforeLegislationStart = fetchAndGetFormData[OwnerBeforeLegislationStartModel](ResidentPropertyKeys.ownerBeforeLegislationStart).map(_.get.ownedBeforeLegislationStart)

    val valueBeforeLegislationStart = fetchAndGetFormData[ValueBeforeLegislationStartModel](ResidentPropertyKeys.valueBeforeLegislationStart).map(_.map(_.amount))

    val howBecameOwner = fetchAndGetFormData[HowBecameOwnerModel](ResidentPropertyKeys.howBecameOwner).map(_.map(_.gainedBy))

    val boughtForLessThanWorth = fetchAndGetFormData[BoughtForLessThanWorthModel](ResidentPropertyKeys.boughtForLessThanWorth).map(_.map(_.boughtForLessThanWorth))

    for {
      disposalDate <- disposalDate
      disposalValue <- disposalValue
      disposalCosts <- disposalCosts
      worthWhenSoldForLess <- worthWhenSoldForLess
      whoDidYouGiveItTo <- whoDidYouGiveItTo
      worthWhenGaveAway <- worthWhenGaveAway
      acquisitionValue <- acquisitionValue
      worthWhenInherited <- worthWhenInherited
      worthWhenGifted <- worthWhenGifted
      worthWhenBoughtForLess <- worthWhenBoughtForLess
      acquisitionCosts <- acquisitionCosts
      improvements <- improvements
      givenAway <- givenAway
      sellForLess <- sellForLess
      ownerBeforeLegislationStart <- ownerBeforeLegislationStart
      valueBeforeLegislationStart <- valueBeforeLegislationStart
      howBecameOwner <- howBecameOwner
      boughtForLessThanWorth <- boughtForLessThanWorth
    } yield YourAnswersSummaryModel(
      disposalDate,
      disposalValue,
      worthWhenSoldForLess,
      whoDidYouGiveItTo,
      worthWhenGaveAway,
      disposalCosts,
      acquisitionValue,
      worthWhenInherited,
      worthWhenGifted,
      worthWhenBoughtForLess,
      acquisitionCosts,
      improvements,
      givenAway,
      sellForLess,
      ownerBeforeLegislationStart,
      valueBeforeLegislationStart,
      howBecameOwner,
      boughtForLessThanWorth
    )
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        "cgt-calculator-resident-properties-frontend",
        Redirect(controllers.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }

  //scalastyle:on

  def getPropertyDeductionAnswers(implicit hc: HeaderCarrier): Future[ChargeableGainAnswers] = {
    val broughtForwardModel = fetchAndGetFormData[LossesBroughtForwardModel](ResidentPropertyKeys.lossesBroughtForward)
    val broughtForwardValueModel = fetchAndGetFormData[LossesBroughtForwardValueModel](ResidentPropertyKeys.lossesBroughtForwardValue)
    val propertyLivedInModel = fetchAndGetFormData[PropertyLivedInModel](ResidentPropertyKeys.propertyLivedIn)
    val privateResidenceReliefModel = fetchAndGetFormData[PrivateResidenceReliefModel](ResidentPropertyKeys.privateResidenceRelief)
    val privateResidenceReliefValueModel = fetchAndGetFormData[PrivateResidenceReliefValueModel](ResidentPropertyKeys.prrValue)
    val lettingsReliefModel = fetchAndGetFormData[LettingsReliefModel](ResidentPropertyKeys.lettingsRelief)
    val lettingsReliefValueModel = fetchAndGetFormData[LettingsReliefValueModel](ResidentPropertyKeys.lettingsReliefValue)

    for {
      propertyLivedIn <- propertyLivedInModel
      lettingsRelief <- lettingsReliefModel
      broughtForward <- broughtForwardModel
      broughtForwardValue <- broughtForwardValueModel
      privateResidenceRelief <- privateResidenceReliefModel
      lettingsReliefValue <- lettingsReliefValueModel
      privateResidenceReliefValue <- privateResidenceReliefValueModel
    } yield {
      ChargeableGainAnswers(
        broughtForward,
        broughtForwardValue,
        propertyLivedIn,
        privateResidenceRelief,
        privateResidenceReliefValue,
        lettingsRelief,
        lettingsReliefValue
      )
    }
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        "cgt-calculator-resident-properties-frontend",
        Redirect(controllers.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }

  def getPropertyIncomeAnswers(implicit hc: HeaderCarrier): Future[IncomeAnswersModel] = {
    val currentIncomeModel = fetchAndGetFormData[income.CurrentIncomeModel](ResidentPropertyKeys.currentIncome)
    val personalAllowanceModel = fetchAndGetFormData[income.PersonalAllowanceModel](ResidentPropertyKeys.personalAllowance)

    for {
      currentIncome <- currentIncomeModel
      personalAllowance <- personalAllowanceModel
    } yield {
      IncomeAnswersModel(currentIncome, personalAllowance)
    }
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        "cgt-calculator-resident-properties-frontend",
        Redirect(controllers.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }

  def getPropertyTotalCosts(input: YourAnswersSummaryModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/calculate-total-costs" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(input)
    )
  }
}
