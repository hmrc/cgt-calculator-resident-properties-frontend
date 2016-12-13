/*
 * Copyright 2016 HM Revenue & Customs
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

package constructors.resident.properties

import common.Dates
import common.resident.HowYouBecameTheOwnerKeys._
import models.resident._
import models.resident.properties._
import uk.gov.hmrc.play.test.UnitSpec

class CalculateRequestConstructorSpec extends UnitSpec {

  "Calling determineDisposalValueToUse" when {

    "The property was: not gifted & not sold for less & was sold for £5000" should {

      "return £5000 as the disposal value" in {

        val answers = YourAnswersSummaryModel(
          givenAway = false,
          sellForLess = Some(false),
          disposalValue = Some(BigDecimal(5000)),
          worthWhenGaveAway = None,
          worthWhenSoldForLess = None,
          /* Parameters from this point onward do not effect the result of the method - dummy values used */
          whoDidYouGiveItTo = None,
          disposalDate = Dates.constructDate(10, 2, 2016),
          worthWhenBoughtForLess = None,
          disposalCosts = BigDecimal(0),
          acquisitionValue = Some(BigDecimal(500)),
          worthWhenInherited = None,
          acquisitionCosts = BigDecimal(100),
          improvements = BigDecimal(10),
          ownerBeforeLegislationStart = true,
          valueBeforeLegislationStart = Some(BigDecimal(5000)),
          howBecameOwner = Some(boughtIt),
          boughtForLessThanWorth = Some(false),
          worthWhenGifted = None
        )

        CalculateRequestConstructor.determineDisposalValueToUse(answers) shouldEqual 5000
      }
    }

    "The property was: gifted & was worth £4000 when gave away" should {

      "return £4000 as the disposal value" in {

        val answers = YourAnswersSummaryModel(
          givenAway = true,
          sellForLess = None,
          disposalValue = None,
          worthWhenGaveAway = Some(BigDecimal(4000)),
          worthWhenSoldForLess = None,
          /* Parameters from this point onward do not effect the result of the method - dummy values used */
          whoDidYouGiveItTo = None,
          disposalDate = Dates.constructDate(10, 2, 2016),
          worthWhenBoughtForLess = None,
          disposalCosts = BigDecimal(0),
          acquisitionValue = Some(BigDecimal(500)),
          worthWhenInherited = None,
          acquisitionCosts = BigDecimal(100),
          improvements = BigDecimal(10),
          ownerBeforeLegislationStart = true,
          valueBeforeLegislationStart = Some(BigDecimal(5000)),
          howBecameOwner = Some(boughtIt),
          boughtForLessThanWorth = Some(false),
          worthWhenGifted = None
        )

        CalculateRequestConstructor.determineDisposalValueToUse(answers) shouldEqual 4000
      }
    }

    "The property was: not gifted & was sold for less anf the proper was worth 3000 when sold for less" should {

      "return £3000 as the disposal value" in {

        val answers = YourAnswersSummaryModel(
          givenAway = false,
          sellForLess = Some(true),
          disposalValue = None,
          worthWhenGaveAway = None,
          worthWhenSoldForLess = Some(3000),
          /* Parameters from this point onward do not effect the result of the method - dummy values used */
          whoDidYouGiveItTo = None,
          disposalDate = Dates.constructDate(10, 2, 2016),
          worthWhenBoughtForLess = Some(3000),
          disposalCosts = BigDecimal(0),
          acquisitionValue = Some(BigDecimal(500)),
          worthWhenInherited = None,
          acquisitionCosts = BigDecimal(100),
          improvements = BigDecimal(10),
          ownerBeforeLegislationStart = true,
          valueBeforeLegislationStart = Some(BigDecimal(5000)),
          howBecameOwner = Some(boughtIt),
          boughtForLessThanWorth = Some(false),
          worthWhenGifted = None
        )

        CalculateRequestConstructor.determineDisposalValueToUse(answers) shouldEqual 3000
      }
    }
  }

  "Calling determineAcquisitionValueToUse" when {

    /* Property was bought for market value */
    "The property was acquired for £4000 and was: " +
      "\n     >> acquired after 31 March 1982" +
      "\n     >> not inherited" +
      "\n     >> not given away as a gift" +
      "\n     >> not sold for less than Market Value" should {

      "return £4000 as the acquisition value" in {

        val answers = YourAnswersSummaryModel(
          ownerBeforeLegislationStart = false,
          howBecameOwner = Some(boughtIt),
          boughtForLessThanWorth = Some(false),
          valueBeforeLegislationStart = None,
          worthWhenInherited = None,
          worthWhenGifted = None,
          worthWhenBoughtForLess = None,
          acquisitionValue = Some(BigDecimal(4000)),
          /* Parameters from this point onward do not effect the result of the method - dummy values used */
          whoDidYouGiveItTo = None,
          givenAway = false,
          sellForLess = Some(false),
          disposalValue = Some(BigDecimal(5000)),
          worthWhenGaveAway = None,
          worthWhenSoldForLess = None,
          disposalDate = Dates.constructDate(10, 2, 2016),
          disposalCosts = BigDecimal(0),
          acquisitionCosts = BigDecimal(100),
          improvements = BigDecimal(10)
        )

        CalculateRequestConstructor.determineAcquisitionValueToUse(answers) shouldEqual 4000
      }
    }

    /* Property was bought for less than market value */
    "The property was acquired for £300 and was: " +
      "\n     >> acquired after 31 March 1982" +
      "\n     >> not inherited" +
      "\n     >> not given away as a gift" +
      "\n     >> sold for less than Market Value" should {

      "return £300 as the acquisition value" in {

        val answers = YourAnswersSummaryModel(
          ownerBeforeLegislationStart = false,
          howBecameOwner = Some(boughtIt),
          boughtForLessThanWorth = Some(true),
          valueBeforeLegislationStart = None,
          worthWhenInherited = None,
          worthWhenGifted = None,
          worthWhenBoughtForLess = Some(BigDecimal(300)),
          acquisitionValue = None,
          /* Parameters from this point onward do not effect the result of the method - dummy values used */
          whoDidYouGiveItTo = None,
          givenAway = false,
          sellForLess = Some(false),
          disposalValue = Some(BigDecimal(5000)),
          worthWhenGaveAway = None,
          worthWhenSoldForLess = None,
          disposalDate = Dates.constructDate(10, 2, 2016),
          disposalCosts = BigDecimal(0),
          acquisitionCosts = BigDecimal(100),
          improvements = BigDecimal(10)
        )

        CalculateRequestConstructor.determineAcquisitionValueToUse(answers) shouldEqual 300
      }
    }

    /* Property was gifted */
    "The property was acquired for £390 and was: " +
      "\n     >> acquired after 31 March 1982" +
      "\n     >> not inherited" +
      "\n     >> given away as a gift" should {

      "return £290 as the acquisition value" in {

        val answers = YourAnswersSummaryModel(
          ownerBeforeLegislationStart = false,
          howBecameOwner = Some(giftedIt),
          boughtForLessThanWorth = None,
          valueBeforeLegislationStart = None,
          worthWhenInherited = None,
          worthWhenGifted = Some(BigDecimal(290)),
          worthWhenBoughtForLess = None,
          acquisitionValue = None,
          /* Parameters from this point onward do not effect the result of the method - dummy values used */
          whoDidYouGiveItTo = None,
          givenAway = false,
          sellForLess = Some(false),
          disposalValue = Some(BigDecimal(5000)),
          worthWhenGaveAway = None,
          worthWhenSoldForLess = None,
          disposalDate = Dates.constructDate(10, 2, 2016),
          disposalCosts = BigDecimal(0),
          acquisitionCosts = BigDecimal(100),
          improvements = BigDecimal(10)
        )

        CalculateRequestConstructor.determineAcquisitionValueToUse(answers) shouldEqual 290
      }
    }

    /* Property was inherited */
    "The property was acquired for £230 and was: " +
      "\n     >> acquired after 31 March 1982" +
      "\n     >> inherited" should {

      "return £230 as the acquisition value" in {

        val answers = YourAnswersSummaryModel(
          ownerBeforeLegislationStart = false,
          howBecameOwner = Some(inheritedIt),
          boughtForLessThanWorth = None,
          valueBeforeLegislationStart = None,
          worthWhenInherited = Some(BigDecimal(230)),
          worthWhenGifted = None,
          worthWhenBoughtForLess = None,
          acquisitionValue = None,
          /* Parameters from this point onward do not effect the result of the method - dummy values used */
          whoDidYouGiveItTo = None,
          givenAway = false,
          sellForLess = Some(false),
          disposalValue = Some(BigDecimal(5000)),
          worthWhenGaveAway = None,
          worthWhenSoldForLess = None,
          disposalDate = Dates.constructDate(10, 2, 2016),
          disposalCosts = BigDecimal(0),
          acquisitionCosts = BigDecimal(100),
          improvements = BigDecimal(10)
        )

        CalculateRequestConstructor.determineAcquisitionValueToUse(answers) shouldEqual 230
      }
    }

    /* Property was acquired on/before 31 March 1982 */
    "The property was acquired for £29 and was: " +
      "\n     >> acquired on/before 31 March 1982" should {

      "return £29 as the acquisition value" in {

        val answers = YourAnswersSummaryModel(
          ownerBeforeLegislationStart = true,
          howBecameOwner = None,
          boughtForLessThanWorth = None,
          valueBeforeLegislationStart = Some(BigDecimal(29)),
          worthWhenInherited = None,
          worthWhenGifted = None,
          worthWhenBoughtForLess = None,
          acquisitionValue = None,
          /* Parameters from this point onward do not effect the result of the method - dummy values used */
          whoDidYouGiveItTo = None,
          givenAway = false,
          sellForLess = Some(false),
          disposalValue = Some(BigDecimal(5000)),
          worthWhenGaveAway = None,
          worthWhenSoldForLess = None,
          disposalDate = Dates.constructDate(10, 2, 2016),
          disposalCosts = BigDecimal(0),
          acquisitionCosts = BigDecimal(100),
          improvements = BigDecimal(10)
        )

        CalculateRequestConstructor.determineAcquisitionValueToUse(answers) shouldEqual 29
      }
    }
  }

  "calling totalGainRequestString" when {

    /* This proves that the determined values are being used */
    "property has been given away and the acquisition was gifted" should {

      "return a valid url variable string" in {
        val answers = YourAnswersSummaryModel(
          disposalDate = Dates.constructDate(10, 2, 2016),
          disposalValue = None,
          worthWhenSoldForLess = None,
          whoDidYouGiveItTo = None,
          worthWhenGaveAway = Some(4000),
          disposalCosts = BigDecimal(0),
          acquisitionValue = None,
          worthWhenInherited = None,
          worthWhenGifted = Some(2000),
          worthWhenBoughtForLess = None,
          acquisitionCosts = BigDecimal(100),
          improvements = BigDecimal(10),
          givenAway = true,
          sellForLess = None,
          ownerBeforeLegislationStart = false,
          valueBeforeLegislationStart = None,
          howBecameOwner = Some(giftedIt),
          boughtForLessThanWorth = Some(true)
        )

        val result = CalculateRequestConstructor.totalGainRequestString(answers)
        result shouldBe s"?disposalValue=4000" +
          s"&disposalCosts=0" +
          s"&acquisitionValue=2000" +
          s"&acquisitionCosts=100" +
          s"&improvements=10" +
          s"&disposalDate=2016-02-10"
      }
    }
  }

  "chargeableGainRequestString" when {

    "supplied with no optional values" should {

      "return a valid url variable string" in {
        val answers = ChargeableGainAnswers(
          Some(OtherPropertiesModel(false)),
          None,
          None,
          Some(LossesBroughtForwardModel(false)),
          None,
          None,
          Some(PropertyLivedInModel(false)),
          None,
          None,
          None,
          None
        )
        val result = CalculateRequestConstructor.chargeableGainRequestString(answers, BigDecimal(11100))
        result shouldBe "&annualExemptAmount=11100"
      }
    }

    "supplied with all optional values except allowable losses" should {

      "return a valid url variable string" in {
        val answers = ChargeableGainAnswers(
          Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(false)),
          None,
          Some(LossesBroughtForwardModel(true)),
          Some(LossesBroughtForwardValueModel(BigDecimal(2000))),
          Some(AnnualExemptAmountModel(BigDecimal(3000))),
          Some(PropertyLivedInModel(true)),
          Some(PrivateResidenceReliefModel(true)),
          Some(PrivateResidenceReliefValueModel(5000)),
          Some(LettingsReliefModel(false)),
          Some(LettingsReliefValueModel(4000))
        )
        val result = CalculateRequestConstructor.chargeableGainRequestString(answers, BigDecimal(11100))
        result shouldBe "&prrValue=5000&broughtForwardLosses=2000&annualExemptAmount=3000"
      }
    }

    "supplied with all optional values including allowable losses" should {

      "return a valid url variable string" in {
        val answers = ChargeableGainAnswers(
          Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(1000))),
          Some(LossesBroughtForwardModel(true)),
          Some(LossesBroughtForwardValueModel(BigDecimal(2000))),
          Some(AnnualExemptAmountModel(BigDecimal(3000))),
          Some(PropertyLivedInModel(true)),
          Some(PrivateResidenceReliefModel(true)),
          Some(PrivateResidenceReliefValueModel(5000)),
          Some(LettingsReliefModel(true)),
          Some(LettingsReliefValueModel(4000))
        )
        val result = CalculateRequestConstructor.chargeableGainRequestString(answers, BigDecimal(11100))
        result shouldBe "&prrValue=5000&lettingReliefs=4000&allowableLosses=1000&broughtForwardLosses=2000&annualExemptAmount=11100"
      }
    }
  }

  "calling .isUsingAnnualExemptAmount" when {

    "disposed other properties and claimed non-zero allowable losses" should {

      "return a true" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(1000))))
        result shouldBe false
      }
    }

    "disposed other properties and claimed zero allowable losses" should {

      "return a false" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(0))))
        result shouldBe true
      }
    }

    "disposed other properties and didn't claim allowable losses" should {

      "return a true" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(false)),
          Some(AllowableLossesValueModel(BigDecimal(0))))
        result shouldBe true
      }
    }

    "disposed no other properties or allowable losses" should {

      "return a false" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(false)),
          Some(AllowableLossesModel(false)),
          Some(AllowableLossesValueModel(BigDecimal(100))))
        result shouldBe false
      }
    }

    "disposed no other properties and but claimed allowable losses" should {

      "return a false" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(false)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(100))))
        result shouldBe false
      }
    }
  }
}
