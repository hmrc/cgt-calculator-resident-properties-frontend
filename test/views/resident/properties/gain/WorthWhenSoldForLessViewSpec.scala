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

import assets.MessageLookup.Resident.Properties.{WorthWhenSoldForLess => messages}
import assets.MessageLookup.{Resident => commonMessages}
import forms.resident.WorthWhenSoldForLessForm._
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.worthWhenSoldForLess

class WorthWhenSoldForLessViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val worthWhenSoldForLessView = fakeApplication.injector.instanceOf[worthWhenSoldForLess]
  "The Property Worth When Sold View when supplied with an empty form" should {

    lazy val view = worthWhenSoldForLessView(worthWhenSoldForLessForm)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a back link" which {

      lazy val backLink = doc.select(".govuk-back-link")

      s"should have the text ${commonMessages.back}" in {
        backLink.text shouldEqual commonMessages.back
      }

      "should link to the Did you sell it for less than it was worth page." in {
        backLink.attr("href") shouldEqual "#"
      }
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("H1")

      s"have the page heading '${messages.question}'" in {
        heading.text shouldBe messages.question
      }

      "have the heading-large class" in {
        heading.hasClass("govuk-heading-xl") shouldEqual true
      }
    }

    "have a form that" should {

      lazy val form = doc.select("form")

      "have the action /calculate-your-capital-gains/resident/properties/property-worth-when-sold" in {
        form.attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/worth-when-sold-for-less"
      }

      "have the method POST" in {
        form.attr("method") shouldEqual "POST"
      }

      "have an input for the amount" which {

        lazy val input = doc.getElementById("#amount")

        "has a label" which {

          lazy val label = doc.select("label")

          s"has the text ${messages.question}" in {
            doc.select("#main-content > div > div > form > div > label").text() shouldEqual messages.question
          }

          "has the class govuk-label" in {
            doc.select("#main-content > div > div > form > div > label").hasClass("govuk-label") shouldEqual true
          }

          "is tied to the input field" in {
            label.attr("for") shouldEqual "amount"
          }
        }

        "have two paragraphs" which {
          s"have a p tag with the text ${messages.paragraphText}" in {
            form.select("p#guideText").text shouldBe messages.paragraphText
          }

          "have a p tag" which {
            s"with the extra text ${messages.extraText}" in {
              doc.getElementsByClass("govuk-inset-text").text shouldBe messages.extraText
            }
          }
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

  "The Property Worth When Sold View when supplied with a correct form" should {

    lazy val form = worthWhenSoldForLessForm.bind(Map("amount" -> "100"))
    lazy val view = worthWhenSoldForLessView(form)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display the value of the form in the input" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }
  }

  "The Property Worth When Sold View when supplied with an incorrect form" should {

    lazy val form = worthWhenSoldForLessForm.bind(Map("amount" -> "adsa"))
    lazy val view = worthWhenSoldForLessView(form)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary__body").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.getElementsByClass("govuk-error-summary").size shouldBe 1
    }
  }
}
