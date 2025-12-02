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

import assets.MessageLookup.Resident.Properties.{ValueBeforeLegislationStart => messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.resident.properties.ValueBeforeLegislationStartForm._
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.valueBeforeLegislationStart

class ValueBeforeLegislationStartViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val valueBeforeLegislationStartView = fakeApplication.injector.instanceOf[valueBeforeLegislationStart]
  case class FakePOST(value: String) {
    lazy val request = fakeRequestToPOSTWithSession(("amount", value))
    lazy val form = valueBeforeLegislationStartForm.bind(Map(("amount", value)))
    lazy val backLink = Some(controllers.routes.GainController.whoDidYouGiveItTo.toString())
    lazy val view = valueBeforeLegislationStartView(valueBeforeLegislationStartForm)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)
  }

  "Worth when gave away View" should {

    lazy val view = valueBeforeLegislationStartView(valueBeforeLegislationStartForm)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have the title of the page ${messages.question}" in {
      doc.title shouldEqual messages.title
    }

    s"have a back link to the owner before April 1982 with text ${commonMessages.back}" in {
      doc.select(".govuk-back-link").attr("href") shouldEqual "#"
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("H1")

      s"have the page heading '${messages.heading}'" in {
        heading.text shouldBe messages.heading
      }

      "have the heading-large class" in {
        heading.hasClass("govuk-heading-l") shouldEqual true
      }
    }

    s"has the information text ${messages.information}" in {
      doc.getElementsByClass("govuk-body").text should include(messages.information)
    }

    s"has the body text ${messages.hintText}" in {
      doc.select("#main-content > div > div > div.govuk-body").text shouldEqual messages.hintText
    }

    s"has the joint ownership text ${messages.jointOwnership}" in {
      doc.getElementsByClass("govuk-inset-text").text shouldEqual messages.jointOwnership
    }


    "have a form that" should {

      lazy val form = doc.select("form")

      "have the action /calculate-your-capital-gains/resident/properties/value-before-legislation-start" in {
        form.attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/value-before-legislation-start"
      }

      "have the method POST" in {
        form.attr("method") shouldEqual "POST"
      }

      "have an input for the amount" which {

        lazy val input = doc.select("#amount")

        "has a label" which {

          lazy val label = doc.select("label")

          s"has the text ${messages.question}" in {
            doc.select("#main-content > div > div > form > div > label").text() shouldEqual messages.question
          }

          "has the class govuk-label--m" in {
            doc.select("#main-content > div > div > form > div > label").hasClass("govuk-label--m") shouldEqual true
          }

          "is tied to the input field" in {
            label.attr("for") shouldEqual "amount"
          }
        }

        "renders in input tags" in {
          input.is("input") shouldEqual true
        }

        "has the field name as 'amount' to bind correctly to the form" in {

        }
      }

      "has a continue button" which {

        lazy val button = doc.getElementsByClass("govuk-button")

        "renders as button tags" in {
          button.is("button") shouldEqual true
        }

        "has type equal to 'submit'" in {
          button.attr("id") shouldEqual "submit"
        }

        "has class of button" in {
          button.hasClass("govuk-button") shouldEqual true
        }

        s"has the text ${commonMessages.continue}" in {
          button.text() shouldEqual commonMessages.continue
        }
      }
    }
  }

  "Worth When Gave Away View with form without errors" should {

    lazy val form = valueBeforeLegislationStartForm.bind(Map("amount" -> "100"))
    lazy val view = valueBeforeLegislationStartView(form)(using fakeRequest, testingMessages)
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

  "Worth When Gave Away View with form with errors" should {

    lazy val form = valueBeforeLegislationStartForm.bind(Map("amount" -> ""))
    lazy val view = valueBeforeLegislationStartView(form)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary__body").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.getElementsByClass("govuk-error-summary").size shouldBe 1
    }
  }
}
