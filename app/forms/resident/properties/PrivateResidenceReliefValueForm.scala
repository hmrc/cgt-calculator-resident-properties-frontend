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

import common.Formatters.text
import common.Transformers._
import common.Validation._
import common.resident.MoneyPounds
import models.resident.properties.PrivateResidenceReliefValueModel
import play.api.data.Forms._
import play.api.data._

object PrivateResidenceReliefValueForm {

  def privateResidenceReliefValueForm(gain: BigDecimal): Form[PrivateResidenceReliefValueModel] = Form(
    mapping(
      "amount" -> text("calc.resident.properties.privateResidenceReliefValue.mandatoryAmount")
        .transform(stripCurrencyCharacters, stripCurrencyCharacters)
        .verifying("calc.resident.properties.privateResidenceReliefValue.mandatoryAmount", mandatoryCheck)
        .verifying("calc.resident.properties.privateResidenceReliefValue.invalidAmount", bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying(constraintBuilder[BigDecimal]("calc.resident.properties.privateResidenceReliefValue.gainExceededError", MoneyPounds(gain, 0).quantity) {
          maxPRRCheck(gain)
        })
        .verifying("calc.resident.properties.privateResidenceReliefValue.minimumAmount", isPositive)
        .verifying("calc.resident.properties.privateResidenceReliefValue.error.decimalPlaces", decimalPlacesCheck)
    )(PrivateResidenceReliefValueModel.apply)(o=>Some(o.amount))
  )
}
