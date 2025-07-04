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

package config

import common.{CommonPlaySpec, WithCommonFakeApplication}
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionKeys

class FrontendGlobalSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar {

  val cgtErrorHandler: CgtErrorHandler = fakeApplication.injector.instanceOf[CgtErrorHandler]

  "Rendering the error_template by causing an error" when {
    "on the resident/properties journey" should {
      s"have a link to the resident/properties start journey '${controllers.routes.PropertiesController.introduction.url}'" in {
        val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/resident/properties/error").withSession(SessionKeys.sessionId -> "12345")
        val result = cgtErrorHandler.standardErrorTemplate("test", "teat-heading", "test-message")(using fakeRequest)
        val doc = Jsoup.parse(result.body)

        doc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldBe controllers.routes.PropertiesController.introduction.url
      }
    }
  }
}
