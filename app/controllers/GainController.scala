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

import java.time.{LocalDate, ZoneId}
import java.util.UUID

import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.{Dates, TaxDates}
import config.AppConfig
import connectors.{CalculatorConnector, SessionCacheConnector}
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
import javax.inject.{Inject, Singleton}
import models.resident._
import models.resident.properties._
import models.resident.properties.gain.{OwnerBeforeLegislationStartModel, WhoDidYouGiveItToModel, WorthWhenGiftedModel}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesProvider}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.{resident => commonViews}
import views.html.calculation.resident.properties.{gain => views}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GainController @Inject()(
                                val calcConnector: CalculatorConnector,
                                val sessionCacheConnector: SessionCacheConnector,
                                val sessionCacheService: SessionCacheService,
                                val messagesControllerComponents: MessagesControllerComponents,
                                implicit val appConfig: AppConfig
                              ) extends FrontendController(messagesControllerComponents) with ValidActiveSession with I18nSupport {

  implicit val ec: ExecutionContext = messagesControllerComponents.executionContext

  implicit val mccMessages: Request[AnyContent] => Messages = request =>
    messagesControllerComponents.messagesApi.preferred(request)
  lazy val messagesProvider: Request[AnyContent] => MessagesProvider = request => new MessagesProvider {
    override def messages: Messages = mccMessages(request)
  }

  override lazy val homeLink: String = controllers.routes.PropertiesController.introduction().url
  override lazy val sessionTimeoutUrl: String = homeLink

  //################# Disposal Date Actions ####################
  val disposalDate: Action[AnyContent] = Action.async { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(views.disposalDate(disposalDateForm())).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate).map {
        case Some(data) => Ok(views.disposalDate(disposalDateForm().fill(data)))
        case None => Ok(views.disposalDate(disposalDateForm()))
      }
    }
  }

  val submitDisposalDate: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(taxYearResult: Option[TaxYearModel]): Future[Result] = {
      if (taxYearResult.isDefined && !taxYearResult.get.isValidYear) Future.successful(Redirect(routes.GainController.outsideTaxYears()))
      else Future.successful(Redirect(routes.GainController.sellOrGiveAway()))
    }

    def bindForm(minimumDate: LocalDate) = {
      disposalDateForm(minimumDate.atStartOfDay(ZoneId.of("Europe/London"))).bindFromRequest.fold(
        errors => {
          Future.successful(
          BadRequest(
            views.disposalDate(errors.copy(errors = errors.errors.map { error =>
              if (error.key == "") error.copy(key = "disposalDateDay") else error
            }))
          )
        )},
        success => {
          (for {
            save <- sessionCacheConnector.saveFormData(keystoreKeys.disposalDate, success)
            taxYearResult <- calcConnector.getTaxYear(s"${success.year}-${success.month}-${success.day}")
            route <- routeRequest(taxYearResult)
          } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
        }
      )
    }

    for {
      minimumDate <- calcConnector.getMinimumDate()
      result <- bindForm(minimumDate)
    } yield result
  }

  //################ Sell or Give Away Actions ######################
  lazy val sellOrGiveAwayBackUrl = routes.GainController.disposalDate().url
  lazy val sellOrGiveAwayPostAction = controllers.routes.GainController.submitSellOrGiveAway()

  val sellOrGiveAway: Action[AnyContent] = ValidateSession.async { implicit request =>

    sessionCacheConnector.fetchAndGetFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway).map {
      case Some(data) => Ok(views.sellOrGiveAway(sellOrGiveAwayForm.fill(data), Some(sellOrGiveAwayBackUrl), homeLink, sellOrGiveAwayPostAction))
      case _ => Ok(views.sellOrGiveAway(sellOrGiveAwayForm, Some(sellOrGiveAwayBackUrl), homeLink, sellOrGiveAwayPostAction))
    }
  }

  val submitSellOrGiveAway: Action[AnyContent] = ValidateSession.async { implicit request =>
    sellOrGiveAwayForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.sellOrGiveAway(errors, Some(sellOrGiveAwayBackUrl), homeLink, sellOrGiveAwayPostAction))),
      success => {
        sessionCacheConnector.saveFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway, success).flatMap(_ =>
        success match {
          case SellOrGiveAwayModel(true) => Future.successful(Redirect(routes.GainController.whoDidYouGiveItTo()))
          case SellOrGiveAwayModel(false) => Future.successful(Redirect(routes.GainController.sellForLess()))
        }
        )
      }
    )
  }

  //################ Who Did You Give It To Actions ######################
  val whoDidYouGiveItTo: Action[AnyContent] = ValidateSession.async { implicit request =>

    sessionCacheConnector.fetchAndGetFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo).map {
      case Some(data) => Ok(views.whoDidYouGiveItTo(whoDidYouGiveItToForm.fill(data)))
      case _ => Ok(views.whoDidYouGiveItTo(whoDidYouGiveItToForm))
    }
  }

  val submitWhoDidYouGiveItTo: Action[AnyContent] = ValidateSession.async { implicit request =>
    whoDidYouGiveItToForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.whoDidYouGiveItTo(errors))),
      success => {
        sessionCacheConnector.saveFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo, success).flatMap(_ =>
        success match {
          case WhoDidYouGiveItToModel("Spouse" | "Charity") => Future.successful(Redirect(routes.GainController.noTaxToPay()))
          case _ => Future.successful(Redirect(routes.GainController.worthWhenGaveAway()))
        }
        )
      }
    )
  }

  //################ No Tax to Pay Actions ######################
  val noTaxToPay: Action[AnyContent] = ValidateSession.async { implicit request =>

    def isGivenToCharity: Future[Boolean] = {
      sessionCacheConnector.fetchAndGetFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo).map {
        case Some(WhoDidYouGiveItToModel("Charity")) => true
        case _ => false
      }
    }

    def result(input: Boolean): Future[Result] = {
      Future.successful(Ok(views.noTaxToPay(input)))
    }

    (for {
      givenToCharity <- isGivenToCharity
      result <- result(givenToCharity)
    } yield result).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  //################ Outside Tax Years Actions ######################
  val outsideTaxYears: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for {
      disposalDate <- sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${disposalDate.get.year}-${disposalDate.get.month}-${disposalDate.get.day}")
    } yield {
      Ok(commonViews.outsideTaxYear(
        taxYear = taxYear.get,
        isAfterApril15 = TaxDates.dateAfterStart(Dates.constructDate(disposalDate.get.day, disposalDate.get.month, disposalDate.get.year)),
        true,
        navBackLink = routes.GainController.disposalDate().url,
        navHomeLink = homeLink,
        continueUrl = routes.GainController.sellOrGiveAway().url,
        navTitle = Messages("calc.base.resident.properties.home")
      ))
    }).recoverToStart(homeLink, sessionTimeoutUrl)
  }


  //################# Worth When Gave Away Actions ############################

  private lazy val worthWhenGaveAwayPostAction = controllers.routes.GainController.submitWorthWhenGaveAway()
  private lazy val worthWhenGaveAwayBackLink = Some(controllers.routes.GainController.whoDidYouGiveItTo().toString)

  val worthWhenGaveAway: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[WorthWhenGaveAwayModel](keystoreKeys.worthWhenGaveAway).map {
      case Some(data) => Ok(views.worthWhenGaveAway(worthWhenGaveAwayForm.fill(data), worthWhenGaveAwayBackLink, homeLink, worthWhenGaveAwayPostAction))
      case None => Ok(views.worthWhenGaveAway(worthWhenGaveAwayForm, worthWhenGaveAwayBackLink, homeLink, worthWhenGaveAwayPostAction))
    }
  }

  val submitWorthWhenGaveAway: Action[AnyContent] = ValidateSession.async { implicit request =>
    worthWhenGaveAwayForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenGaveAway(errors, worthWhenGaveAwayBackLink, homeLink, worthWhenGaveAwayPostAction))),
      success => {
        sessionCacheConnector.saveFormData[WorthWhenGaveAwayModel](keystoreKeys.worthWhenGaveAway, success)
          .map(_ => Redirect(routes.GainController.disposalCosts()))
      }
    )
  }

  //############## Sell for Less Actions ##################
  lazy val sellForLessBackLink = Some(controllers.routes.GainController.sellOrGiveAway().url)

  val sellForLess: Action[AnyContent] = ValidateSession.async {implicit request =>
    sessionCacheConnector.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess).map{
      case Some(data) => Ok(commonViews.properties.gain.sellForLess(sellForLessForm.fill(data), homeLink, sellForLessBackLink))
      case _ => Ok(commonViews.properties.gain.sellForLess(sellForLessForm, homeLink, sellForLessBackLink))
    }
  }

  val submitSellForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    def errorAction(errors: Form[SellForLessModel]) = Future.successful(BadRequest(commonViews.properties.gain.sellForLess(errors, homeLink, sellForLessBackLink)))

    def routeRequest(model: SellForLessModel) = {
      if (model.sellForLess) Future.successful(Redirect(routes.GainController.worthWhenSoldForLess()))
      else Future.successful(Redirect(routes.GainController.disposalValue()))
    }

    def successAction(model: SellForLessModel) = {
      for {
        save <- sessionCacheConnector.saveFormData(keystoreKeys.sellForLess, model)
        route <- routeRequest(model)
      } yield route
    }

    sellForLessForm.bindFromRequest().fold(errorAction, successAction)
  }

  //################ Disposal Value Actions ######################
  val disposalValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[DisposalValueModel](keystoreKeys.disposalValue).map {
      case Some(data) => Ok(views.disposalValue(disposalValueForm.fill(data)))
      case None => Ok(views.disposalValue(disposalValueForm))
    }
  }

  val submitDisposalValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    disposalValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalValue(errors))),
      success => {
        sessionCacheConnector.saveFormData[DisposalValueModel](keystoreKeys.disposalValue, success)
          .map(_ => Redirect(routes.GainController.disposalCosts()))
      }
    )
  }

  //################ Property Worth When Sold For Less Actions ######################
  val worthWhenSoldForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[WorthWhenSoldForLessModel](keystoreKeys.worthWhenSoldForLess).map {
      case Some(data) => Ok(views.worthWhenSoldForLess(worthWhenSoldForLessForm.fill(data)))
      case _ => Ok(views.worthWhenSoldForLess(worthWhenSoldForLessForm))
    }
  }

  val submitWorthWhenSoldForLess: Action[AnyContent] = ValidateSession.async { implicit request =>

    worthWhenSoldForLessForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenSoldForLess(errors))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.worthWhenSoldForLess, success)
          .map(_ => Redirect(routes.GainController.disposalCosts()))
      }
    )
  }

  //################# Disposal Costs Actions ########################
  private def disposalCostsBackLink: (SellOrGiveAwayModel, Option[SellForLessModel]) => String = {
    case (gaveAwayAnswer, _) if gaveAwayAnswer.givenAway => routes.GainController.worthWhenGaveAway().url
    case (_, soldForLessAnswer) if soldForLessAnswer.get.sellForLess => routes.GainController.worthWhenSoldForLess().url
    case (_, _) => routes.GainController.disposalValue().url
  }

  val disposalCosts: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backLink: String) = {
      sessionCacheConnector.fetchAndGetFormData[DisposalCostsModel](keystoreKeys.disposalCosts).map {
        case Some(data) => Ok(views.disposalCosts(disposalCostsForm.fill(data), backLink))
        case None => Ok(views.disposalCosts(disposalCostsForm, backLink))
      }
    }

    (for {
      gaveAway <- sessionCacheConnector.fetchAndGetFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway)
      soldForLess <- sessionCacheConnector.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess)
      route <- routeRequest(disposalCostsBackLink(gaveAway.get, soldForLess))
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  val submitDisposalCosts: Action[AnyContent] = ValidateSession.async { implicit request =>
    def routeRequest(backLink: String) = {
      disposalCostsForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(views.disposalCosts(errors,backLink))),
        success => {
          sessionCacheConnector.saveFormData(keystoreKeys.disposalCosts, success)
            .map(_ => Redirect(routes.GainController.ownerBeforeLegislationStart()))
        }
      )
    }

    (for {
      gaveAway <- sessionCacheConnector.fetchAndGetFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway)
      soldForLess <- sessionCacheConnector.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess)
      route <- routeRequest(disposalCostsBackLink(gaveAway.get, soldForLess))
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  //################# Owner Before Legislation Start Actions ########################
  val ownerBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart).map {
      case Some(data) => Ok(views.ownerBeforeLegislationStart(ownerBeforeLegislationStartForm.fill(data)))
      case None => Ok(views.ownerBeforeLegislationStart(ownerBeforeLegislationStartForm))
    }
  }

  val submitOwnerBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[OwnerBeforeLegislationStartModel]) = Future.successful(BadRequest(views.ownerBeforeLegislationStart(errors)))

    def routeRequest(model: OwnerBeforeLegislationStartModel) = {
      if (model.ownedBeforeLegislationStart) Future.successful(Redirect(routes.GainController.valueBeforeLegislationStart()))
      else Future.successful(Redirect(routes.GainController.howBecameOwner()))
    }

    def successAction(model: OwnerBeforeLegislationStartModel) = {
      for {
        save <- sessionCacheConnector.saveFormData(keystoreKeys.ownerBeforeLegislationStart, model)
        route <- routeRequest(model)
      } yield route
    }

    ownerBeforeLegislationStartForm.bindFromRequest().fold(errorAction, successAction)
  }

  //################# Value Before Legislation Start Actions ########################

  val valueBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart).map {
      case Some(data) => Ok(views.valueBeforeLegislationStart(valueBeforeLegislationStartForm.fill(data)))
      case None => Ok(views.valueBeforeLegislationStart(valueBeforeLegislationStartForm))
    }
  }

  val submitValueBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    valueBeforeLegislationStartForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.valueBeforeLegislationStart(errors))),
      success => {
        sessionCacheConnector.saveFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart, success)
          .map(_ => Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# How Became Owner Actions ########################
  lazy val howBecameOwnerBackLink = Some(controllers.routes.GainController.ownerBeforeLegislationStart().url)
  lazy val howBecameOwnerPostAction = controllers.routes.GainController.submitHowBecameOwner()

  val howBecameOwner: Action[AnyContent] = ValidateSession.async { implicit request =>

    sessionCacheConnector.fetchAndGetFormData[HowBecameOwnerModel](keystoreKeys.howBecameOwner).map {
      case Some(data) => Ok(views.howBecameOwner(howBecameOwnerForm.fill(data), howBecameOwnerBackLink, homeLink, howBecameOwnerPostAction))
      case _ => Ok(views.howBecameOwner(howBecameOwnerForm, howBecameOwnerBackLink, homeLink, howBecameOwnerPostAction))
    }
  }

  val submitHowBecameOwner: Action[AnyContent] = ValidateSession.async { implicit request =>
    howBecameOwnerForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.howBecameOwner(errors, howBecameOwnerBackLink, homeLink, howBecameOwnerPostAction))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.howBecameOwner, success).flatMap(_ =>
        success.gainedBy match {
          case "Gifted" => Future.successful(Redirect(routes.GainController.worthWhenGifted()))
          case "Inherited" => Future.successful(Redirect(routes.GainController.worthWhenInherited()))
          case _ => Future.successful(Redirect(routes.GainController.boughtForLessThanWorth()))
        }
        )
      }
    )
  }

  //################# Bought For Less Than Worth Actions ########################
  lazy val boughtForLessThanWorthBackLink = Some(controllers.routes.GainController.howBecameOwner().url)

  val boughtForLessThanWorth: Action[AnyContent] = ValidateSession.async {implicit request =>
    sessionCacheConnector.fetchAndGetFormData[BoughtForLessThanWorthModel](keystoreKeys.boughtForLessThanWorth).map {
      case Some(data) => Ok(commonViews.properties.gain.buyForLess(boughtForLessThanWorthForm.fill(data), homeLink, boughtForLessThanWorthBackLink))
      case _ => Ok(commonViews.properties.gain.buyForLess(boughtForLessThanWorthForm, homeLink, boughtForLessThanWorthBackLink))
    }
  }

  val submitBoughtForLessThanWorth: Action[AnyContent] = ValidateSession.async { implicit request =>
    def errorAction(errors: Form[BoughtForLessThanWorthModel]) = Future.successful(BadRequest(commonViews.properties.gain.buyForLess(errors, homeLink, boughtForLessThanWorthBackLink)))

    def routeRequest(model: BoughtForLessThanWorthModel) = {
      if (model.boughtForLessThanWorth) Future.successful(Redirect(routes.GainController.worthWhenBoughtForLess()))
      else Future.successful(Redirect(routes.GainController.acquisitionValue()))
    }

    def successAction(model: BoughtForLessThanWorthModel) = {
      for {
        save <- sessionCacheConnector.saveFormData(keystoreKeys.boughtForLessThanWorth, model)
        route <- routeRequest(model)
      } yield route
    }

    boughtForLessThanWorthForm.bindFromRequest().fold(errorAction, successAction)

  }

  //################# Worth When Inherited Actions ########################
  lazy val worthWhenInheritedBackLink = Some(controllers.routes.GainController.howBecameOwner().url)
  lazy val worthWhenInheritedPostAction = controllers.routes.GainController.submitWorthWhenInherited()

  val worthWhenInherited: Action[AnyContent] = ValidateSession.async {implicit request =>
    sessionCacheConnector.fetchAndGetFormData[WorthWhenInheritedModel](keystoreKeys.worthWhenInherited).map {
      case Some(data) => Ok(views.worthWhenInherited(worthWhenInheritedForm.fill(data), worthWhenInheritedBackLink, homeLink, worthWhenInheritedPostAction))
      case _ => Ok(views.worthWhenInherited(worthWhenInheritedForm, worthWhenInheritedBackLink, homeLink, worthWhenInheritedPostAction))
    }
  }

  val submitWorthWhenInherited: Action[AnyContent] = ValidateSession.async { implicit request =>
    worthWhenInheritedForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenInherited(errors, worthWhenInheritedBackLink, homeLink, worthWhenInheritedPostAction))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.worthWhenInherited, success)
          .map(_ => Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Worth When Gifted Actions ########################
  lazy val worthWhenGiftedBackLink = Some(controllers.routes.GainController.howBecameOwner().url)
  lazy val worthWhenGiftedPostAction = controllers.routes.GainController.submitWorthWhenGifted()

  val worthWhenGifted: Action[AnyContent] = ValidateSession.async {implicit request =>
    sessionCacheConnector.fetchAndGetFormData[WorthWhenGiftedModel](keystoreKeys.worthWhenGifted).map {
      case Some(data) => Ok(views.worthWhenGifted(worthWhenGiftedForm.fill(data), worthWhenGiftedBackLink, homeLink, worthWhenGiftedPostAction))
      case _ => Ok(views.worthWhenGifted(worthWhenGiftedForm, worthWhenGiftedBackLink, homeLink, worthWhenGiftedPostAction))
    }
  }

  val submitWorthWhenGifted: Action[AnyContent] = ValidateSession.async { implicit request =>
    worthWhenGiftedForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenGifted(errors, worthWhenGiftedBackLink, homeLink, worthWhenGiftedPostAction))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.worthWhenGifted, success)
          .map(_ => Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Worth When Bought For Less Actions ########################

  val worthWhenBoughtForLess: Action[AnyContent] = ValidateSession.async {implicit request =>
    sessionCacheConnector.fetchAndGetFormData[WorthWhenBoughtForLessModel](keystoreKeys.worthWhenBoughtForLess).map {
      case Some(data) => Ok(views.worthWhenBoughtForLess(worthWhenBoughtForLessForm.fill(data)))
      case _ => Ok(views.worthWhenBoughtForLess(worthWhenBoughtForLessForm))
    }
  }

  val submitWorthWhenBoughtForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    worthWhenBoughtForLessForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenBoughtForLess(errors))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.worthWhenBoughtForLess, success)
          .map(_ => Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Acquisition Value Actions ########################
  val acquisitionValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[AcquisitionValueModel](keystoreKeys.acquisitionValue).map {
      case Some(data) => Ok(views.acquisitionValue(acquisitionValueForm.fill(data)))
      case None => Ok(views.acquisitionValue(acquisitionValueForm))
    }
  }

  val submitAcquisitionValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    acquisitionValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.acquisitionValue(errors))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.acquisitionValue, success)
          .map(_ => Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Acquisition Costs Actions ########################

  private def acquisitionCostsBackLink()(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val ownerOn = sessionCacheConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
    val howBecameOwner = sessionCacheConnector.fetchAndGetFormData[HowBecameOwnerModel](keystoreKeys.howBecameOwner)
    val boughtForLess = sessionCacheConnector.fetchAndGetFormData[BoughtForLessThanWorthModel](keystoreKeys.boughtForLessThanWorth)

    def determineBackLink(ownerOn: Option[OwnerBeforeLegislationStartModel],
                          howBecameOwner: Option[HowBecameOwnerModel],
                          boughtForLess: Option[BoughtForLessThanWorthModel]): Future[Option[String]] = {
      Future.successful((ownerOn, howBecameOwner, boughtForLess) match {
        case (Some(OwnerBeforeLegislationStartModel(true)), _, _) => Some(controllers.routes.GainController.valueBeforeLegislationStart().url)
        case (_, Some(HowBecameOwnerModel("Inherited")), _) => Some(controllers.routes.GainController.worthWhenInherited().url)
        case (_, Some(HowBecameOwnerModel("Gifted")), _) => Some(controllers.routes.GainController.worthWhenGifted().url)
        case (_, _, Some(BoughtForLessThanWorthModel(true))) => Some(controllers.routes.GainController.worthWhenBoughtForLess().url)
        case _ => Some(controllers.routes.GainController.acquisitionValue().url)
      })
    }

    for {
      ownerOn <- ownerOn
      howBecameOwner <- howBecameOwner
      boughtForLess <- boughtForLess
      backLink <- determineBackLink(ownerOn, howBecameOwner, boughtForLess)
    } yield backLink
  }

  private def createAcquisitionCostsForm()(implicit hc: HeaderCarrier): Future[Form[AcquisitionCostsModel]] = {
    sessionCacheConnector.fetchAndGetFormData[AcquisitionCostsModel](keystoreKeys.acquisitionCosts).map {
      case Some(data) => acquisitionCostsForm.fill(data)
      case None => acquisitionCostsForm
    }
  }

  val acquisitionCosts: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for {
      backLink <- acquisitionCostsBackLink
      form <- createAcquisitionCostsForm()
    } yield Ok(views.acquisitionCosts(form, backLink))).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  val submitAcquisitionCosts: Action[AnyContent] = ValidateSession.async { implicit request =>
    acquisitionCostsForm.bindFromRequest.fold(
      errors =>
        (for {
          backLink <- acquisitionCostsBackLink
        } yield BadRequest(views.acquisitionCosts(errors, backLink))).recoverToStart(homeLink, sessionTimeoutUrl),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.acquisitionCosts, success)
          .map(_ => Redirect(routes.GainController.improvements()))
      }
    )
  }

  //################# Improvements Actions ########################
  private def getOwnerBeforeAprilNineteenEightyTwo()(implicit hc: HeaderCarrier): Future[Boolean] = {
    sessionCacheConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
      .map(_.get.ownedBeforeLegislationStart)
  }

  private def getImprovementsForm()(implicit hc: HeaderCarrier): Future[Form[ImprovementsModel]] = {
    sessionCacheConnector.fetchAndGetFormData[ImprovementsModel](keystoreKeys.improvements).map {
      case Some(data) => improvementsForm.fill(data)
      case _ => improvementsForm
    }
  }

  val improvements: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for{
      ownerBeforeAprilNineteenEightyTwo <- getOwnerBeforeAprilNineteenEightyTwo()
      improvementsForm <- getImprovementsForm
    } yield Ok(views.improvements(improvementsForm, ownerBeforeAprilNineteenEightyTwo))).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  val submitImprovements: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal): Future[Result] = {
      if (totalGain > 0) Future.successful(Redirect(routes.DeductionsController.propertyLivedIn()))
      else Future.successful(Redirect(routes.ReviewAnswersController.reviewGainAnswers()))
    }

    def errorAction(form: Form[ImprovementsModel]): Future[Result] = {
      getOwnerBeforeAprilNineteenEightyTwo().map(ownerBeforeAprilNineteenEightyTwo =>
        BadRequest(views.improvements(form, ownerBeforeAprilNineteenEightyTwo))
      )
    }

    def successAction(model: ImprovementsModel): Future[Result] = {
      (for {
        save <- sessionCacheConnector.saveFormData(keystoreKeys.improvements, model)
        answers <- sessionCacheService.getPropertyGainAnswers
        grossGain <- calcConnector.calculateRttPropertyGrossGain(answers)
        route <- routeRequest(grossGain)
      } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
    }

    improvementsForm.bindFromRequest.fold(errorAction, successAction)
  }
}
