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

package views.resident

import assets.MessageLookup.{OutsideTaxYears => messages, Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.outsideTaxYear

class OutsideTaxYearsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  "Outside tax years views" when {
    lazy val outsideTaxYearView = fakeApplication.injector.instanceOf[outsideTaxYear]
    "using a disposal date before 2015/16 with properties" should {
      lazy val taxYear = TaxYearModel("2014/15", false, "2015/16")
      lazy val view = outsideTaxYearView(taxYear, false, true, "back-link", "continue-link", "navTitle")(using fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have charset UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"return a title of ${messages.title}" in {
        doc.title shouldBe messages.title
      }

      "have a home link to '/calculate-your-capital-gains/resident/properties/'" in {
        doc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/"
      }

      s"have a heading of ${messages.heading}" in {
        doc.select("h1").text() shouldBe messages.heading
      }

      s"have a message of ${messages.tooEarly}" in {
        doc.select("p.lede").text() shouldBe messages.tooEarly
      }

      "have a 'Change your date' link that" should {
        lazy val backLink = doc.select("a#change-date-link")

        "have the correct text" in {
          backLink.text shouldBe messages.changeDate
        }

        "have the back-link class" in {
          backLink.hasClass("back-link") shouldBe true
        }

        "have a link to 'back-link'" in {
          backLink.attr("href") shouldBe "back-link"
        }
      }
    }

    "using a disposal date after 2016/17" should {
      lazy val taxYear = TaxYearModel("2017/18", false, "2016/17")
      lazy val view = outsideTaxYearView(taxYear, true, true, "back-link", "continue-link", "navTitle")(using fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have charset UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"return a title of ${messages.title}" in {
        doc.title shouldBe messages.title
      }

      s"have a heading of ${messages.heading}" in {
        doc.select("h1").text() shouldBe messages.heading
      }

      s"have a message of ${messages.content("2016 to 2017")}" in {
        doc.select("p.lede").text() shouldBe messages.content("2016 to 2017")
      }

      "have a back link that" should {
        lazy val backLink = doc.select(".govuk-back-link")

        "have the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        "have the back-link class" in {
          backLink.hasClass("govuk-back-link") shouldBe true
        }

        "have a link to 'back-link'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "have a continue button that" should {
        lazy val button = doc.getElementsByClass("govuk-button")

        "have the correct text 'Continue'" in {
          button.text() shouldBe commonMessages.continue
        }
      }
    }

    "using a disposal date before 2015/16 with shares" should {
      lazy val taxYear = TaxYearModel("2014/15", false, "2015/16")
      lazy val view = outsideTaxYearView(taxYear, false, false, "back-link", "continue-link", "navTitle")(using fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      s"have a message of ${messages.sharesTooEarly}" in {
        doc.select("p.lede").text() shouldBe messages.sharesTooEarly
      }
    }
  }
}
