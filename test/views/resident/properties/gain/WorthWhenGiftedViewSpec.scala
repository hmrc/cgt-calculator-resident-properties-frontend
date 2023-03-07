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

import assets.MessageLookup.Resident.Properties.{WorthWhenGifted => messages}
import assets.MessageLookup.{Resident => commonMessages}
import forms.resident.properties.gain.WorthWhenGiftedForm._
import org.jsoup.Jsoup
import play.api.mvc.Call
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.worthWhenGifted

class WorthWhenGiftedViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val worthWhenGiftedView = fakeApplication.injector.instanceOf[worthWhenGifted]
  "worthWhenGifted view" should {
    val backLink = Some("back-link")
    val homeLink = "homeLink"
    val call = new Call("POST", "postAction")
    lazy val form = worthWhenGiftedForm
    lazy val view = worthWhenGiftedView(form, backLink, homeLink, call)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a back link to back-link" in {
      doc.select("#back-link").attr("href") shouldBe "back-link"
    }

    s"have a nav title of 'navTitle'" in {
      doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").text() shouldBe commonMessages.homeText
    }

    s"have a home link to '/calculate-your-capital-gains/resident/properties/'" in {
      doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
    }

    s"have a title of ${messages.question} - ${commonMessages.homeText} - GOV.UK" in {
      doc.title() shouldBe s"${messages.question} - ${commonMessages.homeText} - GOV.UK"
    }

    s"have a question of ${messages.question}" in {
      doc.select("h1.govuk-heading-xl").text() shouldBe messages.question
    }

    "has a form hint" which {

      lazy val formHint = doc.getElementsByClass("govuk-body")

      s"has the first paragraph of ${messages.hintOne}" in {
        formHint.select("p").get(0).text shouldEqual messages.hintOne
      }

      s"has the second paragraph of ${messages.hintTwo}" in {
        formHint.select("p").get(1).text shouldEqual messages.hintTwo
      }
    }

    s"has the joint ownership text ${messages.jointOwner}" in {
      doc.select("div.govuk-inset-text").text shouldEqual messages.jointOwner
    }

    "have a form tag" in {
      doc.select("form").size() shouldBe 1
    }

    "have a form action of 'postAction'" in {
      doc.select("form").attr("action") shouldBe "postAction"
    }

    "have a form method of 'POST'" in {
      doc.select("form").attr("method") shouldBe "POST"
    }

    s"have a label for an input with text ${messages.question}" in {
      doc.select("label.govuk-label.govuk-visually-hidden").text() shouldEqual messages.question
    }

    s"have an input field with id amount " in {
      doc.body.getElementById("amount").tagName() shouldEqual "input"
    }

    "have a continue button " in {
      doc.select("#submit").text shouldBe commonMessages.continue
    }
  }

  "Disposal Value View with form without errors" should {
    val backLink = Some("back-link")
    val homeLink = "homeLink"
    val call = new Call("POST", "postAction")
    lazy val form = worthWhenGiftedForm.bind(Map("amount" -> "100"))
    lazy val view = worthWhenGiftedView(form, backLink, homeLink, call)(fakeRequest, testingMessages)
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
    val backLink = Some("back-link")
    val homeLink = "homeLink"
    val call = new Call("POST", "postAction")
    lazy val form = worthWhenGiftedForm.bind(Map("amount" -> ""))
    lazy val view = worthWhenGiftedView(form, backLink, homeLink, call)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select("#amount-error").size shouldBe 1
    }
  }
}
