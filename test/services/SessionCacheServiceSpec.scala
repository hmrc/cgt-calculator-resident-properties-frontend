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

package services

import common.KeystoreKeys.{ResidentPropertyKeys => Keys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.AppConfig
import controllers.helpers.CommonMocks
import models.resident
import models.resident._
import models.resident.properties._
import models.resident.properties.gain.{OwnerBeforeLegislationStartModel, WhoDidYouGiveItToModel, WorthWhenGiftedModel}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.mongo.CurrentTimestampSupport
import uk.gov.hmrc.mongo.test.MongoSupport
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.Future

class SessionCacheServiceSpec extends CommonPlaySpec with MockitoSugar with CommonMocks with WithCommonFakeApplication with MongoSupport {

  val hc = mock[HeaderCarrier]

  val sessionRepository = new SessionRepository(mongoComponent = mongoComponent,
    config = fakeApplication.configuration, timestampSupport = new CurrentTimestampSupport())
  val sessionId: String = UUID.randomUUID.toString
  val sessionPair: (String, String) = SessionKeys.sessionId -> sessionId
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(sessionPair)

  val sessionCacheService: SessionCacheService = new SessionCacheService(sessionRepository, mockAppConfig)

  val disposalDate: DisposalDateModel = resident.DisposalDateModel(21, 5, 2016)
  val disposalValue: DisposalValueModel = resident.DisposalValueModel(100000)
  val disposalCosts: DisposalCostsModel = resident.DisposalCostsModel(7500)
  val worthWhenSoldForLess: WorthWhenSoldForLessModel = resident.WorthWhenSoldForLessModel(400000)
  val whoDidYouGiveItTo: WhoDidYouGiveItToModel = resident.properties.gain.WhoDidYouGiveItToModel("Person")
  val worthWhenGaveAway: WorthWhenGaveAwayModel = resident.properties.WorthWhenGaveAwayModel(400000)
  val acquisitionValue: AcquisitionValueModel = resident.AcquisitionValueModel(300000)
  val worthWhenInherited: WorthWhenInheritedModel = resident.WorthWhenInheritedModel(350000)
  val worthWhenGifted: WorthWhenGiftedModel = resident.properties.gain.WorthWhenGiftedModel(350000)
  val worthWhenBoughtForLess: WorthWhenBoughtForLessModel = resident.properties.WorthWhenBoughtForLessModel(300000)
  val acquisitionCosts: AcquisitionCostsModel = resident.AcquisitionCostsModel(7500)
  val improvements: ImprovementsModel = resident.properties.ImprovementsModel(25000)
  val sellOrGiveAway: SellOrGiveAwayModel = resident.properties.SellOrGiveAwayModel(true)
  val sellForLess: SellForLessModel = resident.SellForLessModel(true)
  val ownerBeforeLegislationStart: OwnerBeforeLegislationStartModel = resident.properties.gain.OwnerBeforeLegislationStartModel(true)
  val valueBeforeLegislationStart: ValueBeforeLegislationStartModel = resident.properties.ValueBeforeLegislationStartModel(375000)
  val howBecameOwner: HowBecameOwnerModel = resident.properties.HowBecameOwnerModel("Bought it")
  val boughtForLessThanWorth: BoughtForLessThanWorthModel = resident.properties.BoughtForLessThanWorthModel(true)

  class Setup(initializeCache: Boolean = true) {
    await {
      if (initializeCache) {
        sessionCacheService.saveFormData(Keys.disposalDate, disposalDate)
        sessionCacheService.saveFormData(Keys.disposalValue, disposalValue)
        sessionCacheService.saveFormData(Keys.disposalCosts, disposalCosts)
        sessionCacheService.saveFormData(Keys.worthWhenSoldForLess, worthWhenSoldForLess)
        sessionCacheService.saveFormData(Keys.whoDidYouGiveItTo, whoDidYouGiveItTo)
        sessionCacheService.saveFormData(Keys.worthWhenGaveAway, worthWhenGaveAway)
        sessionCacheService.saveFormData(Keys.acquisitionValue, acquisitionValue)
        sessionCacheService.saveFormData(Keys.worthWhenInherited, worthWhenInherited)
        sessionCacheService.saveFormData(Keys.worthWhenGifted, worthWhenGifted)
        sessionCacheService.saveFormData(Keys.worthWhenBoughtForLess, worthWhenBoughtForLess)
        sessionCacheService.saveFormData(Keys.acquisitionCosts, acquisitionCosts)
        sessionCacheService.saveFormData(Keys.improvements, improvements)
        sessionCacheService.saveFormData(Keys.sellOrGiveAway, sellOrGiveAway)
        sessionCacheService.saveFormData(Keys.sellForLess, sellForLess)
        sessionCacheService.saveFormData(Keys.ownerBeforeLegislationStart, ownerBeforeLegislationStart)
        sessionCacheService.saveFormData(Keys.valueBeforeLegislationStart, valueBeforeLegislationStart)
        sessionCacheService.saveFormData(Keys.howBecameOwner, howBecameOwner)
        sessionCacheService.saveFormData(Keys.boughtForLessThanWorth, boughtForLessThanWorth)
      } else {
        sessionRepository.clear
      }
    }
  }

  "Calling getPropertyGainAnswers" should {
    "return a valid YourAnswersSummaryModel" in new Setup {
      lazy val result: Future[YourAnswersSummaryModel] = sessionCacheService.getPropertyGainAnswers

      await(result).isInstanceOf[YourAnswersSummaryModel] shouldBe true
    }

    "return an exception when missing data" in new Setup(initializeCache = false) {
      lazy val result: Future[YourAnswersSummaryModel] = sessionCacheService.getPropertyGainAnswers

      intercept[ApplicationException](await(result)) shouldBe ApplicationException(
        Redirect(controllers.routes.TimeoutController.timeout()), "None.get"
      )
    }
  }

  "Calling getPropertyDeductionAnswers" should {
    "return a valid ChargeableGainAnswersModel" in new Setup {
      lazy val result: Future[ChargeableGainAnswers] = sessionCacheService.getPropertyDeductionAnswers

      await(result).isInstanceOf[ChargeableGainAnswers] shouldBe true
    }
  }

  "Calling getPropertyIncomeAnswers" should {
    "return a valid IncomeAnswersModel" in new Setup {
      lazy val result: Future[IncomeAnswersModel] = sessionCacheService.getPropertyIncomeAnswers

      await(result).isInstanceOf[IncomeAnswersModel] shouldBe true
    }
  }

  "Self Assessment Be Considered should be date dependant" should {
    val mockAppConfig = mock[AppConfig]
    val testCacheService = new SessionCacheService(sessionRepository, mockAppConfig)
    val boundaryDate = LocalDate.of(2020, 4, 6)

    when(mockAppConfig.selfAssessmentActivateDate).thenReturn(boundaryDate)

    "should Self Assessment be Considered before self assessment cut off date" in new Setup(initializeCache = false) {
      await(testCacheService.saveFormData(Keys.disposalDate, DisposalDateModel(5, 4, 2020)))

      val result = testCacheService.shouldSelfAssessmentBeConsidered()(request)
      await(result) shouldBe true
    }

    "should Self Assessment be Considered on self assessment cut off date" in new Setup(initializeCache = false) {
      await(testCacheService.saveFormData(Keys.disposalDate, DisposalDateModel(6, 4, 2020)))

      val result = testCacheService.shouldSelfAssessmentBeConsidered()(request)
      await(result) shouldBe false
    }

    "should Self Assessment be Considered after self assessment cut off date" in new Setup(initializeCache = false) {
      await(testCacheService.saveFormData(Keys.disposalDate, DisposalDateModel(7, 4, 2020)))

      val result = testCacheService.shouldSelfAssessmentBeConsidered()(request)
      await(result) shouldBe false
    }
  }
}
