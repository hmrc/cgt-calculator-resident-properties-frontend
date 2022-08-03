/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.util.Timeout
import assets.MessageLookup
import common.resident.HowYouBecameTheOwnerKeys
import connectors.CalculatorConnector
import controllers.ReviewAnswersController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.properties._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.redirectLocation
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import common.{CommonPlaySpec, WithCommonFakeApplication}
import views.html.calculation.resident.properties.checkYourAnswers.checkYourAnswers

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class ReviewAnswersControllerSpec extends CommonPlaySpec with FakeRequestHelper
  with MockitoSugar with CommonMocks  with WithCommonFakeApplication {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  val date: LocalDate = LocalDate.of(2016, 5, 8)
  val totalLossModel: YourAnswersSummaryModel = YourAnswersSummaryModel(date, Some(100000), None, None, None, 1000, Some(150000),
    None, None, None, 1000, 5000, givenAway = false, Some(false), ownerBeforeLegislationStart = false, None,
    Some(HowYouBecameTheOwnerKeys.boughtIt), Some(false))
  val totalGainModel: YourAnswersSummaryModel = YourAnswersSummaryModel(date, Some(100000), None, None, None, 1000, Some(30000),
    None, None, None, 1000, 5000, givenAway = false, Some(false), ownerBeforeLegislationStart = false, None,
    Some(HowYouBecameTheOwnerKeys.boughtIt), Some(false))
  val allDeductionsModel: ChargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(true)), Some(LossesBroughtForwardValueModel(10000)),
    Some(PropertyLivedInModel(true)), Some(PrivateResidenceReliefModel(true)), Some(PrivateResidenceReliefValueModel(35000)), Some(LettingsReliefModel(true)),
    Some(LettingsReliefValueModel(5000)))
  val noBroughtForwardLossesModel: ChargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None,
    Some(PropertyLivedInModel(true)), Some(PrivateResidenceReliefModel(true)), Some(PrivateResidenceReliefValueModel(35000)), Some(LettingsReliefModel(true)),
    Some(LettingsReliefValueModel(5000)))
  val noDeductionsModel: ChargeableGainAnswers = ChargeableGainAnswers(Some(LossesBroughtForwardModel(false)), None,
    Some(PropertyLivedInModel(false)), None, None, None, None)
  val incomeAnswersModel: IncomeAnswersModel = IncomeAnswersModel(Some(CurrentIncomeModel(25000)), Some(PersonalAllowanceModel(11000)))
  implicit val timeout: Timeout = Timeout.apply(Duration.create(20, "seconds"))
  implicit val hc: HeaderCarrier = HeaderCarrier()

  def setupController(gainResponse: YourAnswersSummaryModel,
                      deductionsResponse: ChargeableGainAnswers,
                      taxYearModel: Option[TaxYearModel] = None): ReviewAnswersController = {

    val mockConnector = mock[CalculatorConnector]
    val mockSessionCacheService = mock[SessionCacheService]

    when(mockSessionCacheService.getPropertyGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainResponse))

    when(mockSessionCacheService.getPropertyDeductionAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(deductionsResponse))

    when(mockConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockSessionCacheService.getPropertyIncomeAnswers(ArgumentMatchers.any()))
      .thenReturn(incomeAnswersModel)


    new ReviewAnswersController(
      mockConnector,
      mockSessionCacheService,
      mockMessagesControllerComponents,
      fakeApplication.injector.instanceOf[checkYourAnswers])
  }

  "Calling .reviewGainAnswers" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(totalLossModel, allDeductionsModel)
      lazy val result = controller.reviewGainAnswers(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController(totalLossModel, allDeductionsModel)
      lazy val result = controller.reviewGainAnswers(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Review Answers page" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe MessageLookup.NonResident.ReviewAnswers.title
      }

      "have a back link to the improvements page" in {
        Jsoup.parse(bodyOf(result)).select("#back-link").attr("href") shouldBe controllers.routes.GainController.improvements().url
      }
    }
  }

  "Calling .reviewDeductionsAnswers" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(
        totalGainModel,
        allDeductionsModel,
        taxYearModel = Some(TaxYearModel("2016/17", isValidYear = true, "2016/17")))
      lazy val result = controller.reviewDeductionsAnswers(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "provided with a valid session and brought forward losses" should {
      lazy val controller = setupController(
        totalLossModel,
        allDeductionsModel,
        taxYearModel = Some(TaxYearModel("2016/17", isValidYear = true, "2016/17")))
      lazy val result = controller.reviewDeductionsAnswers(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Review Answers page" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe MessageLookup.NonResident.ReviewAnswers.title
      }

      "have a back link to the brought forward losses value page" in {
        Jsoup.parse(bodyOf(result)).select("#back-link").attr("href") shouldBe controllers.routes.DeductionsController.lossesBroughtForwardValue().url
      }
    }

    "provided with a valid session and no brought forward losses" should {
      lazy val controller = setupController(
        totalLossModel,
        noBroughtForwardLossesModel,
        taxYearModel = Some(TaxYearModel("2016/17", isValidYear = true, "2016/17")))
      lazy val result = controller.reviewDeductionsAnswers(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Review Answers page" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe MessageLookup.NonResident.ReviewAnswers.title
      }

      "have a back link to the brought forward losses page" in {
        Jsoup.parse(bodyOf(result)).select("#back-link").attr("href") shouldBe controllers.routes.DeductionsController.lossesBroughtForward().url
      }
    }
  }

  "Calling .reviewFinalAnswers" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(
        totalGainModel,
        noDeductionsModel,
        taxYearModel = Some(TaxYearModel("2016/17", isValidYear = true, "2016/17")))
      lazy val result = controller.reviewFinalAnswers(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController(
        totalLossModel,
        noDeductionsModel,
        taxYearModel = Some(TaxYearModel("2016/17", isValidYear = true, "2016/17")))
      lazy val result = controller.reviewFinalAnswers(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Review Answers page" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe MessageLookup.NonResident.ReviewAnswers.title
      }

      "have a back link to the personal allowance page" in {
        Jsoup.parse(bodyOf(result)).select("#back-link").attr("href") shouldBe controllers.routes.IncomeController.personalAllowance().url
      }
    }
  }
}
