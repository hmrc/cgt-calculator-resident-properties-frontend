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
import models.resident.properties.WorthWhenGaveAwayModel
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._

import scala.concurrent.Future

class WorthWhenGaveAwayActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(getData: Option[WorthWhenGaveAwayModel]): GainController = {
    when(mockSessionCacheService.fetchAndGetFormData[WorthWhenGaveAwayModel](ArgumentMatchers.eq(keystoreKeys.worthWhenGaveAway))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[WorthWhenGaveAwayModel](ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    testingGainController
  }

  "Calling .worthWhenGaveAway from the GainCalculationController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenGaveAway(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(WorthWhenGaveAwayModel(100)))
      lazy val result = target.worthWhenGaveAway(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }
  }

  "Calling .worthWhenGaveAway from the GainCalculationController" should {

    lazy val target = setupTarget(None)
    lazy val result = target.worthWhenGaveAway(fakeRequestWithSession)

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    s"return some html with title of ${MessageLookup.Resident.Properties.PropertiesWorthWhenGaveAway.title}" in {
      contentType(result) shouldBe Some("text/html")
      Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.Resident.Properties.PropertiesWorthWhenGaveAway.title
    }
  }

  "Calling .worthWhenGaveAway from the GainCalculationController with no session" should {

    lazy val target = setupTarget(None)
    lazy val result = target.worthWhenGaveAway(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }
  }

  "Calling .submitWorthWhenGaveAway from the GainController" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("amount", "100"))
    lazy val result = target.submitWorthWhenGaveAway(request.withMethod("POST"))

    "re-direct to the disposal Costs page with a status of 303" in {
      status(result) shouldEqual 303
    }

    "re-direct to the disposal Costs page when supplied with a valid form" in {
      redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/disposal-costs")
    }
  }

  "Calling .submitWorthWhenGaveAway from the GainController" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
    lazy val result = target.submitWorthWhenGaveAway(request)

    "render with a status of 400" in {
      status(result) shouldEqual 400
    }

    "render the worth when gave away page when supplied with an invalid form" in {
      val serviceName = MessageLookup.Resident.homeText
      val pageTitle = MessageLookup.Resident.Properties.PropertiesWorthWhenGaveAway.title
      Jsoup.parse(bodyOf(result)).title() shouldEqual s"Error: $pageTitle - $serviceName - GOV.UK"
    }
  }
}
