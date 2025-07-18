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

package controllers.GainControllerSpec

import assets.MessageLookup
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.GainController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import models.resident.properties.gain.WhoDidYouGiveItToModel
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._

import scala.concurrent.Future

class WhoDidYouGiveItToActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(getData: Option[WhoDidYouGiveItToModel]) : GainController = {
    when(mockSessionCacheService.fetchAndGetFormData[WhoDidYouGiveItToModel](ArgumentMatchers.eq(keystoreKeys.whoDidYouGiveItTo))
      (using ArgumentMatchers.any(), ArgumentMatchers.any())).
      thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[WhoDidYouGiveItToModel](ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful("" -> ""))

    testingGainController
  }

  "Calling .whoDidYouGiveItTo from the GainsController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.whoDidYouGiveItTo(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some html with title of ${MessageLookup.WhoDidYouGiveItTo.title}" in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.WhoDidYouGiveItTo.title
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(WhoDidYouGiveItToModel("Charity")))
      lazy val result = target.whoDidYouGiveItTo(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some html with title of ${MessageLookup.WhoDidYouGiveItTo.title}" in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.WhoDidYouGiveItTo.title
      }
    }
  }

  "Calling .whoDidYouGiveItTo from the GainsController with no session" should{
    lazy val target = setupTarget(None)
    lazy val result = target.whoDidYouGiveItTo(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "to the session timeout page" in {
      redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
    }

  }

  "Calling .submitWhoDidYouGiveItTo from the GainController with a Charity value" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("whoDidYouGiveItTo","Charity"))
    lazy val result = target.submitWhoDidYouGiveItTo(request.withMethod("POST"))

    "when supplied with a valid form" which {
      "redirects" in {
        status(result) shouldEqual 303
      }

      "to the You Have No Tax To Pay page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/no-tax-to-pay")
      }
    }
  }

  "Calling .submitWhoDidYouGiveItTo from the GainController with a Spouse value" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("whoDidYouGiveItTo", "Spouse"))
    lazy val result = target.submitWhoDidYouGiveItTo(request.withMethod("POST"))

    "when supplied with a valid form" which {
      "redirects" in {
        status(result) shouldEqual 303
      }

      "to the You Have No Tax To Pay page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/no-tax-to-pay")
      }
    }
  }

  "Calling .submitWhoDidYouGiveItTo from the GainController with a Someone Else value" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("whoDidYouGiveItTo", "Other"))
    lazy val result = target.submitWhoDidYouGiveItTo(request.withMethod("POST"))

    "when supplied with a valid form" which{
      "redirect" in {
        status(result) shouldEqual 303
      }

      "to the page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/worth-when-gave-away")
      }
    }
  }

  "Calling .submitWhoDidYouGiveIt to from the GainController with an invalid value/bad request" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("whoDidYouGiveItTo", "blah"))
    lazy val result = target.submitWhoDidYouGiveItTo(request)

    "when supplied with an invalid form" which {
      "will generate a 400 error" in {
        status(result) shouldEqual 400
      }
      s"and lead to the current page reloading and return some HTML with title of ${MessageLookup.WhoDidYouGiveItTo.title} " in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.WhoDidYouGiveItTo.title

      }
    }
  }

}
