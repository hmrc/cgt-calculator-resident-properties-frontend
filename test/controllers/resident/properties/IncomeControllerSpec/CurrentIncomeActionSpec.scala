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

package controllers.IncomeControllerSpec

import assets.MessageLookup.{CurrentIncome => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.{CommonPlaySpec, Dates, WithCommonFakeApplication}
import connectors.CalculatorConnector
import controllers.IncomeController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import models.resident._
import models.resident.income._
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import services.SessionCacheService
import views.html.calculation.resident.personalAllowance
import views.html.calculation.resident.properties.income.currentIncome

import scala.concurrent.Future

class CurrentIncomeActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar with CommonMocks {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupTarget(storedData: Option[CurrentIncomeModel],
                  lossesBroughtForward: Boolean = true,
                  disposalDate: Option[DisposalDateModel],
                  taxYear: Option[TaxYearModel]): IncomeController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockSessionCacheConnector = mock[SessionCacheService]

    when(mockSessionCacheConnector.fetchAndGetFormData[CurrentIncomeModel](ArgumentMatchers.eq(keystoreKeys.currentIncome))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(storedData))

    when(mockSessionCacheConnector.fetchAndGetFormData[LossesBroughtForwardModel](ArgumentMatchers.eq(keystoreKeys.lossesBroughtForward))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(LossesBroughtForwardModel(lossesBroughtForward))))

    when(mockSessionCacheConnector.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.eq(keystoreKeys.disposalDate))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(disposalDate))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYear))

    when(mockSessionCacheConnector.saveFormData[LossesBroughtForwardValueModel]
      (ArgumentMatchers.eq(keystoreKeys.currentIncome),ArgumentMatchers.any())
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))


    new IncomeController(mockCalcConnector, mockSessionCacheConnector, mockMessagesControllerComponents,
      fakeApplication.injector.instanceOf[currentIncome],
      fakeApplication.injector.instanceOf[personalAllowance])
  }

  "Calling .currentIncome from the IncomeController with a session" when {

    "supplied with no pre-existing stored data for 2015/16" should {

      lazy val target = setupTarget(None, disposalDate = Some(DisposalDateModel(10, 10, 2015)), taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.currentIncome(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Current Income view for 2015 to 2016" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title("2015 to 2016")
      }

      "supplied with no pre-existing stored data for the Current tax year" should {

        lazy val target = setupTarget(None, disposalDate = Some(DisposalDateModel(10, 10, 2016)), taxYear =
          Some(TaxYearModel(Dates.getCurrentTaxYear, true, Dates.getCurrentTaxYear)))
        lazy val result = target.currentIncome(fakeRequestWithSession)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        "return some html" in {
          contentType(result) shouldBe Some("text/html")
        }

        "display the Current Income for 2016/17 view" in {
          Jsoup.parse(bodyOf(result)).title shouldBe messages.currentYearTitle
        }
      }
    }

    "supplied with pre-existing stored data" should {

      lazy val target = setupTarget(Some(CurrentIncomeModel(40000)), disposalDate = Some(DisposalDateModel(10, 10, 2015)),
        taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "have the amount 40000 pre-populated into the input field" in {
        doc.getElementById("amount").attr("value") shouldBe "40000"
      }
    }

    "no brought forward losses" should {

      lazy val target = setupTarget(None, lossesBroughtForward = false, disposalDate = Some(DisposalDateModel(10, 10, 2015)),
        taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/properties/losses-brought-forward" in {
        doc.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "brought forward losses has been selected" should {

      lazy val target = setupTarget(None, disposalDate = Some(DisposalDateModel(10, 10, 2015)),
        taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/properties/losses-brought-forward-value" in {
        doc.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }
  }

  "Calling .currentIncome from the IncomeController with no session" should {
    lazy val target = setupTarget(None, disposalDate = Some(DisposalDateModel(10, 10, 2015)),
      taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
    lazy val result = target.currentIncome(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
    }
  }

  "calling .submitCurrentIncome from the IncomeController" when {

    "given a valid form should" should {

      lazy val target = setupTarget(Some(CurrentIncomeModel(40000)), disposalDate = Some(DisposalDateModel(10, 10, 2015)),
        taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.submitCurrentIncome(fakeRequestToPOSTWithSession(("amount", "40000")).withMethod("POST"))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      s"redirect to '${controllers.routes.IncomeController.personalAllowance.toString}'" in {
        redirectLocation(result).get shouldBe controllers.routes.IncomeController.personalAllowance.toString
      }
    }

    "given an invalid form" should {

      lazy val target = setupTarget(Some(CurrentIncomeModel(-40000)),
        disposalDate = Some(DisposalDateModel(10, 10, 2015)), taxYear = Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.submitCurrentIncome(fakeRequestToPOSTWithSession(("amount", "-40000")))

      "return a status of 400" in {
        status(result) shouldBe 400
      }
    }
  }
}
