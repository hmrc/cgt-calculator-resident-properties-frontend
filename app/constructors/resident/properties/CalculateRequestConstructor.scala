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

  def totalGainRequest(answers: YourAnswersSummaryModel): Map[String, String] = {
    Map(
      "disposalValue" -> determineDisposalValueToUse(answers).toDouble.toString,
      "disposalCosts" -> answers.disposalCosts.toDouble.toString,
      "acquisitionValue" -> determineAcquisitionValueToUse(answers).toDouble.toString,
      "acquisitionCosts" -> answers.acquisitionCosts.toDouble.toString,
      "improvements" -> answers.improvements.toDouble.toString,
      "disposalDate" -> answers.disposalDate.format(requestFormatter)
    )
  }

  def prrValue(answers: ChargeableGainAnswers): Map[String, String] = {
    (answers.propertyLivedInModel, answers.privateResidenceReliefModel) match {
      case (Some(x), Some(y)) if x.livedInProperty && y.isClaiming =>
        Map("prrValue" -> answers.privateResidenceReliefValueModel.get.amount.toDouble.toString)
      case _ =>
        Map.empty
    }
  }

  def lettingReliefs(answers: ChargeableGainAnswers): Map[String, String] = {
    (answers.propertyLivedInModel, answers.privateResidenceReliefModel, answers.lettingsReliefModel) match {
      case (Some(x), Some(y), Some(z)) if x.livedInProperty && y.isClaiming && z.isClaiming =>
        Map("lettingReliefs" -> answers.lettingsReliefValueModel.get.amount.toDouble.toString)
      case _ =>
        Map.empty
    }
  }

  def broughtForwardLosses(answers: ChargeableGainAnswers): Map[String, String] = {
    answers.broughtForwardModel match {
      case Some(x) if x.option =>
        Map("broughtForwardLosses" -> answers.broughtForwardValueModel.get.amount.toDouble.toString)
      case _ =>
        Map.empty
    }
  }

  def chargeableGainRequest(answers: ChargeableGainAnswers, maxAEA: BigDecimal): Map[String, Any] = {
    prrValue(answers) ++ lettingReliefs(answers) ++ broughtForwardLosses(answers) ++ Map("annualExemptAmount" -> maxAEA.toDouble.toString)
  }

  def incomeAnswersRequest(deductionsAnswers: ChargeableGainAnswers, answers: IncomeAnswersModel): Map[String, Any] = {
    Map(
      "previousIncome" -> answers.currentIncomeModel.get.amount.toDouble,
      "personalAllowance" -> answers.personalAllowanceModel.get.amount.toDouble
    )
  }
}
