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

import common.Dates.constructDate
import common.KeystoreKeys.ResidentPropertyKeys
import config.AppConfig
import models.resident._
import models.resident.properties._
import models.resident.properties.gain.{OwnerBeforeLegislationStartModel, WhoDidYouGiveItToModel, WorthWhenGiftedModel}
import play.api.libs.json.Format
import play.api.mvc.Request
import play.api.mvc.Results.Redirect
import repositories.SessionRepository
import uk.gov.hmrc.mongo.cache.DataKey
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionCacheService @Inject()(sessionRepository: SessionRepository,
                                    appConfig: AppConfig
                                   )(implicit val ec: ExecutionContext) {

  def saveFormData[T](key: String, data: T)(implicit request: Request[_], formats: Format[T]): Future[(String, String)] = {
    sessionRepository.putSession[T](DataKey(key), data)
  }

  def fetchAndGetFormData[T](key: String)(implicit request: Request[_], formats: Format[T]): Future[Option[T]] = {
    sessionRepository.getFromSession[T](DataKey(key))
  }

  def getPropertyGainAnswers(implicit request: Request[_]): Future[YourAnswersSummaryModel] = {
    val disposalDate = fetchAndGetFormData[DisposalDateModel](ResidentPropertyKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))

    val disposalValue = fetchAndGetFormData[DisposalValueModel](ResidentPropertyKeys.disposalValue).map(_.map(_.amount))

    val worthWhenSoldForLess = fetchAndGetFormData[WorthWhenSoldForLessModel](ResidentPropertyKeys.worthWhenSoldForLess).map(_.map(_.amount))

    val whoDidYouGiveItTo = fetchAndGetFormData[WhoDidYouGiveItToModel](ResidentPropertyKeys.whoDidYouGiveItTo).map(_.map(_.option))

    val worthWhenGaveAway = fetchAndGetFormData[WorthWhenGaveAwayModel](ResidentPropertyKeys.worthWhenGaveAway).map(_.map(_.amount))

    val acquisitionValue = fetchAndGetFormData[AcquisitionValueModel](ResidentPropertyKeys.acquisitionValue).map(_.map(_.amount))

    val worthWhenInherited = fetchAndGetFormData[WorthWhenInheritedModel](ResidentPropertyKeys.worthWhenInherited).map(_.map(_.amount))

    val worthWhenGifted = fetchAndGetFormData[WorthWhenGiftedModel](ResidentPropertyKeys.worthWhenGifted).map(_.map(_.amount))

    val worthWhenBoughtForLess = fetchAndGetFormData[WorthWhenBoughtForLessModel](ResidentPropertyKeys.worthWhenBoughtForLess).map(_.map(_.amount))

    val acquisitionCosts = fetchAndGetFormData[AcquisitionCostsModel](ResidentPropertyKeys.acquisitionCosts).map(_.get.amount)

    val disposalCosts = fetchAndGetFormData[DisposalCostsModel](ResidentPropertyKeys.disposalCosts).map(_.get.amount)

    val improvements = fetchAndGetFormData[ImprovementsModel](ResidentPropertyKeys.improvements).map(_.get.amount)

    val givenAway = fetchAndGetFormData[SellOrGiveAwayModel](ResidentPropertyKeys.sellOrGiveAway).map(_.get.givenAway)

    val sellForLess = fetchAndGetFormData[SellForLessModel](ResidentPropertyKeys.sellForLess).map(_.map(_.sellForLess))

    val ownerBeforeLegislationStart = fetchAndGetFormData[OwnerBeforeLegislationStartModel](ResidentPropertyKeys.ownerBeforeLegislationStart).map(_.get.ownedBeforeLegislationStart)

    val valueBeforeLegislationStart = fetchAndGetFormData[ValueBeforeLegislationStartModel](ResidentPropertyKeys.valueBeforeLegislationStart).map(_.map(_.amount))

    val howBecameOwner = fetchAndGetFormData[HowBecameOwnerModel](ResidentPropertyKeys.howBecameOwner).map(_.map(_.gainedBy))

    val boughtForLessThanWorth = fetchAndGetFormData[BoughtForLessThanWorthModel](ResidentPropertyKeys.boughtForLessThanWorth).map(_.map(_.boughtForLessThanWorth))

    for {
      disposalDate <- disposalDate
      disposalValue <- disposalValue
      disposalCosts <- disposalCosts
      worthWhenSoldForLess <- worthWhenSoldForLess
      whoDidYouGiveItTo <- whoDidYouGiveItTo
      worthWhenGaveAway <- worthWhenGaveAway
      acquisitionValue <- acquisitionValue
      worthWhenInherited <- worthWhenInherited
      worthWhenGifted <- worthWhenGifted
      worthWhenBoughtForLess <- worthWhenBoughtForLess
      acquisitionCosts <- acquisitionCosts
      improvements <- improvements
      givenAway <- givenAway
      sellForLess <- sellForLess
      ownerBeforeLegislationStart <- ownerBeforeLegislationStart
      valueBeforeLegislationStart <- valueBeforeLegislationStart
      howBecameOwner <- howBecameOwner
      boughtForLessThanWorth <- boughtForLessThanWorth
    } yield YourAnswersSummaryModel(
      disposalDate,
      disposalValue,
      worthWhenSoldForLess,
      whoDidYouGiveItTo,
      worthWhenGaveAway,
      disposalCosts,
      acquisitionValue,
      worthWhenInherited,
      worthWhenGifted,
      worthWhenBoughtForLess,
      acquisitionCosts,
      improvements,
      givenAway,
      sellForLess,
      ownerBeforeLegislationStart,
      valueBeforeLegislationStart,
      howBecameOwner,
      boughtForLessThanWorth
    )
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.routes.TimeoutController.timeout),
        e.getMessage
      )
  }

  def getPropertyDeductionAnswers(implicit request: Request[_]): Future[ChargeableGainAnswers] = {
    val broughtForwardModel = fetchAndGetFormData[LossesBroughtForwardModel](ResidentPropertyKeys.lossesBroughtForward)
    val broughtForwardValueModel = fetchAndGetFormData[LossesBroughtForwardValueModel](ResidentPropertyKeys.lossesBroughtForwardValue)
    val propertyLivedInModel = fetchAndGetFormData[PropertyLivedInModel](ResidentPropertyKeys.propertyLivedIn)
    val privateResidenceReliefModel = fetchAndGetFormData[PrivateResidenceReliefModel](ResidentPropertyKeys.privateResidenceRelief)
    val privateResidenceReliefValueModel = fetchAndGetFormData[PrivateResidenceReliefValueModel](ResidentPropertyKeys.prrValue)
    val lettingsReliefModel = fetchAndGetFormData[LettingsReliefModel](ResidentPropertyKeys.lettingsRelief)
    val lettingsReliefValueModel = fetchAndGetFormData[LettingsReliefValueModel](ResidentPropertyKeys.lettingsReliefValue)

    for {
      propertyLivedIn <- propertyLivedInModel
      lettingsRelief <- lettingsReliefModel
      broughtForward <- broughtForwardModel
      broughtForwardValue <- broughtForwardValueModel
      privateResidenceRelief <- privateResidenceReliefModel
      lettingsReliefValue <- lettingsReliefValueModel
      privateResidenceReliefValue <- privateResidenceReliefValueModel
    } yield {
      ChargeableGainAnswers(
        broughtForward,
        broughtForwardValue,
        propertyLivedIn,
        privateResidenceRelief,
        privateResidenceReliefValue,
        lettingsRelief,
        lettingsReliefValue
      )
    }
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.routes.TimeoutController.timeout),
        e.getMessage
      )
  }

  def getPropertyIncomeAnswers(implicit request: Request[_]): Future[IncomeAnswersModel] = {
    val currentIncomeModel = fetchAndGetFormData[income.CurrentIncomeModel](ResidentPropertyKeys.currentIncome)
    val personalAllowanceModel = fetchAndGetFormData[income.PersonalAllowanceModel](ResidentPropertyKeys.personalAllowance)

    for {
      currentIncome <- currentIncomeModel
      personalAllowance <- personalAllowanceModel
    } yield {
      IncomeAnswersModel(currentIncome, personalAllowance)
    }
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.routes.TimeoutController.timeout),
        e.getMessage
      )
  }

  def shouldSelfAssessmentBeConsidered()(implicit request: Request[_]): Future[Boolean] = {
    val disposalDate = fetchAndGetFormData[DisposalDateModel](ResidentPropertyKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))
    val selfAssessmentActivateDate = appConfig.selfAssessmentActivateDate
    disposalDate.map(_.isBefore(selfAssessmentActivateDate))
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.routes.TimeoutController.timeout),
        e.getMessage
      )
  }
}
