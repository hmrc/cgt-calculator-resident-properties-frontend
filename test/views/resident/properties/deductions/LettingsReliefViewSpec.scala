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

package views.resident.properties.deductions

import assets.MessageLookup.{Resident => commonMessages}
import assets.MessageLookup.{LettingsRelief => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.properties.LettingsReliefForm._
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{deductions => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class LettingsReliefViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Lettings Relief view" should {

    lazy val view = views.lettingsRelief(lettingsReliefForm, "home-link", Some("back-link"))(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a back link" which {

      s"has text ${commonMessages.back}" in {
        doc.select("#back-link").text shouldEqual commonMessages.back
      }

      "has a link to 'back-link'" in {
        doc.select("#back-link").attr("href") shouldEqual "back-link"
      }
    }

    s"have the question of the page ${messages.title}" in {
      doc.select("h1").text() shouldEqual messages.title
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/lettings-relief"
    }

    s"have a legend for an input with text ${messages.title}" in {
      doc.select("legend.visuallyhidden").text() shouldEqual messages.title
    }

    s"have the help text ${messages.help}" in {
      doc.select("span.form-hint p").text shouldEqual messages.help
    }

    s"should contain a help text link to the market value info page" which {

      lazy val link = doc.getElementById("lettingsReliefLink")

      s"should have the text ${messages.helpOne}" in {
        link.text() should include(messages.helpOne)
      }

      "has the external link class" in {
        link.hasClass("external-link") shouldEqual true
      }

      s"have a link to ${messages.helpLink}" in {
        link.attr("href") shouldEqual messages.helpLink
      }

      "has the attribute rel" in {
        link.hasAttr("rel") shouldEqual true
      }

      "rel has the value of external" in {
        link.attr("rel") shouldEqual "external"
      }

      "has a target attribute" in {
        link.hasAttr("target") shouldEqual true
      }

      "has a target value of _blank" in {
        link.attr("target") shouldEqual "_blank"
      }

      s"and also has the text ${commonMessages.externalLink}" in {
        link.text() should include(commonMessages.externalLink)
      }
    }

    s"have an input field with id isClaiming-yes " in {
      doc.body.getElementById("isClaiming-yes").tagName() shouldEqual "input"
    }

    s"have an input field with id isClaiming-no " in {
      doc.body.getElementById("isClaiming-no").tagName() shouldEqual "input"
    }

    "have a continue button that" should {

      lazy val button = doc.body.getElementById("continue-button")

      s"have the text ${commonMessages.continue}" in {
        button.text shouldEqual commonMessages.continue
      }

      "be of type submit" in {
        button.attr("type") shouldBe "submit"
      }

      "have the class 'button'" in {
        button.hasClass("button") shouldBe true
      }
    }
  }

  "Lettings Relief view with pre-selected values" should {
    lazy val form = lettingsReliefForm.bind(Map(("isClaiming", "Yes")))
    lazy val view = views.lettingsRelief(form, "home-link", Some("back-link"))(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'Yes' auto selected" in {
      doc.body.getElementById("isClaiming-yes").parent.className should include("selected")
    }
  }

  "Lettings Relief view with errors" should {
    lazy val form = lettingsReliefForm.bind(Map(("isClaiming", "")))
    lazy val view = views.lettingsRelief(form, "home-link", Some("back-link"))(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#isClaiming-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select("span.error-notification").size shouldBe 1
    }
  }
}
