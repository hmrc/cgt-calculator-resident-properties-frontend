/*
 * Copyright 2020 HM Revenue & Customs
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
import config.AppConfig
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.resident.income.CurrentIncomeForm._
import forms.resident.income.PersonalAllowanceForm._
import javax.inject.{Singleton, Inject}
import models.resident._
import models.resident.income._
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.calculation.{resident => commonViews}
import views.html.calculation.resident.properties.{income => views}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeController @Inject()(
                                  val calcConnector: CalculatorConnector,
                                  val sessionCacheConnector: SessionCacheConnector,
                                  val messagesControllerComponents: MessagesControllerComponents,
                                  implicit val appConfig: AppConfig
                                ) extends FrontendController(messagesControllerComponents) with ValidActiveSession with I18nSupport {

  implicit val ec: ExecutionContext = messagesControllerComponents.executionContext

  def lossesBroughtForwardResponse(implicit hc: HeaderCarrier): Future[Boolean] = {
    sessionCacheConnector.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
      case Some(LossesBroughtForwardModel(response)) => response
      case None => false
    }
  }

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  override lazy val homeLink: String = controllers.routes.PropertiesController.introduction().url
  override lazy val sessionTimeoutUrl: String = homeLink

  //################################# Current Income Actions ##########################################

  def buildCurrentIncomeBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    lossesBroughtForwardResponse.map { response =>
      if (response) controllers.routes.DeductionsController.lossesBroughtForwardValue().url
      else controllers.routes.DeductionsController.lossesBroughtForward().url
    }
  }

  val currentIncome: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, taxYear: TaxYearModel, currentTaxYear: String): Future[Result] = {

      val inCurrentTaxYear = taxYear.taxYearSupplied == currentTaxYear

      sessionCacheConnector.fetchAndGetFormData[CurrentIncomeModel](keystoreKeys.currentIncome).map {
        case Some(data) => Ok(views.currentIncome(currentIncomeForm.fill(data), backUrl, taxYear, inCurrentTaxYear))
        case None => Ok(views.currentIncome(currentIncomeForm, backUrl, taxYear, inCurrentTaxYear))
      }
    }

    (for {
      backUrl <- buildCurrentIncomeBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      currentTaxYear <- Dates.getCurrentTaxYear
      finalResult <- routeRequest(backUrl, taxYear.get, currentTaxYear)
    } yield finalResult).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  val submitCurrentIncome: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(taxYearModel: TaxYearModel, currentTaxYear: String): Future[Result] = {

      val inCurrentTaxYear = taxYearModel.taxYearSupplied == currentTaxYear

      currentIncomeForm.bindFromRequest.fold(
        errors => buildCurrentIncomeBackUrl.flatMap(url => Future.successful(BadRequest(views.currentIncome(errors, url, taxYearModel, inCurrentTaxYear)))),
        success => {
          sessionCacheConnector.saveFormData[CurrentIncomeModel](keystoreKeys.currentIncome, success)
            .map(_ => Redirect(routes.IncomeController.personalAllowance()))
        }
      )
    }
    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      currentTaxYear <- Dates.getCurrentTaxYear
      route <- routeRequest(taxYear.get, currentTaxYear)
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  //################################# Personal Allowance Actions ##########################################
  def getStandardPA(year: Int, hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    calcConnector.getPA(year)(hc)
  }

  def taxYearValue(taxYear: String): Future[Int] = {
    Future.successful(TaxDates.taxYearStringToInteger(taxYear))
  }

  lazy private val backLinkPersonalAllowance = Some(controllers.routes.IncomeController.currentIncome().toString)
  lazy private val postActionPersonalAllowance = controllers.routes.IncomeController.submitPersonalAllowance()

  val personalAllowance: Action[AnyContent] = ValidateSession.async { implicit request =>

    def fetchKeystorePersonalAllowance(): Future[Form[PersonalAllowanceModel]] = {
      sessionCacheConnector.fetchAndGetFormData[PersonalAllowanceModel](keystoreKeys.personalAllowance).map {
        case Some(data) => personalAllowanceForm().fill(data)
        case _ => personalAllowanceForm()
      }
    }

    def routeRequest(taxYearModel: TaxYearModel, standardPA: BigDecimal, formData: Form[PersonalAllowanceModel], currentTaxYear: String):
    Future[Result] = {
      Future.successful(Ok(commonViews.personalAllowance(formData, taxYearModel, standardPA, homeLink,
        postActionPersonalAllowance, backLinkPersonalAllowance, JourneyKeys.properties, Messages("calc.base.resident.properties.home"), currentTaxYear)))
    }
    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearValue(taxYear.get.calculationTaxYear)
      standardPA <- getStandardPA(year, hc)
      formData <- fetchKeystorePersonalAllowance()
      currentTaxYear <- Dates.getCurrentTaxYear
      route <- routeRequest(taxYear.get, standardPA.get, formData, currentTaxYear)
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  val submitPersonalAllowance: Action[AnyContent] = ValidateSession.async { implicit request =>

    def getMaxPA(year: Int): Future[Option[BigDecimal]] = {
      calcConnector.getPA(year, isEligibleBlindPersonsAllowance = true)(hc)
    }

    def routeRequest(maxPA: BigDecimal, standardPA: BigDecimal, taxYearModel: TaxYearModel, currentTaxYear: String): Future[Result] = {
      personalAllowanceForm(maxPA).bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.personalAllowance(errors, taxYearModel, standardPA, homeLink,
          postActionPersonalAllowance, backLinkPersonalAllowance, JourneyKeys.properties, Messages("calc.base.resident.properties.home"), currentTaxYear))),
        success => {
          sessionCacheConnector.saveFormData(keystoreKeys.personalAllowance, success)
            .map(_ => Redirect(routes.ReviewAnswersController.reviewFinalAnswers()))
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
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }

}
