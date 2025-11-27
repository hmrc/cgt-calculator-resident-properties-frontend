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

import assets.MessageLookup.WhatNextPages.FourTimesAEA as pageMessages
import assets.MessageLookup.WhatNextPages as commonMessages
import common.{CommonPlaySpec, WithCommonFakeApplication}
import org.jsoup.Jsoup
import util.GovUkStylingHelper
import views.BaseViewSpec
import views.html.calculation.resident.properties.whatNext.whatNextSAFourTimesAEA

class WhatNextSAFourTimesAEAViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec with GovUkStylingHelper {

  lazy val whatNextSAFourTimesAEAView = fakeApplication.injector.instanceOf[whatNextSAFourTimesAEA]
  "The whatNextSAFourTimesAEA view" should {

    lazy val view = whatNextSAFourTimesAEAView("back-link")(using fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${commonMessages.title}" in {
      doc.title() shouldBe commonMessages.newTitle
    }

    "have a back link" which {
      lazy val backLink = doc.select(".govuk-back-link")

      s"should have the text ${commonMessages.back}" in {
        backLink.text() shouldBe commonMessages.back
      }
    }

    s"have the question of the page ${commonMessages.title}" should {
      pageWithExpectedMessage(headingStyle, commonMessages.title)(using doc)
    }

    s"have the first paragraph of ${pageMessages.paragraphOne}" in {
      doc.select("#main-content > div > div > p:nth-child(2)").text shouldBe pageMessages.paragraphOne
    }

    s"have the second paragraph of ${pageMessages.paragraphTwo}" in {
      doc.select("#main-content > div > div > p:nth-child(3)").text shouldBe pageMessages.paragraphTwo
    }

    "have a finish button" which {

      lazy val finishButton = doc.select("#finish")

      s"has the text ${commonMessages.finish}" in {
        finishButton.text shouldBe commonMessages.finish
      }

      "has a link to the 'www.gov.uk' page" in {
        finishButton.attr("href") shouldBe "http://www.gov.uk"
      }
    }
  }
}
