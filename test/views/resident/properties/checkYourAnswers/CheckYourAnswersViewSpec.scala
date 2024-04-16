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

package views.resident.properties.checkYourAnswers

import assets.MessageLookup.NonResident.{ReviewAnswers => messages}
import assets.MessageLookup.{Resident => commonMessages}
import assets.ModelsAsset._
import common.{CommonPlaySpec, WithCommonFakeApplication}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.BaseViewSpec
import views.html.calculation.resident.properties.checkYourAnswers.checkYourAnswers

class CheckYourAnswersViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  private val dummyBackLink = "#"
  private val dummyPostCall: Call = Call("POST", "/dummy-url")
  private lazy val checkYourAnswersView = fakeApplication.injector.instanceOf[checkYourAnswers]
  private lazy val view: HtmlFormat.Appendable = checkYourAnswersView(dummyPostCall, dummyBackLink, gainAnswersMostPossibles,
    Some(deductionAnswersMostPossibles), Some(taxYearModel), Some(incomeAnswers))(fakeRequestWithSession, testingMessages)
  private lazy val doc: Document = Jsoup.parse(view.body)

  "have a charset of UTF-8" in {
    doc.charset().toString shouldBe "UTF-8"
  }

  s"have a title ${messages.title}" in {
    doc.title() shouldBe messages.title
  }

  s"have a back button" which {

    lazy val backLink = doc.select(".govuk-back-link")

    s"has the text '${commonMessages.back}'" in {
      backLink.text shouldBe commonMessages.back
    }

    s"has a link to '$dummyBackLink'" in {
      backLink.attr("href") shouldBe dummyBackLink
    }
  }

  s"have a page heading" which {

    s"includes a secondary heading with text '${messages.heading}'" in {
      doc.select("h1.govuk-heading-xl").text shouldBe messages.heading
    }
  }

  "have a section for the check your answers" in {
    doc.select("section").attr("id") shouldBe "yourAnswers"
  }

  "have a continue button that" should {

    lazy val continueButton = doc.select("a.govuk-button")

    s"have the button text '${commonMessages.continue}'" in {
      continueButton.text shouldBe commonMessages.continue
    }

    "have an href of '/dummy-url'" in {
      continueButton.attr("href") shouldBe "/dummy-url"
    }

    "have the class 'button'" in {
      continueButton.hasClass("govuk-button") shouldBe true
    }
  }
}
