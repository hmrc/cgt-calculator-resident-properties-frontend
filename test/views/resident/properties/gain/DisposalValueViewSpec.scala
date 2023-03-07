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

package views.resident.properties.gain

import assets.MessageLookup.{DisposalValue => messages, Resident => commonMessages}
import controllers.helpers.FakeRequestHelper
import forms.resident.DisposalValueForm._
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.disposalValue

class DisposalValueViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with BaseViewSpec {

  lazy val disposalValueView = fakeApplication.injector.instanceOf[disposalValue]
  case class FakePOST(value: String) {
    lazy val request = fakeRequestToPOSTWithSession(("amount", value))
    lazy val form = disposalValueForm.bind(Map(("amount", value)))
    lazy val view = disposalValueView(form)(request, testingMessages)
    lazy val doc = Jsoup.parse(view.body)
  }

  "Disposal Value View" should {

    lazy val view = disposalValueView(disposalValueForm)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have the title of the page ${messages.question}" in {
      doc.title shouldEqual messages.title
    }

    s"have a back link to the Sell For less Page with text ${commonMessages.back}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/sell-for-less"
    }

    s"have the question of the page ${messages.question}" in {
      doc.select("h1").text shouldEqual messages.question
    }

    s"has the help text ${messages.helpText}" in {
      doc.select("#main-content > div > div > div > p").text shouldEqual messages.helpText
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-value"
    }

    s"have a label for an input with text ${messages.question}" in {
      doc.getElementsByClass("govuk-label").text() shouldEqual messages.question
    }

    s"have an input field with id amount " in {
      doc.body.getElementById("amount").tagName() shouldEqual "input"
    }

      "have continue button " in {
      doc.getElementsByClass("govuk-button").text shouldEqual commonMessages.continue
    }
  }

  "Disposal Value View with form without errors" should {

    lazy val form = disposalValueForm.bind(Map("amount" -> "100"))
    lazy val view = disposalValueView(form)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display the value of the form" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }
  }

  "Disposal Value View with form with errors" should {

    lazy val form = disposalValueForm.bind(Map("amount" -> ""))
    lazy val view = disposalValueView(form)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary__body").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.getElementsByClass("govuk-error-summary").size shouldBe 1
    }
  }
}
