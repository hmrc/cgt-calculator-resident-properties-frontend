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

import assets.MessageLookup.Resident.Properties.{WorthWhenInherited => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keyStoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.GainController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import models.resident.WorthWhenInheritedModel
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._

import scala.concurrent.Future

class WorthWhenInheritedActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(getData: Option[WorthWhenInheritedModel]): GainController= {
    when(mockSessionCacheService.fetchAndGetFormData[WorthWhenInheritedModel](ArgumentMatchers.eq(keyStoreKeys.worthWhenInherited))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[WorthWhenInheritedModel](ArgumentMatchers.any(), ArgumentMatchers.any())
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    testingGainController
  }

  "Calling .sellOrGiveAway action" when {

    "request has a valid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenInherited(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        doc.title shouldEqual messages.title
      }

      "have a back link to how-became-owner" in {
        doc.body().select(".govuk-back-link").attr("href") shouldBe "#"
      }

      "have a home link to 'homeLink'" in {
        doc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
      }

      "have a method to POST" in {
        doc.select("form").attr("method") shouldBe "POST"
      }

      "have an action to worth-when-inherited" in {
        doc.select("form").attr("action") shouldBe "/calculate-your-capital-gains/resident/properties/worth-when-inherited"
      }
    }

    "request has a valid session with existing data" should {
      lazy val target = setupTarget(Some(WorthWhenInheritedModel(100)))
      lazy val result = target.worthWhenInherited(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has an invalid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenInherited(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }
  }

  "Calling .submitSellOrGiveAway action" when {

    "a valid form with the answer '100' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitWorthWhenInherited(fakeRequestToPOSTWithSession(("amount", "100")).withMethod("POST"))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the acquisition-costs page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/acquisition-costs")
      }
    }

    "an invalid form with no answer is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitWorthWhenInherited(fakeRequestToPOSTWithSession(("amount", "")))
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the page" in {
        doc.title shouldEqual s"Error: ${messages.title}"
      }

      "raise an error on the page" in {
        doc.body.select(".govuk-error-summary").size shouldBe 1
      }
    }
  }

}
