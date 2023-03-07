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

import assets.MessageLookup.{DisposalCosts => messages, Resident => commonMessages}
import controllers.helpers.FakeRequestHelper
import forms.resident.DisposalCostsForm._
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.disposalCosts

class DisposalCostsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper  with BaseViewSpec {

  lazy val disposalCostsView = fakeApplication.injector.instanceOf[disposalCosts]
  "Disposal Costs view" should {

    lazy val view = disposalCostsView(disposalCostsForm,"backlink")(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have the correct page title" in {
      doc.title shouldBe s"${messages.title} - ${commonMessages.homeText} - GOV.UK"
    }

    "have a back button that" should {

      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      "have a link to Disposal Value" in {
        backLink.attr("href") shouldBe "backlink"
      }
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("H1")

      s"have the page heading '${messages.pageHeading}'" in {
        h1Tag.text shouldBe messages.pageHeading
      }

      "have the heading-large class" in {
        h1Tag.hasClass("govuk-heading-xl") shouldBe true
      }
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.GainController.submitDisposalCosts.toString}'" in {
        form.attr("action") shouldBe controllers.routes.GainController.submitDisposalCosts.toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      "has a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.pageHeading}" in {
          label.text should include(messages.pageHeading)
        }

        "have the class 'govuk-visually-hidden'" in {
          doc.body.select("label.govuk-visually-hidden").size shouldBe 1
        }
      }

      "has a help list of bullet points that" should {

        s"have the title text ${messages.bulletTitle}" in {
          doc.body.select("div p.govuk-body").text shouldBe messages.bulletTitle
        }

        s"have the first bullet of ${messages.bulletOne}" in {
          doc.body.select("div ul.govuk-body li").get(0).text shouldBe messages.bulletOne
        }

        s"have the second bullet of ${messages.bulletTwo}" in {
          doc.body.select("div ul.govuk-body li").get(1).text shouldBe messages.bulletTwo
        }

        s"have the third bullet of ${messages.bulletThree}" in {
          doc.body.select("div ul.govuk-body li").get(2).text shouldBe messages.bulletThree
        }

        s"have the fourth bullet of ${messages.bulletFour}" in {
          doc.body.select("div ul.govuk-body li").get(3).text shouldBe messages.bulletFour
        }
      }

      s"has the important text ${messages.helpText}" in {
        doc.select("div.govuk-inset-text").text shouldEqual messages.helpText
      }

      "has a numeric input field" which {

        lazy val input = doc.body.getElementsByTag("input")

        "has the id 'amount'" in {
          input.attr("id") shouldBe "amount"
        }

        "has the name 'amount'" in {
          input.attr("name") shouldBe "amount"
        }
      }
    }

    "have a continue button that" should {

      lazy val continueButton = doc.select("button#submit")

      s"have the button text '${commonMessages.continue}'" in {
        continueButton.text shouldBe commonMessages.continue
      }

      "have the class 'button'" in {
        continueButton.hasClass("govuk-button") shouldBe true
      }

    }
  }

  "Disposal Costs View with form with errors" which {

    "is due to mandatory field error" should {

      lazy val form = disposalCostsForm.bind(Map("amount" -> ""))
      lazy val view = disposalCostsView(form, "backlink")(fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select(".govuk-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select("#amount-error").size shouldBe 1
      }
    }
  }
}
