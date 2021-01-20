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

package controllers.GainControllerSpec

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import assets.MessageLookup
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import controllers.GainController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import models.resident.DisposalValueModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class DisposalValueActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()

  def setupTarget(getData: Option[DisposalValueModel]): GainController = {
    when(mockSessionCacheConnector.fetchAndGetFormData[DisposalValueModel](ArgumentMatchers.eq(keystoreKeys.disposalValue))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheConnector.saveFormData[DisposalValueModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    testingGainController
  }

  "Calling .disposalValue from the GainCalculationController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.disposalValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(DisposalValueModel(100)))
      lazy val result = target.disposalValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }
  }

  "Calling .disposalValue from the GainCalculationController" should {

    lazy val target = setupTarget(None)
    lazy val result = target.disposalValue(fakeRequestWithSession)

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    s"return some html with title of ${MessageLookup.DisposalValue.question}" in {
      contentType(result) shouldBe Some("text/html")
      Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.DisposalValue.question
    }
  }

  "Calling .disposalValue from the GainCalculationController with no session" should {

    lazy val target = setupTarget(None)
    lazy val result = target.disposalValue(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }
  }

  "Calling .submitDisposalValue from the GainController" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("amount", "100"))
    lazy val result = target.submitDisposalValue(request)

    "re-direct to the disposal Costs page when supplied with a valid form" in {
      status(result) shouldEqual 303
      redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/disposal-costs")
    }
  }

  "Calling .submitDisposalValue from the GainController" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
    lazy val result = target.submitDisposalValue(request)

    "render the disposal value page when supplied with an invalid form" in {
      status(result) shouldEqual 400
      Jsoup.parse(bodyOf(result)).title() shouldEqual MessageLookup.DisposalValue.question
    }
  }
}
