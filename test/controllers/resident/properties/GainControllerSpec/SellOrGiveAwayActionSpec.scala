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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import assets.MessageLookup.{PropertiesSellOrGiveAway => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.GainController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import models.resident.properties.SellOrGiveAwayModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._

import scala.concurrent.Future

class SellOrGiveAwayActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(getData: Option[SellOrGiveAwayModel]): GainController = {
    when(mockSessionCacheService.fetchAndGetFormData[SellOrGiveAwayModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[SellOrGiveAwayModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    testingGainController
  }

  "Calling .sellOrGiveAway action" when {

    "request has a valid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.sellOrGiveAway(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        doc.title shouldEqual messages.title
      }

      "have a back link to Disposal Date" in {
        doc.body().select(".govuk-back-link").attr("href") shouldBe "#"
      }

      "have a home link to 'homeLink'" in {
        doc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
      }

      "have a method to POST" in {
        doc.select("form").attr("method") shouldBe "POST"
      }

      "have an action to sell-or-give-away" in {
        doc.select("form").attr("action") shouldBe "/calculate-your-capital-gains/resident/properties/sell-or-give-away"
      }
    }

    "request has a valid session with existing data" should {
      lazy val target = setupTarget(Some(SellOrGiveAwayModel(true)))
      lazy val result = target.sellOrGiveAway(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has an invalid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.sellOrGiveAway(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }
  }

  "Calling .submitSellOrGiveAway action" when {

    "a valid form with the answer 'Sold' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitSellOrGiveAway(fakeRequestToPOSTWithSession(("givenAway", "Sold")).withMethod("POST"))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the sell-for-less page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/sell-for-less")
      }
    }

    "a valid form with the answer 'Given' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitSellOrGiveAway(fakeRequestToPOSTWithSession(("givenAway", "Given")).withMethod("POST"))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the sell-for-less page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/who-did-you-give-it-to")
      }
    }

    "an invalid form with no answer is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitSellOrGiveAway(fakeRequestToPOSTWithSession(("givenAway", "")))
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the page" in {
        doc.title shouldEqual s"Error: ${messages.title}"
      }

      "raise an error on the page" in {
        doc.body.select(".govuk-error-summary__body").size shouldBe 1
      }
    }
  }
}
