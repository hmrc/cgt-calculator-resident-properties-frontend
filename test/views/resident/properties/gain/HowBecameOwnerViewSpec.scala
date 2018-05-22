/*
 * Copyright 2018 HM Revenue & Customs
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
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{gain => views}
import assets.MessageLookup.{HowBecameOwner => messages}
import assets.MessageLookup.{Resident => commonMessages}
import play.api.mvc.Call
import forms.resident.properties.HowBecameOwnerForm._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class HowBecameOwnerViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "howBecameOwner view" should {
    val backLink = Some("back-link")
    val homeLink = "home-link"
    val postAction = new Call("POST", "post-action")
    lazy val view = views.howBecameOwner(howBecameOwnerForm, backLink, homeLink, postAction)(fakeRequest, applicationMessages, fakeApplication)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    "have a navTitle for properties" in {
      doc.select("span.header__menu__proposition-name").text() shouldBe commonMessages.homeText
    }

    "have a back link to back-link" in {
      doc.select("a#back-link").attr("href") shouldBe "back-link"
    }

    "have a home link to /calculate-your-capital-gains/resident/properties/" in {
      doc.select("a#homeNavHref").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have a header of ${messages.title}" in {
      doc.select("h1.heading-large").text() shouldBe messages.title
    }

    "have a form tag" in {
      doc.select("form").size() shouldBe 1
    }

    "have a form action of 'post-action'" in {
      doc.select("form").attr("action") shouldBe "post-action"
    }

    s"have a visually hidden legend for an input with text ${messages.title}" in {
      doc.select("legend.visuallyhidden").text() shouldBe messages.title
    }

    s"have an input field with id gainedBy-bought " in {
      doc.select("input#gainedBy-bought").size() shouldBe 1
    }

    s"have a label for bought of ${messages.bought}" in {
      doc.select("label[for=gainedBy-bought]").text() shouldBe messages.bought
    }

    s"have an input field with id gainedBy-gifted " in {
      doc.select("input#gainedBy-gifted").size() shouldBe 1
    }

    s"have a label for gifted of ${messages.gifted}" in {
      doc.select("label[for=gainedBy-gifted]").text() shouldBe messages.gifted
    }

    s"have an input field with id gainedBy-inherited " in {
      doc.select("input#gainedBy-inherited").size() shouldBe 1
    }

    s"have a label for inherited of ${messages.inherited}" in {
      doc.select("label[for=gainedBy-inherited]").text() shouldBe messages.inherited
    }

    "have a continue button " in {
      doc.select("#continue-button").text shouldBe commonMessages.continue
    }

  }

  "howBecameOwner view with a value provided as 'Bought'" should {
    val backLink = Some("back-link")
    val homeLink = "home-link"
    val postAction = new Call("POST", "post-action")
    lazy val form = howBecameOwnerForm.bind(Map(("gainedBy", "Bought")))
    lazy val view = views.howBecameOwner(form, backLink, homeLink, postAction)(fakeRequest, applicationMessages, fakeApplication)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'Bought' auto-selected" in {
      doc.select("label[for=gainedBy-bought]").attr("class") shouldBe "block-label selected"
    }
  }

  "howBecameOwner view with a value provided as 'Inherited'" should {
    val backLink = Some("back-link")
    val homeLink = "home-link"
    val postAction = new Call("POST", "post-action")
    lazy val form = howBecameOwnerForm.bind(Map(("gainedBy", "Inherited")))
    lazy val view = views.howBecameOwner(form, backLink, homeLink, postAction)(fakeRequest, applicationMessages, fakeApplication)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'Inherited' auto-selected" in {
      doc.select("label[for=gainedBy-inherited]").attr("class") shouldBe "block-label selected"
    }
  }

  "howBecameOwner view with a value provided as 'Gifted'" should {
    val backLink = Some("back-link")
    val homeLink = "home-link"
    val postAction = new Call("POST", "post-action")
    lazy val form = howBecameOwnerForm.bind(Map(("gainedBy", "Gifted")))
    lazy val view = views.howBecameOwner(form, backLink, homeLink, postAction)(fakeRequest, applicationMessages, fakeApplication)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'Gifted' auto-selected" in {
      doc.select("label[for=gainedBy-gifted]").attr("class") shouldBe "block-label selected"
    }
  }

  "howBecameOwner view with mandatory input erros" should {
    val backLink = Some("back-link")
    val homeLink = "home-link"
    val postAction = new Call("POST", "post-action")
    lazy val form = howBecameOwnerForm.bind(Map(("gainedBy", "")))
    lazy val view = views.howBecameOwner(form, backLink, homeLink, postAction)(fakeRequest, applicationMessages, fakeApplication)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#gainedBy-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
