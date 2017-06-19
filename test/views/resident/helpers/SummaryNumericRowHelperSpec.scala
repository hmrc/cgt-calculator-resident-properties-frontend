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
import views.html.helpers.resident.summaryNumericRowHelper
import assets.MessageLookup.{Resident => commonMessages}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages.Implicits._

class SummaryNumericRowHelperSpec extends UnitSpec with GuiceOneAppPerSuite {

  "The Summary Numeric Row Helper" when {

    "provided with no link" should {
      lazy val row = summaryNumericRowHelper("testID","testQ",2000)
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

      "have no link" in {
        doc.select("#testID-change-link").size shouldBe 0
      }
    }

    "provided with a change link " should {
      lazy val row = summaryNumericRowHelper("testID","testQ",2000,Some("link"))
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
            link.select("span").hasClass("visuallyhidden") shouldEqual true
          }
        }
      }
    }
  }
}
