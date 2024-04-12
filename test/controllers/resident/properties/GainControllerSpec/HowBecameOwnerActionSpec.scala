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

import assets.MessageLookup.{HowBecameOwner => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.GainController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import models.resident.properties.HowBecameOwnerModel
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._

class HowBecameOwnerActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(getData: Option[HowBecameOwnerModel]): GainController = {
    when(mockSessionCacheService.fetchAndGetFormData[HowBecameOwnerModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(getData)

    when(mockSessionCacheService.saveFormData[HowBecameOwnerModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn("" -> "")

    testingGainController
  }

  "Calling .howBecameOwner action" when {

    "provided with a valid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.howBecameOwner(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        doc.title shouldEqual messages.title
      }

      "have a back link to owner-before" in {
        doc.body().select(".govuk-back-link").attr("href") shouldBe "#"
      }

      "have a home link to 'homeLink'" in {
        doc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
      }

      "have a method to POST" in {
        doc.select("form").attr("method") shouldBe "POST"
      }

      "have an action to how-became-owner" in {
        doc.select("form").attr("action") shouldBe "/calculate-your-capital-gains/resident/properties/how-became-owner"
      }
    }

    "provided with a valid session with stored data" should {
      lazy val target = setupTarget(Some(HowBecameOwnerModel("Bought")))
      lazy val result = target.howBecameOwner(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }
    }

    "provided with an invalid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.howBecameOwner(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }
    }
  }

  "Calling .submitHowBecameOwner action" when {
    "a valid form with the answer 'Bought' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Bought")).withMethod("POST"))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the bought-for-less-than-worth page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/bought-for-less-than-worth")
      }
    }
  }

  "a valid form with the answer 'Inherited' is submitted" should {
    lazy val target = setupTarget(None)
    lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Inherited")).withMethod("POST"))

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "redirect to the worth-when-inherited page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/worth-when-inherited")
    }
  }


  "a valid form with the answer 'Gifted' is submitted" should {
    lazy val target = setupTarget(None)
    lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Gifted")).withMethod("POST"))

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "redirect to the worth-when-gifted page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/worth-when-gifted")
    }
  }


  "an invalid form with no answer is submitted" should {
    lazy val target = setupTarget(None)
    lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "")))
    lazy val doc = Jsoup.parse(bodyOf(result))

    "return a status of 400" in {
      status(result) shouldBe 400
    }

    "return to the page" in {
      doc.title shouldEqual s"Error: ${messages.title}"
    }
  }
}
