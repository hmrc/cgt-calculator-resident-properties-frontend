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

package services

import java.util.NoSuchElementException

import common.KeystoreKeys
import config.AppConfig
import controllers.helpers.CommonMocks
import models.resident
import models.resident.properties._
import models.resident.properties.gain.OwnerBeforeLegislationStartModel
import models.resident.{DisposalDateModel, IncomeAnswersModel, SellForLessModel}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.HeaderCarrier
import common.{CommonPlaySpec,WithCommonFakeApplication}
import java.time.LocalDate
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import scala.concurrent.Future

class SessionCacheServiceSpec extends CommonPlaySpec with MockitoSugar with CommonMocks with WithCommonFakeApplication {

  val mockSessionCacheConnectorService = new SessionCacheService(mockSessionCacheConnector, mock[AppConfig])

  val hc = mock[HeaderCarrier]
  val homeLink = mockSessionCacheConnector.homeLink

  "Calling getPropertyGainAnswers" should{

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalDateModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.disposalDate))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(DisposalDateModel(21,5,2016))))


    when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalValueModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.disposalValue))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.DisposalValueModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.WorthWhenSoldForLessModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.worthWhenSoldForLess))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.WorthWhenSoldForLessModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.gain.WhoDidYouGiveItToModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.whoDidYouGiveItTo))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.gain.WhoDidYouGiveItToModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.WorthWhenGaveAwayModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.worthWhenGaveAway))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.WorthWhenGaveAwayModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.AcquisitionValueModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.acquisitionValue))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AcquisitionValueModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.WorthWhenInheritedModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.worthWhenInherited))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.WorthWhenInheritedModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.gain.WorthWhenGiftedModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.worthWhenGifted))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.gain.WorthWhenGiftedModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.WorthWhenBoughtForLessModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.worthWhenBoughtForLess))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.WorthWhenBoughtForLessModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.AcquisitionCostsModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.acquisitionCosts))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AcquisitionCostsModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalCostsModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.disposalCosts))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.DisposalCostsModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.ImprovementsModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.improvements))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.ImprovementsModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.SellOrGiveAwayModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.sellOrGiveAway))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(SellOrGiveAwayModel(true))))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.SellForLessModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.sellForLess))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(SellForLessModel(true))))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.gain.OwnerBeforeLegislationStartModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.ownerBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(OwnerBeforeLegislationStartModel(true))))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.ValueBeforeLegislationStartModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.valueBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.ValueBeforeLegislationStartModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.HowBecameOwnerModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.howBecameOwner))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(HowBecameOwnerModel("Bought"))))

    "return a valid YourAnswersSummaryModel" in{

      when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.BoughtForLessThanWorthModel](ArgumentMatchers
        .eq(KeystoreKeys.ResidentPropertyKeys.boughtForLessThanWorth))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(BoughtForLessThanWorthModel(true))))

      await(mockSessionCacheConnectorService.getPropertyGainAnswers(hc)).isInstanceOf[YourAnswersSummaryModel] shouldBe true
    }

    "return an exception" in{

      when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.BoughtForLessThanWorthModel](ArgumentMatchers
        .eq(KeystoreKeys.ResidentPropertyKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NoSuchElementException("error message")))

      the[ApplicationException] thrownBy await(mockSessionCacheConnectorService.getPropertyGainAnswers(hc)) shouldBe
        ApplicationException(Redirect(controllers.routes.TimeoutController.timeout(homeLink, homeLink)), "cgt-calculator-resident-properties-frontend" + "error message")
    }
  }

  "Calling getPropertyDeductionAnswers" should {
    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.PropertyLivedInModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(resident.properties.PropertyLivedInModel(false))))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.LossesBroughtForwardModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.lossesBroughtForward))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.LossesBroughtForwardValueModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.lossesBroughtForwardValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardValueModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.LettingsReliefModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.lettingsRelief))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.LettingsReliefModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.LettingsReliefValueModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.lettingsReliefValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.LettingsReliefValueModel])))

    when(mockSessionCacheConnector.fetchAndGetFormData[resident.PrivateResidenceReliefModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.privateResidenceRelief))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.PrivateResidenceReliefModel])))

    "return a valid ChargeableGainAnswersModel" in {
      when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.PrivateResidenceReliefValueModel](ArgumentMatchers
        .eq(KeystoreKeys.ResidentPropertyKeys.prrValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(mock[resident.properties.PrivateResidenceReliefValueModel])))

      lazy val result = mockSessionCacheConnectorService.getPropertyDeductionAnswers(hc)

      await(result).isInstanceOf[ChargeableGainAnswers] shouldBe true
    }

    "return an exception" in{
      when(mockSessionCacheConnector.fetchAndGetFormData[resident.properties.PrivateResidenceReliefValueModel](ArgumentMatchers
        .eq(KeystoreKeys.ResidentPropertyKeys.prrValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NoSuchElementException("error message")))

      the[ApplicationException] thrownBy await(mockSessionCacheConnectorService.getPropertyDeductionAnswers(hc)) shouldBe
        ApplicationException(Redirect(controllers.routes.TimeoutController.timeout(homeLink, homeLink)),
          "cgt-calculator-resident-properties-frontend" + "error message"
        )
    }
  }

  "Calling getPropertyIncomeAnswers" should{
    when(mockSessionCacheConnector.fetchAndGetFormData[resident.income.CurrentIncomeModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.currentIncome))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.income.CurrentIncomeModel])))

    "return a valid IncomeAnswersModel" in{
      when(mockSessionCacheConnector.fetchAndGetFormData[resident.income.PersonalAllowanceModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.personalAllowance))
        (ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(mock[resident.income.PersonalAllowanceModel])))

      lazy val result = mockSessionCacheConnectorService.getPropertyIncomeAnswers(hc)
      await(result).isInstanceOf[IncomeAnswersModel] shouldBe true
    }

    "return an exception" in{
      when(mockSessionCacheConnector.fetchAndGetFormData[resident.income.PersonalAllowanceModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.personalAllowance))
        (ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NoSuchElementException("error message")))

      the[ApplicationException] thrownBy await(mockSessionCacheConnectorService.getPropertyIncomeAnswers(hc)) shouldBe
      ApplicationException(Redirect(controllers.routes.TimeoutController.timeout(homeLink, homeLink)),
        "cgt-calculator-resident-properties-frontend" + "error message"
      )
    }

    "Self Assessment Be Considered should be date dependant" should {

      val mockAppConfig = mock[AppConfig]
      val mockCacheService = new SessionCacheService(mockSessionCacheConnector, mockAppConfig)
      val boundaryDate = LocalDate.of(2020, 4, 6)

      when(mockAppConfig.selfAssessmentActivateDate).thenReturn(boundaryDate)

      "should Self Assessment be Considered before self assessment cut off date" in {
        when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalDateModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.disposalDate))
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(DisposalDateModel(5, 4, 2020))))

        val result = mockCacheService.shouldSelfAssessmentBeConsidered()(hc)
        await(result) shouldBe true
      }

      "should Self Assessment be Considered on self assessment cut off date" in {
        when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalDateModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.disposalDate))
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(DisposalDateModel(5, 4, 2020))))

        val result = mockCacheService.shouldSelfAssessmentBeConsidered()(hc)
        await(result) shouldBe true
      }

      "should Self Assessment be Considered after self assessment cut off date" in {
        when(mockSessionCacheConnector.fetchAndGetFormData[resident.DisposalDateModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.disposalDate))
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(DisposalDateModel(7, 4, 2020))))

        val result = mockCacheService.shouldSelfAssessmentBeConsidered()(hc)
        await(result) shouldBe false
      }
    }
  }
}
