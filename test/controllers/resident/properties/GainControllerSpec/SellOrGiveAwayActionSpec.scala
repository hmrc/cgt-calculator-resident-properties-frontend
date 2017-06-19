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

package controllers.GainControllerSpec

import config.AppConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.GainController
import models.resident.properties.SellOrGiveAwayModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{PropertiesSellOrGiveAway => messages}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class SellOrGiveAwayActionSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[SellOrGiveAwayModel]): GainController = {

    val mockConnector = mock[CalculatorConnector]

    when(mockConnector.fetchAndGetFormData[SellOrGiveAwayModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockConnector.saveFormData[SellOrGiveAwayModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockConnector
      override val config: AppConfig = mock[AppConfig]
    }
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
        doc.body().select("a#back-link").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/disposal-date"
      }

      "have a home link to 'homeLink'" in {
        doc.select("a#homeNavHref").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
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
      lazy val result = target.submitSellOrGiveAway(fakeRequestToPOSTWithSession(("givenAway", "Sold")))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the sell-for-less page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/sell-for-less")
      }
    }

    "a valid form with the answer 'Given' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitSellOrGiveAway(fakeRequestToPOSTWithSession(("givenAway", "Given")))

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
        doc.title shouldEqual messages.title
      }

      "raise an error on the page" in {
        doc.body.select("#givenAway-error-summary").size shouldBe 1
      }
    }
  }
}
