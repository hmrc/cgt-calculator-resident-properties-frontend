/*
 * Copyright 2023 HM Revenue & Customs
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

import akka.actor.ActorSystem
import akka.stream.Materializer
import assets.MessageLookup.Resident.Properties.{OwnerBeforeLegislationStart => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keyStoreKeys}
import controllers.GainController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import models.resident.properties.gain.OwnerBeforeLegislationStartModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import common.{CommonPlaySpec,WithCommonFakeApplication}

import scala.concurrent.Future

class OwnerBeforeLegislationStartActionSpec extends CommonPlaySpec with WithCommonFakeApplication
  with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(getData: Option[OwnerBeforeLegislationStartModel]): GainController = {
    when(mockSessionCacheService.fetchAndGetFormData[OwnerBeforeLegislationStartModel](ArgumentMatchers.eq(
      keyStoreKeys.ownerBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[OwnerBeforeLegislationStartModel](ArgumentMatchers.any(), ArgumentMatchers.any())
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    testingGainController
  }

  "Calling .ownerBeforeLegislationStart from the resident GainController" when {

    "request has a valid session and no keystore value" should {
      lazy val target = setupTarget(None)
      lazy val result = target.ownerBeforeLegislationStart(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(OwnerBeforeLegislationStartModel(true)))
      lazy val result = target.ownerBeforeLegislationStart(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.ownerBeforeLegislationStart(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }
  }

  "Calling .submitOwnerBeforeAprilNineteenEightyTwo from the resident GainCalculator" when {

    "a valid form with the answer 'Yes' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("ownedBeforeLegislationStart", "Yes"))
      lazy val result = target.submitOwnerBeforeLegislationStart(request.withMethod("POST"))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the worth on 31/03/1982 sold page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/value-before-legislation-start")
      }
    }

    "a valid form with the answer 'No' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("ownedBeforeLegislationStart", "No"))
      lazy val result = target.submitOwnerBeforeLegislationStart(request.withMethod("POST"))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the how did you become the owner page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/how-became-owner")
      }
    }

    "an invalid form with the answer '' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("ownedBeforeLegislationStart", ""))
      lazy val result = target.submitOwnerBeforeLegislationStart(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "render the Sell For Less page" in {
        doc.title() shouldEqual s"Error: ${messages.title}"
      }
    }
  }
}
