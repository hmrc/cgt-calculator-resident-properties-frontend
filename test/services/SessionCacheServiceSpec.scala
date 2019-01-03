/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.LocalDate
import java.util.{NoSuchElementException, UUID}

import assets.MessageLookup.SellForLess
import play.api.mvc._
import common.Dates.constructDate
import common.KeystoreKeys
import common.KeystoreKeys.ResidentPropertyKeys
import connectors.SessionCacheConnector
import models.resident
import models.resident.{DisposalDateModel, IncomeAnswersModel, SellForLessModel}
import models.resident.income.CurrentIncomeModel
import models.resident.properties.gain.OwnerBeforeLegislationStartModel
import models.resident.properties._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.http.HttpEntity
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.frontend.exceptions.ApplicationException
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SessionCacheServiceSpec extends UnitSpec with MockitoSugar{

  val mockSessionCache = mock[SessionCache]
  val mockSessionCacheConnector = mock[SessionCacheConnector]

  val mockSessionCacheService = new SessionCacheService {
    override val sessionCacheConnector: SessionCacheConnector = new SessionCacheConnector {
      override val sessionCache: SessionCache = mockSessionCache
      override val homeLink: String = ""
    }
  }

  val hc = mock[HeaderCarrier]
  val homeLink = mockSessionCacheConnector.homeLink

  "Calling getPropertyGainAnswers" should{

    when(mockSessionCache.fetchAndGetEntry[resident.DisposalDateModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.disposalDate))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(DisposalDateModel(21,5,2016))))


    when(mockSessionCache.fetchAndGetEntry[resident.DisposalValueModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.disposalValue))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.DisposalValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.WorthWhenSoldForLessModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.worthWhenSoldForLess))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.WorthWhenSoldForLessModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.gain.WhoDidYouGiveItToModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.whoDidYouGiveItTo))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.gain.WhoDidYouGiveItToModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.WorthWhenGaveAwayModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.worthWhenGaveAway))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.WorthWhenGaveAwayModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.AcquisitionValueModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.acquisitionValue))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AcquisitionValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.WorthWhenInheritedModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.worthWhenInherited))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.WorthWhenInheritedModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.gain.WorthWhenGiftedModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.worthWhenGifted))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.gain.WorthWhenGiftedModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.WorthWhenBoughtForLessModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.worthWhenBoughtForLess))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.WorthWhenBoughtForLessModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.AcquisitionCostsModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.acquisitionCosts))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AcquisitionCostsModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.DisposalCostsModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.disposalCosts))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.DisposalCostsModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.ImprovementsModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.improvements))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.ImprovementsModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.SellOrGiveAwayModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.sellOrGiveAway))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(SellOrGiveAwayModel(true))))

    when(mockSessionCache.fetchAndGetEntry[resident.SellForLessModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.sellForLess))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(SellForLessModel(true))))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.gain.OwnerBeforeLegislationStartModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.ownerBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(OwnerBeforeLegislationStartModel(true))))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.ValueBeforeLegislationStartModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.valueBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.ValueBeforeLegislationStartModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.HowBecameOwnerModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.howBecameOwner))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(HowBecameOwnerModel("Bought"))))

    "return a valid YourAnswersSummaryModel" in{

      when(mockSessionCache.fetchAndGetEntry[resident.properties.BoughtForLessThanWorthModel](ArgumentMatchers
        .eq(KeystoreKeys.ResidentPropertyKeys.boughtForLessThanWorth))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(BoughtForLessThanWorthModel(true))))

      await(mockSessionCacheService.getPropertyGainAnswers(hc)).isInstanceOf[YourAnswersSummaryModel] shouldBe true
    }

    "return an exception" in{

      when(mockSessionCache.fetchAndGetEntry[resident.properties.BoughtForLessThanWorthModel](ArgumentMatchers
        .eq(KeystoreKeys.ResidentPropertyKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NoSuchElementException("error message")))

      the[ApplicationException] thrownBy await(mockSessionCacheService.getPropertyGainAnswers(hc)) shouldBe
        ApplicationException("cgt-calculator-resident-properties-frontend",
          Redirect(controllers.routes.TimeoutController.timeout(homeLink, homeLink)),
          "error message")
    }
  }

  "Calling getPropertyDeductionAnswers" should {
    when(mockSessionCache.fetchAndGetEntry[resident.properties.PropertyLivedInModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(resident.properties.PropertyLivedInModel(false))))

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.lossesBroughtForward))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardValueModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.lossesBroughtForwardValue))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.LettingsReliefModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.lettingsRelief))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.LettingsReliefModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.LettingsReliefValueModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.lettingsReliefValue))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.LettingsReliefValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.PrivateResidenceReliefModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.privateResidenceRelief))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.PrivateResidenceReliefModel])))

    "return a valid ChargeableGainAnswersModel" in {
      when(mockSessionCache.fetchAndGetEntry[resident.properties.PrivateResidenceReliefValueModel](ArgumentMatchers
        .eq(KeystoreKeys.ResidentPropertyKeys.prrValue))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(mock[resident.properties.PrivateResidenceReliefValueModel])))

      lazy val result = mockSessionCacheService.getPropertyDeductionAnswers(hc)

      await(result).isInstanceOf[ChargeableGainAnswers] shouldBe true
    }

    "return an exception" in{
      when(mockSessionCache.fetchAndGetEntry[resident.properties.PrivateResidenceReliefValueModel](ArgumentMatchers
        .eq(KeystoreKeys.ResidentPropertyKeys.prrValue))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NoSuchElementException("error message")))

      the[ApplicationException] thrownBy await(mockSessionCacheService.getPropertyDeductionAnswers(hc)) shouldBe
        ApplicationException("cgt-calculator-resident-properties-frontend",
                               Redirect(controllers.routes.TimeoutController.timeout(homeLink, homeLink)), "error message")
    }
  }

  "Calling getPropertyIncomeAnswers" should{
    when(mockSessionCache.fetchAndGetEntry[resident.income.CurrentIncomeModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.currentIncome))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.income.CurrentIncomeModel])))

    "return a valid IncomeAnswersModel" in{
      when(mockSessionCache.fetchAndGetEntry[resident.income.PersonalAllowanceModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.personalAllowance))
        (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(mock[resident.income.PersonalAllowanceModel])))

      lazy val result = mockSessionCacheService.getPropertyIncomeAnswers(hc)
      await(result).isInstanceOf[IncomeAnswersModel] shouldBe true
    }

    "return an exception" in{
      when(mockSessionCache.fetchAndGetEntry[resident.income.PersonalAllowanceModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.personalAllowance))
        (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NoSuchElementException("error message")))

      the[ApplicationException] thrownBy await(mockSessionCacheService.getPropertyIncomeAnswers(hc)) shouldBe
        ApplicationException("cgt-calculator-resident-properties-frontend",
                                 Redirect(controllers.routes.TimeoutController.timeout(homeLink, homeLink)), "error message")
    }
  }
}
