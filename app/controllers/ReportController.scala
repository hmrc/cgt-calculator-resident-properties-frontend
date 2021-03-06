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

package controllers

import java.time.LocalDate

import common.Dates
import common.Dates._
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import it.innove.play.pdf.PdfGenerator
import javax.inject.{Inject, Singleton}
import models.resident.TaxYearModel
import models.resident.properties.YourAnswersSummaryModel
import play.api.{Configuration, Logging}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{MessagesControllerComponents, RequestHeader}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.resident.properties.report._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReportController @Inject()(
                                  val config: Configuration,
                                  val calcConnector: CalculatorConnector,
                                  val sessionCacheService : SessionCacheService,
                                  val messagesControllerComponents: MessagesControllerComponents,
                                  deductionsSummaryReportView : deductionsSummaryReport,
                                  gainSummaryReportView: gainSummaryReport,
                                  finalSummaryReportView: finalSummaryReport,
                                  pdfGenerator : PdfGenerator
                                ) extends FrontendController(messagesControllerComponents) with ValidActiveSession with I18nSupport with Logging {

  override lazy val homeLink: String = controllers.routes.PropertiesController.introduction().url
  override lazy val sessionTimeoutUrl: String = homeLink

  lazy val platformHost: Option[String] = config.getOptional[String]("platform.frontend.host")

  implicit val ec: ExecutionContext = messagesControllerComponents.executionContext

  def host(implicit request: RequestHeader): String = {
    val host = if (platformHost.isDefined) {
      s"https://${request.host}"
    } else {
      s"http://${request.host}"
    }

    logger.info(s"[ReportController][host] host = $host")

    host
  }

  def getTaxYear(disposalDate: LocalDate)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] =
    calcConnector.getTaxYear(disposalDate.format(requestFormatter))

  def getMaxAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    calcConnector.getFullAEA(taxYear)
  }

  def taxYearStringToInteger (taxYear: String): Future[Int] = {
    Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
  }

  def getPropertyTotalCosts(yourAnswersSummaryModel: YourAnswersSummaryModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    calcConnector.getPropertyTotalCosts(yourAnswersSummaryModel)
  }

  val gainSummaryReport = ValidateSession.async { implicit request =>
    implicit val lang = messagesControllerComponents.messagesApi.preferred(request).lang

    (for {
      answers <- sessionCacheService.getPropertyGainAnswers
      costs <- calcConnector.getPropertyTotalCosts(answers)
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)(hc)
      grossGain <- calcConnector.calculateRttPropertyGrossGain(answers)
    } yield {
      pdfGenerator.ok(gainSummaryReportView(
        answers,
        grossGain,
        taxYear.get,
        costs,
        maxAEA.get),
        host
      ).asScala().withHeaders("Content-Disposition" -> s"""attachment; filename="${Messages("calc.resident.summary.title")}.pdf"""")
    }).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  //#####Deductions summary actions#####\\
  val deductionsReport = ValidateSession.async { implicit request =>
    implicit val lang = messagesControllerComponents.messagesApi.preferred(request).lang

    (for {
      answers <- sessionCacheService.getPropertyGainAnswers
      totalCosts <- getPropertyTotalCosts(answers)
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)(hc)
      deductionAnswers <- sessionCacheService.getPropertyDeductionAnswers
      grossGain <- calcConnector.calculateRttPropertyGrossGain(answers)
      chargeableGain <- calcConnector.calculateRttPropertyChargeableGain(answers, deductionAnswers, maxAEA.get)
    } yield {
      pdfGenerator.ok(deductionsSummaryReportView(
        answers,
        deductionAnswers,
        chargeableGain.get,
        taxYear.get,
        totalCosts),
        host
      ).asScala().withHeaders("Content-Disposition" -> s"""attachment; filename="${Messages("calc.resident.summary.title")}.pdf"""")
    }).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  //#####Final summary actions#####\\

  val finalSummaryReport = ValidateSession.async { implicit request =>
    implicit val lang = messagesControllerComponents.messagesApi.preferred(request).lang

    def getTotalDeductions(prrUsed: BigDecimal, lettingsReliefUsed: BigDecimal, lossesUsed: BigDecimal, aeaUsed: BigDecimal): Future[BigDecimal] = {
      Future.successful(prrUsed + lettingsReliefUsed + lossesUsed + aeaUsed)
    }

    def aeaRemaining(maxAEA: BigDecimal, aeaUsed: BigDecimal): BigDecimal = maxAEA - aeaUsed

    (for {
      gainAnswers <- sessionCacheService.getPropertyGainAnswers
      totalCosts <- getPropertyTotalCosts(gainAnswers)
      taxYear <- getTaxYear(gainAnswers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)(hc)
      deductionAnswers <- sessionCacheService.getPropertyDeductionAnswers
      incomeAnswers <- sessionCacheService.getPropertyIncomeAnswers
      currentTaxYear <- Dates.getCurrentTaxYear
      totalGainAndTax <- calcConnector.calculateRttPropertyTotalGainAndTax(gainAnswers, deductionAnswers, maxAEA.get, incomeAnswers)
      totalDeductions <- getTotalDeductions(totalGainAndTax.get.prrUsed.getOrElse(0),
        totalGainAndTax.get.lettingReliefsUsed.getOrElse(0),
        totalGainAndTax.get.broughtForwardLossesUsed,
        totalGainAndTax.get.aeaUsed)

    } yield {

      val isPrrUsed = if (deductionAnswers.propertyLivedInModel.get.livedInProperty) Some(deductionAnswers.privateResidenceReliefModel.get.isClaiming) else None

      val isLettingsReliefUsed = isPrrUsed match {
        case Some(true) => Some(deductionAnswers.lettingsReliefModel.get.isClaiming)
        case _ => None
      }

      val aeaLeftOver = aeaRemaining(maxAEA.getOrElse(0), totalGainAndTax.get.aeaUsed)

      pdfGenerator.ok(finalSummaryReportView(
        gainAnswers,
        deductionAnswers,
        incomeAnswers,
        totalGainAndTax.get,
        taxYear.get,
        taxYear.get.taxYearSupplied == currentTaxYear,
        isPrrUsed,
        isLettingsReliefUsed,
        totalCosts,
        totalDeductions,
        aeaLeftOver),
        host
      ).asScala().withHeaders("Content-Disposition" -> s"""attachment; filename="${Messages("calc.resident.summary.title")}.pdf"""")
    }).recoverToStart(homeLink, sessionTimeoutUrl)
  }
}
