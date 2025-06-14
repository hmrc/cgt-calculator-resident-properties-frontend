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

package views.resident.properties.deductions

import assets.MessageLookup.{PropertyLivedIn => messages, Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.resident.properties.PropertyLivedInForm._
import models.resident.properties.PropertyLivedInModel
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.properties.deductions.propertyLivedIn

class PropertyLivedInViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val propertyLivedInView = fakeApplication.injector.instanceOf[propertyLivedIn]
  "Property lived in view with an empty form" should {

    lazy val view = propertyLivedInView(propertyLivedInForm, Some("back-link"))(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.titleNew
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.getElementsByClass("govuk-fieldset__heading")

      s"have the page heading '${messages.title}'" in {
        h1Tag.text shouldBe messages.title
      }
    }

    s"have the home link to '/calculate-your-capital-gains/resident/properties/'" in {
      doc.getElementsByClass("govuk-header__link govuk-header__service-name").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/"
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
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/property-lived-in"
    }

    "have a legend for the radio inputs" which {

      lazy val legend = doc.select("legend")

      s"contain the text ${messages.title}" in {
        legend.text should include(s"${messages.title}")
      }
    }

    "have a set of radio inputs" which {

      "are surrounded in a div with class form-group" in {
        doc.select("#main-content > div > div > form > div").hasClass("govuk-form-group") shouldEqual true
      }

      "for the option 'Yes'" should {

        lazy val YesRadioOption = doc.select("#main-content > div > div > form > div > fieldset > div > div:nth-child(1) > label")

        "have the property 'for'" in {
          YesRadioOption.hasAttr("for") shouldEqual true
        }

        "the for attribute has the value livedInProperty-Yes" in {
          YesRadioOption.attr("for") shouldEqual "livedInProperty"
        }

        "have the text 'Yes'" in {
          YesRadioOption.text shouldEqual "Yes"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#livedInProperty")

          "have the id 'livedInProperty-Yes'" in {
            optionLabel.attr("id") shouldEqual "livedInProperty"
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

        lazy val NoRadioOption = doc.select("#main-content > div > div > form > div > fieldset > div > div:nth-child(2) > label")

        "have the property 'for'" in {
          NoRadioOption.hasAttr("for") shouldEqual true
        }

        "the for attribute has the value livedInProperty-No" in {
          NoRadioOption.attr("for") shouldEqual "livedInProperty-2"
        }

        "have the text 'No'" in {
          NoRadioOption.text shouldEqual "No"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#livedInProperty-2")

          "have the id 'livedInProperty-No'" in {
            optionLabel.attr("id") shouldEqual "livedInProperty-2"
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

  "Property lived in view with a filled form" which {
    lazy val view = propertyLivedInView(propertyLivedInForm.fill(PropertyLivedInModel(true)), Some("back-link"))(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "for the option 'Yes'" should {

      lazy val YesRadioOption = doc.select("#livedInProperty")

      "have the option auto-selected" in {
        YesRadioOption.hasAttr("checked") shouldBe true
      }
    }
  }

  "Property Lived In view with form errors" should {

    lazy val form = propertyLivedInForm.bind(Map("livedInProperty" -> ""))
    lazy val view = propertyLivedInView(form, Some("back"))(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have an error summary" which {
      "display an error summary message for the page" in {
          doc.body.select(".govuk-error-summary").size shouldBe 1
        }

        "display an error message for the input" in {
          doc.body.select(".govuk-error-message").size shouldBe 1
        }
    }
  }
}
