/*
 * Copyright 2017 HM Revenue & Customs
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

import java.time.LocalDate

import models.resident.DisposalDateModel
import play.api.data.Forms._
import play.api.data._
import common.Validation._
import common.Transformers._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object DisposalDateForm {

  def disposalDateForm(minimumDate: LocalDate = LocalDate.now()): Form[DisposalDateModel] = Form(
    mapping(
      "disposalDateDay" -> text
        .verifying(Messages("calc.resident.disposalDate.invalidDayError"), mandatoryCheck)
        .verifying(Messages("calc.resident.disposalDate.invalidDayError"), integerCheck)
        .transform[Int](stringToInteger, _.toString),
      "disposalDateMonth" -> text
        .verifying(Messages("calc.resident.disposalDate.invalidMonthError"), mandatoryCheck)
        .verifying(Messages("calc.resident.disposalDate.invalidMonthError"), integerCheck)
        .transform[Int](stringToInteger, _.toString),
      "disposalDateYear" -> text
        .verifying(Messages("calc.resident.disposalDate.invalidYearError"), mandatoryCheck)
        .verifying(Messages("calc.resident.disposalDate.invalidYearError"), integerCheck)
        .transform[Int](stringToInteger, _.toString)
        .verifying(Messages("calc.resident.disposalDate.invalidYearRangeError"), validYearRangeCheck)
    )(DisposalDateModel.apply)(DisposalDateModel.unapply)
      .verifying(Messages("calc.common.date.error.invalidDate"), fields => isValidDate(fields.day, fields.month, fields.year))
      .verifying(Messages("calc.common.date.error.beforeMinimum", s"${minimumDate.getDayOfMonth} ${minimumDate.getMonthValue} ${minimumDate.getYear}"),
        fields => dateNotBeforeMinimum(fields.day, fields.month, fields.year, minimumDate))
  )
}
