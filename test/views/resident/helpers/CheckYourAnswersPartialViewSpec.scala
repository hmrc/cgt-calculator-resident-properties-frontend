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

import assets.MessageLookup.Resident.{Properties => propertiesMessages}
import assets.MessageLookup.{Resident => residentMessages}
import assets.ModelsAsset._
import assets.{MessageLookup => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import views.BaseViewSpec
import views.html.playHelpers.checkYourAnswersPartial

class CheckYourAnswersPartialViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {
  lazy val checkYourAnswersPartialView = fakeApplication.injector.instanceOf[checkYourAnswersPartial]
  "The check your answers partial with as much filled in as possible" should {

    lazy val view: HtmlFormat.Appendable = checkYourAnswersPartialView(gainAnswersMostPossibles, Some(deductionAnswersMostPossibles), Some(taxYearModel), Some(incomeAnswers))(using testingMessages)
    lazy val doc: Document = Jsoup.parse(view.body)

    s"have a section for Your answers" which {

      "has a date output row for the Disposal Date" which {

        s"should have the question text '${commonMessages.DisposalDate.question}'" in {
          doc.select("#disposalDate-question").text shouldBe commonMessages.DisposalDate.question
        }

        "should have the date '10 October 2016'" in {
          doc.select("#disposalDate-date").text shouldBe "10 October 2016"
        }

        s"should have a change link to ${routes.GainController.disposalDate.url}" in {
          doc.select("#disposalDate-change-link a").attr("href") shouldBe routes.GainController.disposalDate.url
        }

        "has the question as part of the link" in {
          doc.select("#disposalDate-change-link a").text shouldBe s"${residentMessages.change} ${commonMessages.DisposalDate.question}"
        }

        "has the question component of the link is visuallyhidden" in {
          doc.select("#disposalDate-change-link > a > span").text shouldBe commonMessages.DisposalDate.question
        }
      }

      "has an option output row for sell or give away" which {

        s"should have the question text '${commonMessages.PropertiesSellOrGiveAway.heading}'" in {
          doc.select("#sellOrGiveAway-question").text shouldBe commonMessages.PropertiesSellOrGiveAway.heading
        }

        "should have the value 'Gave it away'" in {
          doc.select("#sellOrGiveAway-option").text shouldBe "Gave it away"
        }

        s"should have a change link to ${routes.GainController.sellOrGiveAway.url}" in {
          doc.select("#sellOrGiveAway-change-link a").attr("href") shouldBe routes.GainController.sellOrGiveAway.url
        }

        "has the question as part of the link" in {
          doc.select("#sellOrGiveAway-change-link a").text shouldBe s"${residentMessages.change} ${commonMessages.PropertiesSellOrGiveAway.heading}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#sellOrGiveAway-change-link > a > span").text shouldBe commonMessages.PropertiesSellOrGiveAway.heading
        }
      }

      //######################################################################################
      "has an option output row for who did you give it to" which {

        s"should have the question text '${commonMessages.WhoDidYouGiveItTo.title}'" in {
          doc.select("#whoDidYouGiveItTo-question").text shouldBe commonMessages.WhoDidYouGiveItTo.title
        }

        "should have the value 'Someone else'" in {
          doc.select("#whoDidYouGiveItTo-option").text shouldBe "Someone else"
        }

        s"should have a change link to ${routes.GainController.whoDidYouGiveItTo.url}" in {
          doc.select("#whoDidYouGiveItTo-change-link a").attr("href") shouldBe routes.GainController.whoDidYouGiveItTo.url
        }

        "has the question as part of the link" in {
          doc.select("#whoDidYouGiveItTo-change-link a").text shouldBe s"${residentMessages.change} ${commonMessages.WhoDidYouGiveItTo.title}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#whoDidYouGiveItTo-change-link > a > span").text shouldBe commonMessages.WhoDidYouGiveItTo.title
        }
      }

      "has a numeric output row for the Value when you gave it away" which {

        s"should have the question text '${propertiesMessages.PropertiesWorthWhenGaveAway.title}'" in {
          doc.select("#worthWhenGaveAway-question").text shouldBe propertiesMessages.PropertiesWorthWhenGaveAway.title
        }

        "should have the value '£10,000'" in {
          doc.select("#worthWhenGaveAway-amount").text shouldBe "£10,000"
        }

        s"should have a change link to ${routes.GainController.worthWhenGaveAway.url}" in {
          doc.select("#worthWhenGaveAway-change-link a").attr("href") shouldBe routes.GainController.worthWhenGaveAway.url
        }

        "has the question as part of the link" in {
          doc.select("#worthWhenGaveAway-change-link a").text shouldBe
            s"${residentMessages.change} ${propertiesMessages.PropertiesWorthWhenGaveAway.title}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#worthWhenGaveAway-change-link > a > span").text shouldBe
            propertiesMessages.PropertiesWorthWhenGaveAway.title
        }
      }
      //######################################################################################

      "has a numeric output row for the Disposal Costs" which {

        s"should have the question text '${commonMessages.DisposalCosts.title}'" in {
          doc.select("#disposalCosts-question").text shouldBe commonMessages.DisposalCosts.title
        }

        "should have the value '£10,000'" in {
          doc.select("#disposalCosts-amount").text shouldBe "£10,000"
        }

        s"should have a change link to ${routes.GainController.disposalCosts.url}" in {
          doc.select("#disposalCosts-change-link a").attr("href") shouldBe routes.GainController.disposalCosts.url
        }

      }

      "has an option output row for owner before april 1982" which {

        s"should have the question text '${propertiesMessages.OwnerBeforeLegislationStart.title}'" in {
          doc.select("#ownerBeforeLegislationStart-question").text shouldBe propertiesMessages.OwnerBeforeLegislationStart.heading
        }

        "should have the value 'Yes'" in {
          doc.select("#ownerBeforeLegislationStart-option").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.GainController.ownerBeforeLegislationStart.url}" in {
          doc.select("#ownerBeforeLegislationStart-change-link a").attr("href") shouldBe routes.GainController.ownerBeforeLegislationStart.url
        }

        "has the question as part of the link" in {
          doc.select("#ownerBeforeLegislationStart-change-link a").text shouldBe
            s"${residentMessages.change} ${propertiesMessages.OwnerBeforeLegislationStart.heading}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#ownerBeforeLegislationStart-change-link > a > span").text shouldBe
            propertiesMessages.OwnerBeforeLegislationStart.heading
        }
      }

      "has a numeric output row for the Value Before Legislation Start" which {

        s"should have the question text '${propertiesMessages.ValueBeforeLegislationStart.question}'" in {
          doc.select("#valueBeforeLegislationStart-question").text shouldBe propertiesMessages.ValueBeforeLegislationStart.question
        }

        "should have the value '£5,000'" in {
          doc.select("#valueBeforeLegislationStart-amount").text shouldBe "£5,000"
        }

        s"should have a change link to ${routes.GainController.valueBeforeLegislationStart.url}" in {
          doc.select("#valueBeforeLegislationStart-change-link a").attr("href") shouldBe routes.GainController.valueBeforeLegislationStart.url
        }

        "has the question as part of the link" in {
          doc.select("#valueBeforeLegislationStart-change-link a").text shouldBe
            s"${residentMessages.change} ${propertiesMessages.ValueBeforeLegislationStart.question}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#valueBeforeLegislationStart-change-link > a > span").text shouldBe
            propertiesMessages.ValueBeforeLegislationStart.question
        }
      }

      "has a numeric output row for the Acquisition Costs" which {

        s"should have the question text '${commonMessages.AcquisitionCosts.title}'" in {
          doc.select("#acquisitionCosts-question").text shouldBe commonMessages.AcquisitionCosts.pageHeading
        }

        "should have the value '£10,000'" in {
          doc.select("#acquisitionCosts-amount").text shouldBe "£10,000"
        }

        s"should have a change link to ${routes.GainController.acquisitionCosts.url}" in {
          doc.select("#acquisitionCosts-change-link a").attr("href") shouldBe routes.GainController.acquisitionCosts.url
        }

      }

      "has a numeric output row for the Improvements" which {

        s"should have the question text '${propertiesMessages.ImprovementsView.questionBefore}'" in {
          doc.select("#improvements-question").text shouldBe propertiesMessages.ImprovementsView.questionBefore
        }

        "should have the value '£30,000'" in {
          doc.select("#improvements-amount").text shouldBe "£30,000"
        }

        s"should have a change link to ${routes.GainController.improvements.url}" in {
          doc.select("#improvements-change-link a").attr("href") shouldBe routes.GainController.improvements.url
        }
      }

      "has an option output row for property lived in" which {

        s"should have the question text '${commonMessages.PropertyLivedIn.title}'" in {
          doc.select("#propertyLivedIn-question").text shouldBe commonMessages.PropertyLivedIn.title
        }

        "should have the value 'Yes'" in {
          doc.select("#propertyLivedIn-option").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.DeductionsController.propertyLivedIn.url}" in {
          doc.select("#propertyLivedIn-change-link a").attr("href") shouldBe routes.DeductionsController.propertyLivedIn.url
        }

        "has the question as part of the link" in {
          doc.select("#propertyLivedIn-change-link a").text shouldBe s"${residentMessages.change} ${commonMessages.PropertyLivedIn.title}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#propertyLivedIn-change-link > a > span").text shouldBe commonMessages.PropertyLivedIn.title
        }
      }

      "has an option output row for eligible for private residence relief in" which {

        s"should have the question text '${commonMessages.PrivateResidenceRelief.heading}'" in {
          doc.select("#privateResidenceRelief-question").text shouldBe commonMessages.PrivateResidenceRelief.heading
        }

        "should have the value 'Yes'" in {
          doc.select("#privateResidenceRelief-option").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.DeductionsController.privateResidenceRelief.url}" in {
          doc.select("#privateResidenceRelief-change-link a").attr("href") shouldBe routes.DeductionsController.privateResidenceRelief.url
        }

        "has the question as part of the link" in {
          doc.select("#privateResidenceRelief-change-link a").text shouldBe s"${residentMessages.change} ${commonMessages.PrivateResidenceRelief.heading}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#privateResidenceRelief-change-link > a > span").text shouldBe commonMessages.PrivateResidenceRelief.heading
        }
      }

      "has an option output row for lettings relief value" which {

        s"should have the question text '${commonMessages.LettingsReliefValue.question}'" in {
          doc.select("#lettingsReliefValue-question").text shouldBe commonMessages.LettingsReliefValue.question
        }

        "should have the value '£4500'" in {
          doc.select("#lettingsReliefValue-amount").text shouldBe "£4,500"
        }

        s"should have a change link to ${routes.DeductionsController.lettingsReliefValue.url}" in {
          doc.select("#lettingsReliefValue-change-link a").attr("href") shouldBe routes.DeductionsController.lettingsReliefValue.url
        }

        "has the question as part of the link" in {
          doc.select("#lettingsReliefValue-change-link a").text shouldBe s"${residentMessages.change} ${commonMessages.LettingsReliefValue.question}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#lettingsReliefValue-change-link > a > span").text shouldBe commonMessages.LettingsReliefValue.question
        }
      }

      "has an option output row for brought forward losses" which {

        s"should have the question text '${commonMessages.LossesBroughtForward.title("2015 to 2016")}'" in {
          doc.select("#broughtForwardLosses-question").text shouldBe commonMessages.LossesBroughtForward.question("2015 to 2016")
        }

        "should have the value 'Yes'" in {
          doc.select("#broughtForwardLosses-option").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.DeductionsController.lossesBroughtForward.url}" in {
          doc.select("#broughtForwardLosses-change-link a").attr("href") shouldBe routes.DeductionsController.lossesBroughtForward.url
        }

        "has the question as part of the link" in {
          doc.select("#broughtForwardLosses-change-link a").text shouldBe
            s"${residentMessages.change} ${commonMessages.LossesBroughtForward.question("2015 to 2016")}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#broughtForwardLosses-change-link > a > span").text shouldBe commonMessages.LossesBroughtForward.question("2015 to 2016")
        }
      }

      "has a numeric output row for brought forward losses value" which {

        s"should have the question text '${commonMessages.LossesBroughtForwardValue.title("2015 to 2016")}'" in {
          doc.select("#broughtForwardLossesValue-question").text shouldBe commonMessages.LossesBroughtForwardValue.question("2015 to 2016")
        }

        "should have the value '£10,000'" in {
          doc.select("#broughtForwardLossesValue-amount").text shouldBe "£10,000"
        }

        s"should have a change link to ${routes.DeductionsController.lossesBroughtForwardValue.url}" in {
          doc.select("#broughtForwardLossesValue-change-link a").attr("href") shouldBe routes.DeductionsController.lossesBroughtForwardValue.url
        }
      }

      "has a numeric output row for current income" which {

        s"should have the question text '${commonMessages.CurrentIncome.question("2015 to 2016")}'" in {
          doc.select("#currentIncome-question").text shouldBe commonMessages.CurrentIncome.question("2015 to 2016")
        }

        "should have the value '£0'" in {
          doc.select("#currentIncome-amount").text shouldBe "£0"
        }

        s"should have a change link to ${routes.IncomeController.currentIncome.url}" in {
          doc.select("#currentIncome-change-link a").attr("href") shouldBe routes.IncomeController.currentIncome.url
        }
      }
      "has a numeric output row for personal allowance" which {

        s"should have the question text '${commonMessages.PersonalAllowance.question("2015 to 2016")}'" in {
          doc.select("#personalAllowance-question").text shouldBe commonMessages.PersonalAllowance.question("2015 to 2016")
        }

        "should have the value '£0'" in {
          doc.select("#personalAllowance-amount").text shouldBe "£0"
        }

        s"should have a change link to ${routes.IncomeController.personalAllowance.url}" in {
          doc.select("#personalAllowance-change-link a").attr("href") shouldBe routes.IncomeController.personalAllowance.url
        }
      }
    }
  }

  "The check your answers partial with display links set to false" should {
    lazy val view: HtmlFormat.Appendable = checkYourAnswersPartialView(gainAnswersMostPossibles,
      Some(deductionAnswersMostPossibles), Some(taxYearModel), Some(incomeAnswers), displayLinks = false)(using testingMessages)
    lazy val doc: Document = Jsoup.parse(view.body)

    "have no links" in {
      doc.select("a").size() shouldBe 0
    }

    "have a row for dates" which {

      "has the correct text" in {
        lazy val questionDiv = doc.select("#disposalDate-question")
        questionDiv.text shouldBe commonMessages.DisposalDate.question
      }

      "has the correct value" in {
        lazy val amountDiv = doc.select("#disposalDate-date")
        amountDiv.text shouldBe "10 October 2016"
      }
    }

    "have a row for numeric values" which {
      "has the correct text" in {
        lazy val questionDiv = doc.select("#worthWhenGaveAway-question")
        questionDiv.text shouldBe commonMessages.NonResident.MarketValue.disposalGaveAwayQuestion
      }
      "has the correct value" in {
        lazy val amountDiv = doc.select("#worthWhenGaveAway-amount")
        amountDiv.text shouldBe "£10,000"

      }
    }

    "have a row for option values" which {
      "has the correct text" in {
        lazy val questionDiv = doc.select("#sellOrGiveAway-question")
        questionDiv.text shouldBe commonMessages.PropertiesSellOrGiveAway.heading
      }
      "has the correct value" in {
        lazy val amountDiv = doc.select("#sellOrGiveAway-option")
        amountDiv.text shouldBe "Gave it away"

      }
    }

    "have a row for text values" which {
      "has the correct text" in {
        lazy val questionDiv = doc.select("#whoDidYouGiveItTo-question")
        questionDiv.text shouldBe commonMessages.WhoDidYouGiveItTo.title
      }
      "has the correct value" in {
        lazy val amountDiv = doc.select("#whoDidYouGiveItTo-option")
        amountDiv.text shouldBe "Someone else"

      }
    }
  }

  "The check your answers partial with as little filled in as possible" should {

    lazy val view: HtmlFormat.Appendable = checkYourAnswersPartialView(gainAnswersMostPossibles,
      Some(deductionAnswersLeastPossibles), Some(taxYearModel), Some(incomeAnswers))(using testingMessages)
    lazy val doc: Document = Jsoup.parse(view.body)

    s"have a section for Your answers" which {

      "has an option output row for property lived in" which {

        s"should have the question text '${commonMessages.PropertyLivedIn.title}'" in {
          doc.select("#propertyLivedIn-question").text shouldBe commonMessages.PropertyLivedIn.title
        }

        "should have the value 'No'" in {
          doc.select("#propertyLivedIn-option").text shouldBe "No"
        }

        s"should have a change link to ${routes.DeductionsController.propertyLivedIn.url}" in {
          doc.select("#propertyLivedIn-change-link a").attr("href") shouldBe routes.DeductionsController.propertyLivedIn.url
        }

        "has the question as part of the link" in {
          doc.select("#propertyLivedIn-change-link a").text shouldBe s"${residentMessages.change} ${commonMessages.PropertyLivedIn.title}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#propertyLivedIn-change-link > a > span").text shouldBe commonMessages.PropertyLivedIn.title
        }
      }

      "does not have an option output row for the eligible for private residence relief" which {

        s"should not display" in {
          doc.select("#privateResidenceRelief-question").size() shouldBe 0
        }
      }

      "does not have an option output row for private residence relief value" which {

        s"should not display" in {
          doc.select("#privateResidenceReliefValue-question").size() shouldBe 0
        }
      }

      "does not have an option output row for the lettings relief" which {

        s"should not display" in {
          doc.select("#lettingsRelief-question").size() shouldBe 0
        }
      }

      "has an option output row for brought forward losses" which {

        s"should have the question text '${commonMessages.LossesBroughtForward.title("2015 to 2016")}'" in {
          doc.select("#broughtForwardLosses-question").text shouldBe commonMessages.LossesBroughtForward.question("2015 to 2016")
        }

        "should have the value 'No'" in {
          doc.select("#broughtForwardLosses-option").text shouldBe "No"
        }

        s"should have a change link to ${routes.DeductionsController.lossesBroughtForward.url}" in {
          doc.select("#broughtForwardLosses-change-link a").attr("href") shouldBe routes.DeductionsController.lossesBroughtForward.url
        }

        "has the question as part of the link" in {
          doc.select("#broughtForwardLosses-change-link a").text shouldBe
            s"${residentMessages.change} ${commonMessages.LossesBroughtForward.question("2015 to 2016")}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#broughtForwardLosses-change-link > a > span").text shouldBe commonMessages.LossesBroughtForward.question("2015 to 2016")
        }
      }

      "does not have an option output row for the brought forward losses value" which {

        s"should not display" in {
          doc.select("#broughtForwardLossesValue-question").size() shouldBe 0
        }
      }
    }
  }
}
