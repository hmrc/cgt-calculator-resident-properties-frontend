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

package views.resident.properties.gain

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import assets.MessageLookup.{Resident => commonMessages}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{gain => views}
import forms.resident.WorthWhenInheritedForm._
import assets.MessageLookup.Resident.Properties.{WorthWhenInherited => messages}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Call
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class WorthWhenInheritedViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "worthWhenInherited view" should {
    val backLink = Some("back-link")
    val homeLink = "homeLink"
    val call = new Call("POST", "postAction")
    lazy val form = worthWhenInheritedForm
    lazy val view = views.worthWhenInherited(form, backLink, homeLink, call)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a back link to back-link" in {
      doc.select("#back-link").attr("href") shouldBe "back-link"
    }

    s"have a nav title of 'navTitle'" in {
      doc.select("span.header__menu__proposition-name").text() shouldBe commonMessages.homeText
    }

    s"have a home link to '/calculate-your-capital-gains/resident/properties/'" in {
      doc.select("a#homeNavHref").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
    }

    s"have a title of ${messages.question}" in {
      doc.title() shouldBe messages.question
    }

    s"have a question of ${messages.question}" in {
      doc.select("h1.heading-large").text() shouldBe messages.question
    }

    "have additional content regarding valuations" in {
      doc.select("div.resident p").first().text() shouldBe messages.help
    }

    s"have a joint ownership section with the text ${messages.jointOwner}" in {
      doc.select("div.panel-indent p").text shouldEqual messages.jointOwner
    }

    "have a form tag" in {
      doc.select("form").size() shouldBe 1
    }

    "have a form action of 'postAction'" in {
      doc.select("form").attr("action") shouldBe "postAction"
    }

    "have a form method of 'POST'" in {
      doc.select("form").attr("method") shouldBe "POST"
    }

    s"have a label for an input with text ${messages.question}" in {
      doc.select("label > span.visuallyhidden").text() shouldEqual messages.question
    }

    s"have an input field with id amount " in {
      doc.body.getElementById("amount").tagName() shouldEqual "input"
    }

    "have a continue button " in {
      doc.select("#continue-button").text shouldBe commonMessages.continue
    }
  }

  "Disposal Value View with form without errors" should {
    val backLink = Some("back-link")
    val homeLink = "homeLink"
    val call = new Call("POST", "postAction")
    lazy val form = worthWhenInheritedForm.bind(Map("amount" -> "100"))
    lazy val view = views.worthWhenInherited(form, backLink, homeLink, call)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display the value of the form" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }
  }

  "Disposal Value View with form with errors" should {
    val backLink = Some("back-link")
    val homeLink = "homeLink"
    val call = new Call("POST", "postAction")
    lazy val form = worthWhenInheritedForm.bind(Map("amount" -> ""))
    lazy val view = views.worthWhenInherited(form, backLink, homeLink, call)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
