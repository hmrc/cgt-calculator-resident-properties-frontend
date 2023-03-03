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

import assets.MessageLookup.{AcquisitionValue => messages, Resident => commonMessages}
import forms.resident.AcquisitionValueForm._
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.acquisitionValue

class AcquisitionValueViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val acquisitionValueView = fakeApplication.injector.instanceOf[acquisitionValue]
  "Acquisition Value view" should {

    lazy val view = acquisitionValueView(acquisitionValueForm)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a back button that" should {

      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      "have a link to Bought For Less Than Worth" in {
        backLink.attr("href") shouldBe controllers.routes.GainController.boughtForLessThanWorth().toString
      }
    }

    "have a H1 tag that" should {
      lazy val heading = doc.select("h1")

      s"have the page heading '${messages.pageHeading}'" in {
        heading.text shouldBe messages.pageHeading
      }

      "have the heading-large class" in {
        heading.hasClass("govuk-heading-xl") shouldBe true
      }
    }


    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.GainController.submitAcquisitionValue().toString}'" in {
        form.attr("action") shouldBe controllers.routes.GainController.submitAcquisitionValue().toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      s"has a paragraph with the text ${messages.jointOwnership}" in {
        doc.body().select("p.govuk-inset-text").text shouldBe messages.jointOwnership
      }

      "has a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.pageHeading}" in {
          label.text should include(messages.pageHeading)
        }

        "have the class 'visuallyhidden'" in {
          label.hasClass("govuk-label govuk-visually-hidden") shouldBe true
        }
      }

      "has a numeric input field that" should {

        lazy val input = doc.body.getElementsByTag("input")

        "have the id 'amount'" in {
          input.attr("id") shouldBe "amount"
        }

        "have the name 'amount'" in {
          input.attr("name") shouldBe "amount"
        }

        "have a type of number" in {
          input.attr("type") shouldBe "text"
        }
      }

      "has a continue button that" should {

        lazy val continueButton = doc.select("button.govuk-button")

        s"have the button text '${commonMessages.continue}'" in {
          continueButton.text shouldBe commonMessages.continue
        }

        "be of id submit" in {
          continueButton.attr("id") shouldBe "submit"
        }

        "have the class 'button'" in {
          continueButton.hasClass("govuk-button") shouldBe true
        }
      }
    }
  }

  "Acquisition Value View with form with errors" should {
    lazy val form = acquisitionValueForm.bind(Map("amount" -> ""))
    lazy val view = acquisitionValueView(form)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select("#amount-error").size shouldBe 1
    }
  }
}
