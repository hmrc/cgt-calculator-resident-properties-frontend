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

import assets.MessageLookup.{Resident => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.income.PersonalAllowanceForm._
import models.resident.income.PersonalAllowanceModel
import common.{CommonPlaySpec, WithCommonFakeApplication}
import common.resident.MoneyPounds
import models.resident.TaxYearModel

class PersonalAllowanceFormSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  lazy val testTaxYear = TaxYearModel("2016/17", isValidYear = true, "2016/17")

  "Creating a form using an empty model" should {
    val form = personalAllowanceForm(testTaxYear)
    "return an empty string for amount" in {
      form.data.isEmpty shouldBe true
    }
  }
  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = PersonalAllowanceModel(1)
      val form = personalAllowanceForm(testTaxYear).fill(model)
      form.data("amount") shouldBe "1"
    }
  }

  "Creating a form using an invalid post" when {

    "supplied with no data for amount" should {

      lazy val form = personalAllowanceForm(testTaxYear).bind(Map("amount" -> ""))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.Errors.personalAllowanceMandatoryAmount}'" in {
        form.error("amount").get.message shouldBe messages.Errors.personalAllowanceMandatoryAmount
      }
    }

    "supplied with a non-numeric value for amount" should {

      lazy val form = personalAllowanceForm(testTaxYear).bind(Map("amount" -> "a"))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.Errors.personalAllowanceInvalidAmount}'" in {
        form.error("amount").get.message shouldBe messages.Errors.personalAllowanceInvalidAmount
      }
    }

    "supplied with a negative amount" should {

      val limit = BigDecimal(11100)
      lazy val form = personalAllowanceForm(testTaxYear, limit).bind(Map("amount" -> "-1000"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.Errors.personalAllowanceMinimumAmount}'" in {
        form.error("amount").get.message shouldBe messages.Errors.personalAllowanceMinimumAmount
      }
    }

    "supplied with an amount that has too many decimal placed" should {

      val limit = BigDecimal(11100)
      lazy val form = personalAllowanceForm(testTaxYear, limit).bind(Map("amount" -> "100.1234"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.Errors.personalAllowanceInvalidAmount}'" in {
        form.error("amount").get.message shouldBe messages.Errors.personalAllowanceInvalidAmount
      }
    }

    "supplied with an amount that is larger than the maximum AEA" should {
            val limit = BigDecimal(11100)
            lazy val form = personalAllowanceForm(testTaxYear, limit).bind(Map("amount" -> "11100.01"))
            "raise form error" in {
              form.hasErrors shouldBe true
            }
           s"error with message '${messages.maximumLimit(MoneyPounds(limit, 0).quantity)}'" in {
                form.error("amount").get.message shouldBe messages.Errors.personalAllowanceMaximumAmount
           }
          }
  }
}
