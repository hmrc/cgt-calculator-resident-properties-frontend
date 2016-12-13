/*
 * Copyright 2016 HM Revenue & Customs
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

import common.Dates._
import common.KeystoreKeys.ResidentPropertyKeys
import config.{CalculatorSessionCache, WSHttp}
import constructors.resident.{properties => propertyConstructor}
import models._
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CalculatorConnector extends CalculatorConnector with ServicesConfig {
  override val sessionCache = CalculatorSessionCache
  override val http = WSHttp
  override val serviceUrl = baseUrl("capital-gains-calculator")
}

trait CalculatorConnector {

  val sessionCache: SessionCache
  val http: HttpGet
  val serviceUrl: String

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

  def saveFormData[T](key: String, data: T)(implicit hc: HeaderCarrier, formats: Format[T]): Future[CacheMap] = {
    sessionCache.cache(key, data)
  }

  def fetchAndGetFormData[T](key: String)(implicit hc: HeaderCarrier, formats: Format[T]): Future[Option[T]] = {
    sessionCache.fetchAndGetEntry(key)
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

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[resident.TaxYearModel]] = {
    http.GET[Option[resident.TaxYearModel]](s"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear")
  }

  def clearKeystore(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    sessionCache.remove()
  }

  //Rtt property calculation methods
  def calculateRttPropertyGrossGain(input: resident.properties.YourAnswersSummaryModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/calculate-total-gain" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(input)
    )
  }

  def calculateRttPropertyChargeableGain(totalGainInput: resident.properties.YourAnswersSummaryModel,
                                         chargeableGainInput: resident.properties.ChargeableGainAnswers,
                                         maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[resident.ChargeableGainResultModel]] = {
    http.GET[Option[resident.ChargeableGainResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-chargeable-gain" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)

    )
  }

  def calculateRttPropertyTotalGainAndTax(totalGainInput: resident.properties.YourAnswersSummaryModel,
                                          chargeableGainInput: resident.properties.ChargeableGainAnswers,
                                          maxAEA: BigDecimal,
                                          incomeAnswers: resident.IncomeAnswersModel)(implicit hc: HeaderCarrier): Future[Option[resident.TotalGainAndTaxOwedModel]] = {
    http.GET[Option[resident.TotalGainAndTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/calculate-resident-capital-gains-tax" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA) +
      propertyConstructor.CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)
    )
  }

  //scalastyle:off
  def getPropertyGainAnswers(implicit hc: HeaderCarrier): Future[resident.properties.YourAnswersSummaryModel] = {
    val disposalDate = fetchAndGetFormData[resident.DisposalDateModel](ResidentPropertyKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))

    //This is a proposed alternate method of writing the map without needing the case statement, need a judgement on whether
    //to use this method or older ones. Fold automatically handles the None/Some cases without matching manually
    val disposalValue = fetchAndGetFormData[resident.DisposalValueModel](ResidentPropertyKeys.disposalValue)
      .map(_.fold[Option[BigDecimal]](None)(input => Some(input.amount)))

    val worthWhenSoldForLess = fetchAndGetFormData[resident.WorthWhenSoldForLessModel](ResidentPropertyKeys.worthWhenSoldForLess).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val whoDidYouGiveItTo = fetchAndGetFormData[resident.properties.gain.WhoDidYouGiveItToModel](ResidentPropertyKeys.whoDidYouGiveItTo).map {
      case Some(data) => Some(data.option)
      case _ => None
    }

    val worthWhenGaveAway = fetchAndGetFormData[resident.properties.WorthWhenGaveAwayModel](ResidentPropertyKeys.worthWhenGaveAway).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val acquisitionValue = fetchAndGetFormData[resident.AcquisitionValueModel](ResidentPropertyKeys.acquisitionValue).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val worthWhenInherited = fetchAndGetFormData[resident.WorthWhenInheritedModel](ResidentPropertyKeys.worthWhenInherited).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val worthWhenGifted = fetchAndGetFormData[resident.properties.gain.WorthWhenGiftedModel](ResidentPropertyKeys.worthWhenGifted).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val worthWhenBoughtForLess = fetchAndGetFormData[resident.properties.WorthWhenBoughtForLessModel](ResidentPropertyKeys.worthWhenBoughtForLess).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val acquisitionCosts = fetchAndGetFormData[resident.AcquisitionCostsModel](ResidentPropertyKeys.acquisitionCosts).map(_.get.amount)
    val disposalCosts = fetchAndGetFormData[resident.DisposalCostsModel](ResidentPropertyKeys.disposalCosts).map(_.get.amount)
    val improvements = fetchAndGetFormData[resident.properties.ImprovementsModel](ResidentPropertyKeys.improvements).map(_.get.amount)
    val givenAway = fetchAndGetFormData[resident.properties.SellOrGiveAwayModel](ResidentPropertyKeys.sellOrGiveAway).map(_.get.givenAway)
    val sellForLess = fetchAndGetFormData[resident.SellForLessModel](ResidentPropertyKeys.sellForLess).map {
      case Some(data) => Some(data.sellForLess)
      case _ => None
    }
    val ownerBeforeLegislationStart = fetchAndGetFormData[resident.properties.gain.OwnerBeforeLegislationStartModel](ResidentPropertyKeys.ownerBeforeLegislationStart)
      .map(_.get.ownedBeforeLegislationStart)
    val valueBeforeLegislationStart = fetchAndGetFormData[resident.properties.ValueBeforeLegislationStartModel](ResidentPropertyKeys.valueBeforeLegislationStart).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val howBecameOwner = fetchAndGetFormData[resident.properties.HowBecameOwnerModel](ResidentPropertyKeys.howBecameOwner).map {
      case Some(data) => Some(data.gainedBy)
      case None => None
    }
    val boughtForLessThanWorth = fetchAndGetFormData[resident.properties.BoughtForLessThanWorthModel](ResidentPropertyKeys.boughtForLessThanWorth).map {
      case Some(data) => Some(data.boughtForLessThanWorth)
      case None => None
    }

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
    } yield resident.properties.YourAnswersSummaryModel(
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
  }

  //scalastyle:on

  def getPropertyDeductionAnswers(implicit hc: HeaderCarrier): Future[resident.properties.ChargeableGainAnswers] = {
    val otherPropertiesModel = fetchAndGetFormData[resident.OtherPropertiesModel](ResidentPropertyKeys.otherProperties)
    val allowableLossesModel = fetchAndGetFormData[resident.AllowableLossesModel](ResidentPropertyKeys.allowableLosses)
    val allowableLossesValueModel = fetchAndGetFormData[resident.AllowableLossesValueModel](ResidentPropertyKeys.allowableLossesValue)
    val broughtForwardModel = fetchAndGetFormData[resident.LossesBroughtForwardModel](ResidentPropertyKeys.lossesBroughtForward)
    val broughtForwardValueModel = fetchAndGetFormData[resident.LossesBroughtForwardValueModel](ResidentPropertyKeys.lossesBroughtForwardValue)
    val annualExemptAmountModel = fetchAndGetFormData[resident.AnnualExemptAmountModel](ResidentPropertyKeys.annualExemptAmount)
    val propertyLivedInModel = fetchAndGetFormData[resident.properties.PropertyLivedInModel](ResidentPropertyKeys.propertyLivedIn)
    val privateResidenceReliefModel = fetchAndGetFormData[resident.PrivateResidenceReliefModel](ResidentPropertyKeys.privateResidenceRelief)
    val privateResidenceReliefValueModel = fetchAndGetFormData[resident.properties.PrivateResidenceReliefValueModel](ResidentPropertyKeys.prrValue)
    val lettingsReliefModel = fetchAndGetFormData[resident.properties.LettingsReliefModel](ResidentPropertyKeys.lettingsRelief)
    val lettingsReliefValueModel = fetchAndGetFormData[resident.properties.LettingsReliefValueModel](ResidentPropertyKeys.lettingsReliefValue)

    for {
      propertyLivedIn <- propertyLivedInModel
      lettingsRelief <- lettingsReliefModel
      otherProperties <- otherPropertiesModel
      allowableLosses <- allowableLossesModel
      allowableLossesValue <- allowableLossesValueModel
      broughtForward <- broughtForwardModel
      broughtForwardValue <- broughtForwardValueModel
      annualExemptAmount <- annualExemptAmountModel
      privateResidenceRelief <- privateResidenceReliefModel
      lettingsReliefValue <- lettingsReliefValueModel
      privateResidenceReliefValue <- privateResidenceReliefValueModel
    } yield {
      resident.properties.ChargeableGainAnswers(
        otherProperties,
        allowableLosses,
        allowableLossesValue,
        broughtForward,
        broughtForwardValue,
        annualExemptAmount,
        propertyLivedIn,
        privateResidenceRelief,
        privateResidenceReliefValue,
        lettingsRelief,
        lettingsReliefValue
      )
    }

  }

  def getPropertyIncomeAnswers(implicit hc: HeaderCarrier): Future[resident.IncomeAnswersModel] = {
    val previousTaxableGainsModel = fetchAndGetFormData[resident.income.PreviousTaxableGainsModel](ResidentPropertyKeys.previousTaxableGains)
    val currentIncomeModel = fetchAndGetFormData[resident.income.CurrentIncomeModel](ResidentPropertyKeys.currentIncome)
    val personalAllowanceModel = fetchAndGetFormData[resident.income.PersonalAllowanceModel](ResidentPropertyKeys.personalAllowance)

    for {
      previousGains <- previousTaxableGainsModel
      currentIncome <- currentIncomeModel
      personalAllowance <- personalAllowanceModel
    } yield {
      resident.IncomeAnswersModel(previousGains, currentIncome, personalAllowance)
    }
  }
}