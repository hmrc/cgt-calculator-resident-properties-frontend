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

import assets.MessageLookup.{PrivateResidenceRelief => messages}
import forms.resident.properties.PrivateResidenceReliefForm._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PrivateResidenceReliefFormSpec extends UnitSpec with GuiceOneAppPerSuite {

  "Creating the form with an empty model" should {

    lazy val form = privateResidenceReliefForm

    "create an empty form" in {
      form.data.isEmpty shouldEqual true
    }
  }

  "Creating a form with an valid 'yes' model" should {

    lazy val form = privateResidenceReliefForm.bind(Map("isClaiming" -> "Yes"))

    "create a form with the data from the model" in {
      form.data("isClaiming") shouldEqual "Yes"
    }

    "raise no form error" in {
      form.hasErrors shouldBe false
    }

    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "Creating a form with a valid 'no' model" should {

    lazy val form = privateResidenceReliefForm.bind(Map("isClaiming" -> "No"))

    "create a form with the data from the model" in {
      form.data("isClaiming") shouldEqual "No"
    }

    "raise no form error" in {
      form.hasErrors shouldBe false
    }

    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "Creating a form using an invalid post" when {

    "supplied with no data for option" should {

      lazy val form = privateResidenceReliefForm.bind(Map("isClaiming" -> ""))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("isClaiming").get.message shouldBe messages.errorSelect
      }

      "supplied with invalid data for option" should {

        lazy val form = privateResidenceReliefForm.bind(Map("isClaiming" -> "asdas"))

        "raise form error" in {
          form.hasErrors shouldBe true
        }

        "raise 1 form error" in {
          form.errors.length shouldBe 1
        }

        "associate the correct error message to the error" in {
          form.error("isClaiming").get.message shouldBe messages.errorSelect
        }
      }
    }
  }
}
