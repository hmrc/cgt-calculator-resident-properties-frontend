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

package views.resident.properties.report

import assets.MessageLookup.{SummaryPage => messages}
import assets.{MessageLookup => commonMessages}
import assets.MessageLookup.Resident.{Properties => propertiesMessages}
import common.Dates
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.properties._
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{report => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class PropertiesDeductionsReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Deductions Report view" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
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
      true,
      None,
      true,
      Some(BigDecimal(5000)),
      None,
      None)

    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(LossesBroughtForwardModel(false)),
      None,
      Some(PropertyLivedInModel(false)),
      None,
      None,
      None,
      None
    )
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(38900),
      BigDecimal(11100),
      BigDecimal(0),
      BigDecimal(11100),
      BigDecimal(0),
      BigDecimal(0),
      Some(BigDecimal(0)),
      Some(BigDecimal(0)),
      0,
      0
    )

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel, 1000)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a banner for tax owed" in {
      doc.select("#tax-owed-banner").size() shouldBe 1
    }

    "have no tax year notice" in {
      doc.select("#notice-summary").size() shouldBe 0
    }

    "have a calculation details section" in {
      doc.select("#calcDetails").size() shouldBe 1
    }

    "have a section for Your answers" which {

      "has an entry for disposal date" in {
        doc.select("#disposalDate-question").size() shouldBe 1
      }

      "has an entry for brought forward losses" in {
        doc.select("#broughtForwardLosses-question").size() shouldBe 1
      }

      "has no entry for personal allowance" in {
        doc.select("#personalAllowance-question").size() shouldBe 0
      }
    }
  }

  "Deductions Report view with all options selected" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      None,
      Some(500),
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      BigDecimal(10000),
      Some(BigDecimal(100000)),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      BigDecimal(10000),
      BigDecimal(30000),
      false,
      Some(true),
      false,
      None,
      Some("Bought"),
      Some(false)
    )

    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(PropertyLivedInModel(true)),
      Some(PrivateResidenceReliefModel(true)),
      Some(PrivateResidenceReliefValueModel(1000)),
      Some(LettingsReliefModel(true)),
      Some(LettingsReliefValueModel(6000))
    )
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(1000),
      BigDecimal(2000),
      Some(BigDecimal(50000)),
      Some(BigDecimal(1000)),
      10000,
      10000
    )

    lazy val taxYearModel = TaxYearModel("2013/14", false, "2015/16")

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel, 5000)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)


    "have a banner for tax owed" in {
      doc.select("#tax-owed-banner").size() shouldBe 1
    }

    "have a tax year notice" in {
      doc.select("#notice-summary").size() shouldBe 1
    }

    "have a calculation details section" in {
      doc.select("#calcDetails").size() shouldBe 1
    }
  }
}
