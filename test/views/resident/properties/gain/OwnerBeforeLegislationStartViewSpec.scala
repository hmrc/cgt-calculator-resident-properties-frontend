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

import assets.MessageLookup.Resident.Properties.{OwnerBeforeLegislationStart => messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.resident.properties.gain.OwnerBeforeLegislationStartForm._
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.ownerBeforeLegislationStart

class OwnerBeforeLegislationStartViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val ownerBeforeLegislationStartView: ownerBeforeLegislationStart = fakeApplication.injector.instanceOf[ownerBeforeLegislationStart]
  "The Owner Before Legislation Start view" should {


    lazy val view = ownerBeforeLegislationStartView(ownerBeforeLegislationStartForm)(using fakeRequest, testingMessages)
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

    "have a back button" which {

      lazy val backLink = doc.select(".govuk-back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "has the govuk-back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      s"has a back link to '${controllers.routes.GainController.disposalCosts.toString}'" in {
        backLink.attr("href") shouldBe "#"
      }
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/owner-before-legislation-start"
    }

    "has the method of POST" in {
      form.attr("method") shouldBe "POST"
    }

    "have a legend for the radio inputs" which {

      lazy val legend = doc.select("legend")

      s"contain the text ${messages.heading}" in {
        legend.text should include(s"${messages.heading}")
      }

      "that has class govuk-fieldset__legend govuk-label--l" in {
        legend.hasClass("govuk-fieldset__legend--l") shouldEqual true
      }
    }

    "have a set of radio inputs" which {

      "are surrounded in a fieldset" which {

        "has the class govuk-radios govuk-radios--inline" in {
          doc.select("#main-content > div > div > form > div > fieldset > div").hasClass("govuk-radios govuk-radios--inline") shouldEqual true
        }

      }

      "for the option 'Yes'" should {

        lazy val YesRadioOption = doc.select("#main-content > div > div > form > div > fieldset > div > div:nth-child(1)")

        "have the text 'Yes'" in {
          YesRadioOption.text shouldEqual "Yes"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#ownedBeforeLegislationStart")

          "have the id 'ownedBeforeLegislationStart-Yes'" in {
            optionLabel.attr("id") shouldEqual "ownedBeforeLegislationStart"
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

        "have the text 'No'" in {
          NoRadioOption.text shouldEqual "No"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#ownedBeforeLegislationStart-2")

          "have the id 'livedInProperty-No'" in {
            optionLabel.attr("id") shouldEqual "ownedBeforeLegislationStart-2"
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

  "ownedBeforeLegislationStart view with form errors" should {

    lazy val form = ownerBeforeLegislationStartForm.bind(Map("ownedBeforeLegislationStart" -> ""))
    lazy val view = ownerBeforeLegislationStartView(form)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have an error summary" which {
      "display an error summary message for the page" in {
        doc.body.select(".govuk-error-summary__body").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select("#ownedBeforeLegislationStart-error").size shouldBe 1
      }
    }

  }

}
