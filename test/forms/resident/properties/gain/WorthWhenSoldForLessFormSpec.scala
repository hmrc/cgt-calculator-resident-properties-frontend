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

package forms.resident.properties.gain

import assets.MessageLookup.{Resident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.resident.WorthWhenSoldForLessForm._
import models.resident.WorthWhenSoldForLessModel

class WorthWhenSoldForLessFormSpec extends CommonPlaySpec with WithCommonFakeApplication {

  "Creating a form using an empty model" should {

    lazy val form = worthWhenSoldForLessForm

    "return an empty string for amount" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      lazy val form = worthWhenSoldForLessForm.fill(WorthWhenSoldForLessModel(1))
      form.data("amount") shouldBe "1"
    }

    "return a form with the data specified from the map" in {
      lazy val form = worthWhenSoldForLessForm.bind(Map("amount" -> "1"))
      form.data("amount") shouldBe "1"
    }
  }

  "Creating a form using an invalid post" when {

    "supplied with no data for amount" should {

      lazy val form = worthWhenSoldForLessForm.bind(Map("amount" -> ""))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise only one error" in {
        form.errors.length shouldEqual 1
      }

      s"error with message '${messages.Errors.worthWhenSoldForLessMandatoryAmount}'" in {
        form.error("amount").get.message shouldBe messages.Errors.worthWhenSoldForLessMandatoryAmount
      }
    }

    "supplied with a non-numeric value for amount" should {

      lazy val form = worthWhenSoldForLessForm.bind(Map("amount" -> "a"))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise only one error" in {
        form.errors.length shouldEqual 1
      }

      s"error with message '${messages.Errors.worthWhenSoldForLessInvalidAmount}'" in {
        form.error("amount").get.message shouldBe messages.Errors.worthWhenSoldForLessInvalidAmount
      }
    }

    "supplied with an amount that is too big" should {
      lazy val form = worthWhenSoldForLessForm.bind(Map(("amount", "9999999999999")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise only one error" in {
        form.errors.length shouldEqual 1
      }

      s"return a form with the error message ${messages.Errors.worthWhenSoldForLessMaximumAmount}" in {
        form.error("amount").get.message shouldBe messages.Errors.worthWhenSoldForLessMaximumAmount
      }
    }

    "supplied with a negative amount" should {

      lazy val form = worthWhenSoldForLessForm.bind(Map("amount" -> "-1000"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "raise only one error" in {
        form.errors.length shouldEqual 1
      }

      s"error with message '${messages.Errors.worthWhenSoldForLessMinimumAmount}'" in {
        form.error("amount").get.message shouldBe messages.Errors.worthWhenSoldForLessMinimumAmount
      }
    }

    "supplied with an amount that has too many decimal places" should {

      lazy val form = worthWhenSoldForLessForm.bind(Map("amount" -> "100.1234"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "raise only one error" in {
        form.errors.length shouldEqual 1
      }

      s"error with message '${messages.Errors.worthWhenSoldForLessDecimalError}'" in {
        form.error("amount").get.message shouldBe messages.Errors.worthWhenSoldForLessDecimalError
      }
    }
  }
}
