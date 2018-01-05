/*
 * Copyright 2018 HM Revenue & Customs
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

import common.KeystoreKeys
import models.resident
import models.resident.properties.ChargeableGainAnswers
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SessionCacheServiceSpec extends UnitSpec with MockitoSugar{

  val mockSessionCache = mock[SessionCache]
  val mockSessionCacheService = mock[SessionCacheService]

  def mockResidentPropertyFetchAndGetFormData(): Unit = {

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.lossesBroughtForward))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardValueModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.lossesBroughtForwardValue))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.PropertyLivedInModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(resident.properties.PropertyLivedInModel(false))))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.LettingsReliefModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.lettingsRelief))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.LettingsReliefModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.LettingsReliefValueModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.lettingsReliefValue))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.LettingsReliefValueModel])))


    when(mockSessionCache.fetchAndGetEntry[resident.PrivateResidenceReliefModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.privateResidenceRelief))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.PrivateResidenceReliefModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.PrivateResidenceReliefValueModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.prrValue))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.PrivateResidenceReliefValueModel])))
  }

//  "Calling getPropertyDeductionAnswers" should {
//
//    "return a valid ChargeableGainAnswersModel" in {
//      val hc = mock[HeaderCarrier]
//      mockResidentPropertyFetchAndGetFormData()
//      lazy val result = mockSessionCacheService.getPropertyDeductionAnswers(hc)
//      await(result).isInstanceOf[ChargeableGainAnswers] shouldBe true
//    }
//  }


}
