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

package controllers

import java.time.LocalDate

import common.Dates
import common.Dates._
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.resident._
import models.resident.properties.{ChargeableGainAnswers, YourAnswersSummaryModel}
import play.api.mvc.Result
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.resident.properties.{summary => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object SummaryController extends SummaryController {
  val calculatorConnector = CalculatorConnector
}

trait SummaryController extends ValidActiveSession {

  val calculatorConnector: CalculatorConnector

  override val homeLink = controllers.routes.PropertiesController.introduction().url
  override val sessionTimeoutUrl = homeLink

  val summary = ValidateSession.async { implicit request =>

    def displayAnnualExemptAmountCheck(claimedOtherProperties: Boolean,
                                       claimedAllowableLosses: Boolean,
                                       allowableLossesValueModel: Option[AllowableLossesValueModel])(implicit hc: HeaderCarrier): Boolean = {
      allowableLossesValueModel match {
        case Some(result) if claimedAllowableLosses && claimedOtherProperties => result.amount == 0
        case _ if claimedOtherProperties && !claimedAllowableLosses => true
        case _ => false
      }
    }

    def buildDeductionsSummaryBackUrl(chargeableGainAnswers: ChargeableGainAnswers)(implicit hc: HeaderCarrier): Future[String] = {
      chargeableGainAnswers.broughtForwardModel.getOrElse(LossesBroughtForwardModel(false)).option match {
        case true => Future.successful(routes.DeductionsController.lossesBroughtForwardValue().url)
        case false => Future.successful(routes.DeductionsController.lossesBroughtForward().url)
      }
    }

    def chargeableGain(grossGain: BigDecimal,
                       yourAnswersSummaryModel: YourAnswersSummaryModel,
                       chargeableGainAnswers: ChargeableGainAnswers,
                       maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
      if (grossGain > 0) calculatorConnector.calculateRttPropertyChargeableGain(yourAnswersSummaryModel, chargeableGainAnswers, maxAEA)
      else Future.successful(None)
    }

    def totalTaxableGain(chargeableGain: Option[ChargeableGainResultModel] = None,
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
                     backUrl: String,
                     taxYear: Option[TaxYearModel],
                     currentTaxYear: String)(implicit hc: HeaderCarrier): Future[Result] = {

      //These lazy vals are called only when the values are determined to be available
      lazy val isPrrUsed = if (chargeableGainAnswers.propertyLivedInModel.get.livedInProperty) {
        Some(chargeableGainAnswers.privateResidenceReliefModel.get.isClaiming)
      } else None

      lazy val isLettingsReliefUsed = isPrrUsed match {
        case Some(true) => Some(chargeableGainAnswers.lettingsReliefModel.get.isClaiming)
        case _ => None
      }


      if (chargeableGain.isDefined && chargeableGain.get.chargeableGain > 0 &&
        incomeAnswers.personalAllowanceModel.isDefined && incomeAnswers.currentIncomeModel.isDefined) Future.successful(
        Ok(views.finalSummary(totalGainAnswers, chargeableGainAnswers, incomeAnswers,
          totalGainAndTax.get, routes.IncomeController.personalAllowance().url, taxYear.get, isPrrUsed, isLettingsReliefUsed,
          taxYear.get.taxYearSupplied == currentTaxYear)))
        //TODO: Update to finalYourAnswersSummary
      else if (grossGain > 0) Future.successful(Ok(views.deductionsSummary(totalGainAnswers, chargeableGainAnswers, chargeableGain.get,
        backUrl, taxYear.get, isPrrUsed, isLettingsReliefUsed)))
        //TODO: Update to reviewYourAnswersDeductions
      else Future.successful(Ok(views.gainSummary(totalGainAnswers, grossGain, taxYear.get)))
      //TODO: Update to reviewYourAnswersGain
    }

    def getMaxAEA(taxYear: Int): Future[Option[BigDecimal]] = {
      calculatorConnector.getFullAEA(taxYear)
    }

    def taxYearStringToInteger (taxYear: String): Future[Int] = {
      Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
    }

    for {
      answers <- calculatorConnector.getPropertyGainAnswers
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)
      grossGain <- calculatorConnector.calculateRttPropertyGrossGain(answers)
      deductionAnswers <- calculatorConnector.getPropertyDeductionAnswers
      backLink <- buildDeductionsSummaryBackUrl(deductionAnswers)
      chargeableGain <- chargeableGain(grossGain, answers, deductionAnswers, maxAEA.get)
      incomeAnswers <- calculatorConnector.getPropertyIncomeAnswers
      totalGain <- totalTaxableGain(chargeableGain, answers, deductionAnswers, incomeAnswers, maxAEA.get)
      currentTaxYear <- Dates.getCurrentTaxYear
      routeRequest <- routeRequest(answers, grossGain, deductionAnswers, chargeableGain, incomeAnswers, totalGain, backLink, taxYear, currentTaxYear)
    } yield routeRequest
  }
}
