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

import assets.MessageLookup.OutsideTaxYears as messages
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.GainController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.resident.properties.GainControllerSpec.GainControllerBaseSpec
import models.resident.{DisposalDateModel, TaxYearModel}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.*

import scala.concurrent.Future

class OutsideTaxYearsActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar with GainControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(disposalDateModel: Option[DisposalDateModel], taxYearModel: Option[TaxYearModel]): GainController = {
    when(mockSessionCacheService.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(disposalDateModel))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    testingGainController
  }

  "Calling .outsideTaxYears from the GainCalculationController" when {

    "there is a valid session" should {
      lazy val target = setupTarget(Some(DisposalDateModel(10, 10, 2018)), Some(TaxYearModel("2018/19", false, "2018/19")))
      lazy val result = target.outsideTaxYears(fakeRequestWithSession)

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }

      s"have a back link to '${controllers.routes.GainController.disposalDate.url}'" in {
        Jsoup.parse(bodyOf(result)).select(".govuk-back-link").attr("href") shouldBe "#"
      }
    }

    "there is no valid session" should {
      lazy val target = setupTarget(Some(DisposalDateModel(10, 10, 2018)), Some(TaxYearModel("2018/19", false, "2018/19")))
      lazy val result = target.outsideTaxYears(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }
  }
}
