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

import assets.MessageLookup.{Resident => messages}
import assets.MessageLookup.{LettingsReliefValue => Messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.properties.LettingsReliefValueForm._
import models.resident.properties.LettingsReliefValueModel
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class LettingsReliefValueFormSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper {

  "Creating a form using an empty model" should {

    lazy val form = lettingsReliefValueForm(10, 20)

    "return an empty string for amount" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {

    lazy val model = LettingsReliefValueModel(1)
    lazy val form = lettingsReliefValueForm(10, 20).fill(model)

    "return a form with the data specified in the model" in {
      form.data("amount") shouldBe "1"
    }

  }

  "Creating a form using an invalid post" when {

    "supplied with no data for amount" should {

      lazy val form = lettingsReliefValueForm(10, 20).bind(Map("amount" -> ""))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe messages.mandatoryAmount
      }
    }

    "supplied with empty space for amount" should {

      lazy val form = lettingsReliefValueForm(10,20).bind(Map("amount" -> "  "))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe messages.mandatoryAmount
      }
    }

    "supplied with non numeric input for amount" should {

      lazy val form = lettingsReliefValueForm(10,20).bind(Map("amount" -> "a"))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe messages.invalidAmount
      }
    }

    "supplied with an amount with 3 numbers after the decimal" should {

      lazy val form = lettingsReliefValueForm(400,20).bind(Map("amount" -> "1.000"))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe messages.invalidAmount
      }
    }

    "supplied with an amount that's greater than the max Lettings Relief" should {

      lazy val form = lettingsReliefValueForm(600000,42000).bind(Map("amount" -> "1000000000.01"))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe Messages.maxLettingsReliefExceeded
      }
    }

    "supplied with an amount that's greater than the PRR entered" should {

      lazy val form = lettingsReliefValueForm(600000,3000).bind(Map("amount" -> "1000000000.01"))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe Messages.lettingsReliefMoreThanPRR
      }
    }

    "supplied with an amount that's greater than the remaining gain" should {

      lazy val form = lettingsReliefValueForm(40000,30000).bind(Map("amount" -> "1000000000.01"))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe Messages.lettingsReliefMoreThanRemainingGain(10000)
      }
    }

    "supplied with an amount that's less than the zero" should {

      lazy val form = lettingsReliefValueForm(500,20).bind(Map("amount" -> "-0.01"))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe messages.minimumAmount
      }
    }
  }

  "Creating a form using a valid post" when {

    "supplied with a valid amount" should {

      lazy val form = lettingsReliefValueForm(5000,20).bind(Map("amount" -> "1"))

      "build a model with the correct amount" in {
        form.value.get shouldBe LettingsReliefValueModel(BigDecimal(1))
      }

      "not raise form error" in {
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount with 1 number after the decimal" should {
      "not raise a form error" in {
        val form = lettingsReliefValueForm(5000,20).bind(Map("amount" -> "1.1"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount with 2 numbers after the decimal" should {
      "not raise a form error" in {
        val form = lettingsReliefValueForm(5000,20).bind(Map("amount" -> "1.11"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount that's equal to the max" should {
      "not raise a form error" in {
        val form = lettingsReliefValueForm(100000,50000).bind(Map("amount" -> "40000"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount that's equal to the min" should {
      "not raise a form error" in {
        val form = lettingsReliefValueForm(5000,20).bind(Map("amount" -> "0"))
        form.hasErrors shouldBe false
      }
    }
  }
}
