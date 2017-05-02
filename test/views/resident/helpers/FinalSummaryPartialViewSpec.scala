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
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.{IncomeAnswersModel, LossesBroughtForwardModel, TaxYearModel, TotalGainAndTaxOwedModel}
import models.resident.properties.{ChargeableGainAnswers, PropertyLivedInModel, YourAnswersSummaryModel}
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits.applicationMessages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{SummaryDetails => summaryMessages}
import views.html.{helpers => views}

class FinalSummaryPartialViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "FinalSummaryPartial" when {

    val incomeAnswers = IncomeAnswersModel(
      currentIncomeModel = Some(CurrentIncomeModel(0)),
      personalAllowanceModel = Some(PersonalAllowanceModel(0))
    )

    "the property was sold inside tax years, bought after legislation start," +
      " with no reliefs or brought forward losses and taxed at 18%" should {

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
        aeaUsed = 10,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 20000,
        firstRate = 18,
        secondBand = None,
        secondRate = None,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0,
        baseRateTotal = 30000
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = views.finalSummaryPartial(gainAnswers, deductionAnswers, incomeAnswers, results,
        taxYearModel, None, None, 100, 100, 0)(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has a banner" which {
        lazy val banner = doc.select("#tax-owed-banner")

        "contains a h1" which {
          lazy val h1 = banner.select("h1")

          s"has the text '£3,600.00'" in {
            h1.text() shouldEqual "£3,600.00"
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

        "has a div for total gain" which {

          lazy val div = doc.select("#totalGain").get(0)

          "has a h3 tag" which {

            s"has the text '${summaryMessages.yourTotalGain}'" in {
              div.select("h3").text shouldBe summaryMessages.yourTotalGain
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
              div.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValue
            }

            "has the value '£0'" in {
              div.select("#acquisitionValue-amount").text shouldBe "£0"
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

          lazy val div = doc.select("#deductions")

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

            "has the value '£10'" in {
              div.select("#aeaUsed-amount").text shouldBe "£10"
            }
          }

          "not have a row for brought forward losses used" in {
            div.select("#lossesUsed-text") shouldBe empty
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

          lazy val div = doc.select("#taxableGain")

          "has a h3 tag" which {

            s"has the text '${summaryMessages.yourTaxableGain}'" in {
              div.select("h3").text shouldBe summaryMessages.yourTaxableGain
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

          lazy val div = doc.select("#taxRate")

          "has a h3 tag" which {

            s"has the text ${summaryMessages.yourTaxRate}" in {
              div.select("h3").text shouldBe summaryMessages.yourTaxRate
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

          "does not have a row for second band" in {
            div.select("#secondBand-text") shouldBe empty
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

          "has a h2 tag" which {

            s"has the text ${summaryMessages.remainingDeductions}" in {
              div.select("h2").text shouldBe summaryMessages.remainingDeductions
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
    }

    "the property was sold for less than worth" should {

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

      lazy val view = views.finalSummaryPartial(gainAnswers, deductionAnswers, incomeAnswers, results, taxYearModel,
        None, None, 100, 100, 0)(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has a numeric output row and a tax rate" which {

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
    }

    "reliefs are used" should {

    }

    "a Brought Forward Loss is entered and none remains" should {

    }

    "a Brought Forward Loss remains" should {
      //      "have a row for losses to carry forward from tax years" in {
      //        s"has the text ${summaryMessages.lossesToCarryForwardFromTaxYears("2015 to 2016")}" in {
      //          div.select("#lossesToCarryForwardTaxYears-text").text shouldBe summaryMessages.lossesToCarryForwardFromTaxYears("2015 to 2016")
      //        }
      //
      //        "has the value '£???'" in {
      //          div.select("#lossesToCarryForwardTaxYears-amount").text shouldBe "£???"
      //        }
      //      }
    }

    "the property was bought before 31 March 1982" should {

    }

    "the property was given away" should {

    }
  }
}
