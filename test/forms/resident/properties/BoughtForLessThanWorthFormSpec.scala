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

import forms.resident.properties.BoughtForLessThanWorthForm._
import models.resident.properties.BoughtForLessThanWorthModel
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class BoughtForLessThanWorthFormSpec extends UnitSpec with GuiceOneAppPerSuite {

  "Creating the BoughtForLessThanWorth form from valid inputs" should {

    "return a populated form using .fill" in {
      val model = BoughtForLessThanWorthModel(true)
      val form = boughtForLessThanWorthForm.fill(model)

      form.value.get shouldBe BoughtForLessThanWorthModel(true)
    }

    "return a populated form using .bind with an answer of Yes" in {
      val form = boughtForLessThanWorthForm.bind(Map(("boughtForLessThanWorth", "Yes")))

      form.value.get shouldBe BoughtForLessThanWorthModel(true)
    }

    "return a populated form using .bind with an answer of No" in {
      val form = boughtForLessThanWorthForm.bind(Map(("boughtForLessThanWorth", "No")))

      form.value.get shouldBe BoughtForLessThanWorthModel(false)
    }
  }

  "Creating the BoughtForLessThanWorth form from invalid inputs" when {

    "supplied with no selection" should {
      lazy val form = boughtForLessThanWorthForm.bind(Map(("boughtForLessThanWorth", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }
    }

    "supplied with an incorrect selection" should {
      lazy val form = boughtForLessThanWorthForm.bind(Map(("boughtForLessThanWorth", "true")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }
    }
  }
}
