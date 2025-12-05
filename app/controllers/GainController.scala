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
import common.{Dates, TaxDates}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.resident.AcquisitionCostsForm._
import forms.resident.AcquisitionValueForm._
import forms.resident.DisposalCostsForm._
import forms.resident.DisposalDateForm._
import forms.resident.DisposalValueForm._
import forms.resident.WorthWhenInheritedForm._
import forms.resident.WorthWhenSoldForLessForm._
import forms.resident.properties.BoughtForLessThanWorthForm._
import forms.resident.properties.HowBecameOwnerForm._
import forms.resident.properties.ImprovementsForm._
import forms.resident.properties.SellForLessForm._
import forms.resident.properties.SellOrGiveAwayForm._
import forms.resident.properties.ValueBeforeLegislationStartForm._
import forms.resident.properties.WorthWhenBoughtForLessForm._
import forms.resident.properties.WorthWhenGaveAwayForm._
import forms.resident.properties.gain.OwnerBeforeLegislationStartForm._
import forms.resident.properties.gain.WhoDidYouGiveItToForm._
import forms.resident.properties.gain.WorthWhenGiftedForm._
import models.resident._
import models.resident.properties._
import models.resident.properties.gain.{OwnerBeforeLegislationStartModel, WhoDidYouGiveItToModel, WorthWhenGiftedModel}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesProvider}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.resident.outsideTaxYear
import views.html.calculation.resident.properties.gain._

import java.time.LocalDate
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GainController @Inject()(
                                val calcConnector: CalculatorConnector,
                                val sessionCacheService: SessionCacheService,
                                val messagesControllerComponents: MessagesControllerComponents,
                                disposalCostsView: disposalCosts,
                                disposalDateView: disposalDate,
                                disposalValueView: disposalValue,
                                worthWhenInheritedView: worthWhenInherited,
                                worthWhenGiftedView: worthWhenGifted,
                                worthWhenGaveAwayView: worthWhenGaveAway,
                                worthWhenBoughtForLessView: worthWhenBoughtForLess,
                                worthWhenSoldForLessView: worthWhenSoldForLess,
                                sellForLessView: sellForLess,
                                buyForLessView: buyForLess,
                                ownerBeforeLegislationStartView: ownerBeforeLegislationStart,
                                valueBeforeLegislationStartView: valueBeforeLegislationStart,
                                acquisitionValueView: acquisitionValue,
                                acquisitionCostsView: acquisitionCosts,
                                sellOrGiveAwayView: sellOrGiveAway,
                                whoDidYouGiveItToView: whoDidYouGiveItTo,
                                noTaxToPayView: noTaxToPay,
                                howBecameOwnerView: howBecameOwner,
                                improvementsView: improvements,
                                outsideTaxYearView: outsideTaxYear
                              ) extends FrontendController(messagesControllerComponents) with ValidActiveSession with I18nSupport {

  implicit val ec: ExecutionContext = messagesControllerComponents.executionContext

  implicit val mccMessages: Request[AnyContent] => Messages = request =>
    messagesControllerComponents.messagesApi.preferred(request)
  lazy val messagesProvider: Request[AnyContent] => MessagesProvider = request => new MessagesProvider {
    override def messages: Messages = mccMessages(request)
  }

  //################# Disposal Date Actions ####################
  def disposalDate: Action[AnyContent] = Action.async { implicit request =>
    if (request.session.get(SessionKeys.portalState).isEmpty) {
      val sessionId = request.session.get(SessionKeys.sessionId).getOrElse {
        s"session-${UUID.randomUUID.toString}"
      }

      val updatedSession = request.session +
        (SessionKeys.portalState -> "OnBoarding") +
        (SessionKeys.sessionId   -> sessionId)

      Future.successful(
        Ok(disposalDateView(disposalDateForm()))
          .withSession(updatedSession)
      )
    } else {
      sessionCacheService.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate).map {
        case Some(data) => Ok(disposalDateView(disposalDateForm().fill(data)))
        case None => Ok(disposalDateView(disposalDateForm()))
      }
    }
  }

  def submitDisposalDate: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(taxYearResult: Option[TaxYearModel]): Future[Result] = {
      if (taxYearResult.isDefined && !taxYearResult.get.isValidYear) Future.successful(Redirect(routes.GainController.outsideTaxYears))
      else Future.successful(Redirect(routes.GainController.sellOrGiveAway))
    }

    def bindForm(minimumDate: LocalDate) = {
      disposalDateForm(minimumDate).bindFromRequest().fold(
        errors => {
          Future.successful(
          BadRequest(
            disposalDateView(errors.copy(errors = errors.errors.map { error =>
              if (error.key == "") error.copy(key = "disposalDateDay") else error
            }))
          )
        )},
        success => {
          (for {
            _ <- sessionCacheService.saveFormData(keystoreKeys.disposalDate, success)
            taxYearResult <- calcConnector.getTaxYear(s"${success.year}-${success.month}-${success.day}")
            route <- routeRequest(taxYearResult)
          } yield route).recoverToStart()
        }
      )
    }

    for {
      minimumDate <- calcConnector.getMinimumDate()
      result <- bindForm(minimumDate)
    } yield result
  }

  //################ Sell or Give Away Actions ######################
  lazy val sellOrGiveAwayBackUrl = routes.GainController.disposalDate.url
  lazy val sellOrGiveAwayPostAction = controllers.routes.GainController.submitSellOrGiveAway

  def sellOrGiveAway: Action[AnyContent] = ValidateSession.async { implicit request =>

    sessionCacheService.fetchAndGetFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway).map {
      case Some(data) => Ok(sellOrGiveAwayView(sellOrGiveAwayForm.fill(data), Some(sellOrGiveAwayBackUrl), sellOrGiveAwayPostAction))
      case _ => Ok(sellOrGiveAwayView(sellOrGiveAwayForm, Some(sellOrGiveAwayBackUrl), sellOrGiveAwayPostAction))
    }
  }

  def submitSellOrGiveAway: Action[AnyContent] = ValidateSession.async { implicit request =>
    sellOrGiveAwayForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(sellOrGiveAwayView(errors, Some(sellOrGiveAwayBackUrl), sellOrGiveAwayPostAction))),
      success => {
        sessionCacheService.saveFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway, success).flatMap(_ =>
        success match {
          case SellOrGiveAwayModel(true) => Future.successful(Redirect(routes.GainController.whoDidYouGiveItTo))
          case SellOrGiveAwayModel(false) => Future.successful(Redirect(routes.GainController.sellForLess))
        }
        )
      }
    )
  }

  //################ Who Did You Give It To Actions ######################
  def whoDidYouGiveItTo: Action[AnyContent] = ValidateSession.async { implicit request =>

    sessionCacheService.fetchAndGetFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo).map {
      case Some(data) => Ok(whoDidYouGiveItToView(whoDidYouGiveItToForm.fill(data)))
      case _ => Ok(whoDidYouGiveItToView(whoDidYouGiveItToForm))
    }
  }

  def submitWhoDidYouGiveItTo: Action[AnyContent] = ValidateSession.async { implicit request =>
    whoDidYouGiveItToForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(whoDidYouGiveItToView(errors))),
      success => {
        sessionCacheService.saveFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo, success).flatMap(_ =>
        success match {
          case WhoDidYouGiveItToModel("Spouse" | "Charity") => Future.successful(Redirect(routes.GainController.noTaxToPay))
          case _ => Future.successful(Redirect(routes.GainController.worthWhenGaveAway))
        }
        )
      }
    )
  }

  //################ No Tax to Pay Actions ######################
  def noTaxToPay: Action[AnyContent] = ValidateSession.async { implicit request =>

    def isGivenToCharity: Future[Boolean] = {
      sessionCacheService.fetchAndGetFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo).map {
        case Some(WhoDidYouGiveItToModel("Charity")) => true
        case _ => false
      }
    }

    def result(input: Boolean): Future[Result] = {
      Future.successful(Ok(noTaxToPayView(input, routes.GainController.whoDidYouGiveItTo.url)))
    }

    (for {
      givenToCharity <- isGivenToCharity
      result <- result(givenToCharity)
    } yield result).recoverToStart()
  }

  //################ Outside Tax Years Actions ######################
  def outsideTaxYears: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for {
      disposalDate <- sessionCacheService.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${disposalDate.get.year}-${disposalDate.get.month}-${disposalDate.get.day}")
    } yield {
      Ok(outsideTaxYearView(
        taxYear = taxYear.get,
        isAfterApril15 = TaxDates.dateAfterStart(Dates.constructDate(disposalDate.get.day, disposalDate.get.month, disposalDate.get.year)),
        true,
        navBackLink = routes.GainController.disposalDate.url,
        continueUrl = routes.GainController.sellOrGiveAway.url,
        navTitle = Messages("calc.base.resident.properties.home")
      ))
    }).recoverToStart()
  }


  //################# Worth When Gave Away Actions ############################

  private lazy val worthWhenGaveAwayPostAction = controllers.routes.GainController.submitWorthWhenGaveAway
  private lazy val worthWhenGaveAwayBackLink = Some(controllers.routes.GainController.whoDidYouGiveItTo.toString)

  def worthWhenGaveAway: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[WorthWhenGaveAwayModel](keystoreKeys.worthWhenGaveAway).map {
      case Some(data) => Ok(worthWhenGaveAwayView(worthWhenGaveAwayForm.fill(data), worthWhenGaveAwayBackLink, worthWhenGaveAwayPostAction))
      case None => Ok(worthWhenGaveAwayView(worthWhenGaveAwayForm, worthWhenGaveAwayBackLink, worthWhenGaveAwayPostAction))
    }
  }

  def submitWorthWhenGaveAway: Action[AnyContent] = ValidateSession.async { implicit request =>
    worthWhenGaveAwayForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(worthWhenGaveAwayView(errors, worthWhenGaveAwayBackLink, worthWhenGaveAwayPostAction))),
      success => {
        sessionCacheService.saveFormData[WorthWhenGaveAwayModel](keystoreKeys.worthWhenGaveAway, success)
          .map(_ => Redirect(routes.GainController.disposalCosts))
      }
    )
  }

  //############## Sell for Less Actions ##################
  lazy val sellForLessBackLink = Some(controllers.routes.GainController.sellOrGiveAway.url)

  def sellForLess: Action[AnyContent] = ValidateSession.async {implicit request =>
    sessionCacheService.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess).map{
      case Some(data) => Ok(sellForLessView(sellForLessForm.fill(data), sellForLessBackLink))
      case _ => Ok(sellForLessView(sellForLessForm, sellForLessBackLink))
    }
  }

  val submitSellForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    def errorAction(errors: Form[SellForLessModel]) = Future.successful(BadRequest(sellForLessView(errors, sellForLessBackLink)))

    def routeRequest(model: SellForLessModel) = {
      if (model.sellForLess) Future.successful(Redirect(routes.GainController.worthWhenSoldForLess))
      else Future.successful(Redirect(routes.GainController.disposalValue))
    }

    def successAction(model: SellForLessModel) = {
      for {
        _ <- sessionCacheService.saveFormData(keystoreKeys.sellForLess, model)
        route <- routeRequest(model)
      } yield route
    }

    sellForLessForm.bindFromRequest().fold(errorAction, successAction)
  }

  //################ Disposal Value Actions ######################
  def disposalValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[DisposalValueModel](keystoreKeys.disposalValue).map {
      case Some(data) => Ok(disposalValueView(disposalValueForm.fill(data)))
      case None => Ok(disposalValueView(disposalValueForm))
    }
  }

  def submitDisposalValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    disposalValueForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(disposalValueView(errors))),
      success => {
        sessionCacheService.saveFormData[DisposalValueModel](keystoreKeys.disposalValue, success)
          .map(_ => Redirect(routes.GainController.disposalCosts))
      }
    )
  }

  //################ Property Worth When Sold For Less Actions ######################
  def worthWhenSoldForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[WorthWhenSoldForLessModel](keystoreKeys.worthWhenSoldForLess).map {
      case Some(data) => Ok(worthWhenSoldForLessView(worthWhenSoldForLessForm.fill(data)))
      case _ => Ok(worthWhenSoldForLessView(worthWhenSoldForLessForm))
    }
  }

  def submitWorthWhenSoldForLess: Action[AnyContent] = ValidateSession.async { implicit request =>

    worthWhenSoldForLessForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(worthWhenSoldForLessView(errors))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.worthWhenSoldForLess, success)
          .map(_ => Redirect(routes.GainController.disposalCosts))
      }
    )
  }

  //################# Disposal Costs Actions ########################
  private def disposalCostsBackLink: (SellOrGiveAwayModel, Option[SellForLessModel]) => String = {
    case (gaveAwayAnswer, _) if gaveAwayAnswer.givenAway => routes.GainController.worthWhenGaveAway.url
    case (_, soldForLessAnswer) if soldForLessAnswer.get.sellForLess => routes.GainController.worthWhenSoldForLess.url
    case (_, _) => routes.GainController.disposalValue.url
  }

  def disposalCosts: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backLink: String) = {
      sessionCacheService.fetchAndGetFormData[DisposalCostsModel](keystoreKeys.disposalCosts).map {
        case Some(data) => Ok(disposalCostsView(disposalCostsForm.fill(data), backLink))
        case None => Ok(disposalCostsView(disposalCostsForm, backLink))
      }
    }

    (for {
      gaveAway <- sessionCacheService.fetchAndGetFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway)
      soldForLess <- sessionCacheService.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess)
      route <- routeRequest(disposalCostsBackLink(gaveAway.get, soldForLess))
    } yield route).recoverToStart()
  }

  def submitDisposalCosts: Action[AnyContent] = ValidateSession.async { implicit request =>
    def routeRequest(backLink: String) = {
      disposalCostsForm.bindFromRequest().fold(
        errors => Future.successful(BadRequest(disposalCostsView(errors,backLink))),
        success => {
          sessionCacheService.saveFormData(keystoreKeys.disposalCosts, success)
            .map(_ => Redirect(routes.GainController.ownerBeforeLegislationStart))
        }
      )
    }

    (for {
      gaveAway <- sessionCacheService.fetchAndGetFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway)
      soldForLess <- sessionCacheService.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess)
      route <- routeRequest(disposalCostsBackLink(gaveAway.get, soldForLess))
    } yield route).recoverToStart()
  }

  //################# Owner Before Legislation Start Actions ########################
  def ownerBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart).map {
      case Some(data) => Ok(ownerBeforeLegislationStartView(ownerBeforeLegislationStartForm.fill(data)))
      case None => Ok(ownerBeforeLegislationStartView(ownerBeforeLegislationStartForm))
    }
  }

  def submitOwnerBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[OwnerBeforeLegislationStartModel]) = Future.successful(BadRequest(ownerBeforeLegislationStartView(errors)))

    def routeRequest(model: OwnerBeforeLegislationStartModel) = {
      if (model.ownedBeforeLegislationStart) Future.successful(Redirect(routes.GainController.valueBeforeLegislationStart))
      else Future.successful(Redirect(routes.GainController.howBecameOwner))
    }

    def successAction(model: OwnerBeforeLegislationStartModel) = {
      for {
        _ <- sessionCacheService.saveFormData(keystoreKeys.ownerBeforeLegislationStart, model)
        route <- routeRequest(model)
      } yield route
    }

    ownerBeforeLegislationStartForm.bindFromRequest().fold(errorAction, successAction)
  }

  //################# Value Before Legislation Start Actions ########################

  def valueBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart).map {
      case Some(data) => Ok(valueBeforeLegislationStartView(valueBeforeLegislationStartForm.fill(data)))
      case None => Ok(valueBeforeLegislationStartView(valueBeforeLegislationStartForm))
    }
  }

  def submitValueBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    valueBeforeLegislationStartForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(valueBeforeLegislationStartView(errors))),
      success => {
        sessionCacheService.saveFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart, success)
          .map(_ => Redirect(routes.GainController.acquisitionCosts))
      }
    )
  }

  //################# How Became Owner Actions ########################
  lazy val howBecameOwnerBackLink = Some(controllers.routes.GainController.ownerBeforeLegislationStart.url)
  lazy val howBecameOwnerPostAction = controllers.routes.GainController.submitHowBecameOwner

  def howBecameOwner: Action[AnyContent] = ValidateSession.async { implicit request =>

    sessionCacheService.fetchAndGetFormData[HowBecameOwnerModel](keystoreKeys.howBecameOwner).map {
      case Some(data) => Ok(howBecameOwnerView(howBecameOwnerForm.fill(data), howBecameOwnerBackLink, howBecameOwnerPostAction))
      case _ => Ok(howBecameOwnerView(howBecameOwnerForm, howBecameOwnerBackLink, howBecameOwnerPostAction))
    }
  }

  def submitHowBecameOwner: Action[AnyContent] = ValidateSession.async { implicit request =>
    howBecameOwnerForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(howBecameOwnerView(errors, howBecameOwnerBackLink, howBecameOwnerPostAction))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.howBecameOwner, success).flatMap(_ =>
        success.gainedBy match {
          case "Gifted" => Future.successful(Redirect(routes.GainController.worthWhenGifted))
          case "Inherited" => Future.successful(Redirect(routes.GainController.worthWhenInherited))
          case _ => Future.successful(Redirect(routes.GainController.boughtForLessThanWorth))
        }
        )
      }
    )
  }

  //################# Bought For Less Than Worth Actions ########################
  lazy val boughtForLessThanWorthBackLink = Some(controllers.routes.GainController.howBecameOwner.url)

  def boughtForLessThanWorth: Action[AnyContent] = ValidateSession.async {implicit request =>
    sessionCacheService.fetchAndGetFormData[BoughtForLessThanWorthModel](keystoreKeys.boughtForLessThanWorth).map {
      case Some(data) => Ok(buyForLessView(boughtForLessThanWorthForm.fill(data), boughtForLessThanWorthBackLink))
      case _ => Ok(buyForLessView(boughtForLessThanWorthForm, boughtForLessThanWorthBackLink))
    }
  }

  def submitBoughtForLessThanWorth: Action[AnyContent] = ValidateSession.async { implicit request =>
    def errorAction(errors: Form[BoughtForLessThanWorthModel]) = Future.successful(BadRequest(buyForLessView(errors, boughtForLessThanWorthBackLink)))

    def routeRequest(model: BoughtForLessThanWorthModel) = {
      if (model.boughtForLessThanWorth) Future.successful(Redirect(routes.GainController.worthWhenBoughtForLess))
      else Future.successful(Redirect(routes.GainController.acquisitionValue))
    }

    def successAction(model: BoughtForLessThanWorthModel) = {
      for {
        _ <- sessionCacheService.saveFormData(keystoreKeys.boughtForLessThanWorth, model)
        route <- routeRequest(model)
      } yield route
    }

    boughtForLessThanWorthForm.bindFromRequest().fold(errorAction, successAction)

  }

  //################# Worth When Inherited Actions ########################
  lazy val worthWhenInheritedBackLink = Some(controllers.routes.GainController.howBecameOwner.url)
  lazy val worthWhenInheritedPostAction = controllers.routes.GainController.submitWorthWhenInherited

  def worthWhenInherited: Action[AnyContent] = ValidateSession.async {implicit request =>
    sessionCacheService.fetchAndGetFormData[WorthWhenInheritedModel](keystoreKeys.worthWhenInherited).map {
      case Some(data) => Ok(worthWhenInheritedView(worthWhenInheritedForm.fill(data), worthWhenInheritedBackLink, worthWhenInheritedPostAction))
      case _ => Ok(worthWhenInheritedView(worthWhenInheritedForm, worthWhenInheritedBackLink, worthWhenInheritedPostAction))
    }
  }

  def submitWorthWhenInherited: Action[AnyContent] = ValidateSession.async { implicit request =>
    worthWhenInheritedForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(worthWhenInheritedView(errors, worthWhenInheritedBackLink, worthWhenInheritedPostAction))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.worthWhenInherited, success)
          .map(_ => Redirect(routes.GainController.acquisitionCosts))
      }
    )
  }

  //################# Worth When Gifted Actions ########################
  lazy val worthWhenGiftedBackLink = Some(controllers.routes.GainController.howBecameOwner.url)
  lazy val worthWhenGiftedPostAction = controllers.routes.GainController.submitWorthWhenGifted

  def worthWhenGifted: Action[AnyContent] = ValidateSession.async {implicit request =>
    sessionCacheService.fetchAndGetFormData[WorthWhenGiftedModel](keystoreKeys.worthWhenGifted).map {
      case Some(data) => Ok(worthWhenGiftedView(worthWhenGiftedForm.fill(data), worthWhenGiftedBackLink, worthWhenGiftedPostAction))
      case _ => Ok(worthWhenGiftedView(worthWhenGiftedForm, worthWhenGiftedBackLink, worthWhenGiftedPostAction))
    }
  }

  def submitWorthWhenGifted: Action[AnyContent] = ValidateSession.async { implicit request =>
    worthWhenGiftedForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(worthWhenGiftedView(errors, worthWhenGiftedBackLink, worthWhenGiftedPostAction))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.worthWhenGifted, success)
          .map(_ => Redirect(routes.GainController.acquisitionCosts))
      }
    )
  }

  //################# Worth When Bought For Less Actions ########################

  def worthWhenBoughtForLess: Action[AnyContent] = ValidateSession.async {implicit request =>
    sessionCacheService.fetchAndGetFormData[WorthWhenBoughtForLessModel](keystoreKeys.worthWhenBoughtForLess).map {
      case Some(data) => Ok(worthWhenBoughtForLessView(worthWhenBoughtForLessForm.fill(data)))
      case _ => Ok(worthWhenBoughtForLessView(worthWhenBoughtForLessForm))
    }
  }

  def submitWorthWhenBoughtForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    worthWhenBoughtForLessForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(worthWhenBoughtForLessView(errors))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.worthWhenBoughtForLess, success)
          .map(_ => Redirect(routes.GainController.acquisitionCosts))
      }
    )
  }

  //################# Acquisition Value Actions ########################
  def acquisitionValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[AcquisitionValueModel](keystoreKeys.acquisitionValue).map {
      case Some(data) => Ok(acquisitionValueView(acquisitionValueForm.fill(data)))
      case None => Ok(acquisitionValueView(acquisitionValueForm))
    }
  }

  def submitAcquisitionValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    acquisitionValueForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(acquisitionValueView(errors))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.acquisitionValue, success)
          .map(_ => Redirect(routes.GainController.acquisitionCosts))
      }
    )
  }

  //################# Acquisition Costs Actions ########################

  private def acquisitionCostsBackLink()(implicit request: Request [?]): Future[Option[String]] = {
    val ownerOn = sessionCacheService.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
    val howBecameOwner = sessionCacheService.fetchAndGetFormData[HowBecameOwnerModel](keystoreKeys.howBecameOwner)
    val boughtForLess = sessionCacheService.fetchAndGetFormData[BoughtForLessThanWorthModel](keystoreKeys.boughtForLessThanWorth)

    def determineBackLink(ownerOn: Option[OwnerBeforeLegislationStartModel],
                          howBecameOwner: Option[HowBecameOwnerModel],
                          boughtForLess: Option[BoughtForLessThanWorthModel]): Future[Option[String]] = {
      Future.successful((ownerOn, howBecameOwner, boughtForLess) match {
        case (Some(OwnerBeforeLegislationStartModel(true)), _, _) => Some(controllers.routes.GainController.valueBeforeLegislationStart.url)
        case (_, Some(HowBecameOwnerModel("Inherited")), _) => Some(controllers.routes.GainController.worthWhenInherited.url)
        case (_, Some(HowBecameOwnerModel("Gifted")), _) => Some(controllers.routes.GainController.worthWhenGifted.url)
        case (_, _, Some(BoughtForLessThanWorthModel(true))) => Some(controllers.routes.GainController.worthWhenBoughtForLess.url)
        case _ => Some(controllers.routes.GainController.acquisitionValue.url)
      })
    }

    for {
      ownerOn <- ownerOn
      howBecameOwner <- howBecameOwner
      boughtForLess <- boughtForLess
      backLink <- determineBackLink(ownerOn, howBecameOwner, boughtForLess)
    } yield backLink
  }

  private def createAcquisitionCostsForm()(implicit request: Request [?]): Future[Form[AcquisitionCostsModel]] = {
    sessionCacheService.fetchAndGetFormData[AcquisitionCostsModel](keystoreKeys.acquisitionCosts).map {
      case Some(data) => acquisitionCostsForm.fill(data)
      case None => acquisitionCostsForm
    }
  }

  def acquisitionCosts: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for {
      backLink <- acquisitionCostsBackLink()
      form <- createAcquisitionCostsForm()
    } yield Ok(acquisitionCostsView(form, backLink))).recoverToStart()
  }

  def submitAcquisitionCosts: Action[AnyContent] = ValidateSession.async { implicit request =>
    acquisitionCostsForm.bindFromRequest().fold(
      errors =>
        (for {
          backLink <- acquisitionCostsBackLink()
        } yield BadRequest(acquisitionCostsView(errors, backLink))).recoverToStart(),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.acquisitionCosts, success)
          .map(_ => Redirect(routes.GainController.improvements))
      }
    )
  }

  //################# Improvements Actions ########################
  private def getOwnerBeforeAprilNineteenEightyTwo()(implicit request: Request[?]): Future[Boolean] = {
    sessionCacheService.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
      .map(_.get.ownedBeforeLegislationStart)
  }

  private def getImprovementsForm(ownerBeforeAprilNineteenEightyTwo: Boolean)(implicit request: Request[?]): Future[Form[ImprovementsModel]] = {
    sessionCacheService.fetchAndGetFormData[ImprovementsModel](keystoreKeys.improvements).map {
      case Some(data) => improvementsForm(ownerBeforeAprilNineteenEightyTwo).fill(data)
      case _ => improvementsForm(ownerBeforeAprilNineteenEightyTwo)
    }
  }

  def improvements: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for{
      ownerBeforeAprilNineteenEightyTwo <- getOwnerBeforeAprilNineteenEightyTwo()
      improvementsForm <- getImprovementsForm(ownerBeforeAprilNineteenEightyTwo)
    } yield Ok(improvementsView(improvementsForm, ownerBeforeAprilNineteenEightyTwo))).recoverToStart()
  }

  def submitImprovements: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal): Future[Result] = {
      if (totalGain > 0) Future.successful(Redirect(routes.DeductionsController.propertyLivedIn))
      else Future.successful(Redirect(routes.ReviewAnswersController.reviewGainAnswers))
    }

    def errorAction(form: Form[ImprovementsModel]): Future[Result] = {
      for {
        ownerBeforeAprilNineteenEightyTwo <- getOwnerBeforeAprilNineteenEightyTwo()
      } yield BadRequest(improvementsView(form, ownerBeforeAprilNineteenEightyTwo))
    }

    def successAction(model: ImprovementsModel): Future[Result] = {
      (for {
        _ <- sessionCacheService.saveFormData(keystoreKeys.improvements, model)
        answers <- sessionCacheService.getPropertyGainAnswers
        grossGain <- calcConnector.calculateRttPropertyGrossGain(answers)
        route <- routeRequest(grossGain)
      } yield route).recoverToStart()
    }

    (for {
      ownerBeforeAprilNineteenEightyTwo <- getOwnerBeforeAprilNineteenEightyTwo()
      result <- improvementsForm(ownerBeforeAprilNineteenEightyTwo).bindFromRequest().fold(errorAction, successAction)
    } yield result).recoverToStart()
  }
}
