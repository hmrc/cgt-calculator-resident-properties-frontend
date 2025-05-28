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

import common.Formatters.text
import common.Transformers._
import common.Validation._
import models.resident.{LossesBroughtForwardModel, TaxYearModel}
import play.api.data.Forms._
import play.api.data._

object LossesBroughtForwardForm {

  def lossesBroughtForwardForm(taxYear: TaxYearModel): Form[LossesBroughtForwardModel] = Form(
    mapping(
      "option" -> text("calc.resident.lossesBroughtForward.errorSelect", TaxYearModel.convertToSummaryFormat(taxYear.taxYearSupplied)(0), TaxYearModel.convertToSummaryFormat(taxYear.taxYearSupplied)(1))
          .verifying(constraintBuilder[String]("calc.resident.lossesBroughtForward.errorSelect", TaxYearModel.convertToSummaryFormat(taxYear.taxYearSupplied)(0), TaxYearModel.convertToSummaryFormat(taxYear.taxYearSupplied)(1)) {
            mandatoryCheck
          })
          .verifying(constraintBuilder[String]("calc.resident.lossesBroughtForward.errorSelect", TaxYearModel.convertToSummaryFormat(taxYear.taxYearSupplied)(0), TaxYearModel.convertToSummaryFormat(taxYear.taxYearSupplied)(1)) {
            yesNoCheck
          })
        .transform[Boolean](stringToBoolean, booleanToString)
    )(LossesBroughtForwardModel.apply)(o=>Some(o.option))
  )
}
