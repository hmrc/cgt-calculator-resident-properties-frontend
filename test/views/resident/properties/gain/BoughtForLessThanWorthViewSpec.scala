/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.resident.properties.BoughtForLessThanWorthForm._
import models.resident.properties.BoughtForLessThanWorthModel
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.buyForLess

class BoughtForLessThanWorthViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val boughtForLessThanWorthView = fakeApplication.injector.instanceOf[buyForLess]
  "Sell for less view with an empty form" should {

    lazy val view = boughtForLessThanWorthView(boughtForLessThanWorthForm, "home-link", Some("back-link"))(fakeRequest, testingMessages)
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

      s"have the page heading '${messages.title}'" in {
        h1Tag.text shouldBe messages.title
      }

      "have the heading-large class" in {
        h1Tag.hasClass("heading-large") shouldBe true
      }
    }

    s"have the home link to '/calculate-your-capital-gains/resident/properties/'" in {
      doc.select("#homeNavHref").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/"
    }

    "have a back button" which {

      lazy val backLink = doc.select("a#back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "has the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "has a back link to 'back'" in {
        backLink.attr("href") shouldBe "back-link"
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

      "that is visually hidden" in {
        legend.hasClass("visuallyhidden") shouldEqual true
      }
    }

    "have a set of radio inputs" which {

      "are surrounded in a div with class form-group" in {
        doc.select("div#radio-input").hasClass("form-group") shouldEqual true
      }

      "for the option 'Yes'" should {

        lazy val YesRadioOption = doc.select(".block-label[for=boughtForLessThanWorth-yes]")

        "have a label with class 'block-label'" in {
          YesRadioOption.hasClass("block-label") shouldEqual true
        }

        "have the property 'for'" in {
          YesRadioOption.hasAttr("for") shouldEqual true
        }

        "the for attribute has the value boughtForLessThanWorth-Yes" in {
          YesRadioOption.attr("for") shouldEqual "boughtForLessThanWorth-yes"
        }

        "have the text 'Yes'" in {
          YesRadioOption.text shouldEqual "Yes"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#boughtForLessThanWorth-yes")

          "have the id 'boughtForLessThanWorth-Yes'" in {
            optionLabel.attr("id") shouldEqual "boughtForLessThanWorth-yes"
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

        lazy val NoRadioOption = doc.select(".block-label[for=boughtForLessThanWorth-no]")

        "have a label with class 'block-label'" in {
          NoRadioOption.hasClass("block-label") shouldEqual true
        }

        "have the property 'for'" in {
          NoRadioOption.hasAttr("for") shouldEqual true
        }

        "the for attribute has the value boughtForLessThanWorth-No" in {
          NoRadioOption.attr("for") shouldEqual "boughtForLessThanWorth-no"
        }

        "have the text 'No'" in {
          NoRadioOption.text shouldEqual "No"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#boughtForLessThanWorth-no")

          "have the id 'livedInProperty-No'" in {
            optionLabel.attr("id") shouldEqual "boughtForLessThanWorth-no"
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
        button.hasClass("button") shouldEqual true
      }

      "has attribute 'type'" in {
        button.hasAttr("type") shouldEqual true
      }

      "has type value of 'submit'" in {
        button.attr("type") shouldEqual "submit"
      }

      "has attribute id" in {
        button.hasAttr("id") shouldEqual true
      }

      "has id equal to continue-button" in {
        button.attr("id") shouldEqual "continue-button"
      }

      s"has the text ${commonMessages.continue}" in {
        button.text shouldEqual s"${commonMessages.continue}"
      }
    }
  }

  "Sell for less view with a filled form" which {
    lazy val view = boughtForLessThanWorthView(boughtForLessThanWorthForm.fill(BoughtForLessThanWorthModel(true)), "home-link", Some("back-link"))(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "for the option 'Yes'" should {

      lazy val YesRadioOption = doc.select(".block-label[for=boughtForLessThanWorth-yes]")

      "have the option auto-selected" in {
        YesRadioOption.attr("class") shouldBe "block-label selected"
      }
    }
  }

  "Sell for less view with form errors" should {

    lazy val form = boughtForLessThanWorthForm.bind(Map("boughtForLessThanWorth" -> ""))
    lazy val view = boughtForLessThanWorthView(form, "home", Some("back"))(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have an error summary" which {
      "display an error summary message for the page" in {
        doc.body.select("#boughtForLessThanWorth-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select(".form-group .error-notification").size shouldBe 1
      }
    }
  }
}
