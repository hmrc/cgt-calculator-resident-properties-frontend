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

package controllers

import common.Dates.requestFormatter
import connectors.CalculatorConnector
import constructors.resident.properties.CalculateRequestConstructor
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.resident.SaUserForm
import models.resident._
import models.resident.properties.{ChargeableGainAnswers, YourAnswersSummaryModel}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.resident.properties.whatNext.saUser

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SaUserController @Inject()(
                                  val calculatorConnector: CalculatorConnector,
                                  val sessionCacheService: SessionCacheService,
                                  val messagesControllerComponents: MessagesControllerComponents,
                                  saUserView: saUser
                                ) extends FrontendController(messagesControllerComponents) with ValidActiveSession with I18nSupport {

  implicit val ec: ExecutionContext = messagesControllerComponents.executionContext

  def saUser: Action[AnyContent] = ValidateSession.async { implicit request =>
    for {
      assessmentRequired <- sessionCacheService.shouldSelfAssessmentBeConsidered()
      whereToNext <- {
        if(assessmentRequired) {
          Future(Ok(saUserView(SaUserForm.saUserForm)))
        } else{
          submitSaUserImpl(assessmentRequired)
        }
      }
    } yield whereToNext
  }

  def submitSaUser: Action[AnyContent] = ValidateSession.async { implicit request =>
    submitSaUserImpl(true)
  }

  private def submitSaUserImpl(selfAssessmentRequired: Boolean)(implicit request: play.api.mvc.Request[_]): Future[Result] = {
    def chargeableGain(grossGain: BigDecimal,
                       yourAnswersSummaryModel: YourAnswersSummaryModel,
                       chargeableGainAnswers: ChargeableGainAnswers,
                       maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
      if (grossGain > 0) calculatorConnector.calculateRttPropertyChargeableGain(yourAnswersSummaryModel, chargeableGainAnswers, maxAEA)
      else Future.successful(None)
    }

    def totalTaxableGain(chargeableGain: Option[ChargeableGainResultModel],
                         yourAnswersSummaryModel: YourAnswersSummaryModel,
                         chargeableGainAnswers: ChargeableGainAnswers,
                         incomeAnswersModel: IncomeAnswersModel,
                         maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[TotalGainAndTaxOwedModel]] = {
      if (chargeableGain.isDefined && chargeableGain.get.chargeableGain > 0 &&
        incomeAnswersModel.personalAllowanceModel.isDefined && incomeAnswersModel.currentIncomeModel.isDefined) {
        calculatorConnector.calculateRttPropertyTotalGainAndTax(yourAnswersSummaryModel, chargeableGainAnswers, maxAEA, incomeAnswersModel)
      }
      else Future.successful(None)
    }

    def taxYearStringToInteger(taxYear: String): Future[Int] = {
      Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
    }

    def saAction(disposalValue: BigDecimal, taxOwed: Option[BigDecimal], maxAEA: BigDecimal): Future[Result] = {
      taxOwed match {
        case Some(tax) if tax > 0 => Future{Redirect(controllers.routes.WhatNextSAController.whatNextSAGain)}
        case _ if disposalValue >= 4 * maxAEA => Future{Redirect(controllers.routes.WhatNextSAController.whatNextSAOverFourTimesAEA)}
        case _ => Future{Redirect(controllers.routes.WhatNextSAController.whatNextSANoGain)}
      }
    }

    def nonSaAction(taxOwed: Option[BigDecimal]) = {
      taxOwed match {
        case Some(gain) if gain > 0 => Future{Redirect(controllers.routes.WhatNextNonSaController.whatNextNonSaGain)}
        case _ => Future{Redirect(controllers.routes.WhatNextNonSaController.whatNextNonSaLoss)}
      }
    }

    def routeAction(selfAssessmentRequired: Boolean, saUserModel: SaUserModel, totalGainAndTaxOwedModel: Option[TotalGainAndTaxOwedModel],
                    maxAEA: BigDecimal, disposalValue: BigDecimal): Future[Result] = {
      val taxOwed = totalGainAndTaxOwedModel.map {
        _.taxOwed
      }

      if(selfAssessmentRequired) {
        if (saUserModel.isInSa) {
          saAction(disposalValue, taxOwed, maxAEA)
        } else {
          nonSaAction(taxOwed)
        }
      }else {
        nonSaAction(taxOwed)
      }
    }

    def errorAction(form: Form[SaUserModel]) = {
      Future.successful(BadRequest(saUserView(form)))
    }

    def successAction(model: SaUserModel) = {
      (for {
        answers <- sessionCacheService.getPropertyGainAnswers
        grossGain <- calculatorConnector.calculateRttPropertyGrossGain(answers)
        deductionAnswers <- sessionCacheService.getPropertyDeductionAnswers
        taxYear <- calculatorConnector.getTaxYear(answers.disposalDate.format(requestFormatter))
        taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
        maxAEA <- calculatorConnector.getFullAEA(taxYearInt)
        chargeableGain <- chargeableGain(grossGain, answers, deductionAnswers, maxAEA.get)
        incomeAnswers <- sessionCacheService.getPropertyIncomeAnswers
        finalResult <- totalTaxableGain(chargeableGain, answers, deductionAnswers, incomeAnswers, maxAEA.get)
        route <- routeAction(selfAssessmentRequired, model, finalResult, maxAEA.get, CalculateRequestConstructor.determineDisposalValueToUse(answers))
      } yield route).recoverToStart()
    }

    if(selfAssessmentRequired) {
      SaUserForm.saUserForm.bindFromRequest().fold(errorAction, successAction)
    }else {
      SaUserForm.optionalsaUserForm.bindFromRequest().fold(errorAction, successAction)
    }
  }
}
