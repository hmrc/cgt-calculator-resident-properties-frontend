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

package controllers

import assets.MessageLookup
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.apache.pekko.util.Timeout
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.redirectLocation
import views.html.calculation.resident.properties.whatNext.{whatNextNonSaGain, whatNextNonSaLoss}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class WhatNextNonSaControllerSpec extends CommonPlaySpec with FakeRequestHelper with CommonMocks with MockitoSugar with WithCommonFakeApplication {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  implicit val timeout: Timeout = new Timeout(Duration.create(20, "seconds"))

  def setupController(): WhatNextNonSaController = {

    when(mockAppConfig.residentIFormUrl).thenReturn("iform-url")

    when(mockSessionCacheService.shouldSelfAssessmentBeConsidered()(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(true))

    new WhatNextNonSaController(mockMessagesControllerComponents,
      mockSessionCacheService,
      fakeApplication.injector.instanceOf[whatNextNonSaGain],
      fakeApplication.injector.instanceOf[whatNextNonSaLoss],
      mockAppConfig)
  }

  "Calling .whatNextNonSaGain" when {

    "provided with an invalid session" should {
      lazy val controller = setupController()
      lazy val result = controller.whatNextNonSaGain(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController()
      lazy val result = controller.whatNextNonSaGain(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the What Next Non-SA Gain page" in {
        doc.title() shouldBe MessageLookup.WhatNextNonSaGain.title
      }

      "have a link to the iForm from app config" in {
        doc.select("a.govuk-button").attr("href") shouldBe "iform-url"
      }
    }
  }

  "Calling .whatNextNonSaLoss" should {

    "provided with an invalid session" should {
      lazy val controller = setupController()
      lazy val result = controller.whatNextNonSaLoss(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController()
      lazy val result = controller.whatNextNonSaLoss(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the What Next Non-SA Loss page" in {
        doc.title() shouldBe MessageLookup.WhatNextNonSaLoss.title
      }

      "have a link to the iForm from app config" in {
        doc.getElementsByClass("govuk-button").attr("href") shouldBe "iform-url"
      }
    }
  }
}
