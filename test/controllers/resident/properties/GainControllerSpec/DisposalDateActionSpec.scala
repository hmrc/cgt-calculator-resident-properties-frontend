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

import assets.MessageLookup.{DisposalDate => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.GainController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import models.resident.{DisposalDateModel, TaxYearModel}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class DisposalDateActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(getData: Option[DisposalDateModel]): GainController = {
    when(mockSessionCacheService.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.eq(keystoreKeys.disposalDate))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[DisposalDateModel](ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    when(mockCalcConnector.getMinimumDate()(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(LocalDate.parse("2015-06-04")))

    testingGainController
  }

  case class FakePOSTRequest (dateResponse: TaxYearModel, inputOne: (String, String), inputTwo: (String, String), inputThree: (String, String)) {

    def setupTarget(): GainController = {
      when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(using ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(dateResponse)))

      when(mockSessionCacheService.saveFormData[DisposalDateModel](ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful("" -> ""))

      when(mockCalcConnector.getMinimumDate()(using ArgumentMatchers.any()))
        .thenReturn(Future.successful(LocalDate.parse("2015-06-04")))

      testingGainController
    }

    val target = setupTarget()
    val result = target.submitDisposalDate(fakeRequestToPOSTWithSession(inputOne, inputTwo, inputThree).withMethod("POST"))
    val doc = Jsoup.parse(bodyOf(result))
  }

  "Calling .disposalDate from the GainCalculationController" should {

    "when there is no keystore data" should {

      lazy val target = setupTarget(None)
      lazy val result = target.disposalDate(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a page with the title ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "when there is keystore data" should {

      lazy val target = setupTarget(Some(DisposalDateModel(10, 10, 2016)))
      lazy val result = target.disposalDate(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a page with the title ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }
  }

  "Calling .disposalDate from the GainCalculationController with no session" should {
    lazy val target = setupTarget(None)
    lazy val result = target.disposalDate(fakeRequest)

    "return a status of 200" in {
      status(result) shouldBe 200
    }
  }

  "Calling .submitDisposalDate from the GainCalculationController" should {

    "when there is a valid form" should {

      lazy val dateResponse = TaxYearModel("2016/17", true, "2016/17")
      lazy val request = FakePOSTRequest(dateResponse, ("disposalDate.day", "28"), ("disposalDate.month", "4"), ("disposalDate.year", "2016"))

      "return a status of 303" in {
        status(request.result) shouldBe 303
      }

      "redirect to the Sell Or Give Away page" in {
        redirectLocation(request.result) shouldBe Some("/calculate-your-capital-gains/resident/properties/sell-or-give-away")
      }
    }

    "when there is no session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitDisposalDate(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "when there is an invalid form" should {

      lazy val dateResponse = TaxYearModel("2016/17", true, "2016/17")
      lazy val request = FakePOSTRequest(dateResponse, ("disposalDate.day", "32"), ("disposalDate.month", "4"), ("disposalDate.year", "2016"))

      "return a status of 400 with an invalid POST" in {
        status(request.result) shouldBe 400
      }

      s"return a page with the title ${messages.errorTitle}" in {
        Jsoup.parse(bodyOf(request.result)).title shouldBe messages.errorTitle
      }
    }

    "when there is a date that is greater than any specified tax year" should {

      lazy val dateResponse = TaxYearModel("2019/20", false, "2016/17")
      lazy val request = FakePOSTRequest(dateResponse, ("disposalDate.day", "30"), ("disposalDate.month", "4"), ("disposalDate.year", "2019"))

      "return a status of 303" in {
        status(request.result) shouldBe 303
      }

      "redirect to the outside know years page" in {
        redirectLocation(request.result) shouldBe Some("/calculate-your-capital-gains/resident/properties/outside-tax-years")
      }
    }
    "when there is a date that is less than any specified tax year" should {

      lazy val dateResponse = TaxYearModel("2013/14", false, "2015/16")
      lazy val request = FakePOSTRequest(dateResponse, ("disposalDate.day", "12"), ("disposalDate.month", "4"), ("disposalDate.year", "2013"))

      "return a status of 400" in {
        status(request.result) shouldBe 400
      }

      s"return a page with the title ${messages.errorTitle}" in {
        Jsoup.parse(bodyOf(request.result)).title shouldBe messages.errorTitle
      }
    }
  }
}
