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

package views.resident.properties.income

import assets.MessageLookup.{CurrentIncome => messages, Resident => commonMessages}
import forms.resident.income.CurrentIncomeForm._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.income.currentIncome

class CurrentIncomeViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val currentIncomeView = fakeApplication.injector.instanceOf[currentIncome]
  lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

  "Current Income view" should {

    lazy val view = currentIncomeView(currentIncomeForm(taxYearModel), "", taxYearModel, false)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title("2015 to 2016")}" in {
      doc.title() shouldBe messages.title("2015 to 2016")
    }

    s"have a back link with text ${commonMessages.back}" in {
      doc.select("#back-link").text() shouldEqual "Back"
    }

    s"have the question of the page ${messages.question("2015 to 2016")}" in {
      doc.select("h1").text shouldEqual messages.question("2015 to 2016")
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.IncomeController.submitCurrentIncome().toString}'" in {
        form.attr("action") shouldBe controllers.routes.IncomeController.submitCurrentIncome().toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }


      "has a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.question("2015 to 2016")}" in {
          label.text should include(messages.question("2015 to 2016"))
        }

        "have the class 'govuk-visually-hidden'" in {
          label.attr("class") shouldBe "govuk-label govuk-visually-hidden"
        }
      }

      s"have the help text ${messages.helpText}" in {
        doc.body.getElementsByClass("govuk-body").text shouldBe messages.helpText
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

        lazy val continueButton = doc.select("button.govuk-button")

        s"have the button text '${commonMessages.continue}'" in {
          continueButton.text shouldBe commonMessages.continue
        }

        "be of id submit" in {
          continueButton.attr("id") shouldBe "submit"
        }

        "have the class 'govuk-button'" in {
          continueButton.hasClass("govuk-button") shouldBe true
        }
      }
    }
  }

  "The Current Income View with form with errors" which {

    "is due to mandatory field error" should {

      lazy val form = currentIncomeForm(taxYearModel).bind(Map("amount" -> ""))
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = currentIncomeView(form, "", taxYearModel, false)(fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.getElementsByClass("govuk-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.getElementsByClass("govuk-error-message").size shouldBe 1
      }
    }
  }
}
