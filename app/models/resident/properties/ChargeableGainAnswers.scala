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

package models.resident.properties

import models.resident._
import constructors.resident.properties.CalculateRequestConstructor._


case class ChargeableGainAnswers (otherPropertiesModel: Option[OtherPropertiesModel],
                                  allowableLossesModel: Option[AllowableLossesModel],
                                  allowableLossesValueModel: Option[AllowableLossesValueModel],
                                  broughtForwardModel: Option[LossesBroughtForwardModel],
                                  broughtForwardValueModel: Option[LossesBroughtForwardValueModel],
                                  annualExemptAmountModel: Option[AnnualExemptAmountModel],
                                  propertyLivedInModel: Option[PropertyLivedInModel],
                                  privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                                  privateResidenceReliefValueModel: Option[PrivateResidenceReliefValueModel],
                                  lettingsReliefModel: Option[LettingsReliefModel],
                                  lettingsReliefValueModel: Option[LettingsReliefValueModel]) {

  val displayPRRValueAndLettingsRelief = (propertyLivedInModel, privateResidenceReliefModel) match {
    case (Some(PropertyLivedInModel(true)), Some(PrivateResidenceReliefModel(true))) => true
    case _ => false
  }

  val displayLettingsReliefValue = (displayPRRValueAndLettingsRelief, lettingsReliefModel) match {
    case (true, Some(LettingsReliefModel(true))) => true
    case _ => false
  }

  val displayAllowableLossesValue = (otherPropertiesModel, allowableLossesModel) match {
    case (Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(true))) => true
    case _ => false
  }

  val displayAnnualExemptAmount = isUsingAnnualExemptAmount(otherPropertiesModel, allowableLossesModel, allowableLossesValueModel)

  val displayPreviousTaxableGains = (displayAnnualExemptAmount, annualExemptAmountModel) match {
    case (true, Some(AnnualExemptAmountModel(value))) if value == 0  => true
    case _ => false
  }
}
