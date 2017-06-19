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

import models.resident.properties.LettingsReliefModel
import forms.resident.properties.LettingsReliefForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{LettingsRelief => messages}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class LettingsReliefFormSpec extends UnitSpec with GuiceOneAppPerSuite {

  "Creating the form for Lettings Relief from a valid selection" should {
    "return a populated form using .fill" in {

      lazy val model = LettingsReliefModel(true)
      lazy val form = lettingsReliefForm.fill(model)

      form.value shouldBe Some(LettingsReliefModel(true))
    }

    "return a valid model if supplied with valid selection" in {
      val form = lettingsReliefForm.bind(Map(("isClaiming", "Yes")))
      form.value shouldBe Some(LettingsReliefModel(true))
    }
  }

  "Creating the form for Lettings Relief from invalid selection" when {
    "supplied with no selection" should {

      lazy val form = lettingsReliefForm.bind(Map(("isClaiming", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.errorSelect}" in {
        form.error("isClaiming").get.message shouldBe messages.errorSelect
      }
    }

    "supplied with non Yes/No selection" should {
      lazy val form = lettingsReliefForm.bind(Map(("isClaiming", "abc")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.errorSelect}" in {
        form.error("isClaiming").get.message shouldBe messages.errorSelect
      }
    }
  }
}
