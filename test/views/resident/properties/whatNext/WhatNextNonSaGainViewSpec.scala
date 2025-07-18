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

import assets.MessageLookup.{WhatNextNonSaGain => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import org.jsoup.Jsoup
import views.BaseViewSpec
import views.html.calculation.resident.properties.whatNext.whatNextNonSaGain

class WhatNextNonSaGainViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  lazy val whatNextNonSaGainView = fakeApplication.injector.instanceOf[whatNextNonSaGain]
  "whatNextNonSaGain view" should {

    lazy val view = whatNextNonSaGainView("iFormUrl", "BackLinkUrl")(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"return a title of ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    s"have a heading of ${messages.heading}" in {
      doc.select("h1").text shouldBe messages.heading
    }

    s"have a first paragraph with text ${messages.detailsOne}" in {
      doc.select("#details-one").text shouldBe messages.detailsOne
    }

    s"have a second paragraph with text ${messages.detailsTwo}" in {
      doc.select("#details-two").text shouldBe messages.detailsTwo
    }

    "have a report now link" which {

      lazy val reportNow = doc.select("a.govuk-button")

      s"has text ${messages.reportNow}" in {
        reportNow.text shouldBe messages.reportNow
      }

      "has a link to 'iFormUrl'" in {
        reportNow.attr("href") shouldBe "iFormUrl"
      }
    }

    "have exit survey text and link" which {

      "has the exit survey text" in {
        doc.select("#exit-survey-message").text shouldBe messages.exitSurveyText
      }

      "has a link to the exit survey page" in {
        doc.select("#exit-survey-link").text() shouldBe messages.exitSurveyLinkText
        doc.select("#exit-survey-link").attr("href") shouldBe messages.exitSurveyLink
      }
    }
  }
}
