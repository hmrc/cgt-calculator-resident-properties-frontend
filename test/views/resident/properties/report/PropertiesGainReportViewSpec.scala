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

import assets.MessageLookup.Resident.{Properties => propertiesMessages}
import assets.{MessageLookup => commonMessages}
import assets.MessageLookup.{SummaryPage => messages}
import common.Dates._
import controllers.helpers.FakeRequestHelper
import models.resident.TaxYearModel
import models.resident.properties.YourAnswersSummaryModel
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{report => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class PropertiesGainReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Summary view" should {

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 1990),
      None,
      None,
      whoDidYouGiveItTo = Some("Other"),
      worthWhenGaveAway = Some(10000),
      20,
      None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      40,
      50,
      true,
      None,
      true,
      Some(BigDecimal(5000)),
      None,
      None
    )

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = views.gainSummaryReport(testModel, -2000, taxYearModel, 1000, 2000)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have the hmrc logo with the hmrc name" in {
      doc.select("span.organisation-logo-text").text shouldBe "HM Revenue & Customs"
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

    s"have a section for Your answers" which {

      "has an entry for disposal date" in {
        doc.select("#disposalDate-question").size() shouldBe 1
      }

      "has no entry for brought forward losses" in {
        doc.select("#broughtForwardLosses-question").size() shouldBe 0
      }

      "has no entry for personal allowance" in {
        doc.select("#personalAllowance-question").size() shouldBe 0
      }
    }
  }

  "Summary when supplied with a date outside the known tax years and no gain or loss" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 2015),
      None,
      Some(500),
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      20,
      Some(30),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      40,
      50,
      false,
      Some(true),
      false,
      None,
      Some("Bought"),
      Some(false)
    )

    lazy val view = views.gainSummaryReport(testModel, 0, taxYearModel, 1000, 4000)(fakeRequest, applicationMessages)
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
