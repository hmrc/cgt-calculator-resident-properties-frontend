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

package views.resident.helpers

import assets.MessageLookup.{Resident => commonMessages}
import org.jsoup.Jsoup
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.playHelpers.resident.summaryNumericRowHelper

class SummaryNumericRowHelperSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  implicit val messages = testingMessages
  implicit val lang = messages.lang

  "The Summary Numeric Row Helper" when {
    lazy val summaryNumericRowHelperView = fakeApplication.injector.instanceOf[summaryNumericRowHelper]

    "provided with no link" should {
      lazy val row = summaryNumericRowHelperView("testID","testQ",2000)
      lazy val doc = Jsoup.parse(row.body)

      "have no link" in {
        doc.select("#testID-change-link").size shouldBe 0
      }
    }

    "provided with a change link " should {
      lazy val row = summaryNumericRowHelperView("testID","testQ",2000,Some("link"))
      lazy val doc = Jsoup.parse(row.body)


      "have a question section" which {

        lazy val questionDiv = doc.select("#testID-question")

        "has the correct text" in {
          questionDiv.text shouldBe "testQ"
        }

      }

      "have a value section" which {

        lazy val amountDiv = doc.select("#testID-amount")

        "has a correct value" in {
          amountDiv.text shouldBe "£2,000"
        }
      }

      "include a change link" which {
        lazy val link = doc.select("#testID-change-link a")

        "has the correct link" in {
          link.attr("href") shouldBe "link"
        }

        "has the text" in {
          link.text shouldBe commonMessages.change + " testQ"
        }

        "has a question" which {
          "contains the correct text" in {
            link.select("span").text shouldEqual "testQ"
          }

          "is visible to only screen readers" in {
            link.select("span").hasClass("govuk-visually-hidden") shouldEqual true
          }
        }
      }
    }
  }
}
