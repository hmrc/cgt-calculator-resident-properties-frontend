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

package forms.resident.properties

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.resident.properties.SellOrGiveAwayForm._
import models.resident.properties.SellOrGiveAwayModel
import assets.MessageLookup.{PropertiesSellOrGiveAway => messages}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class SellOrGiveAwayFormSpec extends UnitSpec with GuiceOneAppPerSuite {

  "Creating the form from a model" should {

    "create an empty form when the model is empty" in {
      lazy val form = sellOrGiveAwayForm
      form.data.isEmpty shouldBe true
    }

    "create a map with the option 'Given' when the model contains a true" in {
      lazy val model = SellOrGiveAwayModel(true)
      lazy val form = sellOrGiveAwayForm.fill(model)
      form.data.get("givenAway") shouldBe Some("Given")
    }

    "create a map with the option 'Sold' when the model contains a false" in {
      lazy val model = SellOrGiveAwayModel(false)
      lazy val form = sellOrGiveAwayForm.fill(model)
      form.data.get("givenAway") shouldBe Some("Sold")
    }
  }

  "Creating the form from a valid map" should {

    "create a model containing true from the option 'Given'" in {
      lazy val map = Map(("givenAway", "Given"))
      lazy val form = sellOrGiveAwayForm.bind(map)
      form.value.get shouldBe SellOrGiveAwayModel(true)
    }

    "create a model containing false from the option 'Sold'" in {
      lazy val map = Map(("givenAway", "Sold"))
      lazy val form = sellOrGiveAwayForm.bind(map)
      form.value.get shouldBe SellOrGiveAwayModel(false)
    }
  }

  "Creating the form from an invalid map" when {

    "no data is provided" should {
      lazy val map = Map(("givenAway", ""))
      lazy val form = sellOrGiveAwayForm.bind(map)

      "produce a form with errors" in {
        form.hasErrors shouldBe true
      }

      "contain only one error" in {
        form.errors.length shouldBe 1
      }

      s"contain the error message ${messages.errorMandatory}" in{
        form.errors.head.message shouldBe messages.errorMandatory
      }
    }

    "incorrect data is provided" should {
      lazy val map = Map(("givenAway", "test"))
      lazy val form = sellOrGiveAwayForm.bind(map)

      "produce a form with errors" in {
        form.hasErrors shouldBe true
      }

      "contain only one error" in {
        form.errors.length shouldBe 1
      }

      s"contain the error message ${messages.errorMandatory}" in{
        form.errors.head.message shouldBe messages.errorMandatory
      }
    }
  }

}
