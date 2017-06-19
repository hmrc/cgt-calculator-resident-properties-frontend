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

package routes.properties

import org.scalatest._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import controllers.routes._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class RoutesSpec extends UnitSpec with GuiceOneAppPerSuite with Matchers {

  "The URL for the introduction Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/" in {
      val path = controllers.routes.PropertiesController.introduction().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/"
    }
  }

  "The URL for the disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-date" in {
      GainController.disposalDate().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-date"
    }
  }

  "The URL for the submit disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-date" in {
      GainController.submitDisposalDate().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-date"
    }
  }

  "The URL for the outside tax years Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/outside-tax-years" in {
      GainController.outsideTaxYears().url shouldEqual "/calculate-your-capital-gains/resident/properties/outside-tax-years"
    }
  }

  "The URL for the GET Sell Or Give Away action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/sell-or-give-away" in {
      GainController.sellOrGiveAway().url shouldEqual "/calculate-your-capital-gains/resident/properties/sell-or-give-away"
    }
  }

  "The URL for the POST Sell Or Give Away action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/sell-or-give-away" in {
      GainController.submitSellOrGiveAway().url shouldEqual "/calculate-your-capital-gains/resident/properties/sell-or-give-away"
    }
  }

  "The URL for the GET No Tax to Pay action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/no-tax-to-pay" in {
      GainController.noTaxToPay().url shouldEqual "/calculate-your-capital-gains/resident/properties/no-tax-to-pay"
    }
  }

  "The URL for GET for the Who Did You Give It To action" should {
    "be equal to /calculate-your-capital/gains/resident/properties/who-did-you-give-it-to" in {
      GainController.whoDidYouGiveItTo().url shouldEqual "/calculate-your-capital-gains/resident/properties/who-did-you-give-it-to"
    }
  }

  "The URL for POST for the Who Did You Give It To action" should {
    "be equal to /calculate-your-capital/gains/resident/properties/who-did-you-give-it-to" in {
      GainController.submitWhoDidYouGiveItTo().url shouldEqual "/calculate-your-capital-gains/resident/properties/who-did-you-give-it-to"
    }
  }

  "The URL for the GET Worth When Gave Away action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/worth-when-gave-away" in {
      GainController.worthWhenGaveAway().url shouldEqual "/calculate-your-capital-gains/resident/properties/worth-when-gave-away"
    }
  }

  "The URL for the POST Worth When Gave Away action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/worth-when-gave-away" in {
      GainController.submitWorthWhenGaveAway().url shouldEqual "/calculate-your-capital-gains/resident/properties/worth-when-gave-away"
    }
  }
  
  "The URL for the disposal value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-value" in {
      GainController.disposalValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-value"
    }
  }

  "The URL for the submit disposal value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-value" in {
      GainController.submitDisposalValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-value"
    }
  }

  "The URL for the property worth when sold Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/property-worth-when-sold" in {
      GainController.worthWhenSoldForLess().url shouldEqual "/calculate-your-capital-gains/resident/properties/worth-when-sold-for-less"
    }
  }

  "The URL for the submit property worth when sold Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/property-worth-when-sold" in {
      GainController.submitWorthWhenSoldForLess().url shouldEqual "/calculate-your-capital-gains/resident/properties/worth-when-sold-for-less"
    }
  }

  "The URL for the acquisition value or market value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-value" in {
      GainController.acquisitionValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-value"
    }
  }

  "The URL for the submit acquisition value or market value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-value" in {
      GainController.submitAcquisitionValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-value"
    }
  }

  "The URL for the Worth When Inherited Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/worth-when-inherited" in {
      GainController.worthWhenInherited().url shouldEqual "/calculate-your-capital-gains/resident/properties/worth-when-inherited"
    }
  }

  "The URL for the submit Worth When Inherited Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/worth-when-inherited" in {
      GainController.submitWorthWhenInherited().url shouldEqual "/calculate-your-capital-gains/resident/properties/worth-when-inherited"
    }
  }

  "The URL for the Worth When Bought For Less Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/worth-when-bought" in {
      GainController.worthWhenBoughtForLess().url shouldEqual "/calculate-your-capital-gains/resident/properties/worth-when-bought-for-less"
    }
  }

  "The URL for the submit Worth When Bought For Less Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/worth-when-inherited" in {
      GainController.submitWorthWhenBoughtForLess().url shouldEqual "/calculate-your-capital-gains/resident/properties/worth-when-bought-for-less"
    }
  }

  "The URL for the disposal costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-costs" in {
      GainController.disposalCosts().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-costs"
    }
  }

  "The URL for the submit disposal costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-costs" in {
      GainController.submitDisposalCosts().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-costs"
    }
  }

  "The URL for the ownerBeforeLegislationStart action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/owner-before-legislation-start" in {
      GainController.ownerBeforeLegislationStart().url shouldEqual "/calculate-your-capital-gains/resident/properties/owner-before-legislation-start"
    }
  }

  "The URL for the submit ownerBeforeLegislationStart action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/owner-before-legislation-start" in {
      GainController.submitOwnerBeforeLegislationStart().url shouldEqual "/calculate-your-capital-gains/resident/properties/owner-before-legislation-start"
    }
  }

  "The URL for the howBecameOwner action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/how-became-owner" in {
      GainController.howBecameOwner().url shouldEqual "/calculate-your-capital-gains/resident/properties/how-became-owner"
    }
  }

  "The URL for the submit howBecameOwner action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/how-became-owner" in {
      GainController.submitHowBecameOwner().url shouldEqual "/calculate-your-capital-gains/resident/properties/how-became-owner"
    }
  }

  "The URL for the boughtForLessThanWorth action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/bought-for-less-than-worth" in {
      GainController.boughtForLessThanWorth().url shouldEqual "/calculate-your-capital-gains/resident/properties/bought-for-less-than-worth"
    }
  }

  "The URL for the submit boughtForLessThanWorth action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/bought-for-less-than-worth" in {
      GainController.submitBoughtForLessThanWorth().url shouldEqual "/calculate-your-capital-gains/resident/properties/bought-for-less-than-worth"
    }
  }

  "The URL for the acquisition costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-costs" in {
      GainController.acquisitionCosts().url shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-costs"
    }
  }

  "The URL for the submit acquisition costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-costs" in {
      GainController.acquisitionCosts().url shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-costs"
    }
  }

  "The URL for the improvements Action" should {
    s"be equal to /calculate-your-capital-gains/resident/properties/improvements" in {
      GainController.improvements().url shouldEqual "/calculate-your-capital-gains/resident/properties/improvements"
    }
  }

  "The URL for the summary action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/summary" in {
      SummaryController.summary().url shouldEqual "/calculate-your-capital-gains/resident/properties/summary"
    }
  }

  "The URL for the lettings relief value input action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/lettings-relief-value" in {
      val path = controllers.routes.DeductionsController.lettingsReliefValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/lettings-relief-value"
    }
  }

  "The URL for the lettingsRelief action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/lettings-relief" in {
      DeductionsController.lettingsRelief().url shouldEqual "/calculate-your-capital-gains/resident/properties/lettings-relief"
    }
  }

  "The URL for the submitLettingsRelief action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/lettings-relief" in {
      DeductionsController.submitLettingsRelief().url shouldEqual "/calculate-your-capital-gains/resident/properties/lettings-relief"
    }
  }

  "The URL for the private residence relief action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/private-residence-relief" in {
      DeductionsController.privateResidenceRelief().url shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief"
    }
  }

  "The URL for the submit private residence relief action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/private-residence-relief" in {
      DeductionsController.submitPrivateResidenceRelief().url shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief"
    }
  }

  "The URL for the lossesBroughtForward action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/losses-brought-forward" in {
      DeductionsController.lossesBroughtForward().url shouldEqual "/calculate-your-capital-gains/resident/properties/losses-brought-forward"
    }
  }

   "The URL for the lossesBroughtForwardValue action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/losses-brought-forward-value" in {
      DeductionsController.lossesBroughtForwardValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/losses-brought-forward-value"
    }
  }

  "The URL for the submitLossesBroughtForwardValue action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/losses-brought-forward-value" in {
      DeductionsController.submitLossesBroughtForwardValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/losses-brought-forward-value"
    }
  }

  "The URL for the currentIncome action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/current-income" in {
      IncomeController.currentIncome().url shouldEqual "/calculate-your-capital-gains/resident/properties/current-income"
    }
  }

  "The URL for the personalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/personal-allowance" in {
      IncomeController.personalAllowance().url shouldEqual "/calculate-your-capital-gains/resident/properties/personal-allowance"
    }
  }

  "The URL for the submit personalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/personal-allowance" in {
      IncomeController.submitPersonalAllowance().url shouldEqual "/calculate-your-capital-gains/resident/properties/personal-allowance"
    }
  }

  "The URL for the propertyLivedIn action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/property-lived-in" in {
      DeductionsController.propertyLivedIn().url shouldEqual "/calculate-your-capital-gains/resident/properties/property-lived-in"
    }
  }

  "The URL for the submit propertyLivedIn action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/property-lived-in" in {
      DeductionsController.submitPropertyLivedIn().url shouldEqual "/calculate-your-capital-gains/resident/properties/property-lived-in"
    }
  }

  "The URL for the sellForLess action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/sell-for-less" in {
      GainController.sellForLess().url shouldEqual "/calculate-your-capital-gains/resident/properties/sell-for-less"
    }
  }

  "The URL for the submit sellForLess action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/sell-for-less" in {
      GainController.submitSellForLess().url shouldEqual "/calculate-your-capital-gains/resident/properties/sell-for-less"
    }
  }

  "The URL for the gainSummaryReport action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/gain-report" in {
      ReportController.gainSummaryReport().url shouldEqual "/calculate-your-capital-gains/resident/properties/gain-report"
    }
  }

  "The URL for the deductionsReport action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/deductions-report" in {
      ReportController.deductionsReport().url shouldEqual "/calculate-your-capital-gains/resident/properties/deductions-report"
    }
  }

  "The URL for the finalSummaryReport action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/final-report" in {
      ReportController.finalSummaryReport().url shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
    }
  }

  "The URL for the GET Private Residence Relief Value action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/private-residence-relief-value" in {
      DeductionsController.privateResidenceReliefValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief-value"
    }
  }

  "The URL for the POST Private Residence Relief Value action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/private-residence-relief-value" in {
      DeductionsController.submitPrivateResidenceReliefValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief-value"
    }
  }

  "The URL for the whatNextSAOverFourTimesAEA action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/what-next-zero-gain-over-limit" in {
      WhatNextSAController.whatNextSAOverFourTimesAEA().url shouldEqual "/calculate-your-capital-gains/resident/properties/what-next-sa-no-gain-over-limit"
    }
  }

  "The URL for the whatNextSANoGain action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/what-next-no-gain" in {
      WhatNextSAController.whatNextSANoGain().url shouldEqual "/calculate-your-capital-gains/resident/properties/what-next-sa-no-gain"
    }
  }

  "The URL for the whatNextSAGain action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/what-next-gain" in {
      WhatNextSAController.whatNextSAGain().url shouldEqual "/calculate-your-capital-gains/resident/properties/what-next-sa-gain"
    }
  }
}
