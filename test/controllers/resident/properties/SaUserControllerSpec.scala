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

package controllers.resident.properties

import assets.MessageLookup
import controllers.SaUserController
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

class SaUserControllerSpec extends UnitSpec with OneAppPerSuite with FakeRequestHelper with MockitoSugar {

  "Calling .saUser" when {

    "no session is provided" should {
      lazy val result = SaUserController.saUser(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the missing session page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "a session is provided" should {
      lazy val result = SaUserController.saUser(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the saUser page" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe MessageLookup.SaUser.title
      }
    }
  }
}
