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

import assets.MessageLookup.{AcquisitionValue => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.GainController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import models.resident.AcquisitionValueModel
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._

import scala.concurrent.Future

class AcquisitionValueActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(getData: Option[AcquisitionValueModel]): GainController = {
    when(mockSessionCacheService.fetchAndGetFormData[AcquisitionValueModel](ArgumentMatchers.eq(keystoreKeys.acquisitionValue))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[AcquisitionValueModel](ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    testingGainController
  }

  "Calling .acquisitionValue from the GainCalculationController with session" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None)
      lazy val result = target.acquisitionValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Acquisition Value view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(AcquisitionValueModel(1000)))
      lazy val result = target.acquisitionValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Acquisition Value view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }
  }

  "Calling .acquisitionValue from the GainCalculationController with no session" should {
    lazy val target = setupTarget(None)
    lazy val result = target.acquisitionValue(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
    }
  }

  "Calling .submitAcquisitionValue from the GainCalculationConroller" when {

    "a valid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitAcquisitionValue(request.withMethod("POST"))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the acquisition costs page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/acquisition-costs")
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitAcquisitionValue(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the acquisition value page" in {
        doc.title() shouldEqual s"Error: ${messages.title}"
      }
    }
  }
}
