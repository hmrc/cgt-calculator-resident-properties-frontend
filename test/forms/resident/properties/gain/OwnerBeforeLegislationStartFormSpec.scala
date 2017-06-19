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

package forms.resident.properties.gain

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import models.resident.properties.gain.OwnerBeforeLegislationStartModel
import forms.resident.properties.gain.OwnerBeforeLegislationStartForm._
import assets.MessageLookup.Resident.Properties.{OwnerBeforeLegislationStart => messages}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class OwnerBeforeLegislationStartFormSpec  extends UnitSpec with GuiceOneAppPerSuite {

  "Creating a form without a model" should {

    "create an empty form" in {
      lazy val form = ownerBeforeLegislationStartForm
      form.data.isEmpty shouldEqual true
    }
  }

  "Creating a form using a valid model" should {

    "return a form with the answer of Yes" in {
      lazy val model = OwnerBeforeLegislationStartModel(true)
      lazy val form = ownerBeforeLegislationStartForm.fill(model)
      form.data.get("ownedBeforeLegislationStart") shouldEqual Some("Yes")
    }

    "return a form with the answer of No" in {
      lazy val model = OwnerBeforeLegislationStartModel(false)
      lazy val form = ownerBeforeLegislationStartForm.fill(model)
      form.data.get("ownedBeforeLegislationStart") shouldEqual Some("No")
    }
  }

  "Creating a form using a valid map" should {

    "return a form with a value of Yes" in {
      lazy val form = ownerBeforeLegislationStartForm.bind(Map(("ownedBeforeLegislationStart", "Yes")))
      form.value shouldEqual Some(OwnerBeforeLegislationStartModel(true))
    }

    "return a form with a value of No" in {
      lazy val form = ownerBeforeLegislationStartForm.bind(Map(("ownedBeforeLegislationStart", "No")))
      form.value shouldEqual Some(OwnerBeforeLegislationStartModel(false))
    }
  }

  "Creating a form using an invalid map" when {

    "supplied with no data" should {
      lazy val form = ownerBeforeLegislationStartForm.bind(Map(("ownedBeforeLegislationStart", "")))

      "return a form with errors" in {
        form.hasErrors shouldEqual true
      }

      "return 1 error" in {
        form.errors.size shouldEqual 1
      }

      s"return an error with message ${messages.errorSelectAnOption}" in {
        form.error("ownedBeforeLegislationStart").get.message shouldEqual messages.errorSelectAnOption
      }
    }

    "supplied with invalid data" should {
      lazy val form = ownerBeforeLegislationStartForm.bind(Map(("ownedBeforeLegislationStart", "a")))

      "return a form with errors" in {
        form.hasErrors shouldEqual true
      }

      "return 1 error" in {
        form.errors.size shouldEqual 1
      }

      s"return an error with message ${messages.errorSelectAnOption}" in {
        form.error("ownedBeforeLegislationStart").get.message shouldEqual messages.errorSelectAnOption
      }
    }
  }

}
