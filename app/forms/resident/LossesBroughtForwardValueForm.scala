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

package forms.resident

import common.Constants
import common.Formatters.text
import common.Transformers._
import common.Validation._
import common.resident.MoneyPounds
import models.resident.{LossesBroughtForwardValueModel, TaxYearModel}
import play.api.data.Forms._
import play.api.data._

object LossesBroughtForwardValueForm {

  def lossesBroughtForwardValueForm(taxYear: TaxYearModel):Form[LossesBroughtForwardValueModel] = Form(
    mapping(
      "amount" -> text("calc.resident.lossesBroughtForward.errorSelect")
        .transform(stripCurrencyCharacters, stripCurrencyCharacters)
        .verifying(constraintBuilder[String]("calc.resident.lossesBroughtForwardValue.mandatoryAmount", taxYear.startYear, taxYear.endYear) {
          mandatoryCheck
        })
        .verifying(constraintBuilder[String]("calc.resident.lossesBroughtForwardValue.invalidAmount", taxYear.startYear, taxYear.endYear) {
          bigDecimalCheck
        })
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying(constraintBuilder("calc.resident.lossesBroughtForwardValue.maximumAmount", MoneyPounds(Constants.maxNumeric, 0).quantity) { maxCheck })
        .verifying("calc.resident.lossesBroughtForwardValue.minimumAmount", isPositive)
        .verifying(constraintBuilder("calc.resident.lossesBroughtForwardValue.error.decimalPlaces", taxYear.startYear, taxYear.endYear) {
          decimalPlacesCheck
        })
    )(LossesBroughtForwardValueModel.apply)(o=>Some(o.amount))
  )

}
