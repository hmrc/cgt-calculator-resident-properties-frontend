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

package views.resident.properties.whatNext

import _root_.views.BaseViewSpec
import assets.MessageLookup.WhatNextPages.{WhatNextNoGain => pageMessages}
import assets.MessageLookup.{Resident => serviceMessages, WhatNextPages => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import org.jsoup.Jsoup
import views.html.calculation.resident.properties.whatNext.whatNextSaNoGain

class WhatNextSANoGainViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val whatNextSaNoGainView = fakeApplication.injector.instanceOf[whatNextSaNoGain]
  lazy val pageTitle = s"${commonMessages.title} - ${serviceMessages.homeText} - GOV.UK"
  "The whatNextSaNoGain view" should {

    lazy val view = whatNextSaNoGainView("back-link", "iFormUrl", "2016 to 2017")(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title $pageTitle" in {
      doc.title() shouldBe pageTitle
    }

    "have a back link to 'back-link'" in {
      doc.select(".govuk-back-link").attr("href") shouldBe "#"
    }

    "have the correct heading" in {
      doc.select("h1").text shouldBe commonMessages.title
    }

    "have a bullet point list" which {

      s"has the title ${pageMessages.bulletPointTitle}" in {
        doc.select("#bullet-list-title").text shouldBe pageMessages.bulletPointTitle
      }

      s"has a first bullet point of ${pageMessages.bulletPointOne("2016 to 2017")}" in {
        doc.select("ul.govuk-list li").get(0).text shouldBe pageMessages.bulletPointOne("2016 to 2017")
      }

      s"has a second bullet point of ${pageMessages.bulletPointTwo}" in {
        doc.select("ul.govuk-list li").get(1).text shouldBe pageMessages.bulletPointTwo
      }
    }

    s"have an important information section with the text ${pageMessages.importantInformation}" in {
      doc.select("#important-information").text shouldBe pageMessages.importantInformation
    }

    "have a paragraph with the text ..." in {
      doc.select("#report-now-information").text shouldBe pageMessages.whatNextInformation
    }

    "have a Report now button" which {

      lazy val reportNowButton = doc.select("a#report-now-button")

      s"has the text ${commonMessages.reportNow}" in {
        reportNowButton.text shouldBe commonMessages.reportNow
      }

      "has the class govuk-button" in {
        reportNowButton.hasClass("govuk-button") shouldBe true
      }

      "has a link to the 'iFormUrl'" in {
        reportNowButton.attr("href") shouldBe "iFormUrl"
      }
    }

    "have exit survey text and link" which {

      "has the exit survey text" in {
        doc.select("#exit-survey-message").text shouldBe pageMessages.exitSurveyText
      }

      "has a link to the exit survey page" in {
        doc.select("#exit-survey-link").text() shouldBe pageMessages.exitSurveyLinkText
        doc.select("#exit-survey-link").attr("href") shouldBe pageMessages.exitSurveyLink
      }
    }
  }
}