/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.resident.properties.gain

import common.Constants
import common.Transformers._
import common.Validation._
import models.resident.properties.gain.WorthWhenGiftedModel
import play.api.data.Form
import play.api.data.Forms._
import common.Formatters.text
import common.resident.MoneyPounds

object WorthWhenGiftedForm {

  val worthWhenGiftedForm = Form(
    mapping(
      "amount" -> text("calc.resident.properties.worthWhenGifted.mandatoryAmount")
        .transform(stripCurrencyCharacters, stripCurrencyCharacters)
        .verifying("calc.resident.properties.worthWhenGifted.mandatoryAmount", mandatoryCheck)
        .verifying("calc.resident.properties.worthWhenGifted.invalidAmount", bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying(constraintBuilder("calc.resident.properties.worthWhenGifted.maximumAmount", MoneyPounds(Constants.maxNumeric, 0).quantity) { maxCheck })
        .verifying("calc.resident.properties.worthWhenGifted.minimumAmount", isPositive)
        .verifying("calc.resident.properties.worthWhenGifted.invalidAmount", decimalPlacesCheck)
    )(WorthWhenGiftedModel.apply)(WorthWhenGiftedModel.unapply)
  )
}
