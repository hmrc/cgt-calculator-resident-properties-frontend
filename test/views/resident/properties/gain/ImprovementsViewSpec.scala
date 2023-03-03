/*
 * Copyright 2023 HM Revenue & Customs
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

import assets.MessageLookup.Resident.Properties.{ImprovementsView => messages}
import forms.resident.properties.ImprovementsForm._
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.improvements

class ImprovementsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val improvementsView = fakeApplication.injector.instanceOf[improvements]
  "Improvements view" should {

    lazy val view = improvementsView(improvementsForm, false)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("H1")

      s"have the page heading '${messages.question}'" in {
        heading.text shouldBe messages.question
      }

      "have the heading-large class" in {
        heading.hasClass("govuk-heading-xl") shouldBe true
      }
    }

    "have the correct body" in {
      doc.select(".govuk-body").text shouldBe messages.hint
    }

    s"has the joint ownership text ${messages.jointOwner}" in {
      doc.select(".govuk-inset-text").text shouldEqual messages.jointOwner
    }

    "have the correct label" in {
      val label = doc.select("label")
      label.text should startWith(messages.label)
    }

    "have a hidden label" in {
      val label = doc.select("label")
      label.hasClass("govuk-label govuk-visually-hidden") shouldBe true
    }

    s"have a drop down link with the text ${messages.improvementsHelpButton}" in {
      doc.body.getElementsByTag("summary").hasClass("govuk-details__summary") shouldBe true
      doc.body.getElementsByTag("summary").text shouldEqual messages.improvementsHelpButton
    }

    s"have an additional line help line one ${messages.improvementsAdditionalContentOne}" in {
      doc.body.getElementsByClass("govuk-details__text").text() should include(messages.improvementsAdditionalContentOne)
    }

    s"have an additional line help line two ${messages.improvementsAdditionalContentTwo}" in {
      doc.body.getElementsByClass("govuk-details__text").text() should include(messages.improvementsAdditionalContentTwo)
    }

    "not display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 0
    }

    "not display an error message for the input" in {
      doc.body.select(".govuk-error-message").size shouldBe 0
    }
  }

  "Improvements View with a property acquired before April 1982" should {
    lazy val view = improvementsView(improvementsForm, true)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title of ${messages.titleBefore}" in {
      doc.title shouldBe messages.titleBefore
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("H1")

      s"have the page heading '${messages.questionBefore}'" in {
        heading.text shouldBe messages.questionBefore
      }

      "have the heading-large class" in {
        heading.hasClass("govuk-heading-xl") shouldBe true
      }
    }

    "have the correct label" in {
      val label = doc.select("label").first()
      label.text should startWith(messages.questionBefore)
    }

    "have a hidden label" in {
      val label = doc.select("label")
      label.hasClass("govuk-label govuk-visually-hidden") shouldBe true
    }
  }

  "Improvements View with form without errors" should {

    lazy val form = improvementsForm.bind(Map("amount" -> "100"))
    lazy val view = improvementsView(form, false)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display the value of the form" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".govuk-error-message").size shouldBe 0
    }
  }

  "Improvements View with form with errors" should {

    lazy val form = improvementsForm.bind(Map("amount" -> ""))
    lazy val view = improvementsView(form, false)(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".govuk-error-message").size shouldBe 1
    }
  }

}
