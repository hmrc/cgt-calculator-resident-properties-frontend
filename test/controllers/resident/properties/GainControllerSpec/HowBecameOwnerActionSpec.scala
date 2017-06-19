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
import models.resident.properties.HowBecameOwnerModel
import org.mockito.ArgumentMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{HowBecameOwner => messages}
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._

class HowBecameOwnerActionSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[HowBecameOwnerModel]): GainController = {

    val mockConnector = mock[CalculatorConnector]

    when(mockConnector.fetchAndGetFormData[HowBecameOwnerModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(getData)

    when(mockConnector.saveFormData[HowBecameOwnerModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(mock[CacheMap])

    new GainController {
      override val calcConnector: CalculatorConnector = mockConnector
      override val config: AppConfig = mock[AppConfig]
    }
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
        doc.body().select("a#back-link").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/owner-before-legislation-start"
      }

      "have a home link to 'homeLink'" in {
        doc.select("a#homeNavHref").attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/"
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
      lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Bought")))

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
    lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Inherited")))

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "redirect to the worth-when-inherited page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/worth-when-inherited")
    }
  }


  "a valid form with the answer 'Gifted' is submitted" should {
    lazy val target = setupTarget(None)
    lazy val result = target.submitHowBecameOwner(fakeRequestToPOSTWithSession(("gainedBy", "Gifted")))

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
      doc.title shouldEqual messages.title
    }

    "raise an error on the page" in {
      doc.body.select("#gainedBy-error-summary").size shouldBe 1
    }
  }
}
