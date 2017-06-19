/*
 * Copyright 2017 HM Revenue & Customs
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
import config.AppConfig
import connectors.CalculatorConnector
import controllers.GainController
import controllers.helpers.FakeRequestHelper
import models.resident.properties.WorthWhenGaveAwayModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class WorthWhenGaveAwayActionSpec extends UnitSpec with GuiceOneAppPerSuite with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[WorthWhenGaveAwayModel]): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[WorthWhenGaveAwayModel](ArgumentMatchers.eq(keystoreKeys.worthWhenGaveAway))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[WorthWhenGaveAwayModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config: AppConfig = mock[AppConfig]
    }
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
    lazy val result = target.submitWorthWhenGaveAway(request)

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
      Jsoup.parse(bodyOf(result)).title() shouldEqual MessageLookup.Resident.Properties.PropertiesWorthWhenGaveAway.title
    }
  }
}
