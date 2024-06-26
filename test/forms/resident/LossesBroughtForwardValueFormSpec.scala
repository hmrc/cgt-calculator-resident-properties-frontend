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

import assets.MessageLookup.{Resident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import forms.resident.LossesBroughtForwardValueForm._
import models.resident.{LossesBroughtForwardValueModel, TaxYearModel}

class LossesBroughtForwardValueFormSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {

  lazy val testTaxYear = TaxYearModel("2016/17", isValidYear = true, "2016/17")
  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      val model = LossesBroughtForwardValueModel(1)
      val form = lossesBroughtForwardValueForm(testTaxYear).fill(model)
      form.data("amount") shouldBe "1"
    }
  }

  "Creating a form using map" when {

    "supplied with valid data" should {
      lazy val form = lossesBroughtForwardValueForm(testTaxYear).bind(Map(("amount", "1000")))

      "return a form with the mapped data" in {
        form.get shouldBe LossesBroughtForwardValueModel(BigDecimal(1000))
      }
    }

    "supplied with no data for amount" should {

      lazy val form = lossesBroughtForwardValueForm(testTaxYear).bind(Map("amount" -> ""))

      "have a form error" in {
        form.hasErrors shouldBe true
      }

      s"have an error with message '${messages.Errors.lossesBroughtForwardValueMandatoryAmount}'" in {
        form.error("amount").get.message shouldBe messages.Errors.lossesBroughtForwardValueMandatoryAmount
      }
    }

    "supplied with non-numeric data for amount" should {

      lazy val form = lossesBroughtForwardValueForm(testTaxYear).bind(Map("amount" -> "a"))

      "have a form error" in {
        form.hasErrors shouldBe true
      }

      s"have an error with message '${messages.Errors.lossesBroughtForwardValueInvalidAmount}'" in {
        form.error("amount").get.message shouldBe messages.Errors.lossesBroughtForwardValueInvalidAmount
      }
    }

    "supplied with an amount that is too big" should {

      lazy val form = lossesBroughtForwardValueForm(testTaxYear).bind(Map("amount" -> "9999999999999"))

      "have a form error" in {
        form.hasErrors shouldBe true
      }

      s"have an error with message '${messages.Errors.lossesBroughtForwardValueMaximumAmount}'" in {
        form.error("amount").get.message shouldBe messages.Errors.lossesBroughtForwardValueMaximumAmount
      }
    }

    "supplied with a negative amount" should {

      lazy val form = lossesBroughtForwardValueForm(testTaxYear).bind(Map("amount" -> "-1000"))

      "have a form error" in {
        form.hasErrors shouldBe true
      }

      s"have an error with message '${messages.Errors.lossesBroughtForwardValueMinimumAmount}'" in {
        form.error("amount").get.message shouldBe messages.Errors.lossesBroughtForwardValueMinimumAmount
      }
    }

    "supplied with an amount that has too many decimal places" should {

      lazy val form = lossesBroughtForwardValueForm(testTaxYear).bind(Map("amount" -> "1000.001"))

      "have a form error" in {
        form.hasErrors shouldBe true
      }

      s"have an error with message '${messages.Errors.lossesBroughtForwardValueDecimalError}'" in {
        form.error("amount").get.message shouldBe messages.Errors.lossesBroughtForwardValueDecimalError
      }
    }
  }
}
