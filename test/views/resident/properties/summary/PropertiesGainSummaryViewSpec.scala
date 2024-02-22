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

import _root_.views.BaseViewSpec
import assets.MessageLookup.{Resident => residentMessages, SummaryDetails => summaryMessages, SummaryPage => messages}
import common.Dates._
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.routes
import models.resident.TaxYearModel
import models.resident.properties.YourAnswersSummaryModel
import org.jsoup.Jsoup
import org.mockito.Mockito._
import views.html.calculation.resident.properties.summary.gainSummary

class PropertiesGainSummaryViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val gainSummaryView = fakeApplication.injector.instanceOf[gainSummary]
  "Summary view" should {

    val testModel = YourAnswersSummaryModel(
      disposalDate = constructDate(12, 9, 2015),
      disposalValue = Some(1000),
      worthWhenSoldForLess = None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      disposalCosts = BigDecimal(20),
      acquisitionValue = Some(50000),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      acquisitionCosts = BigDecimal(40),
      improvements = BigDecimal(50),
      givenAway = false,
      sellForLess = Some(false),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      howBecameOwner = Some("Bought"),
      boughtForLessThanWorth = Some(false)
    )

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    when(mockAppConfig.urBannerLink)
      .thenReturn(summaryMessages.bannerPanelLinkURL)

    lazy val view = gainSummaryView(testModel, -2000, 1000, taxYearModel, 11000, showUserResearchPanel = true)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title("2015 to 2016")}" in {
      doc.title() shouldBe messages.title("2015 to 2016")
    }

    s"have a back button" which {

      lazy val backLink = doc.select(".govuk-back-link")

      s"has the text '${residentMessages.back}'" in {
        backLink.text shouldBe residentMessages.back
      }

      s"has a link to '${routes.ReviewAnswersController.reviewGainAnswers.url}'" in {
        backLink.attr("href") shouldBe "#"
      }

    }

    "has a banner" which {
      lazy val banner = doc.select("#tax-owed-banner")

      "contains a h2" which {
        lazy val h2 = banner.select("h2")

        s"has the text '£0.00'" in {
          h2.text() shouldEqual "£0.00"
        }
      }

      "contains a body" which {
        lazy val h2 = banner.select(".govuk-panel__title")

        s"has the text ${summaryMessages.cgtToPay("2015 to 2016")}" in {
          h2.text() shouldEqual summaryMessages.cgtToPay("2015 to 2016")
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

      "has a div for total loss" which {

        lazy val div = doc.select("#yourTotalLoss").get(0)

        "has a h3 tag" which {

          s"has the text '${summaryMessages.yourTotalLoss}'" in {
            div.select("#yourTotalLoss > caption").text shouldBe summaryMessages.yourTotalLoss
          }
        }

        "has a row for disposal value" which {
          s"has the text '${summaryMessages.disposalValue}'" in {
            div.select("#disposalValue-text").text shouldBe summaryMessages.disposalValue
          }

          "has the value '£1,000'" in {
            div.select("#disposalValue-amount").text shouldBe "£1,000"
          }
        }

        "has a row for acquisition value" which {
          s"has the text '${summaryMessages.acquisitionValue}'" in {
            div.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValue
          }

          "has the value '£50,000'" in {
            div.select("#acquisitionValue-amount").text shouldBe "£50,000"
          }
        }

        "has a row for total costs" which {
          s"has the text '${summaryMessages.totalCosts}'" in {
            div.select("#totalCosts-text").text shouldBe summaryMessages.totalCosts
          }

          "has the value '£1,000'" in {
            div.select("#totalCosts-amount").text shouldBe "£1,000"
          }
        }

        "has a row for total loss" which {
          s"has the text '${summaryMessages.totalLoss}'" in {
            div.select("#totalLoss-text").text shouldBe summaryMessages.totalLoss
          }

          "has the value '£2,000'" in {
            div.select("#totalLoss-amount").text shouldBe "£2,000"
          }
        }
      }

      "has a div for deductions" which {

        lazy val div = doc.select("#yourDeductions")

        "has a h3 tag" which {

          s"has the text '${summaryMessages.yourDeductions}'" in {
            div.select("#yourDeductions > caption").text shouldBe summaryMessages.yourDeductions
          }
        }

        "not have a row for reliefs used" in {
          div.select("#reliefsUsed-text") shouldBe empty
        }

        "has a row for AEA used" which {

          s"has the text '${summaryMessages.aeaUsed}'" in {
            div.select("#aeaUsed-text").text shouldBe summaryMessages.aeaUsed
          }

          "has the value '£0'" in {
            div.select("#aeaUsed-amount").text shouldBe "£0"
          }
        }

        "not have a row for brought forward losses used" in {
          div.select("#lossesUsed-text") shouldBe empty
        }

        "has a row for total deductions" which {

          s"has the text '${summaryMessages.totalDeductions}'" in {
            div.select("#totalDeductions-text").text shouldBe summaryMessages.totalDeductions
          }

          "has the value '£0'" in {
            div.select("#totalDeductions-amount").text shouldBe "£0"
          }
        }
      }

      "has a div for Taxable Gain" which {

        lazy val div = doc.select("#yourTaxableGain")

        "does not have a h3 tag" in {
          div.select("h3") shouldBe empty
        }

        "does not have a row for gain" in {
          div.select("#gain-text") shouldBe empty
        }

        "does not have a row for minus deductions" in {
          div.select("#minusDeductions-text") shouldBe empty
        }

        "has a row for taxable gain" which {
          s"has the text '${summaryMessages.taxableGain}'" in {
            div.select("#taxableGain-text").text shouldBe summaryMessages.taxableGain
          }

          "has the value '£0'" in {
            div.select("#taxableGain-amount").text shouldBe "£0"
          }
        }
      }

      "has a div for tax rate" which {

        lazy val div = doc.select("#yourTaxRate")

        "does not have a h3 tag" in {
          div.select("h3") shouldBe empty
        }

        "does not have a row for first band"  in {
          div.select("#firstBand-text") shouldBe empty
        }

        "does not have a row for second band" in {
          div.select("#secondBand-text") shouldBe empty
        }

        "has a row for tax to pay" which {

          s"has the text ${summaryMessages.taxToPay}" in {
            div.select("#taxToPay-text").text shouldBe summaryMessages.taxToPay
          }

          "has the value '£0'" in {
            div.select("#taxToPay-amount").text shouldBe "£0"
          }
        }
      }
    }

    "have a section for the Your remaining deductions" which {

      "has a div for remaining deductions" which {

        lazy val div = doc.select("#remainingDeductions")

        "has a h2 tag" which {

          s"has the text ${summaryMessages.remainingDeductions}" in {
            div.select("#remainingDeductions > table > caption").text shouldBe summaryMessages.remainingDeductions
          }
        }

        "has a row for annual exempt amount left" which {
          s"has the text ${summaryMessages.remainingAnnualExemptAmount("2015 to 2016")}" in {
            div.select("#aeaLeft-text").text shouldBe summaryMessages.remainingAnnualExemptAmount("2015 to 2016")
          }

          "has the value '£11,000'" in {
            div.select("#aeaLeft-amount").text shouldBe "£11,000"
          }
        }

        "not have a row for brought forward losses remaining" in {
          div.select("#broughtForwardLossesRemaining-text") shouldBe empty
        }

        "has a row for losses to carry forward" which {
          s"has the text${summaryMessages.lossesToCarryForwardFromCalculation}" in {
            div.select("#lossesToCarryForwardFromCalc-text").text shouldBe summaryMessages.lossesToCarryForwardFromCalculation
          }

          "has the value '£2,000" in {
            div.select("#lossesToCarryForwardFromCalc-amount").text shouldBe "£2,000"
          }
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

      "has the class bold-small" in {
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

  "Summary view" should {

    val testModel = YourAnswersSummaryModel(
      disposalDate = constructDate(12, 9, 2015),
      disposalValue = Some(1000),
      worthWhenSoldForLess = None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      disposalCosts = BigDecimal(20),
      acquisitionValue = Some(50000),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      acquisitionCosts = BigDecimal(40),
      improvements = BigDecimal(50),
      givenAway = false,
      sellForLess = Some(false),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      howBecameOwner = Some("Bought"),
      boughtForLessThanWorth = Some(false)
    )

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = gainSummaryView(testModel, -2000, 1000, taxYearModel, 11000, showUserResearchPanel = false)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "does not have ur panel" in {
      doc.select("div#ur-panel").size() shouldBe 0
    }

  }

}
