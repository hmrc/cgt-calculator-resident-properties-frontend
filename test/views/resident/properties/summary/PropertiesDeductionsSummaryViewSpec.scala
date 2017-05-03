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

package views.resident.properties.summary

import assets.MessageLookup.Resident.{Properties => propertiesMessages}
import assets.MessageLookup.{Resident => residentMessages, SummaryPage => messages}
import assets.{MessageLookup => pageMessages}
import common.Dates
import controllers.helpers.FakeRequestHelper
import controllers.routes
import models.resident._
import models.resident.properties._
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{summary => views}

class PropertiesDeductionsSummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

//  "Properties Deductions Summary view" should {
//    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
//      None,
//      None,
//      whoDidYouGiveItTo = Some("Other"),
//      worthWhenGaveAway = Some(10000),
//      BigDecimal(10000),
//      None,
//      worthWhenInherited = None,
//      worthWhenGifted = None,
//      worthWhenBoughtForLess = None,
//      BigDecimal(10000),
//      BigDecimal(30000),
//      true,
//      None,
//      true,
//      Some(BigDecimal(5000)),
//      None,
//      None
//    )
//
//    lazy val deductionAnswers = ChargeableGainAnswers(
//      Some(LossesBroughtForwardModel(false)),
//      None,
//      Some(PropertyLivedInModel(false)),
//      None,
//      None,
//      None,
//      None
//    )
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
//    lazy val backLink = "/calculate-your-capital-gains/resident/properties/losses-brought-forward"
//
//    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
//
//    lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel)(fakeRequestWithSession, applicationMessages)
//    lazy val doc = Jsoup.parse(view.body)
//
//    "have a charset of UTF-8" in {
//      doc.charset().toString shouldBe "UTF-8"
//    }
//
//    s"have a title ${messages.title}" in {
//      doc.title() shouldBe messages.title
//    }
//
//    s"have a back button" which {
//
//      lazy val backLink = doc.getElementById("back-link")
//
//      "has the id 'back-link'" in {
//        backLink.attr("id") shouldBe "back-link"
//      }
//
//      s"has the text '${residentMessages.back}'" in {
//        backLink.text shouldBe residentMessages.back
//      }
//
//      s"has a link to '${routes.DeductionsController.lossesBroughtForward().toString()}'" in {
//        backLink.attr("href") shouldBe routes.DeductionsController.lossesBroughtForward().toString
//      }
//
//    }
//  }
}
