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
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import common.CommonPlaySpec
import views.BaseViewSpec
import views.html.playHelpers.resident.summaryTextRowHelper

class SummaryTextRowHelperSpec extends CommonPlaySpec with BaseViewSpec {

  implicit val messages = testingMessages
  implicit val lang = messages.lang

  "The Summary Text Row Helper" when {
    lazy val summaryTextRowHelperView = fakeApplication.injector.instanceOf[summaryTextRowHelper]

    "not provided with any change links" should {
      lazy val row: HtmlFormat.Appendable = summaryTextRowHelperView("testID", "testQ", "testValue")
      lazy val doc: Document = Jsoup.parse(row.body)

      "have no link" in {
        lazy val link = doc.select("#testID-change-link a")

        link.size() shouldBe 0
      }
    }

    "provided with a change link " should {

      lazy val row: HtmlFormat.Appendable = summaryTextRowHelperView("testID", "testQ", "testValue", Some("link"))
      lazy val doc: Document = Jsoup.parse(row.body)

      "have a question section" which {
        lazy val questionDiv = doc.select("#testID-question")

        "has the correct text" in {
          questionDiv.text shouldBe "testQ"
        }
      }

      "have an text value section" which {
        lazy val amountDiv = doc.select("#testID-option")

        "has the correct option" in {
          amountDiv.text shouldBe "testValue"
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
