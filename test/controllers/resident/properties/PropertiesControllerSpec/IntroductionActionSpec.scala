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

package controllers.PropertiesControllerSpec

import assets.MessageLookup.{IntroductionView => messages}
import controllers.helpers.FakeRequestHelper
import controllers.PropertiesController
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class IntroductionActionSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar with FakeRequestHelper {

  "Calling the introduction action" should {

    lazy val result = PropertiesController.introduction(fakeRequest)

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    "return some html" in {
      contentType(result) shouldBe Some("text/html")
    }

    "display the introduction view" in {
      Jsoup.parse(bodyOf(result)).title shouldBe messages.title
    }

  }
}
