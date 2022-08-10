/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.resident.LossesBroughtForwardForm._
import forms.resident.LossesBroughtForwardValueForm._
import forms.resident.properties.LettingsReliefForm._
import forms.resident.properties.LettingsReliefValueForm._
import forms.resident.properties.PrivateResidenceReliefForm._
import forms.resident.properties.PrivateResidenceReliefValueForm._
import forms.resident.properties.PropertyLivedInForm._
import javax.inject.{Singleton, Inject}
import models.resident._
import models.resident.properties._
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.resident.properties.deductions._
import views.html.calculation.resident._


import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeductionsController @Inject()(
                                      val calcConnector: CalculatorConnector,
                                      val sessionCacheConnector: SessionCacheConnector,
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

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  def totalGain(answerSummary: YourAnswersSummaryModel, hc: HeaderCarrier): Future[BigDecimal] = calcConnector.calculateRttPropertyGrossGain(answerSummary)(hc)

  def answerSummary(hc: HeaderCarrier): Future[YourAnswersSummaryModel] = sessionCacheService.getPropertyGainAnswers(hc)

  override lazy val homeLink: String = controllers.routes.PropertiesController.introduction().url
  override lazy val sessionTimeoutUrl: String = homeLink


  //################# Property Lived In Actions #############################
  val propertyLivedIn: Action[AnyContent] = ValidateSession.async { implicit request =>

    val backLink = Some(controllers.routes.GainController.improvements().toString)

    sessionCacheConnector.fetchAndGetFormData[PropertyLivedInModel](keystoreKeys.propertyLivedIn).map {
      case Some(data) => Ok(propertyLivedInView(propertyLivedInForm.fill(data), homeLink, backLink))
      case _ => Ok(propertyLivedInView(propertyLivedInForm, homeLink, backLink))
    }
  }

  val submitPropertyLivedIn: Action[AnyContent] = ValidateSession.async { implicit request =>

    lazy val backLink = Some(routes.GainController.improvements().url)

    def errorAction(errors: Form[PropertyLivedInModel]) = Future.successful(BadRequest(propertyLivedInView(
      errors, homeLink, backLink
    )))

    def routeRequest(model: PropertyLivedInModel) = {
      if (model.livedInProperty) Future.successful(Redirect(routes.DeductionsController.privateResidenceRelief()))
      else Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
    }

    def successAction(model: PropertyLivedInModel) = {
      for {
        save <- sessionCacheConnector.saveFormData(keystoreKeys.propertyLivedIn, model)
        route <- routeRequest(model)
      } yield route
    }

    propertyLivedInForm.bindFromRequest().fold(errorAction, successAction)
  }


  //########## Private Residence Relief Actions ##############
  val privateResidenceRelief: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[PrivateResidenceReliefModel](keystoreKeys.privateResidenceRelief).map {
      case Some(data) => Ok(privateResidenceReliefView(privateResidenceReliefForm.fill(data)))
      case _ => Ok(privateResidenceReliefView(privateResidenceReliefForm))
    }
  }

  val submitPrivateResidenceRelief: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[PrivateResidenceReliefModel]) = Future.successful(BadRequest(privateResidenceReliefView(errors)))

    def routeRequest(model: PrivateResidenceReliefModel) = {
      if (model.isClaiming) Future.successful(Redirect(routes.DeductionsController.privateResidenceReliefValue()))
      else Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
    }

    def successAction(model: PrivateResidenceReliefModel) = {
      for {
        save <- sessionCacheConnector.saveFormData(keystoreKeys.privateResidenceRelief, model)
        route <- routeRequest(model)
      } yield route
    }

    privateResidenceReliefForm.bindFromRequest().fold(errorAction, successAction)
  }


  //########## Private Residence Relief Value Actions ##############
  val privateResidenceReliefValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal) = {
      sessionCacheConnector.fetchAndGetFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue).map {
        case Some(data) => Ok(privateResidenceReliefValueView(privateResidenceReliefValueForm(totalGain).fill(data), homeLink, totalGain))
        case None => Ok(privateResidenceReliefValueView(privateResidenceReliefValueForm(totalGain), homeLink, totalGain))
      }
    }

    for {
      answerSummary <- answerSummary(hc)
      totalGain <- totalGain(answerSummary, hc)
      route <- routeRequest(totalGain)
    } yield route
  }

  val submitPrivateResidenceReliefValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def successAction(model: PrivateResidenceReliefValueModel) = {
      sessionCacheConnector.saveFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue, model).map (_ =>
        Redirect(routes.DeductionsController.lettingsRelief()))

    }

    def routeRequest(gain: BigDecimal): Future[Result] = {
      privateResidenceReliefValueForm(gain).bindFromRequest.fold(
        errors => Future.successful(BadRequest(privateResidenceReliefValueView(errors, homeLink, gain))),
        success => successAction(success)
      )
    }
    for {
      answerSummary <- answerSummary(hc)
      totalGain <- totalGain(answerSummary, hc)
      route <- routeRequest(totalGain)
    } yield route
  }

  //############## Lettings Relief Actions ##################
  private lazy val lettingsReliefBackUrl = routes.DeductionsController.privateResidenceReliefValue().url

  val lettingsRelief: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[LettingsReliefModel](keystoreKeys.lettingsRelief).map {
      case Some(data) => Ok(lettingsReliefView(lettingsReliefForm.fill(data), homeLink, Some(lettingsReliefBackUrl)))
      case None => Ok(lettingsReliefView(lettingsReliefForm, homeLink, Some(lettingsReliefBackUrl)))
    }
  }

  val submitLettingsRelief: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(form: Form[LettingsReliefModel]) = {
      Future.successful(BadRequest(lettingsReliefView(form, homeLink, Some(lettingsReliefBackUrl))))
    }

    def routeRequest(model: LettingsReliefModel) = {
      model match {
        case LettingsReliefModel(true) => Future.successful(Redirect(routes.DeductionsController.lettingsReliefValue()))
        case _ => Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
      }
    }

    def successAction(model: LettingsReliefModel) = {
      for {
        save <- sessionCacheConnector.saveFormData[LettingsReliefModel](keystoreKeys.lettingsRelief, model)
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
        Future.successful(routes.DeductionsController.lettingsReliefValue().url)
      case (true, Some(PrivateResidenceReliefModel(true)), _) => Future.successful(routes.DeductionsController.lettingsRelief().url)
      case (true, _, _) => Future.successful(routes.DeductionsController.privateResidenceRelief().url)
      case _ => Future.successful(routes.DeductionsController.propertyLivedIn().url)
    }
  }

  //################# Lettings Relief Value Input Actions ########################
  val lettingsReliefValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal, prrValue: BigDecimal): Future[Result] = {
      sessionCacheConnector.fetchAndGetFormData[LettingsReliefValueModel](keystoreKeys.lettingsReliefValue).map {
        case Some(data) => Ok(lettingsReliefValueView(lettingsReliefValueForm(totalGain, prrValue).fill(data), homeLink, totalGain))
        case None => Ok(lettingsReliefValueView(lettingsReliefValueForm(totalGain, prrValue), homeLink, totalGain))
      }
    }

    (for {
      answerSummary <- answerSummary(hc)
      totalGain <- totalGain(answerSummary, hc)
      prrValue <- sessionCacheConnector.fetchAndGetFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue)
      route <- routeRequest(totalGain, prrValue.fold(BigDecimal(0))(_.amount))
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  val submitLettingsReliefValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal, prrValue: BigDecimal) = {
      lettingsReliefValueForm(totalGain, prrValue).bindFromRequest().fold(
        errors => Future.successful(BadRequest(lettingsReliefValueView(errors, homeLink, totalGain))),
        success => {
          sessionCacheConnector.saveFormData[LettingsReliefValueModel](keystoreKeys.lettingsReliefValue, success).map (_ =>
            Redirect(routes.DeductionsController.lossesBroughtForward()))
        })
    }

    (for {
      answerSummary <- answerSummary(hc)
      totalGain <- totalGain(answerSummary, hc)
      prrValue <- sessionCacheConnector.fetchAndGetFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue)
      route <- routeRequest(totalGain, prrValue.fold(BigDecimal(0))(_.amount))
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }



  //################# Brought Forward Losses Actions ############################
  private lazy val lossesBroughtForwardPostAction = controllers.routes.DeductionsController.submitLossesBroughtForward()

  def lossesBroughtForwardBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    for {
      livedInProperty <- sessionCacheConnector.fetchAndGetFormData[PropertyLivedInModel](keystoreKeys.propertyLivedIn)
      privateResidenceRelief <- sessionCacheConnector.fetchAndGetFormData[PrivateResidenceReliefModel](keystoreKeys.privateResidenceRelief)
      lettingsRelief <- sessionCacheConnector.fetchAndGetFormData[LettingsReliefModel](keystoreKeys.lettingsRelief)
      backUrl <- otherPropertiesData(livedInProperty, privateResidenceRelief, lettingsRelief)
    } yield backUrl
  }

  val lossesBroughtForward: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backLinkUrl: String, taxYear: TaxYearModel): Future[Result] = {
      sessionCacheConnector.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
        case Some(data) => Ok(lossesBroughtForwardView(lossesBroughtForwardForm(taxYear).fill(data), lossesBroughtForwardPostAction,
          backLinkUrl, taxYear, homeLink, navTitle))
        case _ => Ok(lossesBroughtForwardView(lossesBroughtForwardForm(taxYear), lossesBroughtForwardPostAction, backLinkUrl,
          taxYear, homeLink, navTitle))
      }
    }

    (for {
      backLinkUrl <- lossesBroughtForwardBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(backLinkUrl, taxYear.get)
    } yield finalResult).recoverToStart(homeLink, sessionTimeoutUrl)

  }

  def positiveChargeableGainCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      gainAnswers <- sessionCacheService.getPropertyGainAnswers
      chargeableGainAnswers <- sessionCacheService.getPropertyDeductionAnswers
      chargeableGain <- calcConnector.calculateRttPropertyChargeableGain(gainAnswers, chargeableGainAnswers, 11000).map(_.get.chargeableGain)
    } yield chargeableGain

    match {
      case result if result.>(0) => true
      case _ => false
    }
  }

  val submitLossesBroughtForward: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, taxYearModel: TaxYearModel): Future[Result] = {
      lossesBroughtForwardForm(taxYearModel).bindFromRequest.fold(
        errors => Future.successful(BadRequest(lossesBroughtForwardView(errors, lossesBroughtForwardPostAction, backUrl,
          taxYearModel, homeLink, navTitle))),
        success => {
          sessionCacheConnector.saveFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward, success).flatMap(
            _ => if (success.option) Future.successful(Redirect(routes.DeductionsController.lossesBroughtForwardValue()))
            else {
              positiveChargeableGainCheck.map { positiveChargeableGain =>
                if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome())
                else Redirect(routes.ReviewAnswersController.reviewDeductionsAnswers())
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
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)

  }


  //################# Brought Forward Losses Value Actions ##############################
  private lazy val lossesBroughtForwardValueBackLink = routes.DeductionsController.lossesBroughtForward().url
  private lazy val lossesBroughtForwardValuePostAction = routes.DeductionsController.submitLossesBroughtForwardValue()

  val lossesBroughtForwardValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def retrieveKeystoreData(taxYear: TaxYearModel): Future[Form[LossesBroughtForwardValueModel]] = {
      sessionCacheConnector.fetchAndGetFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue).map {
        case Some(data) => lossesBroughtForwardValueForm(taxYear).fill(data)
        case _ => lossesBroughtForwardValueForm(taxYear)
      }
    }

    def routeRequest(taxYear: TaxYearModel, formData: Form[LossesBroughtForwardValueModel]): Future[Result] = {
      Future.successful(Ok(lossesBroughtForwardValueView(
        formData,
        taxYear,
        navBackLink = lossesBroughtForwardValueBackLink,
        navHomeLink = homeLink,
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
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  val submitLossesBroughtForwardValue: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      lossesBroughtForwardValueForm(taxYear).bindFromRequest.fold(
        errors => {
            Future.successful(BadRequest(lossesBroughtForwardValueView(
              errors,
              taxYear,
              navBackLink = lossesBroughtForwardValueBackLink,
              navHomeLink = homeLink,
              postAction = lossesBroughtForwardValuePostAction,
              navTitle = navTitle)))
        },
        success => {
          sessionCacheConnector.saveFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue, success).flatMap(
            _ =>  positiveChargeableGainCheck.map { positiveChargeableGain =>
              if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome())
              else Redirect(routes.ReviewAnswersController.reviewDeductionsAnswers())
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
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)

  }
}
