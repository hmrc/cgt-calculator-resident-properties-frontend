/*
 * Copyright 2022 HM Revenue & Customs
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

import common.Transformers._
import common.Validation._
import models.resident.income.PersonalAllowanceModel
import play.api.data.Forms._
import play.api.data._
import common.Formatters.text
import common.resident.MoneyPounds
import models.resident.TaxYearModel

object PersonalAllowanceForm {

  def validateMaxPA(maxPersonalAllowance: BigDecimal): BigDecimal => Boolean = {
    input => if (input > maxPersonalAllowance) false else true
  }

  def personalAllowanceForm(taxYear: TaxYearModel, maxPA: BigDecimal = BigDecimal(0)): Form[PersonalAllowanceModel] = Form(
    mapping(
      "amount" -> text("calc.resident.personalAllowance.mandatoryAmount", taxYear.startYear, taxYear.endYear)
        .verifying(constraintBuilder("calc.resident.personalAllowance.mandatoryAmount", taxYear.startYear, taxYear.endYear) {
          mandatoryCheck
        })
        .verifying(constraintBuilder("calc.resident.personalAllowance.invalidAmount", taxYear.startYear, taxYear.endYear) {
          bigDecimalCheck
        })
        .transform[BigDecimal](stringToBigDecimal, _.toString())
        .verifying(constraintBuilder("calc.resident.personalAllowance.maximumAmount", taxYear.startYear, taxYear.endYear, MoneyPounds(maxPA).quantity) {
          validateMaxPA(maxPA)
        })
        .verifying(constraintBuilder("calc.resident.personalAllowance.minimumAmount", taxYear.startYear, taxYear.endYear) {
          isPositive
        })
        .verifying(constraintBuilder("calc.resident.personalAllowance.invalidAmount", taxYear.startYear, taxYear.endYear) {
          decimalPlacesCheckNoDecimal
        })
    )(PersonalAllowanceModel.apply)(PersonalAllowanceModel.unapply)
  )

}
