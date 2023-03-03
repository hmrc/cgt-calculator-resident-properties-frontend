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

package views.resident.properties.deductions

import assets.MessageLookup.{PrivateResidenceRelief => messages, Resident => commonMessages}
import forms.resident.properties.PrivateResidenceReliefForm._
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.deductions.privateResidenceRelief

class PrivateResidenceReliefViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val privateResidenceReliefView = fakeApplication.injector.instanceOf[privateResidenceRelief]
  "Private Residence Relief view" should {

    lazy val view = privateResidenceReliefView(privateResidenceReliefForm)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("h1")

      s"have the page heading '${messages.heading}'" in {
        h1Tag.text shouldBe messages.heading
      }

      "have the heading-large class" in {
        h1Tag.hasClass("govuk-heading-xl") shouldBe true
      }
    }

    "have a back link" which {

      s"should have text ${commonMessages.back}" in {
        doc.select("#back-link").text() shouldEqual "Back"
      }

      "has the back-link class" in {
        doc.select("#back-link").hasClass("govuk-back-link") shouldBe true
      }

      "and link back to the property lived in page" in {
        doc.select("#back-link").attr("href") shouldEqual s"${controllers.routes.DeductionsController.propertyLivedIn().url}"
      }
    }

    s"have the question of the page ${messages.heading}" in {
      doc.select("h1").text shouldEqual messages.heading
    }

    "have a help text with the link that" should {

      lazy val helptext = doc.select("#main-content > div > div > form > p")

      s"have the text ${messages.helpTextLink}" in {
        helptext.text() should include(messages.helpTextLink)
      }

      s"have an internal span with the text ${commonMessages.externalLink}" in {
        helptext.select("#privateResidenceReliefQuestionLink > span").text() shouldEqual commonMessages.externalLink
      }

      "have the address https://www.gov.uk/government/publications/" +
        "private-residence-relief-hs283-self-assessment-helpsheet" in {
        helptext.select("a").attr("href") shouldEqual "https://www.gov.uk/government/publications/" +
          "private-residence-relief-hs283-self-assessment-helpsheet"
      }

      "the link should have a set of attributes" which {

        "has the govuk-link class" in {
          doc.select("#privateResidenceReliefQuestionLink").hasClass("govuk-link") shouldEqual true
        }

        "has the attribute rel" in {
          doc.select("#privateResidenceReliefQuestionLink").hasAttr("rel") shouldEqual true
        }

        "rel has the value of external" in {
          doc.select("#privateResidenceReliefQuestionLink").attr("rel") shouldEqual "external"
        }

        "has a target attribute" in {
          doc.select("#privateResidenceReliefQuestionLink").hasAttr("target") shouldEqual true
        }

        "has a target value of _blank" in {
          doc.select("#privateResidenceReliefQuestionLink").attr("target") shouldEqual "_blank"
        }
      }

      "record GA statistics" which {
        "has a data-journey-click attribute" in {
          doc.select("#privateResidenceReliefQuestionLink").hasAttr("data-journey-click") shouldEqual true
        }

        "with the GA value of help:govUK:rtt-properties-privateReliefQuestionHelp" in {
          doc.select("#privateResidenceReliefQuestionLink").attr("data-journey-click") shouldEqual "help:govUK:rtt-properties-privateResidenceReliefQuestionHelp"
        }
      }
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.DeductionsController.privateResidenceRelief().toString}'" in {
        form.attr("action") shouldBe controllers.routes.DeductionsController.privateResidenceRelief().toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      "has a set of radio of options" which {

        lazy val radioButtons = doc.body.getElementsByTag("fieldset")

        "has a legend" which {

          "exists" in {
            radioButtons.select("legend").size() shouldEqual 1
          }

          "has the class 'govuk-visually-hidden'" in {
            radioButtons.select("legend").hasClass("govuk-visually-hidden") shouldEqual true
          }

          s"has the text '${messages.heading}'" in {
            radioButtons.select("legend").text() shouldEqual messages.heading
          }
        }

        "has an input for the yes option" which {

          lazy val yesButton = radioButtons.select("label[for=isClaiming]")

          "has a label with text 'Yes'" in {
            yesButton.text shouldEqual "Yes"
          }

          "has id option-yes" in {
            yesButton.parents().first().select("input").attr("id") shouldEqual "isClaiming"
          }

          "has a name of 'isClaiming'" in {
            yesButton.parents().first().select("input").attr("name") shouldEqual "isClaiming"
          }

          "has a value of 'Yes'" in {
            yesButton.parents().first().select("input").attr("value") shouldEqual "Yes"
          }
        }

        "has an input for the no option" which {

          lazy val noButton = radioButtons.select("label[for=isClaiming-2]")

          "has a label with text 'No'" in {
            noButton.text shouldEqual "No"
          }

          "has id option-no" in {
            noButton.parents().first().select("input").attr("id") shouldEqual "isClaiming-2"
          }

          "has a name of 'isClaiming'" in {
            noButton.parents().first().select("input").attr("name") shouldEqual "isClaiming"
          }

          "has a value of 'No'" in {
            noButton.parents().first().select("input").attr("value") shouldEqual "No"
          }
        }
      }

      "have a continue button that" should {

        lazy val continueButton = doc.select("button#submit")

        s"have the button text '${commonMessages.continue}'" in {
          continueButton.text shouldBe commonMessages.continue
        }

        "have id submit" in {
          continueButton.attr("id") shouldBe "submit"
        }

        "have the class 'govuk-button'" in {
          continueButton.hasClass("govuk-button") shouldBe true
        }
      }
    }
  }

  "The Private Residence Relief View with form with errors" which {

    "is due to mandatory field error" should {

      lazy val form = privateResidenceReliefForm.bind(Map("amount" -> ""))
      lazy val view = privateResidenceReliefView(form)(fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select(".govuk-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select(".govuk-error-message").size shouldBe 1
      }
    }
  }
}
