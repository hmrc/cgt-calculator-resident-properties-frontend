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

import assets.MessageLookup.Resident.Properties.{PropertiesWorthWhenGaveAway => messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.routes
import forms.resident.properties.WorthWhenGaveAwayForm._
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.worthWhenGaveAway

class WorthWhenGaveAwayViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val worthWhenGaveAwayView = fakeApplication.injector.instanceOf[worthWhenGaveAway]
  case class FakePOST(value: String) {
    lazy val request = fakeRequestToPOSTWithSession(("amount", value))
    lazy val form = worthWhenGaveAwayForm.bind(Map(("amount", value)))
    lazy val backLink = Some(controllers.routes.GainController.whoDidYouGiveItTo.toString())
    lazy val view = worthWhenGaveAwayView(worthWhenGaveAwayForm, backLink, routes.GainController.submitWorthWhenGaveAway)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)
  }

  "Worth when gave away View" should {

    lazy val backLink = Some(controllers.routes.GainController.whoDidYouGiveItTo.toString())
    lazy val view = worthWhenGaveAwayView(worthWhenGaveAwayForm, backLink, routes.GainController.submitWorthWhenGaveAway)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have the title of the page ${messages.title}" in {
      doc.title shouldEqual s"${messages.title} - ${commonMessages.homeText} - GOV.UK"
    }

    s"have a back link to the Who did you give it to Page with text ${commonMessages.back}" in {
      doc.select(".govuk-back-link").attr("href") shouldEqual "#"
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("H1")

      s"have the page heading '${messages.title}'" in {
        heading.text shouldBe messages.title
      }

      "have the heading-large class" in {
        heading.hasClass("govuk-heading-xl") shouldEqual true
      }
    }

    "have a form that" should {

      lazy val form = doc.select("form")

      "have the action /calculate-your-capital-gains/resident/properties/worth-when-gave-away" in {
        form.attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/worth-when-gave-away"
      }

      "have the method POST" in {
        form.attr("method") shouldEqual "POST"
      }

      "have an input for the amount" which {

        lazy val input = doc.select("#amount")

        "has a label" which {

          lazy val label = doc.select("label")

          s"has the text ${messages.title}" in {
            label.text() shouldEqual messages.title
          }

          "has the class govuk-label--m" in {
            label.hasClass("govuk-label--m") shouldEqual true
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

        "have two paragraphs" which {
          s"have a p tag with the text ${messages.paragraphText}" in {
            form.select("p#guideText").text shouldBe messages.paragraphText
          }

          "have a p tag" which {
            s"with the extra text ${messages.extraText}" in {
              form.select(".govuk-inset-text").text shouldBe messages.extraText
            }
          }
        }
      }

      "has a continue button" which {

        lazy val button = doc.select("#submit")

        "renders as button tags" in {
          button.is("button") shouldEqual true
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

    lazy val form = worthWhenGaveAwayForm.bind(Map("amount" -> "100"))
    lazy val backLink = Some(controllers.routes.GainController.whoDidYouGiveItTo.toString())
    lazy val view = worthWhenGaveAwayView(form, backLink, routes.GainController.submitWorthWhenGaveAway)(using fakeRequest, testingMessages)
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

    lazy val form = worthWhenGaveAwayForm.bind(Map("amount" -> ""))
    lazy val backLink = Some(controllers.routes.GainController.whoDidYouGiveItTo.toString())
    lazy val view = worthWhenGaveAwayView(form, backLink, routes.GainController.submitWorthWhenGaveAway)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select("#amount-error").size shouldBe 1
    }
  }
}
