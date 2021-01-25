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

package controllers.resident.properties.DeductionsControllerSpec

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import assets.MessageLookup.{PrivateResidenceRelief => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keyStoreKeys}
import controllers.DeductionsController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import models.resident.PrivateResidenceReliefModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import common.{CommonPlaySpec,WithCommonFakeApplication}

import scala.concurrent.Future

class PrivateResidenceReliefActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with DeductionsControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()

  def setupTarget(getData: Option[PrivateResidenceReliefModel]): DeductionsController= {
    when(mockSessionCacheConnector.fetchAndGetFormData[PrivateResidenceReliefModel](ArgumentMatchers.eq(keyStoreKeys.privateResidenceRelief))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheConnector.saveFormData[PrivateResidenceReliefModel](ArgumentMatchers.any(), ArgumentMatchers.any())
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    testingDeductionsController
  }

  "Calling .propertyLivedIn from the resident DeductionsController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.privateResidenceRelief(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(PrivateResidenceReliefModel(true)))
      lazy val result = target.privateResidenceRelief(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.privateResidenceRelief(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }
  }

  "Calling .submitPrivateResidenceRelief from the resident DeductionsCalculator" when {

    "a valid form with the answer 'Yes' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "Yes"))
      lazy val result = target.submitPrivateResidenceRelief(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the private residence relief value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/private-residence-relief-value")
      }
    }

    "a valid form with the answer 'No' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "No"))
      lazy val result = target.submitPrivateResidenceRelief(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the brought forward losses page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DeductionsController.lossesBroughtForward().url)
      }
    }

    "an invalid form with the answer '' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", ""))
      lazy val result = target.submitPrivateResidenceRelief(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "render the Property Lived In page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}
