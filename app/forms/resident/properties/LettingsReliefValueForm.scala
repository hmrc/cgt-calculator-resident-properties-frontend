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

package forms.resident.properties

import common.Transformers._
import common.Validation._
import common.Constants._
import play.api.data.Forms._
import play.api.data._
import models.resident.properties.LettingsReliefValueModel
import play.api.i18n.Messages

import scala.math._

object LettingsReliefValueForm {

  def displayMaxLettingsRelief(amount: BigDecimal, prr: BigDecimal, remainingGain: BigDecimal): Boolean =
    !(amount > maxLettingsRelief && prr >= maxLettingsRelief && remainingGain >= maxLettingsRelief)

  def displayGreaterThanPrr(amount: BigDecimal, prr: BigDecimal, remainingGain: BigDecimal): Boolean =
    !(amount > prr && remainingGain >= prr && maxLettingsRelief >= prr)

  def displayGreaterThanRemainingGain(amount: BigDecimal, prr: BigDecimal, remainingGain: BigDecimal): Boolean =
    !(amount > remainingGain && remainingGain <= prr && remainingGain <= maxLettingsRelief)

  def lettingsReliefValueForm(gain: BigDecimal, prrValue: BigDecimal): Form[LettingsReliefValueModel] =
    Form(mapping(
      "amount" -> text
        .verifying(Messages("calc.common.error.mandatoryAmount"), mandatoryCheck)
        .verifying(Messages("calc.common.error.invalidAmount"), bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying(Messages("calc.common.error.minimumAmount"), isPositive)
        .verifying(Messages("calc.common.error.invalidAmount"), decimalPlacesCheck)
        .verifying(Messages("calc.resident.lettingsReliefValue.error.moreThanCappedAmount", maxLettingsRelief), x =>
          displayMaxLettingsRelief(x, prrValue, gain - prrValue))
        .verifying(Messages("calc.resident.lettingsReliefValue.error.moreThanPrr", prrValue), x =>
          displayGreaterThanPrr(x, prrValue, gain - prrValue))
        .verifying(Messages("calc.resident.lettingsReliefValue.error.moreThanRemainingGain", gain - prrValue), x =>
          displayGreaterThanRemainingGain(x, prrValue, gain - prrValue))
    )(LettingsReliefValueModel.apply)(LettingsReliefValueModel.unapply))
}
