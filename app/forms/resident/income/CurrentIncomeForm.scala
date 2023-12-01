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

package forms.resident.income

import common.Constants
import common.Transformers._
import common.Validation._
import models.resident.income.CurrentIncomeModel
import play.api.data.Forms._
import play.api.data._
import common.Formatters.text
import common.resident.MoneyPounds
import models.resident.TaxYearModel

object CurrentIncomeForm {

  def currentIncomeForm(taxYear: TaxYearModel):Form[CurrentIncomeModel] = Form(
    mapping(
      "amount" -> text("calc.resident.currentIncome.mandatoryAmount", taxYear.startYear, taxYear.endYear)
        .transform(stripCurrencyCharacters, stripCurrencyCharacters)
        .verifying(constraintBuilder[String]("calc.resident.currentIncome.mandatoryAmount", taxYear.startYear, taxYear.endYear){
          mandatoryCheck
        })
        .verifying(constraintBuilder[String]("calc.resident.currentIncome.invalidAmount", taxYear.startYear, taxYear.endYear) {
          bigDecimalCheck
        })
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying(constraintBuilder[BigDecimal]("calc.resident.currentIncome.maximumAmount", taxYear.startYear, taxYear.endYear, MoneyPounds(Constants.maxNumeric, 0).quantity){
          maxCheck
        })
        .verifying(constraintBuilder[BigDecimal]("calc.resident.currentIncome.minimumAmount", taxYear.startYear, taxYear.endYear){
          isPositive
        })
        .verifying(constraintBuilder[BigDecimal]("calc.resident.currentIncome.invalidAmount", taxYear.startYear, taxYear.endYear){
          decimalPlacesCheck
        })
    )(CurrentIncomeModel.apply)(CurrentIncomeModel.unapply)
  )
}
