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

package forms.resident.properties

import common.Constants._
import common.Formatters.text
import common.Transformers._
import common.Validation._
import common.resident.MoneyPounds
import models.resident.properties.LettingsReliefValueModel
import play.api.data.Forms._
import play.api.data._

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
      "amount" -> text("calc.resident.lettingsReliefValue.mandatoryAmount")
        .transform(stripCurrencyCharacters, stripCurrencyCharacters)
        .verifying("calc.resident.lettingsReliefValue.mandatoryAmount", mandatoryCheck)
        .verifying("calc.resident.lettingsReliefValue.invalidAmount", bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying("calc.resident.lettingsReliefValue.minimumAmount", isPositive)
        .verifying("calc.resident.lettingsReliefValue.error.decimalPlaces", decimalPlacesCheck)
        .verifying(constraintBuilder[BigDecimal]("calc.resident.lettingsReliefValue.error.moreThanCappedAmount", MoneyPounds(maxLettingsRelief, 0).quantity) { x =>
          displayMaxLettingsRelief(x, prrValue, gain - prrValue)
        })
        .verifying(constraintBuilder[BigDecimal]("calc.resident.lettingsReliefValue.error.moreThanPrr", MoneyPounds(prrValue, 0).quantity) { x =>
          displayGreaterThanPrr(x, prrValue, gain - prrValue)
        })
        .verifying(constraintBuilder[BigDecimal]("calc.resident.lettingsReliefValue.error.moreThanRemainingGain", MoneyPounds(gain - prrValue, 0).quantity) { x =>
          displayGreaterThanRemainingGain(x, prrValue, gain - prrValue)
        })
    )(LettingsReliefValueModel.apply)(o=>Some(o.amount)))
}
