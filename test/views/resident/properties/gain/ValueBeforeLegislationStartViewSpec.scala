/*
 * Copyright 2018 HM Revenue & Customs
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

import assets.MessageLookup.{Resident => commonMessages}
import assets.MessageLookup.Resident.Properties.{ValueBeforeLegislationStart => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.properties.ValueBeforeLegislationStartForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{gain => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class ValueBeforeLegislationStartViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  case class FakePOST(value: String) {
    lazy val request = fakeRequestToPOSTWithSession(("amount", value))
    lazy val form = valueBeforeLegislationStartForm.bind(Map(("amount", value)))
    lazy val backLink = Some(controllers.routes.GainController.whoDidYouGiveItTo().toString())
    lazy val view = views.valueBeforeLegislationStart(valueBeforeLegislationStartForm)(fakeRequest, applicationMessages, fakeApplication)
    lazy val doc = Jsoup.parse(view.body)
  }

  "Worth when gave away View" should {

    lazy val backLink = Some(controllers.routes.GainController.ownerBeforeLegislationStart().toString())
    lazy val view = views.valueBeforeLegislationStart(valueBeforeLegislationStartForm)(fakeRequest, applicationMessages, fakeApplication)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have the title of the page ${messages.question}" in {
      doc.title shouldEqual messages.question
    }

    s"have a back link to the owner before April 1982 with text ${commonMessages.back}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/owner-before-legislation-start"
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("H1")

      s"have the page heading '${messages.question}'" in {
        heading.text shouldBe messages.question
      }

      "have the heading-large class" in {
        heading.hasClass("heading-large") shouldEqual true
      }
    }

    s"has the information text ${messages.information}" in {
      doc.select("article > p").text should include(messages.information)
    }

    s"has the hint text ${messages.hintText}" in {
      doc.select("article > div.form-hint > p").text shouldEqual messages.hintText
    }

    s"has the joint ownership text ${messages.jointOwnership}" in {
      doc.select("article > div.panel-indent > p").text shouldEqual messages.jointOwnership
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
            label.select("span").first().text() shouldEqual messages.question
          }

          "has the class visually hidden" in {
            label.select("span").hasClass("visuallyhidden") shouldEqual true
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

        lazy val button = doc.select("#continue-button")

        "renders as button tags" in {
          button.is("button") shouldEqual true
        }

        "has type equal to 'submit'" in {
          button.attr("type") shouldEqual "submit"
        }

        "has class of button" in {
          button.hasClass("button") shouldEqual true
        }

        s"has the text ${commonMessages.continue}" in {
          button.text() shouldEqual commonMessages.continue
        }
      }
    }
  }

  "Worth When Gave Away View with form without errors" should {

    lazy val form = valueBeforeLegislationStartForm.bind(Map("amount" -> "100"))
    lazy val backLink = Some(controllers.routes.GainController.ownerBeforeLegislationStart().toString())
    lazy val view = views.valueBeforeLegislationStart(form)(fakeRequest, applicationMessages, fakeApplication)
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
    lazy val backLink = Some(controllers.routes.GainController.ownerBeforeLegislationStart().toString())
    lazy val view = views.valueBeforeLegislationStart(form)(fakeRequest, applicationMessages, fakeApplication)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
