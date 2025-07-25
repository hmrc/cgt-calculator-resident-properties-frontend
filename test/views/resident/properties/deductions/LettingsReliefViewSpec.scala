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

package views.resident.properties.deductions

import assets.MessageLookup.{LettingsRelief => messages, Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import forms.resident.properties.LettingsReliefForm._
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.properties.deductions.lettingsRelief

class LettingsReliefViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val lettingsReliefView = fakeApplication.injector.instanceOf[lettingsRelief]
  "Lettings Relief view" should {

    lazy val view = lettingsReliefView(lettingsReliefForm, Some("back-link"))(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)
    lazy val title = s"${messages.title} - ${commonMessages.homeText} - GOV.UK"

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title $title" in {
      doc.title() shouldBe title
    }

    "have a back link" which {

      s"has text ${commonMessages.back}" in {
        doc.select(".govuk-back-link").text shouldEqual commonMessages.back
      }

      "has a link to 'back-link'" in {
        doc.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    s"have the question of the page ${messages.title}" in {
      doc.select("h1").text() shouldEqual messages.title
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/lettings-relief"
    }

    s"have a legend for an input with text ${messages.title}" in {
      doc.select("legend.govuk-visually-hidden").text() shouldEqual messages.title
    }

    s"have the help text ${messages.help}" in {
      doc.select("p.govuk-body").text should include(messages.help)
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
        link.attr("rel") `contains` "external"
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
      doc.body.getElementById("isClaiming").tagName() shouldEqual "input"
    }

    s"have an input field with id isClaiming-no " in {
      doc.body.getElementById("isClaiming-2").tagName() shouldEqual "input"
    }

    "have a continue button that" should {

      lazy val button = doc.body.getElementById("submit")

      s"have the text ${commonMessages.continue}" in {
        button.text shouldEqual commonMessages.continue
      }

      "have the class 'govuk-button'" in {
        button.hasClass("govuk-button") shouldBe true
      }
    }
  }

  "Lettings Relief view with pre-selected values" should {
    lazy val form = lettingsReliefForm.bind(Map(("isClaiming", "Yes")))
    lazy val view = lettingsReliefView(form, Some("back-link"))(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'Yes' auto selected" in {
      doc.body.getElementById("isClaiming").hasAttr("checked") shouldBe true
    }
  }

  "Lettings Relief view with errors" should {
    lazy val form = lettingsReliefForm.bind(Map(("isClaiming", "")))
    lazy val view = lettingsReliefView(form, Some("back-link"))(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select("#isClaiming-error").size shouldBe 1
    }
  }
}
