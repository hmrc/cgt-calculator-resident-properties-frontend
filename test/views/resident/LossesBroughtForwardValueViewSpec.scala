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

package views.resident

import assets.MessageLookup.{LossesBroughtForwardValue => messages, Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.routes
import forms.resident.LossesBroughtForwardValueForm._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.lossesBroughtForwardValue

class LossesBroughtForwardValueViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {
  lazy val lossesBroughtForwardValueView = fakeApplication.injector.instanceOf[lossesBroughtForwardValue]
  lazy val testTaxYear = TaxYearModel("2016/17", isValidYear = true, "2016/17")

  "Losses Brought Forward Value view" when {
    "provided with a date in the 2015/16 tax year" should {
      lazy val taxYear = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = lossesBroughtForwardValueView(lossesBroughtForwardValueForm(testTaxYear), taxYear, "back-link",
        routes.DeductionsController.submitLossesBroughtForwardValue, "navTitle")(using fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.title("2015 to 2016")}" in {
        doc.title() shouldBe messages.title("2015 to 2016")
      }

      "have a dynamic navTitle with text Calculate your Capital Gains Tax" in {
        doc.getElementsByClass("govuk-header__link govuk-header__service-name").text shouldEqual "Calculate your Capital Gains Tax"
      }

      "have a home link to '/calculate-your-capital-gains/resident/properties/'" in {
        doc.getElementsByClass("govuk-header__link govuk-header__service-name").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/"
      }

      "have a back button that" should {

        lazy val backLink = doc.select(".govuk-back-link")

        "have the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        "have the back-link class" in {
          backLink.hasClass("govuk-back-link") shouldBe true
        }

        "have a link to 'back-link'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "have a H1 tag that" should {

        lazy val h1Tag = doc.select("H1")

        s"have the page heading '${messages.question("2015 to 2016")}'" in {
          h1Tag.text shouldBe messages.question("2015 to 2016")
        }

        "have the govuk-label-wrapper class" in {
          h1Tag.hasClass("govuk-label-wrapper") shouldBe true
        }
      }

      "have a form" which {
        lazy val form = doc.getElementsByTag("form")

        s"has the action '${routes.DeductionsController.submitLossesBroughtForwardValue.url}'" in {
          form.attr("action") shouldBe routes.DeductionsController.submitLossesBroughtForwardValue.url
        }

        "has the method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        "has a label that" should {

          lazy val label = doc.body.getElementsByTag("label")

          s"have the question ${messages.question("2015 to 2016")}" in {
            label.text should include(messages.question("2015 to 2016"))
          }

        }

        "has a numeric input field" which {

          lazy val input = doc.body.getElementsByTag("input")

          "has the id 'amount'" in {
            input.attr("id") shouldBe "amount"
          }

          "has the name 'amount'" in {
            input.attr("name") shouldBe "amount"
          }

          "is of type number" in {
            input.attr("type") shouldBe "text"
          }
        }

        "have a continue button that" should {

          lazy val continueButton = doc.select("#submit")

          s"have the button text '${commonMessages.continue}'" in {
            continueButton.text shouldBe commonMessages.continue
          }

          "be of type submit" in {
            continueButton.attr("id") shouldBe "submit"
          }

          "have the class 'button'" in {
            continueButton.hasClass("govuk-button") shouldBe true
          }
        }
      }
    }

    "provided with a date in the 2014/15 tax year" should {

      lazy val taxYear = TaxYearModel("2014/15", false, "2015/16")
      lazy val view = lossesBroughtForwardValueView(lossesBroughtForwardValueForm(testTaxYear), taxYear, "back-link",
        routes.DeductionsController.submitLossesBroughtForwardValue, "navTitle")(using fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      s"have a title ${messages.title("2014 to 2015")}" in {
        doc.title() shouldBe messages.title("2014 to 2015")
      }

      "have a H1 tag that" should {

        lazy val h1Tag = doc.select("H1")

        s"have the page heading '${messages.question("2014 to 2015")}'" in {
          h1Tag.text shouldBe messages.question("2014 to 2015")
        }

        "have the govuk-label-wrapper class" in {
          h1Tag.hasClass("govuk-label-wrapper") shouldBe true
        }
      }

      "have a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.question("2014 to 2015")}" in {
          label.text should include(messages.question("2014 to 2015"))
        }

      }
    }
  }

  "Losses Brought Forward Value view with stored values" should {
    lazy val form = lossesBroughtForwardValueForm(testTaxYear).bind(Map(("amount", "1000")))
    lazy val taxYear = TaxYearModel("2015/16", true, "2015/16")
    lazy val view = lossesBroughtForwardValueView(form, taxYear, "back-link",
      routes.DeductionsController.submitLossesBroughtForwardValue, "navTitle")(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have the value of 1000 auto-filled in the input" in {
      lazy val input = doc.body.getElementsByTag("input")
      input.`val` shouldBe "1000"
    }
  }

  "Losses Brought Forward Value view with errors" should {
    lazy val form = lossesBroughtForwardValueForm(testTaxYear).bind(Map(("amount", "")))
    lazy val taxYear = TaxYearModel("2015/16", true, "2015/16")
    lazy val view = lossesBroughtForwardValueView(form, taxYear, "back-link",
      routes.DeductionsController.submitLossesBroughtForwardValue, "navTitle")(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".govuk-error-message").size shouldBe 1
    }
  }
}
