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
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.resident.LossesBroughtForwardForm._
import forms.resident.LossesBroughtForwardValueForm._
import forms.resident.properties.LettingsReliefForm._
import forms.resident.properties.LettingsReliefValueForm._
import forms.resident.properties.PrivateResidenceReliefForm._
import forms.resident.properties.PrivateResidenceReliefValueForm._
import forms.resident.properties.PropertyLivedInForm._
import models.resident._
import models.resident.properties._
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.resident._
import views.html.calculation.resident.properties.deductions._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeductionsController @Inject()(
                                      val calcConnector: CalculatorConnector,
                                      val sessionCacheService: SessionCacheService,
                                      val messagesControllerComponents: MessagesControllerComponents,
                                      propertyLivedInView: propertyLivedIn,
                                      privateResidenceReliefView: privateResidenceRelief,
                                      privateResidenceReliefValueView: privateResidenceReliefValue,
                                      lettingsReliefView: lettingsRelief,
                                      lettingsReliefValueView: lettingsReliefValue,
                                      lossesBroughtForwardView: lossesBroughtForward,
                                      lossesBroughtForwardValueView: lossesBroughtForwardValue
                                    ) extends FrontendController(messagesControllerComponents) with ValidActiveSession with I18nSupport {

  private def navTitle(implicit request : Request[_]): String =
    Messages("calc.base.resident.properties.home")(messagesControllerComponents.messagesApi.preferred(request))

  implicit val ec: ExecutionContext = messagesControllerComponents.executionContext

  def getDisposalDate(implicit request: Request[_]): Future[Option[DisposalDateModel]] = {
    sessionCacheService.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  def totalGain(answerSummary: YourAnswersSummaryModel, hc: HeaderCarrier): Future[BigDecimal] = calcConnector.calculateRttPropertyGrossGain(answerSummary)(hc)

  def answerSummary(implicit request: Request [_]): Future[YourAnswersSummaryModel] = sessionCacheService.getPropertyGainAnswers(request)

  //################# Property Lived In Actions #############################
  def propertyLivedIn: Action[AnyContent] = ValidateSession.async { implicit request =>

    val backLink = Some(controllers.routes.GainController.improvements.toString)

    sessionCacheService.fetchAndGetFormData[PropertyLivedInModel](keystoreKeys.propertyLivedIn).map {
      case Some(data) => Ok(propertyLivedInView(propertyLivedInForm.fill(data), backLink))
      case _ => Ok(propertyLivedInView(propertyLivedInForm, backLink))
    }
  }

  def submitPropertyLivedIn: Action[AnyContent] = ValidateSession.async { implicit request =>

    lazy val backLink = Some(routes.GainController.improvements.url)

    def errorAction(errors: Form[PropertyLivedInModel]) = Future.successful(BadRequest(propertyLivedInView(
      errors, backLink
    )))

    def routeRequest(model: PropertyLivedInModel) = {
      if (model.livedInProperty) Future.successful(Redirect(routes.DeductionsController.privateResidenceRelief))
      else Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward))
    }

    def successAction(model: PropertyLivedInModel) = {
      for {
        save <- sessionCacheService.saveFormData(keystoreKeys.propertyLivedIn, model)
        route <- routeRequest(model)
      } yield route
    }

    propertyLivedInForm.bindFromRequest().fold(errorAction, successAction)
  }


  //########## Private Residence Relief Actions ##############
  def privateResidenceRelief: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[PrivateResidenceReliefModel](keystoreKeys.privateResidenceRelief).map {
      case Some(data) => Ok(privateResidenceReliefView(privateResidenceReliefForm.fill(data)))
      case _ => Ok(privateResidenceReliefView(privateResidenceReliefForm))
    }
  }

  def submitPrivateResidenceRelief: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[PrivateResidenceReliefModel]) = Future.successful(BadRequest(privateResidenceReliefView(errors)))

    def routeRequest(model: PrivateResidenceReliefModel) = {
      if (model.isClaiming) Future.successful(Redirect(routes.DeductionsController.privateResidenceReliefValue))
      else Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward))
    }

    def successAction(model: PrivateResidenceReliefModel) = {
      for {
        save <- sessionCacheService.saveFormData(keystoreKeys.privateResidenceRelief, model)
        route <- routeRequest(model)
      } yield route
    }

    privateResidenceReliefForm.bindFromRequest().fold(errorAction, successAction)
  }


  //########## Private Residence Relief Value Actions ##############
  def privateResidenceReliefValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal) = {
      sessionCacheService.fetchAndGetFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue).map {
        case Some(data) => Ok(privateResidenceReliefValueView(privateResidenceReliefValueForm(totalGain).fill(data), totalGain))
        case None => Ok(privateResidenceReliefValueView(privateResidenceReliefValueForm(totalGain), totalGain))
      }
    }

    for {
      answerSummary <- answerSummary(request: Request[_])
      totalGain <- totalGain(answerSummary, hc)
      route <- routeRequest(totalGain)
    } yield route
  }

  def submitPrivateResidenceReliefValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def successAction(model: PrivateResidenceReliefValueModel) = {
      sessionCacheService.saveFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue, model).map (_ =>
        Redirect(routes.DeductionsController.lettingsRelief))

    }

    def routeRequest(gain: BigDecimal): Future[Result] = {
      privateResidenceReliefValueForm(gain).bindFromRequest().fold(
        errors => Future.successful(BadRequest(privateResidenceReliefValueView(errors, gain))),
        success => successAction(success)
      )
    }
    for {
      answerSummary <- answerSummary(request: Request[_])
      totalGain <- totalGain(answerSummary, hc)
      route <- routeRequest(totalGain)
    } yield route
  }

  //############## Lettings Relief Actions ##################
  private lazy val lettingsReliefBackUrl = routes.DeductionsController.privateResidenceReliefValue.url

  def lettingsRelief: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[LettingsReliefModel](keystoreKeys.lettingsRelief).map {
      case Some(data) => Ok(lettingsReliefView(lettingsReliefForm.fill(data), Some(lettingsReliefBackUrl)))
      case None => Ok(lettingsReliefView(lettingsReliefForm, Some(lettingsReliefBackUrl)))
    }
  }

  def submitLettingsRelief: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[LettingsReliefModel]) = {
      Future.successful(BadRequest(lettingsReliefView(form, Some(lettingsReliefBackUrl))))
    }

    def routeRequest(model: LettingsReliefModel) = {
      model match {
        case LettingsReliefModel(true) => Future.successful(Redirect(routes.DeductionsController.lettingsReliefValue))
        case _ => Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward))
      }
    }

    def successAction(model: LettingsReliefModel) = {
      for {
        save <- sessionCacheService.saveFormData[LettingsReliefModel](keystoreKeys.lettingsRelief, model)
        route <- routeRequest(model)
      } yield route
    }

    lettingsReliefForm.bindFromRequest().fold(
      errors => errorAction(errors),
      success => successAction(success)
    )
  }

  private def otherPropertiesData(propertyLivedInModel: Option[PropertyLivedInModel],
                                  privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                                  lettingsReliefModel: Option[LettingsReliefModel]): Future[String] = {
    (propertyLivedInModel.get.livedInProperty, privateResidenceReliefModel, lettingsReliefModel) match {
      case (true, Some(PrivateResidenceReliefModel(true)), Some(LettingsReliefModel(true))) =>
        Future.successful(routes.DeductionsController.lettingsReliefValue.url)
      case (true, Some(PrivateResidenceReliefModel(true)), _) => Future.successful(routes.DeductionsController.lettingsRelief.url)
      case (true, _, _) => Future.successful(routes.DeductionsController.privateResidenceRelief.url)
      case _ => Future.successful(routes.DeductionsController.propertyLivedIn.url)
    }
  }

  //################# Lettings Relief Value Input Actions ########################
  def lettingsReliefValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal, prrValue: BigDecimal): Future[Result] = {
      sessionCacheService.fetchAndGetFormData[LettingsReliefValueModel](keystoreKeys.lettingsReliefValue).map {
        case Some(data) => Ok(lettingsReliefValueView(lettingsReliefValueForm(totalGain, prrValue).fill(data), totalGain))
        case None => Ok(lettingsReliefValueView(lettingsReliefValueForm(totalGain, prrValue), totalGain))
      }
    }

    (for {
      answerSummary <- answerSummary(request: Request[_])
      totalGain <- totalGain(answerSummary, hc)
      prrValue <- sessionCacheService.fetchAndGetFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue)
      route <- routeRequest(totalGain, prrValue.fold(BigDecimal(0))(_.amount))
    } yield route).recoverToStart()
  }

  def submitLettingsReliefValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal, prrValue: BigDecimal) = {
      lettingsReliefValueForm(totalGain, prrValue).bindFromRequest().fold(
        errors => Future.successful(BadRequest(lettingsReliefValueView(errors, totalGain))),
        success => {
          sessionCacheService.saveFormData[LettingsReliefValueModel](keystoreKeys.lettingsReliefValue, success).map (_ =>
            Redirect(routes.DeductionsController.lossesBroughtForward))
        })
    }

    (for {
      answerSummary <- answerSummary(request: Request[_])
      totalGain <- totalGain(answerSummary, hc)
      prrValue <- sessionCacheService.fetchAndGetFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue)
      route <- routeRequest(totalGain, prrValue.fold(BigDecimal(0))(_.amount))
    } yield route).recoverToStart()
  }



  //################# Brought Forward Losses Actions ############################
  private lazy val lossesBroughtForwardPostAction = controllers.routes.DeductionsController.submitLossesBroughtForward

  def lossesBroughtForwardBackUrl(implicit request: Request [_]): Future[String] = {
    for {
      livedInProperty <- sessionCacheService.fetchAndGetFormData[PropertyLivedInModel](keystoreKeys.propertyLivedIn)
      privateResidenceRelief <- sessionCacheService.fetchAndGetFormData[PrivateResidenceReliefModel](keystoreKeys.privateResidenceRelief)
      lettingsRelief <- sessionCacheService.fetchAndGetFormData[LettingsReliefModel](keystoreKeys.lettingsRelief)
      backUrl <- otherPropertiesData(livedInProperty, privateResidenceRelief, lettingsRelief)
    } yield backUrl
  }

  def lossesBroughtForward: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backLinkUrl: String, taxYear: TaxYearModel): Future[Result] = {
      sessionCacheService.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
        case Some(data) => Ok(lossesBroughtForwardView(lossesBroughtForwardForm(taxYear).fill(data), lossesBroughtForwardPostAction,
          backLinkUrl, taxYear, navTitle))
        case _ => Ok(lossesBroughtForwardView(lossesBroughtForwardForm(taxYear), lossesBroughtForwardPostAction, backLinkUrl,
          taxYear, navTitle))
      }
    }

    (for {
      backLinkUrl <- lossesBroughtForwardBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(backLinkUrl, taxYear.get)
    } yield finalResult).recoverToStart()

  }

  def taxYearStringToInteger(taxYear: String): Future[Int] = {
    Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
  }

  def positiveChargeableGainCheck(implicit request: Request [_]): Future[Boolean] = {
    for {
      gainAnswers <- sessionCacheService.getPropertyGainAnswers
      chargeableGainAnswers <- sessionCacheService.getPropertyDeductionAnswers
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- calcConnector.getFullAEA(taxYearInt)(hc)
      chargeableGain <- calcConnector.calculateRttPropertyChargeableGain(gainAnswers, chargeableGainAnswers, maxAEA.get).map(_.get.chargeableGain)
    } yield chargeableGain

    match {
      case result if result.>(0) => true
      case _ => false
    }
  }

  def submitLossesBroughtForward: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, taxYearModel: TaxYearModel): Future[Result] = {
      lossesBroughtForwardForm(taxYearModel).bindFromRequest().fold(
        errors => Future.successful(BadRequest(lossesBroughtForwardView(errors, lossesBroughtForwardPostAction, backUrl,
          taxYearModel, navTitle))),
        success => {
          sessionCacheService.saveFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward, success).flatMap(
            _ => if (success.option) Future.successful(Redirect(routes.DeductionsController.lossesBroughtForwardValue))
            else {
              positiveChargeableGainCheck.map { positiveChargeableGain =>
                if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome)
                else Redirect(routes.ReviewAnswersController.reviewDeductionsAnswers)
              }
            }
          )
        }
      )
    }

    (for {
      backUrl <- lossesBroughtForwardBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      route <- routeRequest(backUrl, taxYear.get)
    } yield route).recoverToStart()

  }


  //################# Brought Forward Losses Value Actions ##############################
  private lazy val lossesBroughtForwardValueBackLink = routes.DeductionsController.lossesBroughtForward.url
  private lazy val lossesBroughtForwardValuePostAction = routes.DeductionsController.submitLossesBroughtForwardValue

  def lossesBroughtForwardValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def retrieveKeystoreData(taxYear: TaxYearModel): Future[Form[LossesBroughtForwardValueModel]] = {
      sessionCacheService.fetchAndGetFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue).map {
        case Some(data) => lossesBroughtForwardValueForm(taxYear).fill(data)
        case _ => lossesBroughtForwardValueForm(taxYear)
      }
    }

    def routeRequest(taxYear: TaxYearModel, formData: Form[LossesBroughtForwardValueModel]): Future[Result] = {
      Future.successful(Ok(lossesBroughtForwardValueView(
        formData,
        taxYear,
        navBackLink = lossesBroughtForwardValueBackLink,
        postAction = lossesBroughtForwardValuePostAction,
        navTitle = navTitle
      )))
    }

    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      formData <- retrieveKeystoreData(taxYear.get)
      route <- routeRequest(taxYear.get, formData)
    } yield route).recoverToStart()
  }

  def submitLossesBroughtForwardValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      lossesBroughtForwardValueForm(taxYear).bindFromRequest().fold(
        errors => {
            Future.successful(BadRequest(lossesBroughtForwardValueView(
              errors,
              taxYear,
              navBackLink = lossesBroughtForwardValueBackLink,
              postAction = lossesBroughtForwardValuePostAction,
              navTitle = navTitle)))
        },
        success => {
          sessionCacheService.saveFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue, success).flatMap(
            _ =>  positiveChargeableGainCheck.map { positiveChargeableGain =>
              if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome)
              else Redirect(routes.ReviewAnswersController.reviewDeductionsAnswers)
            }
          )
        }
      )
    }

    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      route <- routeRequest(taxYear.get)
    } yield route).recoverToStart()

  }
}
