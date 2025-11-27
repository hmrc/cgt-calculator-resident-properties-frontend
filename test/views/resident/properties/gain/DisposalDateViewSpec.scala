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

import assets.MessageLookup.{DisposalDate as messages, Resident as commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import forms.resident.DisposalDateForm.*
import models.resident.DisposalDateModel
import org.jsoup.Jsoup
import util.GovUkStylingHelper
import views.BaseViewSpec
import views.html.calculation.resident.properties.gain.disposalDate

import java.time.LocalDate

class DisposalDateViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with BaseViewSpec with GovUkStylingHelper {

  lazy val disposalDateView = fakeApplication.injector.instanceOf[disposalDate]
  "Disposal Date view" should {

    lazy val view = disposalDateView(disposalDateForm(LocalDate.parse("2014-04-06"))(using testingMessages))(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have the title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have the heading question ${messages.question}" should {
      pageWithExpectedMessage(legendHeadingStyle, messages.question)(using doc)
    }

    "have the helptext 'For example, 4 9 2016'" in {
      doc.body.getElementsByClass("govuk-hint").text should include(messages.helpText)
    }

    "have an input box for day" in {
      doc.body.getElementById("disposalDate.day").parent.text shouldBe messages.day
    }

    "have an input box for month" in {
      doc.body.getElementById("disposalDate.month").parent.text shouldBe messages.month
    }

    "have an input box for year" in {
      doc.body.getElementById("disposalDate.year").parent.text shouldBe messages.year
    }

    "have a button with the text 'Continue'" in {
      doc.body.getElementById("submit").text shouldBe commonMessages.continue
    }

    "have a back link" in {
      doc.body.select(".govuk-back-link").text shouldBe commonMessages.back
    }

    "have a back link to the introduction page" in {
      doc.body.select(".govuk-back-link").attr("href") shouldBe "#"
    }
  }

  "Disposal Date view with a pre-filled form" should {

    lazy val form = disposalDateForm(LocalDate.parse("2014-04-06"))(using testingMessages).fill(DisposalDateModel(10, 6, 2016))
    lazy val view = disposalDateView(form)(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a value auto-filled in the day input" in {
      doc.body.getElementById("disposalDate.day").`val`() shouldBe "10"
    }

    "have a value auto-filled in the month input" in {
      doc.body.getElementById("disposalDate.month").`val`() shouldBe "6"
    }

    "have a value auto-filled in the year input" in {
      doc.body.getElementById("disposalDate.year").`val`() shouldBe "2016"
    }
  }
}
