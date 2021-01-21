/*
 * Copyright 2021 HM Revenue & Customs
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

import assets.MessageLookup.{Resident => commonMessages, WhoDidYouGiveItTo => messages}
import forms.resident.properties.gain.WhoDidYouGiveItToForm._
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.{gain => views}

class WhoDidYouGiveItToViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {
  "Property Recipient view" should {

  lazy val view = views.whoDidYouGiveItTo(whoDidYouGiveItToForm)(fakeRequest, testingMessages, mockAppConfig)
  lazy val doc = Jsoup.parse(view.body)

  "have a charset of UTF-8" in {
    doc.charset().toString shouldBe "UTF-8"
  }

  s"have a title of ${messages.title}" in {
    doc.title() shouldBe messages.title
  }

    "have a back button that" should {
      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have a link to Did You Sell or Give Away" in {
        backLink.attr("href") shouldBe controllers.routes.GainController.sellOrGiveAway().toString()
      }
    }
    "have a H1 tag that" should {
      lazy val heading = doc.select("h1")

      s"have the page heading '${messages.title}'" in {
        heading.text shouldBe messages.title
      }
      "have the heading-large class" in {
        heading.hasClass("heading-large") shouldBe true
      }
    }

    "have a form" which {
      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.GainController.submitWhoDidYouGiveItTo().toString}" in {
        form.attr("action") shouldBe controllers.routes.GainController.submitWhoDidYouGiveItTo().toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
        }

    }

    "have additional content that" should {
      s"have a label for the Your Spouse or Civil Partner option" in {
        doc.select("label").get(0).text() shouldEqual messages.spouse
      }

      s"have a label for the A Charity option" in {
        doc.select("label").get(1).text() shouldEqual messages.charity
      }

      s"have a label for the Someone Else option" in {
        doc.select("label").get(2).text() shouldEqual messages.other
      }
    }

    "has a continue button that" should {
      lazy val continueButton = doc.select("button#continue-button")

      s"have the button text '${commonMessages.continue}'" in {
        continueButton.text shouldBe commonMessages.continue
      }

      "be of type submit" in {
        continueButton.attr("type") shouldBe "submit"
      }

      "have the class 'button'" in {
        continueButton.hasClass("button") shouldBe true
      }
    }
  }

  "WhoDidYouGiveItToView with form with errors" should {
    lazy val form = whoDidYouGiveItToForm.bind(Map("whoDidYouGiveItTo" -> ""))
    lazy val view = views.whoDidYouGiveItTo(form)(fakeRequest, testingMessages, mockAppConfig)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message regarding incorrect value being inputted" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
