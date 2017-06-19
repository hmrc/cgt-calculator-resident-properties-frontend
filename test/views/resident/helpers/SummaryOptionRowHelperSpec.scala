/*
 * Copyright 2017 HM Revenue & Customs
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

import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.helpers.resident.summaryOptionRowHelper
import assets.MessageLookup.{Resident => commonMessages}
import org.jsoup.nodes.Document
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.twirl.api.HtmlFormat

class SummaryOptionRowHelperSpec extends UnitSpec with WithFakeApplication {

  "The Summary Numeric Row Helper" when {

    "not provided with any change links" should {
      lazy val row: HtmlFormat.Appendable = summaryOptionRowHelper("testID","testQ",answer = true)
      lazy val doc: Document = Jsoup.parse(row.body)

      "have a question section" which {
        lazy val questionDiv = doc.select("#testID-question")

        "has the correct text" in {
          questionDiv.text shouldBe "testQ"
        }
      }

      "have an option section" which {
        lazy val amountDiv = doc.select("#testID-option")

        "has the correct option" in {
          amountDiv.text shouldBe "Yes"
        }
      }
    }

    s"provided with a change link " should {

      lazy val row: HtmlFormat.Appendable = summaryOptionRowHelper("testID","testQ",answer = true, Some("link"))
      lazy val doc: Document = Jsoup.parse(row.body)

      "have a question section" which {
        lazy val questionDiv = doc.select("#testID-question")

        "has the correct text" in {
          questionDiv.text shouldBe "testQ"
        }
      }

      "have an option section" which {
        lazy val amountDiv = doc.select("#testID-option")

        "has the correct option" in {
          amountDiv.text shouldBe "Yes"
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
            link.select("span").hasClass("visuallyhidden") shouldEqual true
          }
        }
      }
    }
  }
}
