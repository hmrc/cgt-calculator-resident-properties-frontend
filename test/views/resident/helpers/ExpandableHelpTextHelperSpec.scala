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

import common.{CommonPlaySpec, WithCommonFakeApplication}
import org.jsoup.Jsoup
import play.twirl.api.Html
import views.html.playHelpers.expandableHelpTextHelper

class ExpandableHelpTextHelperSpec extends CommonPlaySpec with WithCommonFakeApplication {

  val expandableHelpTextHelperView = fakeApplication.injector.instanceOf[expandableHelpTextHelper]
  val content = expandableHelpTextHelperView("testQ", Html("someHtml"))
  val doc = Jsoup.parse(content.body)

  "Expandable Help Text Helper" should {

    "have a details tag" which {

      val details = doc.select("details#help")

      "has the id 'help'" in {
        details.attr("id") shouldBe "help"
      }

    }

    "have a header summary" which {

      val summary = doc.select("summary")

      "has 'aria-controls' of 'details-content-0" in {
        summary.hasClass("govuk-details__summary") shouldBe true
      }

      "has span with a class of 'summary'" in {
        summary.select("span").hasClass("govuk-details__summary-text") shouldBe true
      }

      "contains text 'testQ'" in {
        summary.select("summary").text shouldBe "testQ"
      }
    }

    "have hidden html" which {

      val hiddenHtml = doc.select("div")

      "has the class 'panel-indent'" in {
        hiddenHtml.hasClass("govuk-details__text") shouldBe true
      }

      "contains additional html text" in {
        hiddenHtml.text shouldBe "someHtml"
      }

    }
  }
}
