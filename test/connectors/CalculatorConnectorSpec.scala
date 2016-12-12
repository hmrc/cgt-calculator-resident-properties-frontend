/*
 * Copyright 2016 HM Revenue & Customs
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
import org.mockito.Matchers
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

    when(mockSessionCache.fetchAndGetEntry[resident.OtherPropertiesModel](Matchers.eq(KeystoreKeys.ResidentPropertyKeys.otherProperties))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(resident.OtherPropertiesModel(false))))

    when(mockSessionCache.fetchAndGetEntry[resident.AllowableLossesModel](Matchers.eq(KeystoreKeys.ResidentPropertyKeys.allowableLosses))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AllowableLossesModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.AllowableLossesValueModel](Matchers.eq(KeystoreKeys.ResidentPropertyKeys.allowableLossesValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AllowableLossesValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardModel](Matchers.eq(KeystoreKeys.ResidentPropertyKeys.lossesBroughtForward))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.LossesBroughtForwardValueModel](Matchers.eq(KeystoreKeys.ResidentPropertyKeys.lossesBroughtForwardValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.LossesBroughtForwardValueModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.AnnualExemptAmountModel](Matchers.eq(KeystoreKeys.ResidentPropertyKeys.annualExemptAmount))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.AnnualExemptAmountModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.PropertyLivedInModel](Matchers.eq(KeystoreKeys.ResidentPropertyKeys.propertyLivedIn))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(resident.properties.PropertyLivedInModel(false))))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.LettingsReliefModel](Matchers.eq(KeystoreKeys.ResidentPropertyKeys.lettingsRelief))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.LettingsReliefModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.LettingsReliefValueModel](Matchers.eq(KeystoreKeys.ResidentPropertyKeys.lettingsReliefValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.properties.LettingsReliefValueModel])))


    when(mockSessionCache.fetchAndGetEntry[resident.PrivateResidenceReliefModel](Matchers.eq(KeystoreKeys.ResidentPropertyKeys.privateResidenceRelief))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(mock[resident.PrivateResidenceReliefModel])))

    when(mockSessionCache.fetchAndGetEntry[resident.properties.PrivateResidenceReliefValueModel](Matchers.eq(KeystoreKeys.ResidentPropertyKeys.prrValue))(Matchers.any(), Matchers.any()))
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
