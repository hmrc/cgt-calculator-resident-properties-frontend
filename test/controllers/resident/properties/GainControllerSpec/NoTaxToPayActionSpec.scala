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
import assets.MessageLookup.{NoTaxToPay => messages}
import common.KeystoreKeys.ResidentPropertyKeys
import controllers.GainController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import models.resident.properties.gain.WhoDidYouGiveItToModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import common.{CommonPlaySpec,WithCommonFakeApplication}

import scala.concurrent.Future

class NoTaxToPayActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(givenTo: String): GainController = {
    when(mockSessionCacheService.fetchAndGetFormData[WhoDidYouGiveItToModel](ArgumentMatchers.eq(ResidentPropertyKeys.whoDidYouGiveItTo))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(WhoDidYouGiveItToModel(givenTo))))

    testingGainController
  }

  "Calling .noTaxToPay" when {

    "A valid session is provided when gifted to charity" should {
      lazy val target = setupTarget("Charity")
      lazy val result = target.noTaxToPay(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title} - Calculate your Capital Gains Tax - GOV.UK" in {
        doc.title shouldEqual messages.title + " - Calculate your Capital Gains Tax - GOV.UK"
      }

      "have text explaining why tax is not owed" in {
        doc.body().getElementsByClass("govuk-body").text() shouldBe messages.charityText
      }
    }

    "A valid session is provided when gifted to a spouse" should {
      lazy val target = setupTarget("Spouse")
      lazy val result = target.noTaxToPay(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title} - Calculate your Capital Gains Tax - GOV.UK" in {
        doc.title shouldEqual messages.title + " - Calculate your Capital Gains Tax - GOV.UK"
      }

      "have text explaining why tax is not owed" in {
        doc.body().getElementsByClass("govuk-body").text() shouldBe messages.spouseText
      }
    }

    "An invalid session is provided" should {
      lazy val target = setupTarget("Other")
      lazy val result = target.noTaxToPay(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }
  }
}
