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

package forms.resident

import common.Constants
import common.Transformers._
import common.Validation._
import models.resident.DisposalValueModel
import play.api.data.Forms._
import play.api.data._
import common.Formatters.text
import common.resident.MoneyPounds

object DisposalValueForm {

  val disposalValueForm = Form(
    mapping(
      "amount" -> text("calc.resident.disposal.value.mandatoryAmount")
        .transform(stripCurrencyCharacters, stripCurrencyCharacters)
        .verifying("calc.resident.disposal.value.mandatoryAmount", mandatoryCheck)
        .verifying("calc.resident.disposal.value.invalidAmount", bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying(constraintBuilder("calc.resident.disposal.value.maximumAmount", MoneyPounds(Constants.maxNumeric, 0).quantity){
          maxCheck
        })
        .verifying("calc.resident.disposal.value.minimumAmount", isPositive)
        .verifying("calc.resident.disposal.value.invalidAmount", decimalPlacesCheck)
    )(DisposalValueModel.apply)(DisposalValueModel.unapply)
  )
}
