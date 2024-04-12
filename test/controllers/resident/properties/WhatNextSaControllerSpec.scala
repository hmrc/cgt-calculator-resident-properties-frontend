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

package controllers.resident.properties

import java.time._

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import assets.MessageLookup
import controllers.WhatNextSAController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import models.resident.DisposalDateModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import common.{CommonPlaySpec, WithCommonFakeApplication}
import views.html.calculation.resident.properties.whatNext.{whatNextSAFourTimesAEA, whatNextSaGain, whatNextSaNoGain}

import scala.concurrent.Future

class WhatNextSaControllerSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar with CommonMocks {

  val date: LocalDate = LocalDate.of(2016, 5, 8)

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupController(disposalDate: DisposalDateModel): WhatNextSAController = {
    when(mockSessionCacheService.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(disposalDate)))
    when(mockAppConfig.residentIFormUrl).thenReturn("iform-url")

    new WhatNextSAController(mockCalcConnector, mockSessionCacheService, mockMessagesControllerComponents,
      fakeApplication.injector.instanceOf[whatNextSAFourTimesAEA],
      fakeApplication.injector.instanceOf[whatNextSaNoGain],
      fakeApplication.injector.instanceOf[whatNextSaGain],
      mockAppConfig)
  }

  "Calling .whatNextSAOverFourTimesAEA" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSAOverFourTimesAEA(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSAOverFourTimesAEA(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the WhatNextFourTimesAEA page" in {
        Jsoup.parse(bodyOf(result)).select("#main-content").text should include(MessageLookup.WhatNextPages.FourTimesAEA.paragraphOne)
      }

      "have a back link to the confirm-sa page" in {
        Jsoup.parse(bodyOf(result)).select(".govuk-back-link").attr("href") shouldBe "#"
      }
    }
  }

  "Calling .whatNextSANoGain" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSANoGain(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSANoGain(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the WhatNextFourTimesAEA page" in {
        Jsoup.parse(bodyOf(result)).select("#bullet-list-title").text should include(MessageLookup.WhatNextPages.WhatNextNoGain.bulletPointTitle)
      }

      "have a back link to the confirm-sa page" in {
        Jsoup.parse(bodyOf(result)).select(".govuk-back-link").attr("href") shouldBe "#"
      }
    }
  }

  "Calling .whatNextSAGain" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSAGain(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSAGain(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the WhatNextFourTimesAEA page" in {
        Jsoup.parse(bodyOf(result)).select("#bullet-list-title").text should include(MessageLookup.WhatNextPages.WhatNextGain.bulletPointTitle)
      }

      "have a back link to the confirm-sa page" in {
        Jsoup.parse(bodyOf(result)).select(".govuk-back-link").attr("href") shouldBe "#"
      }
    }
  }
}
