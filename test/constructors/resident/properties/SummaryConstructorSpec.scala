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

package constructors.resident.properties

import assets.MessageLookup.{SummaryPage => messages}
import models.resident._
import models.resident.properties.{ChargeableGainAnswers, PropertyLivedInModel}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec

class SummaryConstructorSpec extends UnitSpec with GuiceOneAppPerSuite {

  "Calling the .gainMessage function" when {

    "result is a loss" should {

      s"return the message '${messages.totalLoss}'" in {
        SummaryConstructor.gainMessage(BigDecimal(-1000)) shouldBe messages.totalLoss
      }
    }

    "result is a gain" should {

      s"return the message '${messages.totalGain}'" in {
        SummaryConstructor.gainMessage(BigDecimal(1000)) shouldBe messages.totalGain
      }
    }

    "result is 0" should {

      s"return the message '${messages.totalGain}'" in {
        SummaryConstructor.gainMessage(BigDecimal(0)) shouldBe messages.totalGain
      }
    }
  }

  "Calling the .broughtForwardLossesUsed function" when {

    "no brought forward losses are claimed" should {
      lazy val answers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)),
        None, Some(PropertyLivedInModel(false)), None, None, None, None)

      "return a value of '0'" in {
        SummaryConstructor.broughtForwardLossesUsed(answers) shouldBe "0"
      }
    }

    "no brought forward losses are claimed with a provided value" should {
      lazy val answers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), Some(LossesBroughtForwardValueModel(BigDecimal(10000))),
        Some(PropertyLivedInModel(false)), None, None, None, None)

      "return a value of '0'" in {
        SummaryConstructor.broughtForwardLossesUsed(answers) shouldBe "0"
      }
    }

    "brought forward losses are claimed with a provided value" should {
      lazy val answers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(true)), Some(LossesBroughtForwardValueModel(BigDecimal(10000))),
        Some(PropertyLivedInModel(false)), None, None, None, None)

      "return a value of '10,000'" in {
        SummaryConstructor.broughtForwardLossesUsed(answers) shouldBe "10,000"
      }
    }

    "brought forward losses are claimed with a provided value with decimal places" should {
      lazy val answers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(true)), Some(LossesBroughtForwardValueModel(BigDecimal(9999.99))),
        Some(PropertyLivedInModel(false)), None, None, None, None)

      "return a value of '10,000' when rounded up" in {
        SummaryConstructor.broughtForwardLossesUsed(answers) shouldBe "10,000"
      }
    }
  }
}
