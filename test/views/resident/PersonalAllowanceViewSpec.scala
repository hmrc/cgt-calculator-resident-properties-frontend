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


import assets.DateAsset
import assets.MessageLookup.{PersonalAllowance => messages, Resident => commonMessages}
import common.resident.JourneyKeys
import common.{CommonPlaySpec, Dates, WithCommonFakeApplication}
import forms.resident.income.PersonalAllowanceForm._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.personalAllowance

class PersonalAllowanceViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {
  lazy val testTaxYear = TaxYearModel("2016/17", isValidYear = true, "2016/17")

  val postAction = controllers.routes.IncomeController.submitPersonalAllowance

  "Personal Allowance view" when {
    lazy val personalAllowanceView = fakeApplication.injector.instanceOf[personalAllowance]
    "supplied with a 2015/16 tax year" should {

      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = personalAllowanceView(personalAllowanceForm(testTaxYear), taxYearModel, BigDecimal(10600), postAction,
        Some("back-link"), JourneyKeys.properties, "navTitle", Dates.getCurrentTaxYear)(fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.question("2015 to 2016")}" in {
        doc.title() shouldBe messages.title("2015 to 2016")
      }

      "have a back button that" should {
        lazy val backLink = doc.select(".govuk-back-link")
        "have the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"have the text ${commonMessages.back}" in {
          backLink.text() shouldBe commonMessages.back
        }

        "have a link to Current Income" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      s"have the page heading '${messages.question("2015 to 2016")}'" in {
        doc.select("h1").text shouldBe messages.question("2015 to 2016")
      }

      s"have the help text ${messages.help}" in {
        doc.select("form p").get(0).text() shouldBe messages.help
      }

      s"have a list title of ${messages.listTitle("2015", "2016", "")}" in {
        doc.select("form p").get(1).text() shouldBe messages.listTitle("2015", "2016", "£10,600")
      }

      s"have a list with the first entry of ${messages.listOne}" in {
        doc.select("form li").get(0).text() shouldBe messages.listOne
      }

      s"have a list with the second entry of ${messages.listTwo}" in {
        doc.select("form li").get(1).text() shouldBe messages.listTwo
      }

      "have a link" which {
        lazy val link = doc.select("form div").first()

        s"has the initial text ${messages.linkText}" in {
          link.select("span").text() shouldBe messages.linkText
        }

        "has the href to the gov uk rates page" in {
          link.select("a").attr("href") shouldBe "https://www.gov.uk/income-tax-rates/current-rates-and-allowances"
        }

        s"has the link text ${messages.link}" in {
          link.select("a").text() shouldBe messages.link
        }
      }
      "the link should have a set of attributes" which {

        "has the external link class" in {
          doc.select("#currentRatesAndAllowancesLink").hasClass("govuk-link") shouldEqual true
        }

        "has the attribute rel" in {
          doc.select("#currentRatesAndAllowancesLink").hasAttr("rel") shouldEqual true
        }

        "rel has the value of external" in {
          doc.select("#currentRatesAndAllowancesLink").attr("rel") shouldEqual "external"
        }

        "has a target attribute" in {
          doc.select("#currentRatesAndAllowancesLink").hasAttr("target") shouldEqual true
        }

        "has a target value of _blank" in {
          doc.select("#currentRatesAndAllowancesLink").attr("target") shouldEqual "_blank"
        }
      }

      "record GA statistics" which {
        "has a data-journey-click attribute" in {
          doc.select("#currentRatesAndAllowancesLink").hasAttr("data-journey-click") shouldEqual true
        }

        "with the GA value of help:govUK:rtt-properties-currentRatesAndAllowancesHelp" in {
          doc.select("#currentRatesAndAllowancesLink").attr("data-journey-click") shouldEqual "help:govUK:rtt-properties-currentRatesAndAllowancesHelp"
        }
      }
      "have a form" which {
        lazy val form = doc.getElementsByTag("form")

        s"has the action '${postAction.url}'" in {
          form.attr("action") shouldBe postAction.url
        }

        "has the method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"have a legend for an input with text ${messages.question("2015 to 2016")}" in {
          doc.body.getElementsByClass("govuk-heading-xl").text() shouldEqual messages.question("2015 to 2016")
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
        lazy val continueButton = doc.getElementsByClass("govuk-button")

        s"have the button text '${commonMessages.continue}'" in {
          continueButton.text shouldBe commonMessages.continue
        }

        "be of id submit" in {
          continueButton.attr("id") shouldBe "submit"
        }
      }


      "Personal Allowance view with stored values" should {
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val form = personalAllowanceForm(testTaxYear).bind(Map(("amount", "1000")))
        lazy val view = personalAllowanceView(form, taxYearModel, BigDecimal(10600), postAction,
          Some("back-link"), JourneyKeys.properties, "navTitle", Dates.getCurrentTaxYear)(fakeRequest, testingMessages)
        lazy val doc = Jsoup.parse(view.body)

        "have the value of 1000 auto-filled in the input" in {
          lazy val input = doc.body.getElementsByTag("input")
          input.`val` shouldBe "1000"
        }
      }
    }

    "supplied with a the current tax year" should {

      lazy val taxYearModel = TaxYearModel(await(Dates.getCurrentTaxYear), true, await(Dates.getCurrentTaxYear))
      lazy val view = personalAllowanceView(personalAllowanceForm(testTaxYear), taxYearModel, BigDecimal(11000), postAction,
        Some("back-link"), JourneyKeys.properties, "navTitle", Dates.getCurrentTaxYear)(fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)
      lazy val h1Tag = doc.select("H1")

      s"have the page heading '${messages.currentYearTitle}'" in {
        h1Tag.text shouldBe messages.currentYearTitle
      }

      s"have a legend for an input with text ${messages.currentYearTitle}" in {
        doc.body.getElementsByClass("govuk-heading-xl").text() shouldEqual messages.currentYearTitle
      }
    }

    "supplied with a tax year a year after the current tax year" should {

      lazy val nextTaxYearEndInt = await(Dates.getCurrentTaxYear).take(4).toInt + 2
      lazy val taxYearModel = TaxYearModel(Dates.taxYearToString(nextTaxYearEndInt), false, "2016/17")
      lazy val view = personalAllowanceView(personalAllowanceForm(testTaxYear), taxYearModel, BigDecimal(11000), postAction,
        Some("back-link"), JourneyKeys.properties, "navTitle", "2016/17")(fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)
      lazy val h1Tag = doc.select("H1")

      val nextTaxYear = await(DateAsset.getYearAfterCurrentTaxYear)
      val nextTaxYearModel = TaxYearModel(nextTaxYear, true, "2016/17")
      val nextYearString = s"${nextTaxYearModel.startYear} to ${nextTaxYearModel.endYear}"

      s"have a title ${messages.question(s"$nextTaxYear")}" in {
        doc.title() shouldBe messages.title(nextYearString)
      }

      s"have the page heading '${messages.question(s"$nextTaxYear")}'" in {
        h1Tag.text shouldBe messages.question(nextYearString)
      }

      s"have a legend for an input with text ${messages.question(s"$nextTaxYear")}" in {
        doc.body.getElementsByClass("govuk-heading-xl").text() shouldEqual messages.question(nextYearString)
      }
    }

    "Personal Allowance View with form with errors" which {

      "is due to mandatory field error" should {

        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val form = personalAllowanceForm(testTaxYear).bind(Map("amount" -> ""))
        lazy val view = personalAllowanceView(form, taxYearModel, BigDecimal(11000), postAction,
          Some("back-link"), JourneyKeys.properties, "navTitle", Dates.getCurrentTaxYear)(fakeRequest, testingMessages)
        lazy val doc = Jsoup.parse(view.body)

        "not display an error summary message for the amount" in {
          doc.body.select(".govuk-error-summary").size shouldBe 1
        }

        "not display an error message for the input" in {
          doc.body.select(".govuk-error-message").size shouldBe 1
        }
      }
    }
  }
}
