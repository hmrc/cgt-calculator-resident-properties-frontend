/*
 * Copyright 2016 HM Revenue & Customs
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

import java.util.UUID

import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.{Dates, TaxDates}
import config.{AppConfig, ApplicationConfig}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
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
import play.api.i18n.Messages
import play.api.mvc.{Action, Result}
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import views.html.calculation.{resident => commonViews}
import views.html.calculation.resident.properties.{gain => views}

import scala.concurrent.Future

object GainController extends GainController {
  val calcConnector = CalculatorConnector
  val config = ApplicationConfig
}

trait GainController extends ValidActiveSession {

  val calcConnector: CalculatorConnector
  val config: AppConfig

  val navTitle = Messages("calc.base.resident.properties.home")
  override val homeLink = controllers.routes.PropertiesController.introduction().url
  override val sessionTimeoutUrl = homeLink

  //################# Disposal Date Actions ####################
  val disposalDate = Action.async { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(views.disposalDate(disposalDateForm)).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate).map {
        case Some(data) => Ok(views.disposalDate(disposalDateForm.fill(data)))
        case None => Ok(views.disposalDate(disposalDateForm))
      }
    }
  }

  val submitDisposalDate = Action.async { implicit request =>

    def routeRequest(taxYearResult: Option[TaxYearModel]): Future[Result] = {
      if (taxYearResult.isDefined && !taxYearResult.get.isValidYear) Future.successful(Redirect(routes.GainController.outsideTaxYears()))
      else Future.successful(Redirect(routes.GainController.sellOrGiveAway()))
    }

    disposalDateForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalDate(errors))),
      success => {
        for {
          save <- calcConnector.saveFormData(keystoreKeys.disposalDate, success)
          taxYearResult <- calcConnector.getTaxYear(s"${success.year}-${success.month}-${success.day}")
          route <- routeRequest(taxYearResult)
        } yield route
      }
    )
  }

  //################ Sell or Give Away Actions ######################
  val sellOrGiveAway = ValidateSession.async { implicit request =>
    val backUrl = routes.GainController.disposalDate().url
    val postAction = controllers.routes.GainController.submitSellOrGiveAway()

    calcConnector.fetchAndGetFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway).map {
      case Some(data) => Ok(views.sellOrGiveAway(sellOrGiveAwayForm.fill(data), Some(backUrl), homeLink, postAction))
      case _ => Ok(views.sellOrGiveAway(sellOrGiveAwayForm, Some(backUrl), homeLink, postAction))
    }
  }

  val submitSellOrGiveAway = ValidateSession.async { implicit request =>
    val backUrl = routes.GainController.disposalDate().url
    val postAction = controllers.routes.GainController.submitSellOrGiveAway()

    sellOrGiveAwayForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.sellOrGiveAway(errors, Some(backUrl), homeLink, postAction))),
      success => {
        calcConnector.saveFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway, success)
        success match {
          case SellOrGiveAwayModel(true) => Future.successful(Redirect(routes.GainController.whoDidYouGiveItTo()))
          case SellOrGiveAwayModel(false) => Future.successful(Redirect(routes.GainController.sellForLess()))
        }
      }
    )
  }

  //################ Who Did You Give It To Actions ######################
  val whoDidYouGiveItTo = ValidateSession.async { implicit request =>

    calcConnector.fetchAndGetFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo).map {
      case Some(data) => Ok(views.whoDidYouGiveItTo(whoDidYouGiveItToForm.fill(data)))
      case _ => Ok(views.whoDidYouGiveItTo(whoDidYouGiveItToForm))
    }
  }

  val submitWhoDidYouGiveItTo = ValidateSession.async { implicit request =>
    whoDidYouGiveItToForm.bindFromRequest.fold(
    errors => Future.successful(BadRequest(views.whoDidYouGiveItTo(errors))),
    success => {
      calcConnector.saveFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo, success)
      success match {
        case WhoDidYouGiveItToModel("Spouse") => Future.successful(Redirect(routes.GainController.noTaxToPay()))
        case WhoDidYouGiveItToModel("Charity") => Future.successful(Redirect(routes.GainController.noTaxToPay()))
        case WhoDidYouGiveItToModel("Other") => Future.successful(Redirect(routes.GainController.worthWhenGaveAway()))
      }
    })
  }

  //################ No Tax to Pay Actions ######################
  val noTaxToPay = ValidateSession.async { implicit request =>

    def isGivenToCharity: Future[Boolean] = {
      calcConnector.fetchAndGetFormData[WhoDidYouGiveItToModel](keystoreKeys.whoDidYouGiveItTo).map {
        case Some(WhoDidYouGiveItToModel("Charity")) => true
        case _ => false
      }
    }

    def result(input: Boolean): Future[Result] = {
      Future.successful(Ok(views.noTaxToPay(input)))
    }

    for {
      givenToCharity <- isGivenToCharity
      result <- result(givenToCharity)
    } yield result
  }

  //################ Outside Tax Years Actions ######################
  val outsideTaxYears = ValidateSession.async { implicit request =>
    for {
      disposalDate <- calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${disposalDate.get.year}-${disposalDate.get.month}-${disposalDate.get.day}")
    } yield {
      Ok(commonViews.outsideTaxYear(
        taxYear = taxYear.get,
        isAfterApril15 = TaxDates.dateAfterStart(Dates.constructDate(disposalDate.get.day, disposalDate.get.month, disposalDate.get.year)),
        true,
        navBackLink = routes.GainController.disposalDate().url,
        navHomeLink = homeLink,
        continueUrl = routes.GainController.sellOrGiveAway().url,
        navTitle = navTitle
      ))
    }
  }


  //################# Worth When Gave Away Actions ############################

  private val worthWhenGaveAwayPostAction = controllers.routes.GainController.submitWorthWhenGaveAway()
  private val worthWhenGaveAwayBackLink = Some(controllers.routes.GainController.whoDidYouGiveItTo().toString)

  val worthWhenGaveAway = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[WorthWhenGaveAwayModel](keystoreKeys.worthWhenGaveAway).map {
      case Some(data) => Ok(views.worthWhenGaveAway(worthWhenGaveAwayForm.fill(data), worthWhenGaveAwayBackLink, homeLink, worthWhenGaveAwayPostAction))
      case None => Ok(views.worthWhenGaveAway(worthWhenGaveAwayForm, worthWhenGaveAwayBackLink, homeLink, worthWhenGaveAwayPostAction))
    }
  }

  val submitWorthWhenGaveAway = ValidateSession.async { implicit request =>
    worthWhenGaveAwayForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenGaveAway(errors, worthWhenGaveAwayBackLink, homeLink, worthWhenGaveAwayPostAction))),
      success => {
        calcConnector.saveFormData[WorthWhenGaveAwayModel](keystoreKeys.worthWhenGaveAway, success)
        Future.successful(Redirect(routes.GainController.disposalCosts()))
      }
    )
  }

  //############## Sell for Less Actions ##################
    val sellForLess = ValidateSession.async {implicit request =>

    val backLink = Some(controllers.routes.GainController.sellOrGiveAway().toString)

    calcConnector.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess).map{
      case Some(data) => Ok(commonViews.properties.gain.sellForLess(sellForLessForm.fill(data), homeLink, backLink))
      case _ => Ok(commonViews.properties.gain.sellForLess(sellForLessForm, homeLink, backLink))
    }
  }

  val submitSellForLess = ValidateSession.async { implicit request =>

    lazy val backLink = Some(controllers.GainController.sellOrGiveAway().toString())

    def errorAction(errors: Form[SellForLessModel]) = Future.successful(BadRequest(commonViews.properties.gain.sellForLess(
      errors, homeLink, backLink
    )))

    def routeRequest(model: SellForLessModel) = {
      if (model.sellForLess) Future.successful(Redirect(routes.GainController.worthWhenSoldForLess()))
      else Future.successful(Redirect(routes.GainController.disposalValue()))
    }

    def successAction(model: SellForLessModel) = {
      for {
        save <- calcConnector.saveFormData(keystoreKeys.sellForLess, model)
        route <- routeRequest(model)
      } yield route
    }

    sellForLessForm.bindFromRequest().fold(errorAction, successAction)

  }

  //################ Disposal Value Actions ######################
  val disposalValue = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalValueModel](keystoreKeys.disposalValue).map {
      case Some(data) => Ok(views.disposalValue(disposalValueForm.fill(data)))
      case None => Ok(views.disposalValue(disposalValueForm))
    }
  }

  val submitDisposalValue = ValidateSession.async { implicit request =>
    disposalValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalValue(errors))),
      success => {
        calcConnector.saveFormData[DisposalValueModel](keystoreKeys.disposalValue, success)
        Future.successful(Redirect(routes.GainController.disposalCosts()))
      }
    )
  }

  //################ Property Worth When Sold For Less Actions ######################
  val worthWhenSoldForLess = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[WorthWhenSoldForLessModel](keystoreKeys.worthWhenSoldForLess).map {
      case Some(data) => Ok(views.worthWhenSoldForLess(worthWhenSoldForLessForm.fill(data)))
      case _ => Ok(views.worthWhenSoldForLess(worthWhenSoldForLessForm))
    }
  }

  val submitWorthWhenSoldForLess = ValidateSession.async { implicit request =>

    worthWhenSoldForLessForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenSoldForLess(errors))),
      success => {
        calcConnector.saveFormData(keystoreKeys.worthWhenSoldForLess, success)
        Future.successful(Redirect(routes.GainController.disposalCosts()))
      }
    )
  }

  //################# Disposal Costs Actions ########################
  private def disposalCostsBackLink: (SellOrGiveAwayModel, Option[SellForLessModel]) => String = {
    case (gaveAwayAnswer, _) if gaveAwayAnswer.givenAway => routes.GainController.worthWhenGaveAway().url
    case (_, soldForLessAnswer) if soldForLessAnswer.get.sellForLess => routes.GainController.worthWhenSoldForLess().url
    case (_, _) => routes.GainController.disposalValue().url
  }

  val disposalCosts = ValidateSession.async { implicit request =>

    def routeRequest(backLink: String) = {
      calcConnector.fetchAndGetFormData[DisposalCostsModel](keystoreKeys.disposalCosts).map {
        case Some(data) => Ok(views.disposalCosts(disposalCostsForm.fill(data), backLink))
        case None => Ok(views.disposalCosts(disposalCostsForm, backLink))
      }
    }

    for {
      gaveAway <- calcConnector.fetchAndGetFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway)
      soldForLess <- calcConnector.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess)
      route <- routeRequest(disposalCostsBackLink(gaveAway.get, soldForLess))
    } yield route
  }

  val submitDisposalCosts = ValidateSession.async { implicit request =>
    def routeRequest(backLink: String) = {
      disposalCostsForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(views.disposalCosts(errors,backLink))),
        success => {
          calcConnector.saveFormData(keystoreKeys.disposalCosts, success)
          Future.successful(Redirect(routes.GainController.ownerBeforeLegislationStart()))}
      )
    }

    for {
      gaveAway <- calcConnector.fetchAndGetFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway)
      soldForLess <- calcConnector.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess)
      route <- routeRequest(disposalCostsBackLink(gaveAway.get, soldForLess))
    } yield route
  }

  //################# Owner Before Legislation Start Actions ########################
  val ownerBeforeLegislationStart = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart).map {
      case Some(data) => Ok(views.ownerBeforeLegislationStart(ownerBeforeLegislationStartForm.fill(data)))
      case None => Ok(views.ownerBeforeLegislationStart(ownerBeforeLegislationStartForm))
    }
  }

  val submitOwnerBeforeLegislationStart = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[OwnerBeforeLegislationStartModel]) = Future.successful(BadRequest(views.ownerBeforeLegislationStart(errors)))

    def routeRequest(model: OwnerBeforeLegislationStartModel) = {
      if (model.ownedBeforeLegislationStart) Future.successful(Redirect(routes.GainController.valueBeforeLegislationStart()))
      else Future.successful(Redirect(routes.GainController.howBecameOwner()))
    }

    def successAction(model: OwnerBeforeLegislationStartModel) = {
      for {
        save <- calcConnector.saveFormData(keystoreKeys.ownerBeforeLegislationStart, model)
        route <- routeRequest(model)
      } yield route
    }

    ownerBeforeLegislationStartForm.bindFromRequest().fold(errorAction, successAction)
  }

  //################# Value Before Legislation Start Actions ########################

  val valueBeforeLegislationStart = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart).map {
      case Some(data) => Ok(views.valueBeforeLegislationStart(valueBeforeLegislationStartForm.fill(data)))
      case None => Ok(views.valueBeforeLegislationStart(valueBeforeLegislationStartForm))
    }
  }

  val submitValueBeforeLegislationStart = ValidateSession.async { implicit request =>
    valueBeforeLegislationStartForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.valueBeforeLegislationStart(errors))),
      success => {
        calcConnector.saveFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# How Became Owner Actions ########################
  val howBecameOwner = ValidateSession.async { implicit request =>
    val backLink = Some(controllers.routes.GainController.ownerBeforeLegislationStart().url)
    val postAction = controllers.routes.GainController.submitHowBecameOwner()

    calcConnector.fetchAndGetFormData[HowBecameOwnerModel](keystoreKeys.howBecameOwner).map {
      case Some(data) => Ok(views.howBecameOwner(howBecameOwnerForm.fill(data), backLink, homeLink, postAction))
      case _ => Ok(views.howBecameOwner(howBecameOwnerForm, backLink, homeLink, postAction))
    }
  }

  val submitHowBecameOwner = ValidateSession.async { implicit request =>
    val backLink = Some(controllers.routes.GainController.ownerBeforeLegislationStart().url)
    val postAction = controllers.routes.GainController.submitHowBecameOwner()

    howBecameOwnerForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.howBecameOwner(errors, backLink, homeLink, postAction))),
      success => {
        calcConnector.saveFormData(keystoreKeys.howBecameOwner, success)
        success.gainedBy match {
          case "Gifted" => Future.successful(Redirect(routes.GainController.worthWhenGifted()))
          case "Inherited" => Future.successful(Redirect(routes.GainController.worthWhenInherited()))
          case _ => Future.successful(Redirect(routes.GainController.boughtForLessThanWorth()))
        }
      }
    )
  }

  //################# Bought For Less Than Worth Actions ########################
  val boughtForLessThanWorth = ValidateSession.async {implicit request =>

    val backLink = Some(controllers.routes.GainController.howBecameOwner().toString)

    calcConnector.fetchAndGetFormData[BoughtForLessThanWorthModel](keystoreKeys.boughtForLessThanWorth).map{
      case Some(data) => Ok(commonViews.properties.gain.buyForLess(boughtForLessThanWorthForm.fill(data), homeLink, backLink))
      case _ => Ok(commonViews.properties.gain.buyForLess(boughtForLessThanWorthForm, homeLink, backLink))
    }
  }

  val submitBoughtForLessThanWorth = ValidateSession.async { implicit request =>

    lazy val backLink = Some(controllers.GainController.howBecameOwner().toString())

    def errorAction(errors: Form[BoughtForLessThanWorthModel]) = Future.successful(BadRequest(commonViews.properties.gain.buyForLess(
      errors, homeLink, backLink
    )))

    def routeRequest(model: BoughtForLessThanWorthModel) = {
      if (model.boughtForLessThanWorth) Future.successful(Redirect(routes.GainController.worthWhenBoughtForLess()))
      else Future.successful(Redirect(routes.GainController.acquisitionValue()))
    }

    def successAction(model: BoughtForLessThanWorthModel) = {
      for {
        save <- calcConnector.saveFormData(keystoreKeys.boughtForLessThanWorth, model)
        route <- routeRequest(model)
      } yield route
    }

    boughtForLessThanWorthForm.bindFromRequest().fold(errorAction, successAction)

  }

  //################# Worth When Inherited Actions ########################
  val worthWhenInherited = ValidateSession.async {implicit request =>
    val backLink = Some(controllers.routes.GainController.howBecameOwner().url)
    val postAction = controllers.routes.GainController.submitWorthWhenInherited()

    calcConnector.fetchAndGetFormData[WorthWhenInheritedModel](keystoreKeys.worthWhenInherited).map {
      case Some(data) => Ok(views.worthWhenInherited(worthWhenInheritedForm.fill(data), backLink, homeLink, postAction))
      case _ => Ok(views.worthWhenInherited(worthWhenInheritedForm, backLink, homeLink, postAction))
    }
  }

  val submitWorthWhenInherited = ValidateSession.async { implicit request =>
    val backLink = Some(controllers.routes.GainController.howBecameOwner().url)
    val postAction = controllers.routes.GainController.submitWorthWhenInherited()

    worthWhenInheritedForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenInherited(errors, backLink, homeLink, postAction))),
      success => {
        calcConnector.saveFormData(keystoreKeys.worthWhenInherited, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Worth When Gifted Actions ########################
   val worthWhenGifted = ValidateSession.async {implicit request =>
    val backLink = Some(controllers.routes.GainController.howBecameOwner().url)
    val postAction = controllers.routes.GainController.submitWorthWhenGifted()

    calcConnector.fetchAndGetFormData[WorthWhenGiftedModel](keystoreKeys.worthWhenGifted).map {
      case Some(data) => Ok(views.worthWhenGifted(worthWhenGiftedForm.fill(data), backLink, homeLink, postAction))
      case _ => Ok(views.worthWhenGifted(worthWhenGiftedForm, backLink, homeLink, postAction))
    }
  }

  val submitWorthWhenGifted = ValidateSession.async { implicit request =>
    val backLink = Some(controllers.routes.GainController.howBecameOwner().url)
    val postAction = controllers.routes.GainController.submitWorthWhenGifted()

    worthWhenGiftedForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenGifted(errors, backLink, homeLink, postAction))),
      success => {
        calcConnector.saveFormData(keystoreKeys.worthWhenGifted, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Worth When Bought For Less Actions ########################

  val worthWhenBoughtForLess = ValidateSession.async {implicit request =>
    calcConnector.fetchAndGetFormData[WorthWhenBoughtForLessModel](keystoreKeys.worthWhenBoughtForLess).map {
      case Some(data) => Ok(views.worthWhenBoughtForLess(worthWhenBoughtForLessForm.fill(data)))
      case _ => Ok(views.worthWhenBoughtForLess(worthWhenBoughtForLessForm))
    }
  }

  val submitWorthWhenBoughtForLess = ValidateSession.async { implicit request =>
    worthWhenBoughtForLessForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenBoughtForLess(errors))),
      success => {
        calcConnector.saveFormData(keystoreKeys.worthWhenBoughtForLess, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Acquisition Value Actions ########################
  val acquisitionValue = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionValueModel](keystoreKeys.acquisitionValue).map {
      case Some(data) => Ok(views.acquisitionValue(acquisitionValueForm.fill(data)))
      case None => Ok(views.acquisitionValue(acquisitionValueForm))
    }
  }

  val submitAcquisitionValue = ValidateSession.async { implicit request =>
    acquisitionValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.acquisitionValue(errors))),
      success => {
        calcConnector.saveFormData(keystoreKeys.acquisitionValue, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Acquisition Costs Actions ########################

  private def acquisitionCostsBackLink()(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val ownerOn = calcConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
    val howBecameOwner = calcConnector.fetchAndGetFormData[HowBecameOwnerModel](keystoreKeys.howBecameOwner)
    val boughtForLess = calcConnector.fetchAndGetFormData[BoughtForLessThanWorthModel](keystoreKeys.boughtForLessThanWorth)

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
    calcConnector.fetchAndGetFormData[AcquisitionCostsModel](keystoreKeys.acquisitionCosts).map {
      case Some(data) => acquisitionCostsForm.fill(data)
      case None => acquisitionCostsForm
    }
  }

  val acquisitionCosts = ValidateSession.async { implicit request =>
    for {
      backLink <- acquisitionCostsBackLink
      form <- createAcquisitionCostsForm()
    } yield Ok(views.acquisitionCosts(form, backLink))
  }

  val submitAcquisitionCosts = ValidateSession.async { implicit request =>
    acquisitionCostsForm.bindFromRequest.fold(
      errors => for {
        backLink <- acquisitionCostsBackLink
      } yield BadRequest(views.acquisitionCosts(errors, backLink)),
      success => {
        calcConnector.saveFormData(keystoreKeys.acquisitionCosts, success)
        Future.successful(Redirect(routes.GainController.improvements()))}
    )
  }

  //################# Improvements Actions ########################
  private def getOwnerBeforeAprilNineteenEightyTwo()(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
      .map(_.get.ownedBeforeLegislationStart)
  }

  private def getImprovementsForm()(implicit hc: HeaderCarrier): Future[Form[ImprovementsModel]] = {
    calcConnector.fetchAndGetFormData[ImprovementsModel](keystoreKeys.improvements).map {
      case Some(data) => improvementsForm.fill(data)
      case _ => improvementsForm
    }
  }

  val improvements = ValidateSession.async { implicit request =>
    for{
      ownerBeforeAprilNineteenEightyTwo <- getOwnerBeforeAprilNineteenEightyTwo()
      improvementsForm <- getImprovementsForm
    } yield Ok(views.improvements(improvementsForm, ownerBeforeAprilNineteenEightyTwo))
  }

  val submitImprovements = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal): Future[Result] = {
      if (totalGain > 0) Future.successful(Redirect(routes.DeductionsController.propertyLivedIn()))
      else Future.successful(Redirect(routes.SummaryController.summary()))
    }

    def errorAction(form: Form[ImprovementsModel]): Future[Result] = {
      getOwnerBeforeAprilNineteenEightyTwo().map(ownerBeforeAprilNineteenEightyTwo =>
        BadRequest(views.improvements(form, ownerBeforeAprilNineteenEightyTwo))
      )
    }

    def successAction(model: ImprovementsModel): Future[Result] = {
      for {
        save <- calcConnector.saveFormData(keystoreKeys.improvements, model)
        answers <- calcConnector.getPropertyGainAnswers
        grossGain <- calcConnector.calculateRttPropertyGrossGain(answers)
        route <- routeRequest(grossGain)
      } yield route
    }

    improvementsForm.bindFromRequest.fold(errorAction, successAction)
  }
}