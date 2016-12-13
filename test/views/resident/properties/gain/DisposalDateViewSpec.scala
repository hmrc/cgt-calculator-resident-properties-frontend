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

package views.resident.properties.gain

import assets.MessageLookup.{DisposalDate => messages}
import assets.MessageLookup.{Resident => commonMessages}
import controllers.helpers.FakeRequestHelper
import forms.resident.DisposalDateForm._
import models.resident.DisposalDateModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{gain => views}

class DisposalDateViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Disposal Date view" should {

    lazy val view = views.disposalDate(disposalDateForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have the title 'When did you sign the contract that made someone else the owner?'" in {
      doc.title() shouldBe messages.title
    }

    "have the heading question 'When did you sign the contract that made someone else the owner?'" in {
      doc.body.getElementsByTag("h1").text should include(messages.question)
    }

    "have the helptext 'For example, 4 9 2016'" in {
      doc.body.getElementsByClass("form-hint").text should include(messages.helpText)
    }

    "have an input box for day" in {
      doc.body.getElementById("disposalDateDay").parent.text shouldBe messages.day
    }

    "have an input box for month" in {
      doc.body.getElementById("disposalDateMonth").parent.text shouldBe messages.month
    }

    "have an input box for year" in {
      doc.body.getElementById("disposalDateYear").parent.text shouldBe messages.year
    }

    "have a button with the text 'Continue'" in {
      doc.body.getElementById("continue-button").text shouldBe commonMessages.continue
    }

    "have a back link" in {
      doc.body.getElementById("back-link").text shouldBe commonMessages.back
    }

    "have a back link to the introduction page" in {
      doc.body.getElementById("back-link").attr("href") shouldBe controllers.routes.PropertiesController.introduction().toString
    }
  }

  "Disposal Date view with a pre-filled form" should {

    lazy val form = disposalDateForm.fill(DisposalDateModel(10, 6, 2016))
    lazy val view = views.disposalDate(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a value auto-filled in the day input" in {
      doc.body.getElementById("disposalDateDay").`val`() shouldBe "10"
    }

    "have a value auto-filled in the month input" in {
      doc.body.getElementById("disposalDateMonth").`val`() shouldBe "6"
    }

    "have a value auto-filled in the year input" in {
      doc.body.getElementById("disposalDateYear").`val`() shouldBe "2016"
    }
  }

  "Disposal Date view with a non-valid date input error" should {

    lazy val form = disposalDateForm.bind(Map(
      ("disposalDateDay", "32"),
      ("disposalDateMonth", "10"),
      ("disposalDateYear", "2016")
    ))
    lazy val view = views.disposalDate(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have the error summary message 'Enter a real date'" in {
      doc.body.getElementById("disposalDateDay-error-summary").text shouldBe messages.realDateError
    }

    "have the input error message 'Enter a real date'" in {
      doc.body.getElementsByClass("error-notification").text shouldBe messages.realDateError
    }
  }

  "Disposal Date view with an empty field date input error" should {

    lazy val form = disposalDateForm.bind(Map(
      ("disposalDateDay", ""),
      ("disposalDateMonth", "10"),
      ("disposalDateYear", "2016")
    ))
    lazy val view = views.disposalDate(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    s"have the error summary message '${messages.invalidDayError}'" in {
      doc.body.getElementById("disposalDateDay-error-summary").text should include(messages.invalidDayError)
    }

    "have the input error message 'Enter a real date'" in {
      doc.body.getElementsByClass("error-notification").text shouldBe messages.realDateError
    }
  }

  "Disposal Date view with a non numeric date input error" should {

    lazy val form = disposalDateForm.bind(Map(
      ("disposalDateDay", "a"),
      ("disposalDateMonth", "b"),
      ("disposalDateYear", "c")
    ))
    lazy val view = views.disposalDate(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    s"have the error summary message '${messages.invalidDayError}'" in {
      doc.body.getElementById("disposalDateDay-error-summary").text should include(messages.invalidDayError)
    }

    s"have the error summary message '${messages.invalidMonthError}'" in {
      doc.body.getElementById("disposalDateMonth-error-summary").text should include(messages.invalidMonthError)
    }

    s"have the error summary message '${messages.invalidYearError}'" in {
      doc.body.getElementById("disposalDateYear-error-summary").text should include(messages.invalidYearError)
    }

    "have the input error message 'Enter a real date'" in {
      doc.body.getElementsByClass("error-notification").text shouldBe messages.realDateError
    }
  }
}
