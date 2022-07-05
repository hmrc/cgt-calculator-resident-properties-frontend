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

import akka.actor.ActorSystem
import akka.stream.Materializer
import assets.{MessageLookup, ModelsAsset}
import controllers.SaUserController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import models.resident.properties.YourAnswersSummaryModel
import models.resident.{ChargeableGainResultModel, TotalGainAndTaxOwedModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import common.{CommonPlaySpec, WithCommonFakeApplication}
import views.html.calculation.resident.properties.whatNext.saUser

import scala.concurrent.Future

class SaUserControllerSpec extends CommonPlaySpec with FakeRequestHelper with MockitoSugar with CommonMocks with WithCommonFakeApplication {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  def setupController(yourAnswersSummaryModel: YourAnswersSummaryModel, chargeableGain: BigDecimal, totalGain: BigDecimal,
                      taxOwed: BigDecimal, assessmentRequired: Boolean = true): SaUserController = {
    when(mockSessionCacheService.getPropertyGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(yourAnswersSummaryModel))

    when(mockSessionCacheService.getPropertyDeductionAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(ModelsAsset.deductionAnswersLeastPossibles))

    when(mockSessionCacheService.getPropertyIncomeAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(ModelsAsset.incomeAnswers))

    when(mockCalcConnector.calculateRttPropertyGrossGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGain))

    when(mockCalcConnector.calculateRttPropertyChargeableGain(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(ChargeableGainResultModel(totalGain, chargeableGain, 0, 0, 0, 0, 0, None, None, 0, 0))))

    when(mockCalcConnector.calculateRttPropertyTotalGainAndTax(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
    .thenReturn(Future.successful(Some(TotalGainAndTaxOwedModel(totalGain, chargeableGain, 11000, 0, 5000, 10000, 5, None, None, None, None, 0, 0))))

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
    .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
    .thenReturn(Future.successful(Some(ModelsAsset.taxYearModel)))

    when(mockSessionCacheService.shouldSelfAssessmentBeConsidered()(ArgumentMatchers.any()))
      .thenReturn(Future.successful(assessmentRequired))

    new SaUserController(mockCalcConnector, mockSessionCacheService, mockMessagesControllerComponents,
      fakeApplication.injector.instanceOf[saUser])
  }

  "Calling .saUser" when {

    "no session is provided" should {
      lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 0, -10000, 0)
      lazy val result = controller.saUser(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the missing session page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "a session is provided" should {
      lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 0, -10000, 0)
      lazy val result = controller.saUser(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the saUser page" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe MessageLookup.SaUser.title
      }

      "a session is provided" should {
        lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 0, -10000, 0)
        lazy val result = controller.saUser(fakeRequestWithSession)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        "load the saUser page" in {
          Jsoup.parse(bodyOf(result)).title() shouldBe MessageLookup.SaUser.title
        }

        "should jump to what next when self assessment not required and no tax liability" should {
          lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 0, -10000, 0, false)
          lazy val result = controller.saUser(fakeRequestWithSession)

          "return a status of 303" in {
            status(result) shouldBe 303
          }

          "redirect to the nonSa loss what next page" in {
            redirectLocation(result) shouldBe Some(controllers.routes.WhatNextNonSaController.whatNextNonSaLoss().url)
          }
        }

        "should jump to what next when self assessment not required and a tax liability" should {
          lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 10000, 5000, 2000, false)
          lazy val result = controller.saUser(fakeRequestWithSession)

          "return a status of 303" in {
            status(result) shouldBe 303
          }

          "redirect to the nonSa gain what next page" in {
            redirectLocation(result) shouldBe Some(controllers.routes.WhatNextNonSaController.whatNextNonSaGain().url)
          }
        }
      }
    }
  }

  "Calling .submitSaUser" when {

    "no session is provided" should {
      lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 0, -10000, 0)
      lazy val result = controller.submitSaUser(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the missing session page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/properties/session-timeout")
      }
    }

    "a non-sa user is submitted" when {
      val form = "isInSa" -> "No"

      "there is no tax liability" should {
        lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 0, -10000, 0)
        lazy val result = controller.submitSaUser(fakeRequestToPOSTWithSession(form).withMethod("POST"))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the nonSa loss what next page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.WhatNextNonSaController.whatNextNonSaLoss().url)
        }
      }

      "there is a tax liability" should {
        lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 10000, 5000, 2000)
        lazy val result = controller.submitSaUser(fakeRequestToPOSTWithSession(form).withMethod("POST"))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the nonSa gain what next page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.WhatNextNonSaController.whatNextNonSaGain().url)
        }
      }
    }

    "a sa user is submitted" when {
      val form = "isInSa" -> "Yes"

      "there is a tax liability" should {
        lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 10000, 5000, 2000)
        lazy val result = controller.submitSaUser(fakeRequestToPOSTWithSession(form).withMethod("POST"))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the nonSa gain what next page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.WhatNextSAController.whatNextSAGain().url)
        }
      }

      "there is no tax liability and a disposal value less than 4*AEA" should {
        lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 0, -10000, 0)
        lazy val result = controller.submitSaUser(fakeRequestToPOSTWithSession(form).withMethod("POST"))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the nonSa loss what next page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.WhatNextSAController.whatNextSANoGain().url)
        }
      }

      "there is no tax liability and a disposal value greater than 4*AEA" should {
        lazy val controller = setupController(ModelsAsset.gainLargeDisposalValue, 0, -10000, 0)
        lazy val result = controller.submitSaUser(fakeRequestToPOSTWithSession(form).withMethod("POST"))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the nonSa loss with value greater than what next page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.WhatNextSAController.whatNextSAOverFourTimesAEA().url)
        }
      }
    }
  }
}
