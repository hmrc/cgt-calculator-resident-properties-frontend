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

import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.resident.JourneyKeys
import common.{Dates, TaxDates}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.resident.income.CurrentIncomeForm._
import forms.resident.income.PersonalAllowanceForm._
import models.resident._
import models.resident.income._
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.resident.personalAllowance
import views.html.calculation.resident.properties.income.currentIncome

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeController @Inject()(
                                  val calcConnector: CalculatorConnector,
                                  val sessionCacheService: SessionCacheService,
                                  val messagesControllerComponents: MessagesControllerComponents,
                                  currentIncomeView: currentIncome,
                                  personalAllowanceView: personalAllowance
                                ) extends FrontendController(messagesControllerComponents) with ValidActiveSession with I18nSupport {

  implicit val ec: ExecutionContext = messagesControllerComponents.executionContext

  def lossesBroughtForwardResponse(implicit request: Request [_]): Future[Boolean] = {
    sessionCacheService.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
      case Some(LossesBroughtForwardModel(response)) => response
      case None => false
    }
  }

  def getDisposalDate(implicit request: Request [_]): Future[Option[DisposalDateModel]] = {
    sessionCacheService.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  //################################# Current Income Actions ##########################################

  def buildCurrentIncomeBackUrl(implicit request: Request [_]): Future[String] = {
    lossesBroughtForwardResponse.map { response =>
      if (response) controllers.routes.DeductionsController.lossesBroughtForwardValue.url
      else controllers.routes.DeductionsController.lossesBroughtForward.url
    }
  }

  val currentIncome: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, taxYear: TaxYearModel, currentTaxYear: String): Future[Result] = {

      val inCurrentTaxYear = taxYear.taxYearSupplied == currentTaxYear

      sessionCacheService.fetchAndGetFormData[CurrentIncomeModel](keystoreKeys.currentIncome).map {
        case Some(data) => Ok(currentIncomeView(currentIncomeForm(taxYear).fill(data), backUrl, taxYear, inCurrentTaxYear))
        case None => Ok(currentIncomeView(currentIncomeForm(taxYear), backUrl, taxYear, inCurrentTaxYear))
      }
    }

    (for {
      backUrl <- buildCurrentIncomeBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      currentTaxYear <- Dates.getCurrentTaxYear
      finalResult <- routeRequest(backUrl, taxYear.get, currentTaxYear)
    } yield finalResult).recoverToStart()
  }

  val submitCurrentIncome: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(taxYearModel: TaxYearModel, currentTaxYear: String): Future[Result] = {

      val inCurrentTaxYear = taxYearModel.taxYearSupplied == currentTaxYear

      currentIncomeForm(taxYearModel).bindFromRequest().fold(
        errors => buildCurrentIncomeBackUrl.flatMap(url => Future.successful(BadRequest(currentIncomeView(errors, url, taxYearModel, inCurrentTaxYear)))),
        success => {
          sessionCacheService.saveFormData[CurrentIncomeModel](keystoreKeys.currentIncome, success)
            .map(_ => Redirect(routes.IncomeController.personalAllowance))
        }
      )
    }
    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      currentTaxYear <- Dates.getCurrentTaxYear
      route <- routeRequest(taxYear.get, currentTaxYear)
    } yield route).recoverToStart()
  }

  //################################# Personal Allowance Actions ##########################################
  def getStandardPA(year: Int, hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    calcConnector.getPA(year)(hc)
  }

  def taxYearValue(taxYear: String): Future[Int] = {
    Future.successful(TaxDates.taxYearStringToInteger(taxYear))
  }

  lazy private val backLinkPersonalAllowance = Some(controllers.routes.IncomeController.currentIncome.toString)
  lazy private val postActionPersonalAllowance = controllers.routes.IncomeController.submitPersonalAllowance

  val personalAllowance: Action[AnyContent] = ValidateSession.async { implicit request =>

    def fetchKeystorePersonalAllowance(taxYear: TaxYearModel): Future[Form[PersonalAllowanceModel]] = {
      sessionCacheService.fetchAndGetFormData[PersonalAllowanceModel](keystoreKeys.personalAllowance).map {
        case Some(data) => personalAllowanceForm(taxYear).fill(data)
        case _ => personalAllowanceForm(taxYear)
      }
    }

    def routeRequest(taxYearModel: TaxYearModel, standardPA: BigDecimal, formData: Form[PersonalAllowanceModel], currentTaxYear: String):
    Future[Result] = {
      Future.successful(Ok(personalAllowanceView(formData, taxYearModel, standardPA,
        postActionPersonalAllowance, backLinkPersonalAllowance, JourneyKeys.properties, Messages("calc.base.resident.properties.home"), currentTaxYear)))
    }
    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearValue(taxYear.get.calculationTaxYear)
      standardPA <- getStandardPA(year, hc)
      formData <- fetchKeystorePersonalAllowance(taxYear.get)
      currentTaxYear <- Dates.getCurrentTaxYear
      route <- routeRequest(taxYear.get, standardPA.get, formData, currentTaxYear)
    } yield route).recoverToStart()
  }

  val submitPersonalAllowance: Action[AnyContent] = ValidateSession.async { implicit request =>

    def getMaxPA(year: Int): Future[Option[BigDecimal]] = {
      calcConnector.getPA(year, isEligibleBlindPersonsAllowance = true, isEligibleMarriageAllowance = true)(hc)
    }

    def routeRequest(maxPA: BigDecimal, standardPA: BigDecimal, taxYearModel: TaxYearModel, currentTaxYear: String): Future[Result] = {
      personalAllowanceForm(taxYearModel, maxPA).bindFromRequest().fold(
        errors => Future.successful(BadRequest(personalAllowanceView(errors, taxYearModel, standardPA,
          postActionPersonalAllowance, backLinkPersonalAllowance, JourneyKeys.properties, Messages("calc.base.resident.properties.home"), currentTaxYear))),
        success => {
          sessionCacheService.saveFormData(keystoreKeys.personalAllowance, success)
            .map(_ => Redirect(routes.ReviewAnswersController.reviewFinalAnswers))
        }
      )
    }

    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearValue(taxYear.get.calculationTaxYear)
      standardPA <- getStandardPA(year, hc)
      maxPA <- getMaxPA(year)
      currentTaxYear <- Dates.getCurrentTaxYear
      route <- routeRequest(maxPA.get, standardPA.get, taxYear.get, currentTaxYear)
    } yield route).recoverToStart()
  }

}
