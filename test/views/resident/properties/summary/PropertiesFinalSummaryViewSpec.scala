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

package views.resident.properties.summary

import assets.MessageLookup.{Resident => residentMessages, SummaryDetails => summaryMessages, SummaryPage => messages}
import common.{CommonPlaySpec, Dates, WithCommonFakeApplication}
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.properties._
import org.jsoup.Jsoup
import org.mockito.Mockito.when
import views.BaseViewSpec
import views.html.calculation.resident.properties.summary.finalSummary

class PropertiesFinalSummaryViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val finalSummaryView = fakeApplication.injector.instanceOf[finalSummary]
  "PropertiesFinalSummaryView" when {
    val incomeAnswers = IncomeAnswersModel(
      currentIncomeModel = Some(CurrentIncomeModel(0)),
      personalAllowanceModel = Some(PersonalAllowanceModel(0))
    )

    val backLinkUrl: String = controllers.routes.ReviewAnswersController.reviewFinalAnswers.url

    "the property was sold inside tax years, bought after legislation start," +
      " with reliefs and brought forward losses and taxed at both tax bands" should {

      val gainAnswers = YourAnswersSummaryModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
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
        broughtForwardValueModel = Some(LossesBroughtForwardValueModel(36.00)),
        propertyLivedInModel = Some(PropertyLivedInModel(false)),
        privateResidenceReliefModel = None,
        privateResidenceReliefValueModel = None,
        lettingsReliefModel = None,
        lettingsReliefValueModel = None
      )
      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = 20000,
        aeaUsed = 10,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 20000,
        firstRate = 18,
        secondBand = Some(10000.00),
        secondRate = Some(28),
        lettingReliefsUsed = Some(BigDecimal(500)),
        prrUsed = Some(BigDecimal(125)),
        broughtForwardLossesUsed = 35,
        allowableLossesUsed = 0,
        baseRateTotal = 30000,
        upperRateTotal = 15000
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      when(mockAppConfig.urBannerLink)
        .thenReturn(summaryMessages.bannerPanelLinkURL)

      lazy val view = finalSummaryView(gainAnswers, deductionAnswers, incomeAnswers, results, backLinkUrl,
        taxYearModel, None, None, 100, 100, showUserResearchPanel = true)(using fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.title("2015 to 2016")}" in {
        doc.title() shouldBe messages.title("2015 to 2016")
      }

      "have a back button" which {

        lazy val backLink = doc.select(".govuk-back-link")

        s"has the text '${residentMessages.back}'" in {
          backLink.text shouldBe residentMessages.back
        }

        s"has a link to final check your answers" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "has a banner" which {
        lazy val banner = doc.select("#tax-owed-banner")

        "contains a paragraph" which {
          lazy val p = banner.select("p")

          s"has the text '£3,600.00'" in {
            p.text() shouldEqual "£3,600.00"
          }
        }

        "contains a h2" which {
          lazy val h1 = banner.select("h1")

          s"has the text ${messages.cgtToPay("2015 to 2016")}" in {
            h1.text() shouldEqual messages.cgtToPay("2015 to 2016")
          }
        }
      }

      "does not have a notice summary" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe true
      }

      "have a section for the Calculation details" which {

        "has a h2 tag" which {

          s"has the text '${summaryMessages.howWeWorkedThisOut}'" in {
            doc.select("section#calcDetails h2").text shouldBe summaryMessages.howWeWorkedThisOut
          }
        }

        "has a div for total gain" which {

          lazy val div = doc.select("#yourTotalGain")

          "has a caption" which {

            s"has the text '${summaryMessages.yourTotalGain}'" in {
              div.select("#yourTotalGain > caption").text shouldBe summaryMessages.yourTotalGain
            }
          }

          "has a row for disposal value" which {

            s"has the text '${summaryMessages.disposalValue}'" in {
              div.select("#disposalValue-text").text shouldBe summaryMessages.disposalValue
            }

            "has the value '£100,000'" in {
              div.select("#disposalValue-amount").text shouldBe "£100,000"
            }
          }

          "has a row for acquisition value" which {
            s"has the text '${summaryMessages.acquisitionValue}'" in {
              div.select("#acquisitionValue-WhenBought-text").text shouldBe summaryMessages.acquisitionValue
            }

            "has the value '£0'" in {
              div.select("#acquisitionValue-WhenBought-amount").text shouldBe "£0"
            }
          }

          "has a row for total costs" which {
            s"has the text '${summaryMessages.totalCosts}'" in {
              div.select("#totalCosts-text").text shouldBe summaryMessages.totalCosts
            }

            "has the value '£100'" in {
              div.select("#totalCosts-amount").text shouldBe "£100"
            }
          }

          "has a row for total gain" which {
            s"has the text '${summaryMessages.totalGain}'" in {
              div.select("#totalGain-text").text shouldBe summaryMessages.totalGain
            }

            "has the value '£50,000'" in {
              div.select("#totalGain-amount").text shouldBe "£50,000"
            }
          }
        }

        "has a div for deductions" which {

          lazy val div = doc.select("#yourDeductions")

          "has a caption" which {

            s"has the text '${summaryMessages.yourDeductions}'" in {
              div.select("#yourDeductions > caption").text shouldBe summaryMessages.yourDeductions
            }
          }

          "has a row for reliefs used" which {
            s"has the text '${summaryMessages.reliefsUsed}'" in {
              div.select("#reliefsUsed-text").text shouldBe summaryMessages.reliefsUsed
            }

            "has the value '£625'" in {
              div.select("#reliefsUsed-amount").text shouldBe "£625"
            }
          }

          "has a row for AEA used" which {

            s"has the text '${summaryMessages.aeaUsed}'" in {
              div.select("#aeaUsed-text").text shouldBe summaryMessages.aeaUsed
            }

            "has the value '£10'" in {
              div.select("#aeaUsed-amount").text shouldBe "£10"
            }
          }

          "has a row for brought forward losses used" which {
            s"has the text '${summaryMessages.broughtForwardLossesUsed}'" in {
              div.select("#lossesUsed-text").text shouldBe summaryMessages.broughtForwardLossesUsed
            }

            "has the value '£35'" in {
              div.select("#lossesUsed-amount").text shouldBe "£35"
            }
          }

          "has a row for total deductions" which {

            s"has the text '${summaryMessages.totalDeductions}'" in {
              div.select("#totalDeductions-text").text shouldBe summaryMessages.totalDeductions
            }

            "has the value '£100'" in {
              div.select("#totalDeductions-amount").text shouldBe "£100"
            }
          }
        }

        "has a div for Taxable Gain" which {

          lazy val div = doc.select("#yourTaxableGain")

          "has a caption" which {

            s"has the text '${summaryMessages.yourTaxableGain}'" in {
              div.select("#yourTaxableGain > caption").text shouldBe summaryMessages.yourTaxableGain
            }
          }

          "has a row for gain" which {
            s"has the text '${summaryMessages.totalGain}'" in {
              div.select("#gain-text").text shouldBe summaryMessages.totalGain
            }

            "has the value '£50,000'" in {
              div.select("#gain-amount").text shouldBe "£50,000"
            }
          }

          "has a row for minus deductions" which {
            s"has the text '${summaryMessages.minusDeductions}'" in {
              div.select("#minusDeductions-text").text shouldBe summaryMessages.minusDeductions
            }

            "has the value '£100'" in {
              div.select("#minusDeductions-amount").text shouldBe "£100"
            }
          }

          "has a row for taxable gain" which {
            s"has the text '${summaryMessages.taxableGain}'" in {
              div.select("#taxableGain-text").text shouldBe summaryMessages.taxableGain
            }

            "has the value '£20,000'" in {
              div.select("#taxableGain-amount").text shouldBe "£20,000"
            }
          }
        }

        "has a div for tax rate" which {

          lazy val div = doc.select("#yourTaxRate")

          "has a caption" which {

            s"has the text ${summaryMessages.yourTaxRate}" in {
              div.select("#yourTaxRate > caption.govuk-table__caption.govuk-table__caption--m").text shouldBe summaryMessages.yourTaxRate
            }
          }

          "has row for first band" which {

            s"has the text '${summaryMessages.taxRate("£20,000", "18")}'" in {
              div.select("#firstBand-text").text shouldBe summaryMessages.taxRate("£20,000", "18")
            }

            "has the value '£30,000'" in {
              div.select("#firstBand-amount").text shouldBe "£30,000"
            }
          }

          "has a row for second band" which {
            s"has the text '${summaryMessages.taxRate("£10,000", "28")}'" in {
              div.select("#secondBand-text").text shouldBe summaryMessages.taxRate("£10,000", "28")
            }

            "has the value '£15,000'" in {
              div.select("#secondBand-amount").text shouldBe "£15,000"
            }
          }

          "has a row for tax to pay" which {

            s"has the text ${summaryMessages.taxToPay}" in {
              div.select("#taxToPay-text").text shouldBe summaryMessages.taxToPay
            }

            "has the value '£3,600'" in {
              div.select("#taxToPay-amount").text shouldBe "£3,600"
            }
          }
        }
      }

      "have a section for the You remaining deductions" which {

        "has a div for remaining deductions" which {

          lazy val div = doc.select("#remainingDeductions")

          "has a caption" which {

            s"has the text ${summaryMessages.remainingDeductions}" in {
              div.select("table > caption").text shouldBe summaryMessages.remainingDeductions
            }
          }

          "has a row for annual exempt amount left" which {
            s"has the text ${summaryMessages.remainingAnnualExemptAmount("2015 to 2016")}" in {
              div.select("#aeaRemaining-text").text shouldBe summaryMessages.remainingAnnualExemptAmount("2015 to 2016")
            }

            "has the value '£0'" in {
              div.select("#aeaRemaining-amount").text shouldBe "£0"
            }
          }

          "not have a row for brought forward losses remaining" in {
            div.select("#broughtForwardLossesRemaining-text") shouldBe empty
          }

          "not have a row for losses to carry forward" in {
            div.select("#lossesToCarryForward-text") shouldBe empty
          }
        }
      }

      "have a section for What happens next" which {
        lazy val section = doc.select("#whatToDoNext")

        "has a h2 tag" which {
          s"has the text ${summaryMessages.whatToDoNext}" in {
            section.select("h2").text shouldBe summaryMessages.whatToDoNext
          }
        }

        "has a paragraph" which {
          s"has the text ${summaryMessages.whatToDoNextDetails}" in {
            section.select("p").text shouldBe summaryMessages.whatToDoNextDetails
          }
        }
      }

      "has a continue button" which {
        s"has the text ${summaryMessages.continue}" in {
          doc.select("#main-content > div > div > div > a.govuk-button").text shouldBe summaryMessages.continue
        }

        "has a link to the what next section" in {
          doc.select("#main-content > div > div > div > a.govuk-button").attr("href") shouldBe controllers.routes.SaUserController.saUser.url
        }
      }

      "has a print Button" which {

        lazy val printSection = doc.select("#print")
        lazy val link = printSection.select("a")

        "has the class govuk-link" in {
          link.hasClass("govuk-link") shouldBe true
        }

        s"links to #" in {
          link.attr("href") shouldBe "#"
        }

        s"has the text ${messages.print}" in {
          link.text shouldBe messages.print
        }
      }

      "has UR panel" in {
        doc.toString.contains(summaryMessages.bannerPanelTitle)
        doc.toString.contains(summaryMessages.bannerPanelLinkText)
        doc.toString.contains(summaryMessages.bannerPanelCloseVisibleText)

      }

    }

    "the property was sold inside tax years and the ur banner is not displayed" should {

      val gainAnswers = YourAnswersSummaryModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
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
        broughtForwardValueModel = Some(LossesBroughtForwardValueModel(36.00)),
        propertyLivedInModel = Some(PropertyLivedInModel(false)),
        privateResidenceReliefModel = None,
        privateResidenceReliefValueModel = None,
        lettingsReliefModel = None,
        lettingsReliefValueModel = None
      )
      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = 20000,
        aeaUsed = 10,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 20000,
        firstRate = 18,
        secondBand = Some(10000.00),
        secondRate = Some(28),
        lettingReliefsUsed = Some(BigDecimal(500)),
        prrUsed = Some(BigDecimal(125)),
        broughtForwardLossesUsed = 35,
        allowableLossesUsed = 0,
        baseRateTotal = 30000,
        upperRateTotal = 15000
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = finalSummaryView(gainAnswers, deductionAnswers, incomeAnswers, results, backLinkUrl,
        taxYearModel, None, None, 100, 100, showUserResearchPanel = false)(using fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "does not have ur panel" in {
        doc.select("div#ur-panel").size() shouldBe 0
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
        val view = finalSummaryView(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel,
          None, None, 100, 100, showUserResearchPanel = false)(using fakeRequest, testingMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
        doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
      }

      "not have lettings relief GA metrics when it is not in scope" in {
        val view = finalSummaryView(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel,
          None, None, 100, 100, showUserResearchPanel = false)(using fakeRequest, testingMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
      }

      "have PRR GA metrics when PRR is used" in {
        val view = finalSummaryView(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel,
          Some(true), None, 100, 100, showUserResearchPanel = false)(using fakeRequest, testingMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 1
        doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
      }

      "not have lettings relief GA metrics when it is used" in {
        val view = finalSummaryView(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel,
          None, Some(true), 100, 100, showUserResearchPanel = false)(using fakeRequest, testingMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 1
        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
      }

      "have PRR GA metrics when PRR is not used" in {
        val view = finalSummaryView(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel,
          Some(false), None, 100, 100, showUserResearchPanel = false)(using fakeRequest, testingMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
        doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 1
      }

      "have lettings relief GA metrics when it is not used" in {
        val view = finalSummaryView(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel,
          None, Some(false), 100, 100, showUserResearchPanel = false)(using fakeRequest, testingMessages)
        val doc = Jsoup.parse(view.body)

        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
        doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 1
      }
    }
  }
}
