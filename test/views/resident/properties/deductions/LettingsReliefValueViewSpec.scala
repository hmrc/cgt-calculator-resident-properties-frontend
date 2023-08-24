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

package views.resident.properties.deductions

import assets.MessageLookup.{LettingsReliefValue => messages, Resident => commonMessages}
import forms.resident.properties.LettingsReliefValueForm._
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.deductions.lettingsReliefValue

class LettingsReliefValueViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val lettingsReliefValueView = fakeApplication.injector.instanceOf[lettingsReliefValue]

  "Reliefs Value view" should {

    lazy val form = lettingsReliefValueForm(1000,100).bind(Map("amount" -> "10"))
    lazy val view = lettingsReliefValueView(form, 1000)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    s"have a back link to the Reliefs Page with text ${commonMessages.back}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/lettings-relief"
    }

    s"have the text ${messages.question} as the h1 tag" in {
      doc.select("h1").text shouldEqual messages.question
    }

    "render a form element" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/lettings-relief-value"
    }

    s"have a hidden legend with the text ${messages.question}" in {
      doc.select("#main-content > div > div > form > div > label").text shouldEqual messages.question
    }

    "render an input field for the reliefs amount" in {
      doc.select("input").attr("id") shouldBe "amount"
    }

    s"have the text ${messages.additionalContent("1,000")} as a inset text" in {
      doc.select("#main-content > div > div > form > p").text shouldEqual messages.additionalContent("1,000")
    }

    "not display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 0
    }

    "not display an error message for the input" in {
      doc.body.select(".govuk-error-message").size shouldBe 0
    }

    "have continue button " in {
      doc.body.getElementsByClass("govuk-button").text shouldEqual commonMessages.continue
    }
  }

  "Reliefs Value View with form without errors" should {

    lazy val form = lettingsReliefValueForm(1000,150).bind(Map("amount" -> "100"))
    lazy val view = lettingsReliefValueView(form, 2000)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    s"have the text ${messages.question} as the h1 tag" in {
      doc.select("h1").text shouldEqual messages.question
    }

    s"have a hidden legend with the text ${messages.question}" in {
      doc.select("#main-content > div > div > form > div > label").text shouldEqual messages.question
    }

    "display the value of the form" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".govuk-error-message").size shouldBe 0
    }
  }

  "Reliefs Value View with form with errors" should {

    lazy val form = lettingsReliefValueForm(1000,100).bind(Map("amount" -> ""))
    lazy val view = lettingsReliefValueView(form, 3000)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    s"have the text ${messages.question} as the h1 tag" in {
      doc.select("h1").text shouldEqual messages.question
    }

    s"have a hidden legend with the text ${messages.question}" in {
      doc.select("#main-content > div > div > form > div > label").text shouldEqual messages.question
    }

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".govuk-error-message").size shouldBe 1
    }
  }
}
