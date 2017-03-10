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

package connectors

import java.util.UUID

import common.KeystoreKeys
import models.resident
import models.resident.properties.ChargeableGainAnswers
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class CalculatorConnectorSpec extends UnitSpec with MockitoSugar {

  val mockHttp = mock[HttpGet]
  val mockSessionCache = mock[SessionCache]
  val sessionId = UUID.randomUUID.toString

  object TargetCalculatorConnector extends CalculatorConnector {
    override val sessionCache = mockSessionCache
    override val http = mockHttp
    override val serviceUrl = "dummy"
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  def mockResidentPropertyFetchAndGetFormData(): Unit = {

    when(mockSessionCache.fetchAndGetEntry[resident.OtherPropertiesModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.otherProperties))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(resident.OtherPropertiesModel(false))))

    when(mockSessionCache.fetchAndGetEntry[resident.AllowableLossesModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.allowableLosses))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AllowableLossesModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.AllowableLossesValueModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.allowableLossesValue))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AllowableLossesValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.lossesBroughtForward))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardValueModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.lossesBroughtForwardValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.AnnualExemptAmountModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.annualExemptAmount))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AnnualExemptAmountModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.PropertyLivedInModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(resident.properties.PropertyLivedInModel(false))))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.LettingsReliefModel](ArgumentMatchers.eq(KeystoreKeys.ResidentPropertyKeys.lettingsRelief))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.LettingsReliefModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.LettingsReliefValueModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.lettingsReliefValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.LettingsReliefValueModel])))


    when(mockSessionCache.fetchAndGetEntry[resident.PrivateResidenceReliefModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.privateResidenceRelief))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.PrivateResidenceReliefModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.PrivateResidenceReliefValueModel](ArgumentMatchers
      .eq(KeystoreKeys.ResidentPropertyKeys.prrValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.PrivateResidenceReliefValueModel])))
  }

  "Calling getPropertyDeductionAnswers" should {

    "return a valid ChargeableGainAnswersModel" in {
      val hc = mock[HeaderCarrier]
      mockResidentPropertyFetchAndGetFormData()
      lazy val result = TargetCalculatorConnector.getPropertyDeductionAnswers(hc)
      await(result).isInstanceOf[ChargeableGainAnswers] shouldBe true
    }
  }
}
