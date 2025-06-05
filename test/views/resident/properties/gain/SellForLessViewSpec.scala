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

import assets.MessageLookup.Resident.Properties.{SellForLess => messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.resident.properties.SellForLessForm._
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.sellForLess

class SellForLessViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val sellForLessView: sellForLess = fakeApplication.injector.instanceOf[sellForLess]
  "Sell for less view with an empty form" should {

    lazy val view = sellForLessView(sellForLessForm, Some("back-link"))(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)
    lazy val form = doc.getElementsByTag("form")

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("h1")

      s"have the page heading '${messages.heading}'" in {
        h1Tag.text shouldBe messages.heading
      }

      "have the govuk-fieldset__heading class" in {
        h1Tag.hasClass("govuk-fieldset__heading") shouldBe true
      }
    }

    s"have the home link to '/calculate-your-capital-gains/resident/properties/'" in {
      doc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/"
    }

    "have a back button" which {

      lazy val backLink = doc.select(".govuk-back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "has the back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      "has a back link to 'back'" in {
        backLink.attr("href") shouldBe "#"
      }
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/sell-for-less"
    }

    "has the method of POST" in {
      form.attr("method") shouldBe "POST"
    }

    "have a legend for the radio inputs" which {

      lazy val legend = doc.select("legend")

      s"contain the text ${messages.heading}" in {
        legend.text should include(s"${messages.heading}")
      }

      "that has class govuk-fieldset__legend govuk-label--xl" in {
        legend.hasClass("govuk-fieldset__legend--xl") shouldEqual true
      }
    }

    "have a set of radio inputs" which {

      "are surrounded in a class govuk-radios govuk-radios--inline" in {
        doc.select("#main-content > div > div > form > div > fieldset > div").hasClass("govuk-radios govuk-radios--inline") shouldEqual true
      }

      "for the option 'Yes'" should {

        lazy val YesRadioOption = doc.select("#main-content > div > div > form > div > fieldset > div > div:nth-child(1)")

        "have a label with class 'govuk-label govuk-radios__label'" in {
          YesRadioOption.select("label").hasClass("govuk-label govuk-radios__label") shouldEqual true
        }

        "have the text 'Yes'" in {
          YesRadioOption.text shouldEqual "Yes"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#sellForLess")

          "have the id 'sellForLess-Yes'" in {
            optionLabel.attr("id") shouldEqual "sellForLess"
          }

          "have the value 'Yes'" in {
            optionLabel.attr("value") shouldEqual "Yes"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'No'" should {

        lazy val NoRadioOption = doc.select("#main-content > div > div > form > div > fieldset > div > div:nth-child(2)")

        "have a label with class 'govuk-label govuk-radios__label'" in {
          NoRadioOption.select("label").hasClass("govuk-label govuk-radios__label") shouldEqual true
        }

        "have the text 'No'" in {
          NoRadioOption.text shouldEqual "No"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#sellForLess-2")

          "have the id 'livedInProperty-No'" in {
            optionLabel.attr("id") shouldEqual "sellForLess-2"
          }

          "have the value 'No'" in {
            optionLabel.attr("value") shouldEqual "No"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }
    }

    "have a continue button" which {

      lazy val button = doc.select("button")

      "has class 'button'" in {
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

  "Sell for less view with form errors" should {

    lazy val form = sellForLessForm.bind(Map("sellForLess" -> ""))
    lazy val view = sellForLessView(form, Some("back"))(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have an error summary" which {
      "display an error summary message for the page" in {
        doc.body.select(".govuk-error-summary__body").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select("#sellForLess-error").size shouldBe 1
      }
    }
  }
}
