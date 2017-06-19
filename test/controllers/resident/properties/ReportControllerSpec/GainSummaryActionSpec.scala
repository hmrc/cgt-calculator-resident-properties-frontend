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

package controllers.ReportControllerSpec

import assets.MessageLookup.{SummaryPage => messages}
import common.Dates
import connectors.CalculatorConnector
import controllers.ReportController
import controllers.helpers.FakeRequestHelper
import models.resident.TaxYearModel
import models.resident.properties.YourAnswersSummaryModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.RequestHeader
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class GainSummaryActionSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper with MockitoSugar {

  def setupTarget
  (
    yourAnswersSummaryModel: YourAnswersSummaryModel,
    grossGain: BigDecimal,
    taxYearModel: Option[TaxYearModel]
  ): ReportController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]

    when(mockCalculatorConnector.getPropertyGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(yourAnswersSummaryModel))

    when(mockCalculatorConnector.calculateRttPropertyGrossGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(grossGain))

    when(mockCalculatorConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockCalculatorConnector.getPropertyTotalCosts(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(BigDecimal(10000)))

    when(mockCalculatorConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(10000))))

    new ReportController {
      override val calcConnector: CalculatorConnector = mockCalculatorConnector

      override def host(implicit request: RequestHeader): String = "http://localhost:9977/"
    }
  }

  "Calling .gainSummaryReport from the ReportController" when {

    "a negative total gain is returned" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(
        Dates.constructDate(12, 1, 2016),
        Some(3000),
        None,
        whoDidYouGiveItTo = Some("Other"),
        worthWhenGaveAway = Some(10000),
        10,
        Some(5000),
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        5,
        0,
        true,
        Some(false),
        true,
        Some(BigDecimal(5000)),
        Some("Bought"),
        Some(false)
      )

      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -6000,
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.gainSummaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "should return the pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "should have a filename of 'Summary.pdf'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }

    "a zero total gain is returned with an invalid tax year" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        Some(3000),
        Some(500),
        whoDidYouGiveItTo = None,
        worthWhenGaveAway = None,
        10,
        Some(5000),
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        5,
        0,
        false,
        Some(true),
        false,
        None,
        Some("Bought"),
        Some(false)
      )

      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -6000,
        taxYearModel = Some(TaxYearModel("2013/2014", false, "2015/16"))
      )
      lazy val result = target.gainSummaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "should return the pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "should have a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }
  }
}
