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
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.twirl.api.HtmlFormat

class SummaryOptionRowHelperSpec extends UnitSpec with WithFakeApplication {

  "The Summary Numeric Row Helper" when {

    "not provided with any change links" should {
      lazy val row: HtmlFormat.Appendable = summaryOptionRowHelper("testID","testQ",answer = true)
      lazy val doc: Document = Jsoup.parse(row.body)

      "have an outer div" which {

        lazy val outerDiv = doc.select("div#testID")

        "has the id 'testID" in {
          outerDiv.attr("id") shouldBe "testID"
        }

        "has the class 'grid-layout'" in {
          outerDiv.hasClass("grid-layout") shouldBe true
        }

        "has the class 'grid-layout--stacked'" in {
          outerDiv.hasClass("grid-layout--stacked") shouldBe true
        }

        "has the class 'form-group'" in {
          outerDiv.hasClass("form-group") shouldBe true
        }

        "has the class 'resident'" in {
          outerDiv.hasClass("resident") shouldBe true
        }

        "has the class 'summary-underline'" in {
          outerDiv.hasClass("summary-underline") shouldBe true
        }
      }

      "have an inner question div" which {

        lazy val questionDiv = doc.select("div#testID-question")

        "has the id 'testID-question" in {
          questionDiv.attr("id") shouldBe "testID-question"
        }

        "has the class 'grid-layout__column'" in {
          questionDiv.hasClass("grid-layout__column") shouldBe true
        }

        "has the class 'grid-layout__column--1-2'" in {
          questionDiv.hasClass("grid-layout__column--1-2") shouldBe true
        }

        "has the text 'testQ'" in {
          questionDiv.text shouldBe "testQ"
        }

      }

      "have an inner amount div" which {

        lazy val amountDiv = doc.select("div#testID-option")

        "has the id 'testID-option" in {
          amountDiv.attr("id") shouldBe "testID-option"
        }

        "has the class 'grid-layout__column'" in {
          amountDiv.hasClass("grid-layout__column") shouldBe true
        }

        "has the class 'grid-layout__column--1-2'" in {
          amountDiv.hasClass("grid-layout__column--1-2") shouldBe true
        }

        "has a span with the text 'testQ'" in {
          amountDiv.select("span").text shouldBe "Yes"
        }
      }
    }

    s"provided with a change link " should {

      lazy val row = summaryOptionRowHelper("testID","testQ",answer = true, Some("link"), allowLinks = true)
      lazy val doc: Document = Jsoup.parse(row.body)

      "have an outer div" which {

        lazy val outerDiv = doc.select("div#testID")

        "has the id 'testID" in {
          outerDiv.attr("id") shouldBe "testID"
        }

        "has the class 'grid-layout'" in {
          outerDiv.hasClass("grid-layout") shouldBe true
        }

        "has the class 'grid-layout--stacked'" in {
          outerDiv.hasClass("grid-layout--stacked") shouldBe true
        }

        "has the class 'form-group'" in {
          outerDiv.hasClass("form-group") shouldBe true
        }

        "has the class 'resident'" in {
          outerDiv.hasClass("resident") shouldBe true
        }

        "has the class 'summary-underline'" in {
          outerDiv.hasClass("summary-underline") shouldBe true
        }
      }

      "have an inner question div" which {

        lazy val questionDiv = doc.select("div#testID-question")

        "has the id 'testID-question" in {
          questionDiv.attr("id") shouldBe "testID-question"
        }

        "has the class 'grid-layout__column'" in {
          questionDiv.hasClass("grid-layout__column") shouldBe true
        }

        "has the class 'grid-layout__column--1-2'" in {
          questionDiv.hasClass("grid-layout__column--1-2") shouldBe true
        }

        "has the text 'testQ'" in {
          questionDiv.text shouldBe "testQ"
        }

      }

      "have an inner amount div" which {

        lazy val amountDiv = doc.select("div#testID-option")

        "has the id 'testID-option" in {
          amountDiv.attr("id") shouldBe "testID-option"
        }

        "has the class 'grid-layout__column'" in {
          amountDiv.hasClass("grid-layout__column") shouldBe true
        }

        "has the class 'grid-layout__column--1-4'" in {
          amountDiv.hasClass("grid-layout__column--1-4") shouldBe true
        }

        "has a span with the text 'testQ'" in {
          amountDiv.text shouldBe "Yes"
        }
      }

      "have an inner link div" which {

        lazy val amountDiv = doc.select("div#testID-change-link")

        "has the id 'testID-change-link" in {
          amountDiv.attr("id") shouldBe "testID-change-link"
        }

        "has the class 'grid-layout__column'" in {
          amountDiv.hasClass("grid-layout__column") shouldBe true
        }

        "has the class 'grid-layout__column--1-4'" in {
          amountDiv.hasClass("grid-layout__column--1-4") shouldBe true
        }

        "includes a change link" which {
          lazy val link = amountDiv.select("a")

          "has a link to 'link'" in {
            link.attr("href") shouldBe "link"
          }

          "has the text 'change'" in {
            link.text shouldBe commonMessages.change + " testQ"
          }

          "has the question visually hidden as part of the link" in {
            link.select("span.visuallyhidden").text shouldBe "testQ"
          }
        }
      }
    }
  }
}
