/*
 * Copyright 2018 HM Revenue & Customs
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

import common.Constants
import common.Validation._
import common.Transformers._
import models.resident.properties.ValueBeforeLegislationStartModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object ValueBeforeLegislationStartForm {

  val valueBeforeLegislationStartForm = Form(
    mapping(
      "amount" -> text
        .verifying("calc.common.error.mandatoryAmount", mandatoryCheck)
        .verifying("calc.common.error.invalidAmount", bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, bigDecimalToString)
        .verifying("calc.common.error.maxAmountExceeded" + s" £${MoneyPounds(Constants.maxNumeric, 0).quantity} " + "calc.common.error.maxAmountExceeded.orLess", maxCheck)
        .verifying("calc.common.error.minimumAmount", isPositive)
        .verifying("calc.common.error.invalidAmount", decimalPlacesCheck)
    )(ValueBeforeLegislationStartModel.apply)(ValueBeforeLegislationStartModel.unapply)
  )
}
