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

package views.resident.properties.gain

import assets.MessageLookup.{PropertiesSellOrGiveAway => messages, Resident => commonMessages}
import forms.resident.properties.SellOrGiveAwayForm._
import org.jsoup.Jsoup
import play.api.mvc.Call
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.{gain => views}

class SellOrGiveAwayViewSpec extends UnitSpec with WithFakeApplication with BaseViewSpec {

  "sellOrGiveAway view" should {
    val backLink = Some("/calculate-your-capital-gains/resident/properties/disposal-date")
    val homeLink = "homeLink"
    val call = new Call("POST", "postAction")
    lazy val form = sellOrGiveAwayForm
    lazy val view = views.sellOrGiveAway(form, backLink, homeLink, call)(fakeRequest, testingMessages, mockAppConfig)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a back link to the Disposal Date Page" in {
      doc.select("#back-link").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/disposal-date"
    }

    s"have a nav title of 'navTitle'" in {
      doc.select("span.header__menu__proposition-name").text() shouldBe commonMessages.homeText
    }

    s"have a home link to '/calculate-your-capital-gains/resident/properties/'" in {
      doc.select("a#homeNavHref").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have a question of ${messages.title}" in {
      doc.select("h1.heading-large").text() shouldBe messages.title
    }

    "have a form tag" in {
      doc.select("form").size() shouldBe 1
    }

    "have a form action of 'postAction'" in {
      doc.select("form").attr("action") shouldBe "postAction"
    }

    s"have a visually hidden legend for an input with text ${messages.title}" in {
      doc.select("legend.visuallyhidden").text() shouldBe messages.title
    }

    s"have an input field with id gaveAway-sold " in {
      doc.select("input#givenAway-sold").size() shouldBe 1
    }

    s"have a label for sold of ${messages.sold}" in {
      doc.select("label[for=givenAway-sold]").text() shouldBe messages.sold
    }

    s"have an input field with id gaveAway-given " in {
      doc.select("input#givenAway-given").size() shouldBe 1
    }

    s"have a label for sold of ${messages.gift}" in {
      doc.select("label[for=givenAway-given]").text() shouldBe messages.gift
    }

    "have a continue button " in {
      doc.select("#continue-button").text shouldBe commonMessages.continue
    }
  }

  "Sell Or Give Away view with pre-selected value of Sold" should {
    val backLink = Some("/calculate-your-capital-gains/resident/properties/disposal-date")
    val homeLink = "homeLink"
    val call = new Call("POST", "postAction")
    lazy val form = sellOrGiveAwayForm.bind(Map(("givenAway", "Sold")))
    lazy val view = views.sellOrGiveAway(form, backLink, homeLink, call)(fakeRequest, testingMessages, mockAppConfig)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'Sold' auto selected" in {
      doc.select("label[for=givenAway-sold]").attr("class") shouldBe "block-label selected"
    }
  }

  "Sell Or Give Away view with pre-selected value of Given" should {
    val backLink = Some("/calculate-your-capital-gains/resident/properties/disposal-date")
    val homeLink = "homeLink"
    val call = new Call("POST", "postAction")
    lazy val form = sellOrGiveAwayForm.bind(Map(("givenAway", "Given")))
    lazy val view = views.sellOrGiveAway(form, backLink, homeLink, call)(fakeRequest, testingMessages, mockAppConfig)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'Given' auto selected" in {
      doc.select("label[for=givenAway-given]").attr("class") shouldBe "block-label selected"
    }
  }

  "The Sell Or Give Away View with form with errors" which {

    "is due to mandatory field error" should {
      val backLink = Some("/calculate-your-capital-gains/resident/properties/disposal-date")
      val homeLink = "homeLink"
      val call = new Call("POST", "postAction")
      lazy val form = sellOrGiveAwayForm.bind(Map("givenAway" -> ""))
      lazy val view = views.sellOrGiveAway(form, backLink, homeLink, call)(fakeRequest, testingMessages, mockAppConfig)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select("#givenAway-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select(".form-group .error-notification").size shouldBe 1
      }
    }
  }

}
