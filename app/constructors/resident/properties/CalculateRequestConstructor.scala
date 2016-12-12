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

package constructors.resident.properties

import common.Dates._
import common.resident.HowYouBecameTheOwnerKeys._
import models.resident._
import models.resident.properties.{ChargeableGainAnswers, YourAnswersSummaryModel}

object CalculateRequestConstructor {

  val determineDisposalValueToUse: YourAnswersSummaryModel => BigDecimal = {
    case x if x.givenAway => x.worthWhenGaveAway.get
    case x if x.sellForLess.get => x.worthWhenSoldForLess.get
    case x => x.disposalValue.get
  }

  val determineAcquisitionValueToUse: YourAnswersSummaryModel => BigDecimal = {
    case x if x.ownerBeforeLegislationStart => x.valueBeforeLegislationStart.get
    case x if x.howBecameOwner.get == inheritedIt => x.worthWhenInherited.get
    case x if x.howBecameOwner.get == giftedIt => x.worthWhenGifted.get
    case x if x.boughtForLessThanWorth.get => x.worthWhenBoughtForLess.get
    case x => x.acquisitionValue.get
  }

  def totalGainRequestString (answers: YourAnswersSummaryModel): String = {
      s"?disposalValue=${determineDisposalValueToUse(answers)}" +
      s"&disposalCosts=${answers.disposalCosts}" +
      s"&acquisitionValue=${determineAcquisitionValueToUse(answers)}" +
      s"&acquisitionCosts=${answers.acquisitionCosts}" +
      s"&improvements=${answers.improvements}" +
      s"&disposalDate=${answers.disposalDate.format(requestFormatter)}"
  }

  def chargeableGainRequestString (answers: ChargeableGainAnswers, maxAEA: BigDecimal): String = {

    //Two new parameters in here the private residence relief claiming and the lettings relief claiming
    s"${if (answers.propertyLivedInModel.get.livedInProperty && answers.privateResidenceReliefModel.get.isClaiming)
      s"&prrValue=${answers.privateResidenceReliefValueModel.get.amount}"
    else ""}" +
    s"${if (answers.propertyLivedInModel.get.livedInProperty &&
      answers.privateResidenceReliefModel.get.isClaiming &&
      answers.lettingsReliefModel.get.isClaiming)
      s"&lettingReliefs=${answers.lettingsReliefValueModel.get.amount}"
    else ""}" +
    s"${if (answers.otherPropertiesModel.get.hasOtherProperties && answers.allowableLossesModel.get.isClaiming)
      s"&allowableLosses=${answers.allowableLossesValueModel.get.amount}"
    else ""}" +
    s"${if (answers.broughtForwardModel.get.option)
      s"&broughtForwardLosses=${answers.broughtForwardValueModel.get.amount}"
    else ""}" +
    s"&annualExemptAmount=${if (isUsingAnnualExemptAmount(answers.otherPropertiesModel, answers.allowableLossesModel, answers.allowableLossesValueModel)) {
      answers.annualExemptAmountModel.get.amount}
    else maxAEA}"
  }

  def incomeAnswersRequestString (deductionsAnswers: ChargeableGainAnswers, answers: IncomeAnswersModel): String ={
    s"${if (deductionsAnswers.otherPropertiesModel.get.hasOtherProperties && deductionsAnswers.annualExemptAmountModel.isDefined &&
            deductionsAnswers.annualExemptAmountModel.get.amount == 0)
      s"&previousTaxableGain=${answers.previousTaxableGainsModel.get.amount}"
      else ""}" +
    s"&previousIncome=${answers.currentIncomeModel.get.amount}" +
    s"&personalAllowance=${answers.personalAllowanceModel.get.amount}"
  }

  def isUsingAnnualExemptAmount (otherPropertiesModel: Option[OtherPropertiesModel],
                                 allowableLossesModel: Option[AllowableLossesModel],
                                 allowableLossesValueModel: Option[AllowableLossesValueModel]): Boolean = {
    (otherPropertiesModel, allowableLossesModel) match {
      case (Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(true))) if allowableLossesValueModel.get.amount == 0 => true
      case (Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(false))) => true
      case _ => false
    }
  }
}
