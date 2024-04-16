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

import common.Dates
import common.Dates._
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import models.resident._
import models.resident.properties.{ChargeableGainAnswers, YourAnswersSummaryModel}
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesControllerComponents, Result}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.resident.properties.summary._

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SummaryController @Inject()(
                                   val calculatorConnector: CalculatorConnector,
                                   val sessionCacheService: SessionCacheService,
                                   val messagesControllerComponents: MessagesControllerComponents,
                                   finalSummaryView: finalSummary,
                                   deductionsSummaryView: deductionsSummary,
                                   gainSummaryView: gainSummary
                                 ) extends FrontendController(messagesControllerComponents) with ValidActiveSession with I18nSupport {

  implicit val ec: ExecutionContext = messagesControllerComponents.executionContext

  def summary = ValidateSession.async { implicit request =>

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

    def getTaxYear(disposalDate: LocalDate): Future[Option[TaxYearModel]] = calculatorConnector.getTaxYear(disposalDate.format(requestFormatter))

    def routeRequest(totalGainAnswers: YourAnswersSummaryModel,
                     grossGain: BigDecimal,
                     chargeableGainAnswers: ChargeableGainAnswers,
                     chargeableGain: Option[ChargeableGainResultModel],
                     incomeAnswers: IncomeAnswersModel,
                     totalGainAndTax: Option[TotalGainAndTaxOwedModel],
                     taxYear: Option[TaxYearModel],
                     currentTaxYear: String,
                     totalCosts: BigDecimal,
                     maxAEA: BigDecimal,
                     showUserResearchPanel: Boolean): Future[Result] = {

      //These lazy vals are called only when the values are determined to be available
      lazy val isPrrUsed = if (chargeableGainAnswers.propertyLivedInModel.get.livedInProperty) {
        Some(chargeableGainAnswers.privateResidenceReliefModel.get.isClaiming)
      } else None

      lazy val isLettingsReliefUsed = isPrrUsed match {
        case Some(true) => Some(chargeableGainAnswers.lettingsReliefModel.get.isClaiming)
        case _ => None
      }

      if (chargeableGain.isDefined && chargeableGain.get.chargeableGain > 0 && incomeAnswers.personalAllowanceModel.isDefined && incomeAnswers.currentIncomeModel.isDefined)
        Future.successful(Ok(finalSummaryView(
          totalGainAnswers,
          chargeableGainAnswers,
          incomeAnswers,
          totalGainAndTax.get,
          routes.ReviewAnswersController.reviewFinalAnswers.url,
          taxYear.get,
          isPrrUsed,
          isLettingsReliefUsed,
          totalCosts,
          chargeableGain.get.deductions,
          showUserResearchPanel = false
        )))
      else if (grossGain > 0)
        Future.successful(Ok(deductionsSummaryView(
          totalGainAnswers,
          chargeableGainAnswers,
          chargeableGain.get,
          routes.ReviewAnswersController.reviewDeductionsAnswers.url,
          taxYear.get,
          isPrrUsed,
          isLettingsReliefUsed,
          totalCosts,
          showUserResearchPanel
        )))
      else
        Future.successful(Ok(gainSummaryView(
          totalGainAnswers,
          grossGain,
          totalCosts,
          taxYear.get,
          maxAEA,
          showUserResearchPanel
        )))
    }

    def getMaxAEA(taxYear: Int): Future[Option[BigDecimal]] = {
      calculatorConnector.getFullAEA(taxYear)
    }

    def taxYearStringToInteger(taxYear: String): Future[Int] = {
      Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
    }

    def getPropertyTotalCosts(yourAnswersSummaryModel: YourAnswersSummaryModel): Future[BigDecimal] = {
      calculatorConnector.getPropertyTotalCosts(yourAnswersSummaryModel)
    }

    val showUserResearchPanel = setURPanelFlag

    (for {
      answers <- sessionCacheService.getPropertyGainAnswers
      totalCosts <- getPropertyTotalCosts(answers)
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)
      grossGain <- calculatorConnector.calculateRttPropertyGrossGain(answers)
      deductionAnswers <- sessionCacheService.getPropertyDeductionAnswers
      chargeableGain <- chargeableGain(grossGain, answers, deductionAnswers, maxAEA.get)
      incomeAnswers <- sessionCacheService.getPropertyIncomeAnswers
      totalGain <- totalTaxableGain(chargeableGain, answers, deductionAnswers, incomeAnswers, maxAEA.get)
      currentTaxYear <- Dates.getCurrentTaxYear
      routeRequest <- routeRequest(answers, grossGain, deductionAnswers, chargeableGain, incomeAnswers, totalGain,
        taxYear, currentTaxYear, totalCosts, maxAEA.get, showUserResearchPanel = showUserResearchPanel)
    } yield routeRequest).recoverToStart()
  }

  private[controllers] def setURPanelFlag(implicit hc: HeaderCarrier): Boolean = {
    val random = new Random()
    val seed = getLongFromSessionID(hc)
    random.setSeed(seed)
    random.nextInt(3) == 0
  }

  private[controllers] def getLongFromSessionID(hc: HeaderCarrier): Long = {
    val session = hc.sessionId.map(_.value).getOrElse("0")
    val numericSessionValues = session.replaceAll("[^0-9]", "") match {
      case "" => "0"
      case num => num
    }
    numericSessionValues.takeRight(10).toLong
  }
}
