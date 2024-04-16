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

import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.TableFor2
import play.api.data.validation.{Invalid, Valid, ValidationError}

import java.time.LocalDate

class ValidationSpec extends CommonPlaySpec {

  "calling common.Validation.isValidDate(day, month, year)" in {
    val table = Table(
      ("day", "month", "year", "expected"),
      (0, 1, 2016, false),
      (1, 0, 2016, false),
      (32, 1, 2016, false),
      (29, 2, 2017, false),
      (29, 2, 2016, true),
      (12, 9, 1990, true),
    )
    forAll(table) { (day, month, year, expected) => Validation.isValidDate(day, month, year) shouldBe expected }
  }

  "calling common.Validation.isPositive(amount)" in {
    val table = Table(
      ("input", "expected"),
      (1, true),
      (0, true),
      (-1, false),
    )
    forAll(table) { (input, expected) => Validation.isPositive(input) shouldBe expected }
  }

  "calling common.Validation.decimalPlacesCheck(amount)" in {
    val table = Table(
      ("input", "expected"),
      (1.0, true),
      (1.1, true),
      (1.11, true),
      (1.1111, false),
    )
    forAll(table) { (input, expected) => Validation.decimalPlacesCheck(input) shouldBe expected }
  }

  "calling common.Validation.isGreaterThanMaxNumeric(amount)" in {
    val table = Table(
      ("input", "expected"),
      (1000000000.0, true),
      (1000000000.01, false),
      (999999999.99, true),
    )
    forAll(table) { (input, expected) => Validation.maxCheck(input) shouldBe expected }
  }

  "calling common.Validation.yesNoCheck" in {
    val table = Table(
      ("input", "expected"),
      ("a", false),
      ("Yes", true),
      ("No", true),
    )
    forAll(table) { (input, expected) => Validation.yesNoCheck(input) shouldBe expected }
  }

  "calling bigDecimalCheck" in {
    val table = Table(
      ("input", "expected"),
      ("abc", false),
      ("", true),
      ("   ", true),
      ("123", true),
    )
    forAll(table) { (input, expected) => Validation.bigDecimalCheck(input) shouldBe expected }
  }

  "calling mandatoryCheck" in {
    val table = Table(
      ("input", "expected"),
      ("", false),
      ("    ", false),
      ("123", true),
    )
    forAll(table) { (input, expected) => Validation.mandatoryCheck(input) shouldBe expected }
  }

  "calling decimalPlacesCheck" in {
    val table = Table(
      ("input", "expected"),
      (1.0, true),
      (1.1, true),
      (1.11, true),
      (1.111, false),
    )
    forAll(table) { (input, expected) => Validation.decimalPlacesCheck(BigDecimal(input)) shouldBe expected }
  }

  "calling maxCheck" in {
    val table = Table(
      ("input", "expected"),
      (900000000.99999, true),
      (1000000000.0, true),
      (1000000001.0, false),
    )
    forAll(table) { (input, expected) => Validation.maxCheck(BigDecimal(input)) shouldBe expected }
  }

  "calling isPositive" in {
    val table = Table(
      ("input", "expected"),
      (0.01, true),
      (0.0, true),
      (-0.01, false),
    )
    forAll(table) { (input, expected) => Validation.isPositive(BigDecimal(input)) shouldBe expected }
  }
  
  "calling yesNoCheck" in {
    val table = Table(
      ("input", "expected"),
      ("Yes", true),
      ("No", true),
      ("", true),
      ("yEs", false),
      ("nO", false),
      ("    ", false),
    )
    forAll(table) { (input, expected) => Validation.yesNoCheck(input) shouldBe expected }
  }

  "Calling .optionalMandatoryCheck" in {
    val table = Table(
      ("input", "expected"),
      (Some(" "), false),
      (None, false),
      (Some("test"), true),
    )
    forAll(table) { (input, expected) => Validation.optionalMandatoryCheck(input) shouldBe expected }
  }

  "Calling .optionalYesNoCheck" in {
    val table: TableFor2[Option[String], Boolean] = Table(
      ("input", "expected"),
      (None, true),
      (Some(""), true),
      (Some("Yes"), true),
      (Some("No"), true),
      (Some("test"), false),
    )
    forAll(table) { (input, expected) => Validation.optionalYesNoCheck(input) shouldBe expected }
  }

  "Calling .dateNotBeforeMinimum" in {
    val table = Table(
      ("day", "month", "year", "date", "expected"),
      (6, 4, 2015, LocalDate.parse("2015-04-05"), Valid),
      (100, 4, 2015, LocalDate.parse("2015-04-05"), Invalid(List(ValidationError(List("calc.common.date.error.beforeMinimum"),"5 4 2015")))),
      (5, 4, 2015, LocalDate.parse("2015-04-05"), Invalid(List(ValidationError(List("calc.common.date.error.beforeMinimum"),"5 4 2015")))),
      (4, 4, 2015, LocalDate.parse("2015-04-05"), Invalid(List(ValidationError(List("calc.common.date.error.beforeMinimum"),"5 4 2015")))),
      (6, 4, 2015, LocalDate.parse("2015-04-08"), Invalid(List(ValidationError(List("calc.common.date.error.beforeMinimum"),"8 4 2015")))),
    )
    forAll(table) { (day, month, year, date, expected) => Validation.dateAfterMinimum(day, month, year, date) shouldBe expected }
  }
}
