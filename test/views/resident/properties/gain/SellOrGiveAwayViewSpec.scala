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

import assets.MessageLookup.{PropertiesSellOrGiveAway => messages, Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.resident.properties.SellOrGiveAwayForm._
import org.jsoup.Jsoup
import play.api.mvc.Call
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.sellOrGiveAway

class SellOrGiveAwayViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val sellOrGiveAwayView = fakeApplication.injector.instanceOf[sellOrGiveAway]
  "sellOrGiveAway view" should {
    val backLink = Some("/calculate-your-capital-gains/resident/properties/disposal-date")
    val call = new Call("POST", "postAction")
    lazy val form = sellOrGiveAwayForm
    lazy val view = sellOrGiveAwayView(form, backLink, call)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a back link to the Disposal Date Page" in {
      doc.select(".govuk-back-link").attr("href") shouldBe "#"
    }

    s"have a nav title of 'navTitle'" in {
      doc.select("body > header > div > div > div.govuk-header__content > a").text() shouldBe commonMessages.homeText
    }

    s"have a home link to '/calculate-your-capital-gains/resident/properties/'" in {
      doc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have a question of ${messages.title}" in {
      doc.select(".govuk-fieldset__heading").text() shouldBe messages.heading
    }

    "have a form tag" in {
      doc.select("form").size() shouldBe 1
    }

    "have a form action of 'postAction'" in {
      doc.select("form").attr("action") shouldBe "postAction"
    }

    s"have an input field with id gaveAway" in {
      doc.select("#givenAway").size() shouldBe 1
    }

    s"have a label for sold of ${messages.sold}" in {
      doc.select("#main-content > div > div > form > div > fieldset > div > div:nth-child(1) > label").text() shouldBe messages.sold
    }

    s"have an input field with id gaveAway-2 " in {
      doc.select("#givenAway-2").size() shouldBe 1
    }

    s"have a label for sold of ${messages.gift}" in {
      doc.select("#main-content > div > div > form > div > fieldset > div > div:nth-child(2) > label").text() shouldBe messages.gift
    }

    "have a continue button " in {
      doc.select("#submit").text shouldBe commonMessages.continue
    }
  }

  "The Sell Or Give Away View with form with errors" which {

    "is due to mandatory field error" should {
      val backLink = Some("/calculate-your-capital-gains/resident/properties/disposal-date")
      val call = new Call("POST", "postAction")
      lazy val form = sellOrGiveAwayForm.bind(Map("givenAway" -> ""))
      lazy val view = sellOrGiveAwayView(form, backLink, call)(using fakeRequest, testingMessages)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select(".govuk-error-summary__body").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select("#givenAway-error").size shouldBe 1
      }
    }
  }

}
