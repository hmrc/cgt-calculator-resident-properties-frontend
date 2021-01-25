/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import assets.MessageLookup
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.redirectLocation
import common.{CommonPlaySpec, WithCommonFakeApplication}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class WhatNextNonSaControllerSpec extends CommonPlaySpec with FakeRequestHelper with CommonMocks with MockitoSugar with WithCommonFakeApplication {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()

  implicit val timeout: Timeout = new Timeout(Duration.create(20, "seconds"))

  def setupController(): WhatNextNonSaController = {

    when(mockAppConfig.analyticsToken).thenReturn("test-token")

    when(mockAppConfig.analyticsHost).thenReturn("analyticsHost")

    when(mockAppConfig.residentIFormUrl).thenReturn("iform-url")

    when(mockSessionCacheService.shouldSelfAssessmentBeConsidered()(ArgumentMatchers.any()))
      .thenReturn(Future.successful(true))

    new WhatNextNonSaController(mockMessagesControllerComponents, mockSessionCacheService, mockAppConfig)
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
        doc.select("#report-now > a").attr("href") shouldBe "iform-url"
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
        doc.select("#report-now > a").attr("href") shouldBe "iform-url"
      }
    }
  }
}
