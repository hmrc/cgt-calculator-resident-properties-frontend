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

package views.resident.helpers

import common.Dates
import controllers.helpers.FakeRequestHelper
import models.resident.properties.{ChargeableGainAnswers, PropertyLivedInModel, YourAnswersSummaryModel}
import models.resident.{LossesBroughtForwardModel, LossesBroughtForwardValueModel, TaxYearModel, TotalGainAndTaxOwedModel}
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits.applicationMessages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{Resident => residentMessages, SummaryDetails => summaryMessages, SummaryPage => messages}
import views.html.{helpers => views}

class GainSummaryPartialViewSpec extends UnitSpec with  WithFakeApplication with FakeRequestHelper
{

  "the property was sold for less than worth" should {

  }

  "the property was bought before 31 March 1982" should {

    val gainAnswers = YourAnswersSummaryModel(
      disposalDate = Dates.constructDate(10, 10, 2015),
      disposalValue = Some(10000),
      worthWhenSoldForLess = None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      disposalCosts = BigDecimal(100000),
      acquisitionValue = Some(0),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      acquisitionCosts = BigDecimal(100000),
      improvements = BigDecimal(300000),
      givenAway = false,
      sellForLess = Some(false),
      ownerBeforeLegislationStart = true,
      valueBeforeLegislationStart = Some(350000.00),
      howBecameOwner = Some("Bought"),
      boughtForLessThanWorth = Some(false)
    )

    val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

    lazy val view = views.gainSummaryPartial(gainAnswers, taxYearModel, 100, 11000)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "has a banner" which {
      lazy val banner = doc.select("#tax-owed-banner")

      "contains a h1" which {
        lazy val h1 = banner.select("h1")

        s"has the text '£0.00'" in {
          h1.text() shouldEqual "£0.00"
        }
      }

      "contains a h2" which {
        lazy val h2 = banner.select("h2")

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
            div.select("h3").text shouldBe summaryMessages.yourTotalLoss
          }
        }

        "has a row for disposal value" which {
          s"has the text '${summaryMessages.disposalValue}'" in {
            div.select("#disposalValue-text").text shouldBe summaryMessages.disposalValue
          }

          "has the value '£10,000'" in {
            div.select("#disposalValue-amount").text shouldBe "£10,000"
          }
        }

        "has a row for acquisition value" which {
          s"has the text '${summaryMessages.acquisitionValueBeforeLegislation}'" in {
            div.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValueBeforeLegislation
          }

          "has the value '£350,000'" in {
            div.select("#acquisitionValue-amount").text shouldBe "£350,000"
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

        "has a row for total loss" which {
          s"has the text '${summaryMessages.totalLoss}'" in {
            div.select("#totalLoss-text").text shouldBe summaryMessages.totalLoss
          }

          "has the value '£0'" in {
            div.select("#totalLoss-amount").text shouldBe "£0"
          }
        }
      }

      "has a div for deductions" which {

        lazy val div = doc.select("#yourDeductions")

        "has a h3 tag" which {

          s"has the text '${summaryMessages.yourDeductions}'" in {
            div.select("h3").text shouldBe summaryMessages.yourDeductions
          }
        }

        "not have a row for reliefs used" in {
          div.select("#reliefsUsed-text") shouldBe empty
        }

        "has a row for AEA used" which {

          s"has the text '${summaryMessages.aeaUsed}'" in {
            div.select("#aeaUsed-text").text shouldBe summaryMessages.aeaUsed
          }

          "has the value '£11,000'" in {
            div.select("#aeaUsed-amount").text shouldBe "£11,000"
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
  }

  "the property was given away" should {
    val gainAnswers = YourAnswersSummaryModel(
      disposalDate = Dates.constructDate(10, 10, 2015),
      disposalValue = None,
      worthWhenSoldForLess = None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = Some(10001.00),
      disposalCosts = BigDecimal(10001),
      acquisitionValue = Some(0),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      acquisitionCosts = BigDecimal(10000),
      improvements = BigDecimal(30000),
      givenAway = true,
      sellForLess = None,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      howBecameOwner = Some("Bought"),
      boughtForLessThanWorth = Some(false)
    )
    val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

    lazy val view = views.gainSummaryPartial(gainAnswers, taxYearModel, 100, 11000)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "has a row for value when the property was given away" which {

      s"has the text '${summaryMessages.marketValue}'" in {
        doc.select("#disposalValue-text").text shouldBe summaryMessages.marketValue
      }

      "has the value '£10,001'" in {
        doc.select("#disposalValue-amount").text shouldBe "£10,001"
      }
    }
  }

  "property was sold outside known tax years" should {

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
    val taxYearModel = TaxYearModel("2018/19", isValidYear = false, "2016/17")

    lazy val view = views.gainSummaryPartial(gainAnswers, taxYearModel, 100, 11000)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    s"display a notice summary with text ${summaryMessages.noticeSummary}" in {
      doc.select("div.notice-wrapper").text should include(summaryMessages.noticeSummary)
    }
  }

  "there are losses to carry forward" should {

  }

}
