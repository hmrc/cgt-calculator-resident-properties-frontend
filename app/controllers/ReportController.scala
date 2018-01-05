/*
 * Copyright 2018 HM Revenue & Customs
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
import models.resident.TaxYearModel
import models.resident.properties.YourAnswersSummaryModel
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import views.html.calculation.resident.properties.{report => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object ReportController extends ReportController {
  val calcConnector = CalculatorConnector
}

trait ReportController extends ValidActiveSession {

  val calcConnector: CalculatorConnector

  val pdfGenerator = new PdfGenerator

  override val homeLink: String = controllers.routes.PropertiesController.introduction().url
  override val sessionTimeoutUrl: String = homeLink

  def host(implicit request: RequestHeader): String = {
    s"http://${request.host}/"
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
    (for {
      answers <- calcConnector.getPropertyGainAnswers
      costs <- calcConnector.getPropertyTotalCosts(answers)
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)(hc)
      grossGain <- calcConnector.calculateRttPropertyGrossGain(answers)
    } yield {
      pdfGenerator.ok(views.gainSummaryReport(
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
    (for {
      answers <- calcConnector.getPropertyGainAnswers
      totalCosts <- getPropertyTotalCosts(answers)
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)(hc)
      deductionAnswers <- calcConnector.getPropertyDeductionAnswers
      grossGain <- calcConnector.calculateRttPropertyGrossGain(answers)
      chargeableGain <- calcConnector.calculateRttPropertyChargeableGain(answers, deductionAnswers, maxAEA.get)
    } yield {
      pdfGenerator.ok(views.deductionsSummaryReport(
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

    def getTotalDeductions(prrUsed: BigDecimal, lettingsReliefUsed: BigDecimal, lossesUsed: BigDecimal, aeaUsed: BigDecimal): Future[BigDecimal] = {
      Future.successful(prrUsed + lettingsReliefUsed + lossesUsed + aeaUsed)
    }

    def aeaRemaining(maxAEA: BigDecimal, aeaUsed: BigDecimal): BigDecimal = maxAEA - aeaUsed

    (for {
      gainAnswers <- calcConnector.getPropertyGainAnswers
      totalCosts <- getPropertyTotalCosts(gainAnswers)
      taxYear <- getTaxYear(gainAnswers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)(hc)
      deductionAnswers <- calcConnector.getPropertyDeductionAnswers
      incomeAnswers <- calcConnector.getPropertyIncomeAnswers
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

      pdfGenerator.ok(views.finalSummaryReport(
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
