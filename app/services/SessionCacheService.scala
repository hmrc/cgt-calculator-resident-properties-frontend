/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.SessionCacheConnector
import javax.inject.Inject
import models.resident._
import models.resident.properties._
import models.resident.properties.gain.{OwnerBeforeLegislationStartModel, WhoDidYouGiveItToModel, WorthWhenGiftedModel}
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionCacheService @Inject()(val sessionCacheConnector: SessionCacheConnector,
                                    implicit val appConfig: AppConfig) {
  def getPropertyGainAnswers(implicit hc: HeaderCarrier): Future[YourAnswersSummaryModel] = {
    val disposalDate = sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](ResidentPropertyKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))

    //This is a proposed alternate method of writing the map without needing the case statement, need a judgement on whether
    //to use this method or older ones. Fold automatically handles the None/Some cases without matching manually

    val disposalValue = sessionCacheConnector.fetchAndGetFormData[DisposalValueModel](ResidentPropertyKeys.disposalValue).map(_.map(_.amount))

    val worthWhenSoldForLess = sessionCacheConnector.fetchAndGetFormData[WorthWhenSoldForLessModel](ResidentPropertyKeys.worthWhenSoldForLess).map(_.map(_.amount))

    val whoDidYouGiveItTo = sessionCacheConnector.fetchAndGetFormData[WhoDidYouGiveItToModel](ResidentPropertyKeys.whoDidYouGiveItTo).map(_.map(_.option))

    val worthWhenGaveAway = sessionCacheConnector.fetchAndGetFormData[WorthWhenGaveAwayModel](ResidentPropertyKeys.worthWhenGaveAway).map(_.map(_.amount))

    val acquisitionValue = sessionCacheConnector.fetchAndGetFormData[AcquisitionValueModel](ResidentPropertyKeys.acquisitionValue).map(_.map(_.amount))

    val worthWhenInherited = sessionCacheConnector.fetchAndGetFormData[WorthWhenInheritedModel](ResidentPropertyKeys.worthWhenInherited).map(_.map(_.amount))

    val worthWhenGifted = sessionCacheConnector.fetchAndGetFormData[WorthWhenGiftedModel](ResidentPropertyKeys.worthWhenGifted).map(_.map(_.amount))

    val worthWhenBoughtForLess = sessionCacheConnector.fetchAndGetFormData[WorthWhenBoughtForLessModel](ResidentPropertyKeys.worthWhenBoughtForLess).map(_.map(_.amount))

    val acquisitionCosts = sessionCacheConnector.fetchAndGetFormData[AcquisitionCostsModel](ResidentPropertyKeys.acquisitionCosts).map(_.get.amount)

    val disposalCosts = sessionCacheConnector.fetchAndGetFormData[DisposalCostsModel](ResidentPropertyKeys.disposalCosts).map(_.get.amount)

    val improvements = sessionCacheConnector.fetchAndGetFormData[ImprovementsModel](ResidentPropertyKeys.improvements).map(_.get.amount)

    val givenAway = sessionCacheConnector.fetchAndGetFormData[SellOrGiveAwayModel](ResidentPropertyKeys.sellOrGiveAway).map(_.get.givenAway)

    val sellForLess = sessionCacheConnector.fetchAndGetFormData[SellForLessModel](ResidentPropertyKeys.sellForLess).map(_.map(_.sellForLess))

    val ownerBeforeLegislationStart = sessionCacheConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](ResidentPropertyKeys.ownerBeforeLegislationStart).map(_.get.ownedBeforeLegislationStart)

    val valueBeforeLegislationStart = sessionCacheConnector.fetchAndGetFormData[ValueBeforeLegislationStartModel](ResidentPropertyKeys.valueBeforeLegislationStart).map(_.map(_.amount))

    val howBecameOwner = sessionCacheConnector.fetchAndGetFormData[HowBecameOwnerModel](ResidentPropertyKeys.howBecameOwner).map(_.map(_.gainedBy))

    val boughtForLessThanWorth = sessionCacheConnector.fetchAndGetFormData[BoughtForLessThanWorthModel](ResidentPropertyKeys.boughtForLessThanWorth).map(_.map(_.boughtForLessThanWorth))

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
        Redirect(controllers.routes.TimeoutController.timeout(sessionCacheConnector.homeLink, sessionCacheConnector.homeLink)),
        "cgt-calculator-resident-properties-frontend" + e.getMessage
      )
  }

  def getPropertyDeductionAnswers(implicit hc: HeaderCarrier): Future[ChargeableGainAnswers] = {
    val broughtForwardModel = sessionCacheConnector.fetchAndGetFormData[LossesBroughtForwardModel](ResidentPropertyKeys.lossesBroughtForward)
    val broughtForwardValueModel = sessionCacheConnector.fetchAndGetFormData[LossesBroughtForwardValueModel](ResidentPropertyKeys.lossesBroughtForwardValue)
    val propertyLivedInModel = sessionCacheConnector.fetchAndGetFormData[PropertyLivedInModel](ResidentPropertyKeys.propertyLivedIn)
    val privateResidenceReliefModel = sessionCacheConnector.fetchAndGetFormData[PrivateResidenceReliefModel](ResidentPropertyKeys.privateResidenceRelief)
    val privateResidenceReliefValueModel = sessionCacheConnector.fetchAndGetFormData[PrivateResidenceReliefValueModel](ResidentPropertyKeys.prrValue)
    val lettingsReliefModel = sessionCacheConnector.fetchAndGetFormData[LettingsReliefModel](ResidentPropertyKeys.lettingsRelief)
    val lettingsReliefValueModel = sessionCacheConnector.fetchAndGetFormData[LettingsReliefValueModel](ResidentPropertyKeys.lettingsReliefValue)

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
        Redirect(controllers.routes.TimeoutController.timeout(sessionCacheConnector.homeLink, sessionCacheConnector.homeLink)),
        "cgt-calculator-resident-properties-frontend" + e.getMessage
      )
  }

  def getPropertyIncomeAnswers(implicit hc: HeaderCarrier): Future[IncomeAnswersModel] = {
    val currentIncomeModel = sessionCacheConnector.fetchAndGetFormData[income.CurrentIncomeModel](ResidentPropertyKeys.currentIncome)
    val personalAllowanceModel = sessionCacheConnector.fetchAndGetFormData[income.PersonalAllowanceModel](ResidentPropertyKeys.personalAllowance)

    for {
      currentIncome <- currentIncomeModel
      personalAllowance <- personalAllowanceModel
    } yield {
      IncomeAnswersModel(currentIncome, personalAllowance)
    }
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.routes.TimeoutController.timeout(sessionCacheConnector.homeLink, sessionCacheConnector.homeLink)),
        "cgt-calculator-resident-properties-frontend" + e.getMessage
      )
  }

  def shouldSelfAssessmentBeConsidered()(implicit hc: HeaderCarrier): Future[Boolean] = {
    val disposalDate = sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](ResidentPropertyKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))
    val selfAssessmentActivateDate = appConfig.selfAssessmentActivateDate
    disposalDate.map(_.isBefore(selfAssessmentActivateDate))
  }
}
