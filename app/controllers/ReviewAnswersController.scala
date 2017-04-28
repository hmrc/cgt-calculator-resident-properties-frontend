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
import common.Dates.requestFormatter
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.resident.TaxYearModel
import models.resident.properties.{ChargeableGainAnswers, YourAnswersSummaryModel}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.resident.properties.checkYourAnswers.checkYourAnswers

import scala.concurrent.Future

object ReviewAnswersController extends ReviewAnswersController {
  val calculatorConnector = CalculatorConnector
}

trait ReviewAnswersController extends ValidActiveSession {

  val calculatorConnector: CalculatorConnector

  def getTaxYear(disposalDate: LocalDate)(implicit hc: HeaderCarrier): Future[TaxYearModel] =
    calculatorConnector.getTaxYear(disposalDate.format(requestFormatter)).map {
      _.get
    }

  def getGainAnswers(implicit hc: HeaderCarrier): Future[YourAnswersSummaryModel] = calculatorConnector.getPropertyGainAnswers

  def getDeductionsAnswers(implicit hc: HeaderCarrier): Future[ChargeableGainAnswers] = calculatorConnector.getPropertyDeductionAnswers

  val reviewGainAnswers: Action[AnyContent] = ValidateSession.async {
    implicit request =>
      getGainAnswers.map { answers =>
        Ok(checkYourAnswers(routes.SummaryController.summary(), "Back link", answers, None, None))
      }
  }

  val reviewDeductionsAnswers: Action[AnyContent] = ValidateSession.async {
    implicit request =>
      for {
        gainAnswers <- getGainAnswers
        deductionsAnswers <- getDeductionsAnswers
        taxYear <- getTaxYear(gainAnswers.disposalDate)
      } yield Ok(checkYourAnswers(routes.SummaryController.summary(), "Back link", gainAnswers, Some(deductionsAnswers), Some(taxYear)))
  }

  val reviewFinalAnswers: Action[AnyContent] = ValidateSession.async {
    implicit request =>
      val getCurrentTaxYear = Dates.getCurrentTaxYear
      val getIncomeAnswers = calculatorConnector.getPropertyIncomeAnswers

      for {
        gainAnswers <- getGainAnswers
        deductionsAnswers <- getDeductionsAnswers
        incomeAnswers <- getIncomeAnswers
        taxYear <- getTaxYear(gainAnswers.disposalDate)
        currentTaxYear <- getCurrentTaxYear
      } yield Ok(checkYourAnswers(routes.SummaryController.summary(), "Back link", gainAnswers,
        Some(deductionsAnswers), Some(taxYear), Some(incomeAnswers), taxYear.taxYearSupplied == currentTaxYear))
  }
}
