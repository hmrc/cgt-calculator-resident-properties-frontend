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

package common

import common.Dates.constructDate
import common.resident.MoneyPounds
import models.resident.DisposalDateModel
import play.api.data.validation._

import java.time.{LocalDate, ZonedDateTime}
import scala.util.{Failure, Success, Try}

object Validation {

  def dateAfterMinimumConstraint(minDate: ZonedDateTime): Constraint[DisposalDateModel] = Constraint({
    value => dateAfterMinimum(value.day, value.month, value.year, minDate.toLocalDate)
  })

  def maxMonetaryValueConstraint(
                                  maxValue: BigDecimal = Constants.maxNumeric,
                                  errMsgKey: String = "calc.common.error.maxAmountExceeded"
                                ): Constraint[BigDecimal] = Constraint("constraints.maxValue")({
    value => maxMoneyCheck(value, maxValue, errMsgKey)
  })

  private def maxMoneyCheck(value: BigDecimal, maxValue: BigDecimal, errMsgKey: String): ValidationResult = {
    if (value <= maxValue) {
      Valid
    } else {
      Invalid(ValidationError(errMsgKey, MoneyPounds(maxValue, 0).quantity))
    }
  }

  def constraintBuilder[A](key: String, args: String*)(condition: A => Boolean): Constraint[A] = {
    Constraint(input => if (condition(input)) {
      Valid
    } else {
      Invalid(ValidationError(key, args*))
    })
  }

  def dateAfterMinimum(day: Int, month: Int, year: Int, minimumDate: LocalDate): ValidationResult = {
    if(isValidDate(day, month, year) && constructDate(day, month, year).isAfter(minimumDate)){
      Valid
    } else {
      Invalid(ValidationError("calc.common.date.error.beforeMinimum", s"${minimumDate.getDayOfMonth} ${minimumDate.getMonthValue} ${minimumDate.getYear}"))
    }
  }

  def isValidDate(day: Int, month: Int, year: Int): Boolean = Try(constructDate(day, month, year)) match {
    case Success(_) => true
    case _ => false
  }

  val bigDecimalCheck: String => Boolean = input => Try(BigDecimal(input)) match {
    case Success(_) => true
    case Failure(_) if input.trim == "" => true
    case Failure(_) => false
  }

  val integerCheck: String => Boolean = input => Try(input.trim.toInt) match {
    case Success(_) => true
    case Failure(_) if input.trim == "" => true
    case Failure(_) => false
  }

  val mandatoryCheck: String => Boolean = input => input.trim != ""

  val optionalMandatoryCheck: Option[String] => Boolean = {
    case Some(input) => mandatoryCheck(input)
    case _ => false
  }

  val decimalPlacesCheck: BigDecimal => Boolean = input => input.scale < 3

  val decimalPlacesCheckNoDecimal: BigDecimal => Boolean = input => input.scale < 1

  val validYearRangeCheck: Int => Boolean = input => input >= 1900 && input <= 9999

  val maxCheck: BigDecimal => Boolean = input => input <= Constants.maxNumeric

  def maxPRRCheck(gain: BigDecimal): BigDecimal => Boolean = input => input <= gain

  val isPositive: BigDecimal => Boolean = input => input >= 0

  val yesNoCheck: String => Boolean = {
    case "Yes" => true
    case "No" => true
    case "" => true
    case _ => false
  }

  val optionalYesNoCheck: Option[String] => Boolean = {
    case Some(input) => yesNoCheck(input)
    case _ => true
  }

  val givenAwayCheck: String => Boolean = {
    case "Given" => true
    case "Sold" => true
    case "" => true
    case _ => false
  }

  val howBecameOwnerCheck: String => Boolean = {
    case "Bought" => true
    case "Gifted" => true
    case "Inherited" => true
    case "" => true
    case _ => false
  }

  val whoDidYouGiveItToCheck: String => Boolean = {
    case "Spouse" => true
    case "Charity" => true
    case "Other" => true
    case "" => true
    case _ => false
  }
}
