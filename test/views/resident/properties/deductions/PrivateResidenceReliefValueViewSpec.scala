/*
 * Copyright 2016 HM Revenue & Customs
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
import assets.MessageLookup.{PrivateResidenceReliefValue => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.properties.PrivateResidenceReliefValueForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{deductions => views}

class PrivateResidenceReliefValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Reliefs Value view" should {

    lazy val form = privateResidenceReliefValueForm(100000).bind(Map("amount" -> "10"))
    lazy val view = views.privateResidenceReliefValue(form, "home-link", 1000)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    s"have a back link to Private Residence Relief" which {
      s"has the text ${commonMessages.back}" in {
        doc.select("#back-link").text shouldEqual commonMessages.back
      }

      s"has a link to /calculate-your-capital-gains/resident/properties/private-residence-relief" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief"
      }
    }

    s"have the text ${messages.question} as the h1 tag" in {
      doc.select("h1").text shouldEqual messages.question
    }

    "render a form element with POST action to /calculate-your-capital-gains/resident/properties/private-residence-relief-value" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief-value"
    }

    s"have a hidden label with the text ${messages.question}" in {
      doc.select("label span.visuallyhidden").text shouldEqual messages.question
    }

    s"have an Indented Panel with the help text ${messages.help("1,000")}" in {
      doc.select("#helpText p").text shouldEqual messages.help("1,000")
    }

    s"have a help link" which {
      s"has the text ${messages.link}" in {
        doc.select("#privateResidenceReliefLink").text shouldEqual (messages.link + " " + commonMessages.externalLink)
      }
    }

    "render an input field for the reliefs amount" in {
      doc.select("input").attr("id") shouldBe "amount"
    }

    "not display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "not display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }

    "have continue button " in {
      doc.body.getElementById("continue-button").text shouldEqual commonMessages.continue
    }
  }

  "Reliefs Value View with form without errors" should {

    val form = privateResidenceReliefValueForm(100000).bind(Map("amount" -> "100"))
    lazy val view = views.privateResidenceReliefValue(form, "home-link", 2000)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    s"have the text ${messages.question} as the h1 tag" in {
      doc.select("h1").text shouldEqual messages.question
    }

    s"have a hidden legend with the text ${messages.question}" in {
      doc.select("label span.visuallyhidden").text shouldEqual messages.question
    }

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

  "Reliefs Value View with form with errors" should {

    val form = privateResidenceReliefValueForm(100000).bind(Map("amount" -> ""))
    lazy val view = views.privateResidenceReliefValue(form, "home-link", 3000)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    s"have the text ${messages.question} as the h1 tag" in {
      doc.select("h1").text shouldEqual messages.question
    }

    s"have a hidden legend with the text ${messages.question}" in {
      doc.select("label span.visuallyhidden").text shouldEqual messages.question
    }

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}