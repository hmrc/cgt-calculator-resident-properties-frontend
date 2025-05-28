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
import common.Dates.requestFormatter
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import models.resident.properties.{ChargeableGainAnswers, YourAnswersSummaryModel}
import models.resident.{LossesBroughtForwardModel, TaxYearModel}
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.resident.properties.checkYourAnswers.checkYourAnswers

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReviewAnswersController @Inject()(
                                         val calculatorConnector: CalculatorConnector,
                                         val sessionCacheService: SessionCacheService,
                                         val messagesControllerComponents: MessagesControllerComponents,
                                         checkYourAnswersView: checkYourAnswers
                                       ) extends FrontendController(messagesControllerComponents) with ValidActiveSession with I18nSupport {


  implicit val ec: ExecutionContext = messagesControllerComponents.executionContext

  def getTaxYear(disposalDate: LocalDate)(implicit hc: HeaderCarrier): Future[TaxYearModel] =
    calculatorConnector.getTaxYear(disposalDate.format(requestFormatter)).map {
      _.get
    }

  def getGainAnswers(implicit request: Request [?]): Future[YourAnswersSummaryModel] = sessionCacheService.getPropertyGainAnswers

  def getDeductionsAnswers(implicit request: Request [?]): Future[ChargeableGainAnswers] = sessionCacheService.getPropertyDeductionAnswers

  private def languageRequest(body : Lang => Future[Result])(implicit request: Request[?]): Future[Result] =
    body(messagesControllerComponents.messagesApi.preferred(request).lang)

  def reviewGainAnswers: Action[AnyContent] = ValidateSession.async { implicit request =>
    languageRequest { _ =>
      getGainAnswers.map { answers =>
        Ok(checkYourAnswersView(
          routes.SummaryController.summary,
          controllers.routes.GainController.improvements.url,
          answers,
          None,
          None))
      }.recoverToStart()
    }
  }

  def reviewDeductionsAnswers: Action[AnyContent] = ValidateSession.async {
    def generateBackUrl(chargeableGainAnswers: ChargeableGainAnswers): Future[String] = {
      if (chargeableGainAnswers.broughtForwardModel.getOrElse(LossesBroughtForwardModel(false)).option) {
        Future.successful(routes.DeductionsController.lossesBroughtForwardValue.url)
      } else {
        Future.successful(routes.DeductionsController.lossesBroughtForward.url)
      }
    }

    implicit request =>
      languageRequest { _ =>
        (for {
          gainAnswers <- getGainAnswers
          deductionsAnswers <- getDeductionsAnswers
          taxYear <- getTaxYear(gainAnswers.disposalDate)
          url <- generateBackUrl(deductionsAnswers)
        } yield {
          Ok(checkYourAnswersView(
            routes.SummaryController.summary,
            url, gainAnswers,
            Some(deductionsAnswers),
            Some(taxYear)))
        }).recoverToStart()
      }
  }

  def reviewFinalAnswers: Action[AnyContent] = ValidateSession.async {
    implicit request =>
      languageRequest { _ =>
        val getCurrentTaxYear = Dates.getCurrentTaxYear
        val getIncomeAnswers = sessionCacheService.getPropertyIncomeAnswers

        (for {
          gainAnswers <- getGainAnswers
          deductionsAnswers <- getDeductionsAnswers
          incomeAnswers <- getIncomeAnswers
          taxYear <- getTaxYear(gainAnswers.disposalDate)
          currentTaxYear <- getCurrentTaxYear
        } yield {
          Ok(checkYourAnswersView(routes.SummaryController.summary,
            routes.IncomeController.personalAllowance.url,
            gainAnswers,
            Some(deductionsAnswers),
            Some(taxYear),
            Some(incomeAnswers),
            taxYear.taxYearSupplied == currentTaxYear))
        }).recoverToStart()
      }
  }
}
