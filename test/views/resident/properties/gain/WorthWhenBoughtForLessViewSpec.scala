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

package views.resident.properties.gain

import assets.MessageLookup.Resident.Properties.{WorthWhenBoughtForLess => messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.resident.properties.WorthWhenBoughtForLessForm._
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.worthWhenBoughtForLess

class WorthWhenBoughtForLessViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val worthWhenBoughtForLessView = fakeApplication.injector.instanceOf[worthWhenBoughtForLess]
  "worthWhenBought view" should {
    lazy val form = worthWhenBoughtForLessForm
    lazy val view = worthWhenBoughtForLessView(form)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a back link to back-link" in {
      doc.select(".govuk-back-link").attr("href") shouldBe "#"
    }

    s"have a title of ${messages.question}" in {
      doc.title() shouldBe messages.title
    }

    s"have a question of ${messages.question}" in {
      doc.getElementsByClass("govuk-heading-xl").text() shouldBe messages.question
    }

    "has a form hint" which {

      lazy val formHint = doc.getElementsByClass("govuk-body")

      s"has the first paragraph of ${messages.helpOne}" in {
        formHint.select("p").get(0).text shouldEqual messages.helpOne
      }

      s"has the second paragraph of ${messages.helpTwo}" in {
        formHint.select("p").get(1).text shouldEqual messages.helpTwo
      }
    }

    s"has the joint ownership text ${messages.jointOwner}" in {
      doc.select(".govuk-inset-text").text shouldEqual messages.jointOwner
    }

    "have a form tag" in {
      doc.select("form").size() shouldBe 1
    }

    "have a form action of 'postAction'" in {
      doc.select("form").attr("action") shouldBe "/calculate-your-capital-gains/resident/properties/worth-when-bought-for-less"
    }

    "have a form method of 'POST'" in {
      doc.select("form").attr("method") shouldBe "POST"
    }

    "have a visually hidden label for the question" in {
      val label = doc.select("label")
      label.hasClass("govuk-label") shouldBe true
    }

    s"have a label for an input with text ${messages.question}" in {
      doc.select("label").first().text() shouldEqual messages.question
    }

    s"have an input field with id amount " in {
      doc.body.getElementById("amount").tagName() shouldEqual "input"
    }

    "have a continue button " in {
      doc.select("#submit").text shouldBe commonMessages.continue
    }
  }

  "Disposal Value View with form without errors" should {
    lazy val form = worthWhenBoughtForLessForm.bind(Map("amount" -> "100"))
    lazy val view = worthWhenBoughtForLessView(form)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display the value of the form" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select(".govuk-error-summary__body").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select("#amount-error").size shouldBe 0
    }
  }

  "Disposal Value View with form with errors" should {
    lazy val form = worthWhenBoughtForLessForm.bind(Map("amount" -> ""))
    lazy val view = worthWhenBoughtForLessView(form)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary__body").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select("#amount-error").size shouldBe 1
    }
  }
}
