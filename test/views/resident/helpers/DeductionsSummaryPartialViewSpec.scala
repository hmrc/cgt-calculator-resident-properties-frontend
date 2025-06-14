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

package views.resident.helpers

import _root_.views.BaseViewSpec
import assets.MessageLookup.{SummaryDetails => summaryMessages}
import common.{CommonPlaySpec, Dates, WithCommonFakeApplication}
import models.resident._
import models.resident.properties.{ChargeableGainAnswers, PropertyLivedInModel, YourAnswersSummaryModel}
import org.jsoup.Jsoup
import views.html.playHelpers.deductionsSummaryPartial

class DeductionsSummaryPartialViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  "DeductionsSummaryPartial" when {
    lazy val deductionsSummaryPartialView = fakeApplication.injector.instanceOf[deductionsSummaryPartial]
    "the property was sold inside tax years, bought after legislation start," +
      " with no reliefs or brought forward losses, with £0 taxable gain and AEA remaining" should {

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
      val results = ChargeableGainResultModel(
        gain = 30000,
        chargeableGain = 0,
        aeaUsed = 900,
        aeaRemaining = 1000,
        deductions = 30000,
        allowableLossesRemaining = 0,
        broughtForwardLossesRemaining = 0,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = deductionsSummaryPartialView(gainAnswers, deductionAnswers, results,
        taxYearModel, 100)(using testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has a banner" which {
        lazy val banner = doc.select("#tax-owed-banner")

        "contains a h2" which {
          lazy val h2 = banner.select("h2")

          s"has the text '£0.00'" in {
            h2.text() shouldEqual "£0.00"
          }
        }

        "contains a h1" which {
          lazy val h1 = banner.select("h1")

          s"has the text ${summaryMessages.cgtToPay("2015 to 2016")}" in {
            h1.text() shouldEqual summaryMessages.cgtToPay("2015 to 2016")
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

          "has a h3 tag" which {

            s"has the text '${summaryMessages.yourTotalGain}'" in {
              doc.getElementsByClass("govuk-table__caption govuk-table__caption--m").text `contains` summaryMessages.yourTotalGain
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

            "has the value '£30,000'" in {
              div.select("#totalGain-amount").text shouldBe "£30,000"
            }
          }
        }

        "has a div for deductions" which {

          lazy val div = doc.getElementById("yourDeductions")

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

            "has the value '£900'" in {
              div.select("#aeaUsed-amount").text shouldBe "£900"
            }
          }

          "not have a row for brought forward losses used" in {
            div.select("#lossesUsed-text") shouldBe empty
          }

          "has a row for total deductions" which {

            s"has the text '${summaryMessages.totalDeductions}'" in {
              div.select("#totalDeductions-text").text shouldBe summaryMessages.totalDeductions
            }

            "has the value '£30,000'" in {
              div.select("#totalDeductions-amount").text shouldBe "£30,000"
            }
          }
        }

        "has a div for Taxable Gain" which {

          lazy val div = doc.getElementById("yourTaxableGain")

          "has a h3 tag" which {

            s"has the text '${summaryMessages.yourTaxableGain}'" in {
              div.getElementsByClass("govuk-table__caption govuk-table__caption--m").text shouldBe summaryMessages.yourTaxableGain
            }
          }

          "has a row for gain" which {
            s"has the text '${summaryMessages.totalGain}'" in {
              div.select("#gain-text").text shouldBe summaryMessages.totalGain
            }

            "has the value '£30,000'" in {
              div.select("#gain-amount").text shouldBe "£30,000"
            }
          }

          "has a row for minus deductions" which {
            s"has the text '${summaryMessages.minusDeductions}'" in {
              div.select("#minusDeductions-text").text shouldBe summaryMessages.minusDeductions
            }

            "has the value '£30,000'" in {
              div.select("#minusDeductions-amount").text shouldBe "£30,000"
            }
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

          "has a row for tax to pay" which {

            s"has the text ${summaryMessages.taxToPay}" in {
              doc.select("#taxToPay-text").text shouldBe summaryMessages.taxToPay
            }

            "has the value '£0'" in {
              doc.select("#taxToPay-amount").text shouldBe "£0"
            }
          }
        }
      }

      "have a section for the Your remaining deductions" which {

        "has a div for remaining deductions" which {

          lazy val div = doc.getElementById("remainingDeductions")

          "has a h2 tag" which {

            s"has the text ${summaryMessages.remainingDeductions}" in {
              div.select("#remainingDeductions > table > caption").text shouldBe summaryMessages.remainingDeductions
            }
          }

          "has a row for annual exempt amount left" which {
            s"has the text ${summaryMessages.remainingAnnualExemptAmount("2015 to 2016")}" in {
              div.select("#aeaRemaining-text").text shouldBe summaryMessages.remainingAnnualExemptAmount("2015 to 2016")
            }

            "has the value '£1,000'" in {
              div.select("#aeaRemaining-amount").text shouldBe "£1,000"
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
      val gainAnswers = YourAnswersSummaryModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = None,
        worthWhenSoldForLess = Some(100000.00),
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
        sellForLess = Some(true),
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
      val results = ChargeableGainResultModel(
        gain = 30000,
        chargeableGain = 0,
        aeaUsed = 900,
        aeaRemaining = 1000,
        deductions = 30000,
        allowableLossesRemaining = 0,
        broughtForwardLossesRemaining = 0,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = deductionsSummaryPartialView(gainAnswers, deductionAnswers, results, taxYearModel, 100)(using testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for worth when sold for less" which {

        s"has the text '${summaryMessages.disposalValue}'" in {
          doc.select("#disposalValue-text").text shouldBe summaryMessages.disposalValue
        }

        "has the value '£100,000'" in {
          doc.select("#disposalValue-amount").text shouldBe "£100,000"
        }
      }
    }

    "reliefs are used" should {

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
      val results = ChargeableGainResultModel(
        gain = 30000,
        chargeableGain = 0,
        aeaUsed = 900,
        aeaRemaining = 1000,
        deductions = 30000,
        allowableLossesRemaining = 0,
        broughtForwardLossesRemaining = 0,
        lettingReliefsUsed = Some(BigDecimal(500)),
        prrUsed = Some(BigDecimal(125)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = deductionsSummaryPartialView(gainAnswers, deductionAnswers, results, taxYearModel, 100)(using testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for reliefs used" which {
        s"has the text '${summaryMessages.reliefsUsed}'" in {
          doc.select("#reliefsUsed-text").text shouldBe summaryMessages.reliefsUsed
        }

        "has the value '£625'" in {
          doc.select("#reliefsUsed-amount").text shouldBe "£625"
        }
      }
    }

    "a Brought Forward Loss is entered and none remains" should {

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
        broughtForwardModel = Some(LossesBroughtForwardModel(true)),
        broughtForwardValueModel = Some(LossesBroughtForwardValueModel(35.00)),
        propertyLivedInModel = Some(PropertyLivedInModel(false)),
        privateResidenceReliefModel = None,
        privateResidenceReliefValueModel = None,
        lettingsReliefModel = None,
        lettingsReliefValueModel = None
      )
      val results = ChargeableGainResultModel(
        gain = 30000,
        chargeableGain = 0,
        aeaUsed = 900,
        aeaRemaining = 1000,
        deductions = 30000,
        allowableLossesRemaining = 0,
        broughtForwardLossesRemaining = 0,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 35,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = deductionsSummaryPartialView(gainAnswers, deductionAnswers, results, taxYearModel, 100)(using testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for brought forward losses used" which {
        s"has the title ${summaryMessages.broughtForwardLossesUsed}" in {
          doc.select("#lossesUsed-text").text shouldBe summaryMessages.broughtForwardLossesUsed
        }

        "has the amount of '£35'" in {
          doc.select("#lossesUsed-amount").text shouldBe "£35"
        }
      }

      "not have a row for brought forward losses remaining" in {
         doc.select("#broughtForwardLossesRemaining-text") shouldBe empty
      }

    }

    "a Brought Forward Loss is entered and some remains" should {

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
        broughtForwardModel = Some(LossesBroughtForwardModel(true)),
        broughtForwardValueModel = Some(LossesBroughtForwardValueModel(35.00)),
        propertyLivedInModel = Some(PropertyLivedInModel(false)),
        privateResidenceReliefModel = None,
        privateResidenceReliefValueModel = None,
        lettingsReliefModel = None,
        lettingsReliefValueModel = None
      )
      val results = ChargeableGainResultModel(
        gain = 30000,
        chargeableGain = 0,
        aeaUsed = 900,
        aeaRemaining = 1000,
        deductions = 30000,
        allowableLossesRemaining = 0,
        broughtForwardLossesRemaining = 5,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 35,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = deductionsSummaryPartialView(gainAnswers, deductionAnswers, results, taxYearModel, 100)(using testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for brought forward losses used" which {
        s"has the title ${summaryMessages.broughtForwardLossesUsed}" in {
          doc.select("#lossesUsed-text").text shouldBe summaryMessages.broughtForwardLossesUsed
        }

        "has the amount of '£35'" in {
          doc.select("#lossesUsed-amount").text shouldBe "£35"
        }
      }

      "have a row for brought forward losses remaining" which {
        s"has the title ${summaryMessages.broughtForwardLossesRemaining}" in {
          doc.select("#broughtForwardLossesRemaining-text").text shouldBe summaryMessages.broughtForwardLossesRemaining
        }

        "has the amount of '£35'" in {
          doc.select("#broughtForwardLossesRemaining-amount").text shouldBe "£5"
        }
      }

    }

    "the property was bought before 31 March 1982" should {

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
        ownerBeforeLegislationStart = true,
        valueBeforeLegislationStart = Some(350000.00),
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
      val results = ChargeableGainResultModel(
        gain = 30000,
        chargeableGain = 0,
        aeaUsed = 900,
        aeaRemaining = 1000,
        deductions = 30000,
        allowableLossesRemaining = 0,
        broughtForwardLossesRemaining = 0,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = deductionsSummaryPartialView(gainAnswers, deductionAnswers, results, taxYearModel, 100)(using testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for acquisition value" which {
        s"has the text '${summaryMessages.acquisitionValueBeforeLegislation}'" in {
          doc.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValueBeforeLegislation
        }

        "has the value '£350,000'" in {
          doc.select("#acquisitionValue-amount").text shouldBe "£350,000"
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
        disposalCosts = BigDecimal(10000),
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
      val deductionAnswers = ChargeableGainAnswers(
        broughtForwardModel = Some(LossesBroughtForwardModel(false)),
        broughtForwardValueModel = None,
        propertyLivedInModel = Some(PropertyLivedInModel(false)),
        privateResidenceReliefModel = None,
        privateResidenceReliefValueModel = None,
        lettingsReliefModel = None,
        lettingsReliefValueModel = None
      )
      val results = ChargeableGainResultModel(
        gain = 30000,
        chargeableGain = 0,
        aeaUsed = 900,
        aeaRemaining = 1000,
        deductions = 30000,
        allowableLossesRemaining = 0,
        broughtForwardLossesRemaining = 0,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = deductionsSummaryPartialView(gainAnswers, deductionAnswers, results, taxYearModel, 100)(using testingMessages)
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
      val results = ChargeableGainResultModel(
        gain = 30000,
        chargeableGain = 0,
        aeaUsed = 900,
        aeaRemaining = 1000,
        deductions = 30000,
        allowableLossesRemaining = 0,
        broughtForwardLossesRemaining = 0,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2018/19", isValidYear = false, "2016/17")

      lazy val view = deductionsSummaryPartialView(gainAnswers, deductionAnswers, results, taxYearModel, 100)(using testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      s"display a notice summary with text ${summaryMessages.noticeSummary}" in {
        doc.body().select(".govuk-warning-text").text should include(summaryMessages.noticeSummary)
      }
    }

    "property was inherited" should {
      val gainAnswers = YourAnswersSummaryModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = Some(100000),
        worthWhenSoldForLess = None,
        whoDidYouGiveItTo = None,
        worthWhenGaveAway = None,
        disposalCosts = BigDecimal(10000),
        acquisitionValue = None,
        worthWhenInherited = Some(300000),
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        acquisitionCosts = BigDecimal(10000),
        improvements = BigDecimal(30000),
        givenAway = false,
        sellForLess = Some(false),
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        howBecameOwner = Some("Inherited"),
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
      val results = ChargeableGainResultModel(
        gain = 30000,
        chargeableGain = 0,
        aeaUsed = 900,
        aeaRemaining = 1000,
        deductions = 30000,
        allowableLossesRemaining = 0,
        broughtForwardLossesRemaining = 0,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = deductionsSummaryPartialView(gainAnswers, deductionAnswers, results,
        taxYearModel, 100)(using testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for acquisition value" which {
        s"has the text '${summaryMessages.acquisitionValue}'" in {
          doc.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValue
        }

        "has the value '£300,000'" in {
          doc.select("#acquisitionValue-amount").text shouldBe "£300,000"
        }
      }
    }

    "property was gifted" should {
      val gainAnswers = YourAnswersSummaryModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = Some(100000),
        worthWhenSoldForLess = None,
        whoDidYouGiveItTo = None,
        worthWhenGaveAway = None,
        disposalCosts = BigDecimal(10000),
        acquisitionValue = None,
        worthWhenInherited = None,
        worthWhenGifted = Some(300000),
        worthWhenBoughtForLess = None,
        acquisitionCosts = BigDecimal(10000),
        improvements = BigDecimal(30000),
        givenAway = false,
        sellForLess = Some(false),
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        howBecameOwner = Some("Gifted"),
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
      val results = ChargeableGainResultModel(
        gain = 30000,
        chargeableGain = 0,
        aeaUsed = 900,
        aeaRemaining = 1000,
        deductions = 30000,
        allowableLossesRemaining = 0,
        broughtForwardLossesRemaining = 0,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = deductionsSummaryPartialView(gainAnswers, deductionAnswers, results,
        taxYearModel, 100)(using testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for acquisition value" which {
        s"has the text '${summaryMessages.acquisitionValue}'" in {
          doc.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValue
        }

        "has the value '£300,000'" in {
          doc.select("#acquisitionValue-amount").text shouldBe "£300,000"
        }
      }
    }

    "property was bought for less than worth" should {
      val gainAnswers = YourAnswersSummaryModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = Some(100000),
        worthWhenSoldForLess = None,
        whoDidYouGiveItTo = None,
        worthWhenGaveAway = None,
        disposalCosts = BigDecimal(10000),
        acquisitionValue = None,
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = Some(300000),
        acquisitionCosts = BigDecimal(10000),
        improvements = BigDecimal(30000),
        givenAway = false,
        sellForLess = Some(false),
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        howBecameOwner = Some("Bought"),
        boughtForLessThanWorth = Some(true)
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
      val results = ChargeableGainResultModel(
        gain = 30000,
        chargeableGain = 0,
        aeaUsed = 900,
        aeaRemaining = 1000,
        deductions = 30000,
        allowableLossesRemaining = 0,
        broughtForwardLossesRemaining = 0,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = deductionsSummaryPartialView(gainAnswers, deductionAnswers, results,
        taxYearModel, 100)(using testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for acquisition value" which {
        s"has the text '${summaryMessages.acquisitionValue}'" in {
          doc.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValue
        }

        "has the value '£300,000'" in {
          doc.select("#acquisitionValue-amount").text shouldBe "£300,000"
        }
      }
    }
  }
}
