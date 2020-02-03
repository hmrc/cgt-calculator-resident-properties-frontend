/*
 * Copyright 2020 HM Revenue & Customs
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

package views.resident.properties.report

import _root_.views.BaseViewSpec
import assets.MessageLookup.{SummaryPage => messages}
import common.Dates
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.properties._
import org.jsoup.Jsoup
import play.api.i18n.Lang
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{report => views}

class PropertiesFinalReportViewSpec extends UnitSpec with WithFakeApplication with BaseViewSpec {

  val fakeLang: Lang = Lang("en")

  "Final Summary view" should {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2015),
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
      givenAway = true,
      None,
      ownerBeforeLegislationStart = true,
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
      None
    )

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

    lazy val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

    lazy val view = views.finalSummaryReport(gainAnswers, deductionAnswers, incomeAnswers, results,
      taxYearModel, isCurrentTaxYear = false, Some(true), Some(true), 100, 100, 0)(fakeRequestWithSession, testingMessages, fakeApplication, fakeLang)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have the HMRC logo with the HMRC name" in {
      doc.select("span.organisation-logo-text").text shouldBe "HM Revenue & Customs"
    }

    "have a page heading" which {
      "includes an amount of tax due of £3,600.00" in {
        doc.select("h1").text should include("£3,600.00")
      }
    }

    "does not have a notice summary" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe true
    }

    s"have a section for Your answers" which {

      "has an entry for disposal date" in {
        doc.select("#disposalDate-question").size() shouldBe 1
      }

      "has an entry for brought forward losses" in {
        doc.select("#broughtForwardLosses-question").size() shouldBe 1
      }

      "has an entry for personal allowance" in {
        doc.select("#personalAllowance-question").size() shouldBe 1
      }
    }
  }
}
