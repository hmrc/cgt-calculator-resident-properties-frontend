/*
 * Copyright 2017 HM Revenue & Customs
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

package views.resident.properties.summary

import assets.MessageLookup.Resident.{Properties => propertiesMessages}
import assets.MessageLookup.{Resident => residentMessages, SummaryPage => messages}
import assets.{MessageLookup => commonMessages}
import common.Dates
import controllers.helpers.FakeRequestHelper
import controllers.routes
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.properties._
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{summary => views}

class PropertiesFinalSummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "PropertiesFinalSummaryView" when {
    val incomeAnswers = IncomeAnswersModel(
      currentIncomeModel = Some(CurrentIncomeModel(0)),
      personalAllowanceModel = Some(PersonalAllowanceModel(0))
    )

    //TODO: change to check your answers
    val backLinkUrl: String = controllers.routes.IncomeController.personalAllowance().url

    "the property was sold inside tax years, bought after legislation start," +
      " with no reliefs or brought forward losses and taxed at 18%" should {

      val gainAnswers = YourAnswersSummaryModel(disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = Some(100000),
        worthWhenSoldForLess = None,
        whoDidYouGiveItTo = None,
        worthWhenGaveAway = None,
        disposalCosts = BigDecimal(10000),
        acquisitionValue = Some(0),
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        acquisitionCosts = BigDecimal(10000),
        improvements = BigDecimal(30000),
        givenAway = false,
        sellForLess = Some(false),
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        howBecameOwner = Some("Bought"),
        boughtForLessThanWorth = Some(false)
      )
      val deductionAnswers = ChargeableGainAnswers(
        broughtForwardModel = Some(LossesBroughtForwardModel(false)),
        broughtForwardValueModel = None,
        propertyLivedInModel = Some(PropertyLivedInModel(false)),
        privateResidenceReliefModel = None,
        privateResidenceReliefValueModel = None,
        lettingsReliefModel = None,
        lettingsReliefValueModel = None
      )
      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = 20000,
        aeaUsed = 0,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 30000,
        firstRate = 18,
        secondBand = None,
        secondRate = None,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLinkUrl,
        taxYearModel, None, None)(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      "have a back button" which {

        lazy val backLink = doc.getElementById("back-link")

        "has the id 'back-link'" in {
          backLink.attr("id") shouldBe "back-link"
        }

        s"has the text '${residentMessages.back}'" in {
          backLink.text shouldBe residentMessages.back
        }

        s"has a link to $backLinkUrl" in {
          backLink.attr("href") shouldEqual backLinkUrl
        }

      }

      "has a banner" which {
        lazy val banner = doc.select("#tax-owed-banner")

        "has the class 'transaction-banner--complete'" in {
          banner.hasClass("transaction-banner--complete") shouldEqual true
        }

        "contains a h1" which {
          lazy val h1 = banner.select("h1")

          "has the class 'bold-xlarge'" in {
            h1.hasClass("bold-xlarge") shouldEqual true
          }

          s"has the text '£3,600.00'" in {
            h1.text() shouldEqual "£3,600.00"
          }
        }

        "contains a h2" which {
          lazy val h2 = banner.select("h2")

          "has the class 'heading-medium'" in {
            h2.hasClass("heading-medium") shouldEqual true
          }

          s"has the text ${messages.cgtToPay("2015/16")}" in {
            h2.text() shouldEqual messages.cgtToPay("2015/16")
          }
        }
      }

      "does not have a notice summary" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe true
      }

      "have a section for the Calculation details" which {

        "has the class 'summary-section' to underline the heading" in {

          doc.select("section#calcDetails h2").hasClass("summary-underline") shouldBe true

        }

        s"has a h2 tag" which {

          s"should have the title '${messages.calcDetailsHeadingDate("2015/16")}'" in {
            doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2015/16")
          }

          "has the class 'heading-large'" in {
            doc.select("section#calcDetails h2").hasClass("heading-large") shouldBe true
          }
        }

        "has a numeric output row for the gain" which {

          "should have the question text 'Total gain'" in {
            doc.select("#gain-question").text shouldBe messages.totalGain
          }

          "should have the value '£50,000'" in {
            doc.select("#gain-amount").text shouldBe "£50,000"
          }
        }

        "has a numeric output row for the deductions" which {

          "should have the question text 'Deductions'" in {
            doc.select("#deductions-question").text shouldBe messages.deductions
          }

          "should have the value '£0'" in {
            doc.select("#deductions-amount").text should include("£0")
          }

          "has a breakdown that" should {

            "include a value for PRR of £0" in {
              doc.select("#deductions-amount").text should include("Private Residence Relief used £0")
            }

            "include a value for Reliefs of £0" in {
              doc.select("#deductions-amount").text should include(s"${messages.lettingReliefsUsed} £0")
            }

            "include a value for Capital gains tax allowance used of £0" in {
              doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsCapitalGainsTax} £0")
            }
          }
        }

        "has a numeric output row for the chargeable gain" which {

          "should have the question text 'Taxable gain'" in {
            doc.select("#chargeableGain-question").text shouldBe messages.chargeableGain
          }

          "should have the value '£20,000'" in {
            doc.select("#chargeableGain-amount").text should include("£20,000")
          }
        }

        "has a numeric output row and a tax rate" which {

          "Should have the question text 'Tax Rate'" in {
            doc.select("#gainAndRate-question").text shouldBe messages.taxRate
          }

          "Should have the value £30,000" in {
            doc.select("#firstBand").text should include("£30,000")
          }
          "Should have the tax rate 18%" in {
            doc.select("#firstBand").text should include("18%")
          }
        }

        "has a numeric output row for the AEA remaining" which {

          "should have the question text 'Capital Gains Tax allowance left for 2015/16" in {
            doc.select("#aeaRemaining-question").text should include(messages.aeaRemaining("2015/16"))
          }

          "include a value for Capital gains tax allowance left of £0" in {
            doc.select("#aeaRemaining-amount").text should include("£0")
          }
        }
      }

      "display the save as PDF Button" which {

        "should render only one button" in {
          doc.select("a.save-pdf-button").size() shouldEqual 1
        }

        "with the class save-pdf-button" in {
          doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
        }

        s"with an href to ${controllers.routes.ReportController.gainSummaryReport().toString}" in {
          doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
        }

        s"have the text ${messages.saveAsPdf}" in {
          doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
        }
      }
    }

    "the calculation returns tax on both side of the rate boundary" should {
      val gainAnswers = YourAnswersSummaryModel(disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = None,
        worthWhenSoldForLess = None,
        whoDidYouGiveItTo = Some("Other"),
        worthWhenGaveAway = Some(10000),
        disposalCosts = BigDecimal(10000),
        acquisitionValue = None,
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        acquisitionCosts = BigDecimal(10000),
        improvements = BigDecimal(30000),
        givenAway = false,
        sellForLess = Some(false),
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        howBecameOwner = Some("Bought"),
        boughtForLessThanWorth = Some(false)
      )
      val deductionAnswers = ChargeableGainAnswers(
        broughtForwardModel = Some(LossesBroughtForwardModel(false)),
        broughtForwardValueModel = None,
        propertyLivedInModel = Some(PropertyLivedInModel(false)),
        privateResidenceReliefModel = None,
        privateResidenceReliefValueModel = None,
        lettingsReliefModel = None,
        lettingsReliefValueModel = None
      )
      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = 20000,
        aeaUsed = 0,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 30000,
        firstRate = 18,
        secondBand = Some(10000),
        secondRate = Some(28),
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0
      )

      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLinkUrl, taxYearModel,
        None, None)(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"have a back button" which {

        lazy val backLink = doc.getElementById("back-link")

        "has the id 'back-link'" in {
          backLink.attr("id") shouldBe "back-link"
        }

        s"has the text '${residentMessages.back}'" in {
          backLink.text shouldBe residentMessages.back
        }

        s"has a link to '$backLinkUrl'" in {
          backLink.attr("href") shouldBe backLinkUrl
        }
      }

      "has a numeric output row and a tax rate" which {

        "Should have the question text 'Tax Rate'" in {
          doc.select("#gainAndRate-question").text shouldBe messages.taxRate
        }

        "Should have the value £30,000 in the first band" in {
          doc.select("#firstBand").text should include("£30,000")
        }
        "Should have the tax rate 18% for the first band" in {
          doc.select("#firstBand").text should include("18%")
        }

        "Should have the value £10,000 in the second band" in {
          doc.select("#secondBand").text should include("£10,000")
        }
        "Should have the tax rate 28% for the first band" in {
          doc.select("#secondBand").text should include("28%")
        }
      }

      "display the save as PDF Button" which {

        "should render only one button" in {
          doc.select("a.save-pdf-button").size() shouldEqual 1
        }

        "with the class save-pdf-button" in {
          doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
        }

        s"with an href to ${controllers.routes.ReportController.gainSummaryReport().toString}" in {
          doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
        }

        s"have the text ${messages.saveAsPdf}" in {
          doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
        }
      }
    }

    "reliefs are used" should {

    }

    "a Brought Forward Loss is entered and none remains" should {

    }

    "a Brought Forward Loss remains" should {

    }

    "the property was bought before 31 March 1982" should {

    }

    "the property was given away" should {

    }

    "a date above the known tax years is supplied" should {

      lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2018),
        None,
        None,
        whoDidYouGiveItTo = Some("Other"),
        worthWhenGaveAway = Some(10000),
        BigDecimal(10000),
        None,
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        BigDecimal(10000),
        BigDecimal(30000),
        true,
        None,
        true,
        Some(BigDecimal(5000)),
        None,
        None
      )

      lazy val deductionAnswers = ChargeableGainAnswers(
        Some(LossesBroughtForwardModel(false)),
        None,
        Some(PropertyLivedInModel(false)),
        None,
        None,
        None,
        None
      )

      lazy val incomeAnswers = IncomeAnswersModel(Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

      lazy val results = TotalGainAndTaxOwedModel(
        50000,
        20000,
        0,
        30000,
        3600,
        30000,
        18,
        Some(10000),
        Some(28),
        Some(BigDecimal(0)),
        Some(BigDecimal(0)),
        0,
        0
      )

      lazy val backLink = "/calculate-your-capital-gains/resident/properties/personal-allowance"

      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

      lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, None, None)(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "does not display the what to do next content" in {
        doc.select("#whatToDoNext").isEmpty shouldBe true
      }

      "display the save as PDF Button" which {

        "should render only one button" in {
          doc.select("a.save-pdf-button").size() shouldEqual 1
        }

        "with the class save-pdf-button" in {
          doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
        }

        s"with an href to ${controllers.routes.ReportController.gainSummaryReport().toString}" in {
          doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
        }

        s"have the text ${messages.saveAsPdf}" in {
          doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
        }
      }
    }

    //GA - move into top tests
    "Properties Final Summary view" should {

      lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2018),
        None,
        None,
        whoDidYouGiveItTo = Some("Other"),
        worthWhenGaveAway = Some(10000),
        BigDecimal(10000),
        None,
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        BigDecimal(10000),
        BigDecimal(30000),
        true,
        None,
        true,
        Some(BigDecimal(5000)),
        None,
        None
      )

      lazy val deductionAnswers = ChargeableGainAnswers(
        Some(LossesBroughtForwardModel(false)),
        None,
        Some(PropertyLivedInModel(false)),
        None,
        None,
        None,
        None)

      lazy val incomeAnswers = IncomeAnswersModel(Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

      lazy val results = TotalGainAndTaxOwedModel(
        50000,
        20000,
        0,
        30000,
        3600,
        30000,
        18,
        Some(10000),
        Some(28),
        Some(BigDecimal(0)),
        Some(BigDecimal(0)),
        0,
        0
      )

      lazy val backLink = "/calculate-your-capital-gains/resident/properties/personal-allowance"

      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

      "not have PRR GA metrics when PRR is not in scope" in {
        val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, None, None)(fakeRequestWithSession, applicationMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
        doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
      }

      "not have lettings relief GA metrics when it is not in scope" in {
        val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, None, None)(fakeRequestWithSession, applicationMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
      }

      "have PRR GA metrics when PRR is used" in {
        val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, Some(true), None)(fakeRequestWithSession, applicationMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 1
        doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
      }

      "not have lettings relief GA metrics when it is used" in {
        val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, None, Some(true))(fakeRequestWithSession, applicationMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 1
        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
      }

      "have PRR GA metrics when PRR is not used" in {
        val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, Some(false), None)(fakeRequestWithSession, applicationMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
        doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 1
      }

      "have lettings relief GA metrics when it is not used" in {
        val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, None, Some(false))(fakeRequestWithSession, applicationMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 1
      }

    }
  }
}
