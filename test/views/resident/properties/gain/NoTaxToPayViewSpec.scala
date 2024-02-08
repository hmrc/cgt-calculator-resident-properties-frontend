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

package views.resident.properties.gain

import assets.MessageLookup.{NoTaxToPay => messages, Resident => commonMessages}
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.noTaxToPay

class NoTaxToPayViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val noTaxToPayView = fakeApplication.injector.instanceOf[noTaxToPay]
  "No Tax to Pay View when gifted to spouse" should {
    lazy val view = noTaxToPayView(forCharity = false, "/calculate-your-capital-gains/resident/properties/who-did-you-give-it-to")(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title} - Calculate your Capital Gains Tax - GOV.UK" in {
      doc.title() shouldBe messages.title + " - Calculate your Capital Gains Tax - GOV.UK"
    }

    "have a back link to back-link" in {
      doc.body().select(".govuk-back-link").attr("href") shouldBe "#"
    }

    "have a home link to home-link" in {
      doc.body().getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
    }

    "have a navTitle for resident properties" in {
      doc.body().getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").text() shouldBe commonMessages.homeText
    }

    s"have a header of ${messages.title}" in {
      doc.body().select("h1.govuk-heading-xl").text() shouldBe messages.title
    }

    "have text explaining why tax is not owed" in {
      doc.body().getElementsByClass("govuk-body").text() shouldBe messages.spouseText
    }
  }

  "No Tax to Pay View when gifted to charity" should {
    lazy val view = noTaxToPayView(forCharity = true, "/calculate-your-capital-gains/resident/properties/who-did-you-give-it-to")(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have text explaining why tax is not owed" in {
      doc.body().getElementsByClass("govuk-body").text() shouldBe messages.charityText
    }
  }
}
