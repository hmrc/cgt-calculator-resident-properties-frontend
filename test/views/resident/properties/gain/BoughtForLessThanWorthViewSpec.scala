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

import assets.MessageLookup.{BoughtForLessThanWorth => messages, Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.resident.properties.BoughtForLessThanWorthForm._
import models.resident.properties.BoughtForLessThanWorthModel
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.buyForLess

class BoughtForLessThanWorthViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val boughtForLessThanWorthView = fakeApplication.injector.instanceOf[buyForLess]
  "Sell for less view with an empty form" should {

    lazy val view = boughtForLessThanWorthView(boughtForLessThanWorthForm, Some("back-link"))(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)
    lazy val form = doc.getElementsByTag("form")

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title} - ${commonMessages.homeText} - GOV.UK" in {
      doc.title shouldBe s"${messages.title} - ${commonMessages.homeText} - GOV.UK"
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("h1")

      s"have the page heading '${messages.title}'" in {
        h1Tag.text shouldBe messages.title
      }

      "have the govuk-fieldset__heading class" in {
        h1Tag.hasClass("govuk-fieldset__heading") shouldBe true
      }
    }

    s"have the home link to '/calculate-your-capital-gains/resident/properties/'" in {
      doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/"
    }

    "have a back button" which {

      lazy val backLink = doc.select(".govuk-back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "has the govuk-back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      "has a back link to 'back'" in {
        backLink.attr("href") shouldBe "#"
      }
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/bought-for-less-than-worth"
    }

    "has the method of POST" in {
      form.attr("method") shouldBe "POST"
    }

    "have a legend for the radio inputs" which {

      lazy val legend = doc.select("legend")

      s"contain the text ${messages.title}" in {
        legend.text should include(s"${messages.title}")
      }
    }

    "have a set of radio inputs" which {

      "are surrounded in a div with class form-group" in {
        doc.select("div.govuk-radios").size() shouldEqual 1
      }

      "for the option 'Yes'" should {

        lazy val YesRadioOption = doc.select(".govuk-radios__item").get(0)

        "have the text 'Yes'" in {
          YesRadioOption.text() shouldEqual "Yes"
        }
      }

      "for the option 'No'" should {

        lazy val NoRadioOption = doc.select(".govuk-radios__item").get(1)

        "have the text 'No'" in {
          NoRadioOption.text() shouldEqual "No"
        }

      }
    }

    "have a continue button" which {

      lazy val button = doc.select("button")

      "has class 'govuk-button'" in {
        button.hasClass("govuk-button") shouldEqual true
      }

      "has attribute id" in {
        button.hasAttr("id") shouldEqual true
      }

      "has id equal to submit" in {
        button.attr("id") shouldEqual "submit"
      }

      s"has the text ${commonMessages.continue}" in {
        button.text shouldEqual s"${commonMessages.continue}"
      }
    }
  }

  "Sell for less view with a filled form" which {
    lazy val view = boughtForLessThanWorthView(boughtForLessThanWorthForm.fill(BoughtForLessThanWorthModel(true)), Some("back-link"))(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "for the option 'Yes'" should {

      lazy val YesRadioOption = doc.select("#boughtForLessThanWorth")

      "have the option auto-selected" in {
        YesRadioOption.hasAttr("checked") shouldBe true
      }
    }
  }

  "Sell for less view with form errors" should {

    lazy val form = boughtForLessThanWorthForm.bind(Map("boughtForLessThanWorth" -> ""))
    lazy val view = boughtForLessThanWorthView(form, Some("back"))(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have an error summary" which {
      "display an error summary message for the page" in {
        doc.body.select(".govuk-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select("#boughtForLessThanWorth-error").size shouldBe 1
      }
    }
  }
}
