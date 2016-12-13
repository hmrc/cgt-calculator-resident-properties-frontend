/*
 * Copyright 2016 HM Revenue & Customs
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

package controllers.DeductionsControllerSpec

import config.{AppConfig, ApplicationConfig}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.DeductionsController
import models.resident.properties.PropertyLivedInModel
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import common.KeystoreKeys.{ResidentPropertyKeys => keyStoreKeys}
import org.mockito.Matchers
import assets.MessageLookup.{PropertyLivedIn => messages}
import org.jsoup.Jsoup
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class PropertyLivedInActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[PropertyLivedInModel]): DeductionsController= {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[PropertyLivedInModel](Matchers.eq(keyStoreKeys.propertyLivedIn))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[PropertyLivedInModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val config: AppConfig = ApplicationConfig
    }
  }

  "Calling .propertyLivedIn from the resident DeductionsController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.propertyLivedIn(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(PropertyLivedInModel(true)))
      lazy val result = target.propertyLivedIn(fakeRequestWithSession)

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
      lazy val result = target.propertyLivedIn(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }
  }

  "Calling .submitPropertyLivedIn from the resident DeductionsCalculator" when {

    "a valid form with the answer 'Yes' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("livedInProperty", "Yes"))
      lazy val result = target.submitPropertyLivedIn(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the private residence relief page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/private-residence-relief")
      }
    }

    "a valid form with the answer 'No' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("livedInProperty", "No"))
      lazy val result = target.submitPropertyLivedIn(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other properties page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/other-properties")
      }
    }

    "an invalid form with the answer '' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("livedInProperty", ""))
      lazy val result = target.submitPropertyLivedIn(request)
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
