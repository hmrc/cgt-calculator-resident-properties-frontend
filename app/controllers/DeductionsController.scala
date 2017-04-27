/*
 * Copyright 2017 HM Revenue & Customs
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
import config.{AppConfig, ApplicationConfig}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.resident.AllowableLossesForm._
import forms.resident.AllowableLossesValueForm._
import forms.resident.AnnualExemptAmountForm._
import forms.resident.LossesBroughtForwardForm._
import forms.resident.LossesBroughtForwardValueForm._
import forms.resident.OtherPropertiesForm._
import forms.resident.properties.LettingsReliefForm._
import forms.resident.properties.LettingsReliefValueForm._
import forms.resident.properties.PrivateResidenceReliefForm._
import forms.resident.properties.PrivateResidenceReliefValueForm._
import forms.resident.properties.PropertyLivedInForm._
import models.resident.properties._
import models.resident.{AllowableLossesValueModel, _}
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Result
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.{resident => commonViews}
import views.html.calculation.resident.properties.{deductions => views}

import scala.concurrent.Future

object DeductionsController extends DeductionsController {
  val calcConnector = CalculatorConnector
  val config = ApplicationConfig
}

trait DeductionsController extends ValidActiveSession {

  val calcConnector: CalculatorConnector
  val config: AppConfig

  val navTitle = Messages("calc.base.resident.properties.home")

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  def totalGain(answerSummary: YourAnswersSummaryModel, hc: HeaderCarrier): Future[BigDecimal] = calcConnector.calculateRttPropertyGrossGain(answerSummary)(hc)

  def answerSummary(hc: HeaderCarrier): Future[YourAnswersSummaryModel] = calcConnector.getPropertyGainAnswers(hc)

  override val homeLink = controllers.routes.PropertiesController.introduction().url
  override val sessionTimeoutUrl = homeLink


  //################# Property Lived In Actions #############################
  val propertyLivedIn = ValidateSession.async { implicit request =>

    val backLink = Some(controllers.routes.GainController.improvements().toString)

    calcConnector.fetchAndGetFormData[PropertyLivedInModel](keystoreKeys.propertyLivedIn).map {
      case Some(data) => Ok(commonViews.properties.deductions.propertyLivedIn(propertyLivedInForm.fill(data), homeLink, backLink))
      case _ => Ok(commonViews.properties.deductions.propertyLivedIn(propertyLivedInForm, homeLink, backLink))
    }
  }

  val submitPropertyLivedIn = ValidateSession.async { implicit request =>

    lazy val backLink = Some(controllers.GainController.improvements.toString())

    def errorAction(errors: Form[PropertyLivedInModel]) = Future.successful(BadRequest(commonViews.properties.deductions.propertyLivedIn(
      errors, homeLink, backLink
    )))

    def routeRequest(model: PropertyLivedInModel) = {
      if (model.livedInProperty) Future.successful(Redirect(routes.DeductionsController.privateResidenceRelief()))
      else Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
    }

    def successAction(model: PropertyLivedInModel) = {
      for {
        save <- calcConnector.saveFormData(keystoreKeys.propertyLivedIn, model)
        route <- routeRequest(model)
      } yield route
    }

    propertyLivedInForm.bindFromRequest().fold(errorAction, successAction)
  }


  //########## Private Residence Relief Actions ##############
  val privateResidenceRelief = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](keystoreKeys.privateResidenceRelief).map {
      case Some(data) => Ok(views.privateResidenceRelief(privateResidenceReliefForm.fill(data)))
      case _ => Ok(views.privateResidenceRelief(privateResidenceReliefForm))
    }
  }

  val submitPrivateResidenceRelief = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[PrivateResidenceReliefModel]) = Future.successful(BadRequest(views.privateResidenceRelief(errors)))

    def routeRequest(model: PrivateResidenceReliefModel) = {
      if (model.isClaiming) Future.successful(Redirect(routes.DeductionsController.privateResidenceReliefValue()))
      else Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
    }

    def successAction(model: PrivateResidenceReliefModel) = {
      for {
        save <- calcConnector.saveFormData(keystoreKeys.privateResidenceRelief, model)
        route <- routeRequest(model)
      } yield route
    }

    privateResidenceReliefForm.bindFromRequest().fold(errorAction, successAction)
  }


  //########## Private Residence Relief Value Actions ##############
  val privateResidenceReliefValue = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal) = {
      calcConnector.fetchAndGetFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue).map {
        case Some(data) => Ok(views.privateResidenceReliefValue(privateResidenceReliefValueForm(totalGain).fill(data), homeLink, totalGain))
        case None => Ok(views.privateResidenceReliefValue(privateResidenceReliefValueForm(totalGain), homeLink, totalGain))
      }
    }

    for {
      answerSummary <- answerSummary(hc)
      totalGain <- totalGain(answerSummary, hc)
      route <- routeRequest(totalGain)
    } yield route
  }

  val submitPrivateResidenceReliefValue = ValidateSession.async { implicit request =>

    def successAction(model: PrivateResidenceReliefValueModel) = {
      calcConnector.saveFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue, model)
      Future.successful(Redirect(routes.DeductionsController.lettingsRelief()))
    }

    def routeRequest(gain: BigDecimal): Future[Result] = {
      privateResidenceReliefValueForm(gain).bindFromRequest.fold(
        errors => Future.successful(BadRequest(views.privateResidenceReliefValue(errors, homeLink, gain))),
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
  private val lettingsReliefBackUrl = routes.DeductionsController.privateResidenceReliefValue().url

  val lettingsRelief = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[LettingsReliefModel](keystoreKeys.lettingsRelief).map {
      case Some(data) => Ok(views.lettingsRelief(lettingsReliefForm.fill(data), homeLink, Some(lettingsReliefBackUrl)))
      case None => Ok(views.lettingsRelief(lettingsReliefForm, homeLink, Some(lettingsReliefBackUrl)))
    }
  }

  val submitLettingsRelief = ValidateSession.async { implicit request =>

    def errorAction(form: Form[LettingsReliefModel]) = {
      Future.successful(BadRequest(views.lettingsRelief(form, homeLink, Some(lettingsReliefBackUrl))))
    }

    def routeRequest(model: LettingsReliefModel) = {
      model match {
        case LettingsReliefModel(true) => Future.successful(Redirect(routes.DeductionsController.lettingsReliefValue()))
        case _ => Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
      }
    }

    def successAction(model: LettingsReliefModel) = {
      for {
        save <- calcConnector.saveFormData[LettingsReliefModel](keystoreKeys.lettingsRelief, model)
        route <- routeRequest(model)
      } yield route
    }

    lettingsReliefForm.bindFromRequest().fold(
      errors => errorAction(errors),
      success => successAction(success)
    )
  }


  //################# Lettings Relief Value Input Actions ########################
  val lettingsReliefValue = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal, prrValue: BigDecimal): Future[Result] = {
      calcConnector.fetchAndGetFormData[LettingsReliefValueModel](keystoreKeys.lettingsReliefValue).map {
        case Some(data) => Ok(views.lettingsReliefValue(lettingsReliefValueForm(totalGain, prrValue).fill(data), homeLink, totalGain))
        case None => Ok(views.lettingsReliefValue(lettingsReliefValueForm(totalGain, prrValue), homeLink, totalGain))
      }
    }

    for {
      answerSummary <- answerSummary(hc)
      totalGain <- totalGain(answerSummary, hc)
      prrValue <- calcConnector.fetchAndGetFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue)
      route <- routeRequest(totalGain, prrValue.fold(BigDecimal(0))(_.amount))
    } yield route
  }

  val submitLettingsReliefValue = ValidateSession.async { implicit request =>

    def routeRequest(totalGain: BigDecimal, prrValue: BigDecimal) = {
      lettingsReliefValueForm(totalGain, prrValue).bindFromRequest().fold(
        errors => Future.successful(BadRequest(views.lettingsReliefValue(errors, homeLink, totalGain))),
        success => {
          calcConnector.saveFormData[LettingsReliefValueModel](keystoreKeys.lettingsReliefValue, success)
          Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
        })
    }

    for {
      answerSummary <- answerSummary(hc)
      totalGain <- totalGain(answerSummary, hc)
      prrValue <- calcConnector.fetchAndGetFormData[PrivateResidenceReliefValueModel](keystoreKeys.prrValue)
      route <- routeRequest(totalGain, prrValue.fold(BigDecimal(0))(_.amount))
    } yield route
  }


  private def otherPropertiesBackUrl()(implicit hc: HeaderCarrier): Future[String] = {
    for {
      livedInProperty <- calcConnector.fetchAndGetFormData[PropertyLivedInModel](keystoreKeys.propertyLivedIn)
      privateResidenceRelief <- calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](keystoreKeys.privateResidenceRelief)
      lettingsRelief <- calcConnector.fetchAndGetFormData[LettingsReliefModel](keystoreKeys.lettingsRelief)
      backUrl <- otherPropertiesData(livedInProperty, privateResidenceRelief, lettingsRelief)
    } yield backUrl
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

  val otherProperties = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[OtherPropertiesModel](keystoreKeys.otherProperties).map {
        case Some(data) => Ok(views.otherProperties(otherPropertiesForm.fill(data), backUrl, taxYear))
        case None => Ok(views.otherProperties(otherPropertiesForm, backUrl, taxYear))
      }
    }

    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      backUrl <- otherPropertiesBackUrl()
      finalResult <- routeRequest(backUrl, taxYear.get)
    } yield finalResult
  }

  val submitOtherProperties = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, taxYearModel: TaxYearModel): Future[Result] = {
      otherPropertiesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(views.otherProperties(errors, backUrl, taxYearModel))),
        success => {
          calcConnector.saveFormData[OtherPropertiesModel](keystoreKeys.otherProperties, success)
          if (success.hasOtherProperties) {
            Future.successful(Redirect(routes.DeductionsController.allowableLosses()))
          } else {
            Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
          }
        }
      )
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      backUrl <- otherPropertiesBackUrl()
      route <- routeRequest(backUrl, taxYear.get)
    } yield route
  }


  //################# Allowable Losses Actions #########################
  val allowableLosses = ValidateSession.async { implicit request =>

    val postAction = controllers.routes.DeductionsController.submitAllowableLosses()
    val backLink = Some(controllers.routes.DeductionsController.otherProperties().toString())

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[AllowableLossesModel](keystoreKeys.allowableLosses).map {
        case Some(data) => Ok(commonViews.allowableLosses(allowableLossesForm.fill(data), taxYear, postAction, backLink, homeLink, navTitle))
        case None => Ok(commonViews.allowableLosses(allowableLossesForm, taxYear, postAction, backLink, homeLink, navTitle))
      }
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(taxYear.get)
    } yield finalResult
  }

  val submitAllowableLosses = ValidateSession.async { implicit request =>

    val postAction = controllers.routes.DeductionsController.submitAllowableLosses()
    val backLink = Some(controllers.routes.DeductionsController.otherProperties().toString())

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      allowableLossesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.allowableLosses(errors, taxYear, postAction, backLink, homeLink, navTitle))),
        success => {
          calcConnector.saveFormData[AllowableLossesModel](keystoreKeys.allowableLosses, success)
          if (success.isClaiming) {
            Future.successful(Redirect(routes.DeductionsController.allowableLossesValue()))
          }
          else {
            Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
          }
        }
      )
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(taxYear.get)
    } yield finalResult
  }


  //################# Allowable Losses Value Actions ############################
  private val allowableLossesValuePostAction = controllers.routes.DeductionsController.submitAllowableLossesValue()
  private val allowableLossesValueBackLink = Some(controllers.routes.DeductionsController.allowableLosses().toString)

  val allowableLossesValue = ValidateSession.async { implicit request =>

    def fetchStoredAllowableLosses(): Future[Form[AllowableLossesValueModel]] = {
      calcConnector.fetchAndGetFormData[AllowableLossesValueModel](keystoreKeys.allowableLossesValue).map {
        case Some(data) => allowableLossesValueForm.fill(data)
        case _ => allowableLossesValueForm
      }
    }

    def routeRequest(taxYear: TaxYearModel, formData: Form[AllowableLossesValueModel]): Future[Result] = {
      Future.successful(Ok(commonViews.allowableLossesValue(formData, taxYear,
        homeLink,
        allowableLossesValuePostAction,
        allowableLossesValueBackLink,
        navTitle)))
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      formData <- fetchStoredAllowableLosses()
      finalResult <- routeRequest(taxYear.get, formData)
    } yield finalResult
  }

  val submitAllowableLossesValue = ValidateSession.async { implicit request =>

    def routeRequest(taxYearModel: TaxYearModel): Future[Result] = {
      allowableLossesValueForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.allowableLossesValue(errors, taxYearModel,
          homeLink,
          allowableLossesValuePostAction,
          allowableLossesValueBackLink,
          navTitle))),
        success => {
          calcConnector.saveFormData[AllowableLossesValueModel](keystoreKeys.allowableLossesValue, success)
          Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
        }
      )
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      route <- routeRequest(taxYear.get)
    } yield route
  }


  //################# Brought Forward Losses Actions ############################
  private val lossesBroughtForwardPostAction = controllers.routes.DeductionsController.submitLossesBroughtForward()

  def lossesBroughtForwardBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    for {
      livedInProperty <- calcConnector.fetchAndGetFormData[PropertyLivedInModel](keystoreKeys.propertyLivedIn)
      privateResidenceRelief <- calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](keystoreKeys.privateResidenceRelief)
      lettingsRelief <- calcConnector.fetchAndGetFormData[LettingsReliefModel](keystoreKeys.lettingsRelief)
      backUrl <- otherPropertiesData(livedInProperty, privateResidenceRelief, lettingsRelief)
    } yield backUrl
  }

  val lossesBroughtForward = ValidateSession.async { implicit request =>

    def routeRequest(backLinkUrl: String, taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
        case Some(data) => Ok(commonViews.lossesBroughtForward(lossesBroughtForwardForm.fill(data), lossesBroughtForwardPostAction,
          backLinkUrl, taxYear, homeLink, navTitle))
        case _ => Ok(commonViews.lossesBroughtForward(lossesBroughtForwardForm, lossesBroughtForwardPostAction, backLinkUrl,
          taxYear, homeLink, navTitle))
      }
    }

    for {
      backLinkUrl <- lossesBroughtForwardBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(backLinkUrl, taxYear.get)
    } yield finalResult

  }

  def positiveChargeableGainCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      gainAnswers <- calcConnector.getPropertyGainAnswers
      chargeableGainAnswers <- calcConnector.getPropertyDeductionAnswers
      chargeableGain <- calcConnector.calculateRttPropertyChargeableGain(gainAnswers, chargeableGainAnswers, 11000).map(_.get.chargeableGain)
    } yield chargeableGain

    match {
      case result if result.>(0) => true
      case _ => false
    }
  }

  val submitLossesBroughtForward = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, taxYearModel: TaxYearModel): Future[Result] = {
      lossesBroughtForwardForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.lossesBroughtForward(errors, lossesBroughtForwardPostAction, backUrl,
          taxYearModel, homeLink, navTitle))),
        success => {
          calcConnector.saveFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward, success)

          if (success.option) Future.successful(Redirect(routes.DeductionsController.lossesBroughtForwardValue()))
          else {
            positiveChargeableGainCheck.map { positiveChargeableGain =>
              if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome())
              else Redirect(routes.SummaryController.summary())
              //TODO: Update to CYA
            }
          }
        }
      )
    }

    for {
      backUrl <- lossesBroughtForwardBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      route <- routeRequest(backUrl, taxYear.get)
    } yield route

  }


  //################# Brought Forward Losses Value Actions ##############################
  private val lossesBroughtForwardValueBackLink = routes.DeductionsController.lossesBroughtForward().url
  private val lossesBroughtForwardValuePostAction = routes.DeductionsController.submitLossesBroughtForwardValue()

  val lossesBroughtForwardValue = ValidateSession.async { implicit request =>

    def retrieveKeystoreData(): Future[Form[LossesBroughtForwardValueModel]] = {
      calcConnector.fetchAndGetFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue).map {
        case Some(data) => lossesBroughtForwardValueForm.fill(data)
        case _ => lossesBroughtForwardValueForm
      }
    }

    def routeRequest(taxYear: TaxYearModel, formData: Form[LossesBroughtForwardValueModel]): Future[Result] = {
      Future.successful(Ok(commonViews.lossesBroughtForwardValue(
        formData,
        taxYear,
        navBackLink = lossesBroughtForwardValueBackLink,
        navHomeLink = homeLink,
        postAction = lossesBroughtForwardValuePostAction,
        navTitle = navTitle
      )))
    }

    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      formData <- retrieveKeystoreData()
      route <- routeRequest(taxYear.get, formData)
    } yield route
  }

  val submitLossesBroughtForwardValue = ValidateSession.async { implicit request =>

    lossesBroughtForwardValueForm.bindFromRequest.fold(
      errors => {
        for {
          disposalDate <- getDisposalDate
          disposalDateString <- formatDisposalDate(disposalDate.get)
          taxYear <- calcConnector.getTaxYear(disposalDateString)
        } yield {
          BadRequest(commonViews.lossesBroughtForwardValue(
            errors,
            taxYear.get,
            navBackLink = lossesBroughtForwardValueBackLink,
            navHomeLink = homeLink,
            postAction = lossesBroughtForwardValuePostAction,
            navTitle = navTitle))
        }
      },
      success => {
        calcConnector.saveFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue, success)
        positiveChargeableGainCheck.map { positiveChargeableGain =>
          if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome())
          else Redirect(routes.SummaryController.summary())
          //TODO: update to checkYourAnswers
        }
      }
    )
  }


  //################# Annual Exempt Amount Input Actions #############################
  private def annualExemptAmountBackLink(implicit hc: HeaderCarrier): Future[Option[String]] = calcConnector
    .fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
    case Some(LossesBroughtForwardModel(true)) =>
      Some(controllers.routes.DeductionsController.lossesBroughtForwardValue().toString)
    case _ =>
      Some(controllers.routes.DeductionsController.lossesBroughtForward().toString)
  }

  private val annualExemptAmountPostAction = controllers.routes.DeductionsController.submitAnnualExemptAmount()

  val annualExemptAmount = ValidateSession.async { implicit request =>

    def routeRequest(backLink: Option[String]) = {
      calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](keystoreKeys.annualExemptAmount).map {
        case Some(data) => Ok(commonViews.annualExemptAmount(annualExemptAmountForm().fill(data), backLink, annualExemptAmountPostAction,
          homeLink, JourneyKeys.properties, navTitle))
        case None => Ok(commonViews.annualExemptAmount(annualExemptAmountForm(), backLink, annualExemptAmountPostAction, homeLink,
          JourneyKeys.properties, navTitle))
      }
    }

    for {
      backLink <- annualExemptAmountBackLink(hc)
      result <- routeRequest(backLink)
    } yield result
  }

  def positiveAEACheck(model: AnnualExemptAmountModel)(implicit hc: HeaderCarrier): Future[Boolean] = {
    Future(model.amount > 0)
  }

  val submitAnnualExemptAmount = ValidateSession.async { implicit request =>

    def taxYearStringToInteger(taxYear: String): Future[Int] = {
      Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
    }

    def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
      Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
    }

    def getMaxAEA(taxYear: Int): Future[Option[BigDecimal]] = {
      calcConnector.getFullAEA(taxYear)
    }

    def routeRequest(maxAEA: BigDecimal, backLink: Option[String]): Future[Result] = {
      annualExemptAmountForm(maxAEA).bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.annualExemptAmount(errors, backLink, annualExemptAmountPostAction, homeLink,
          JourneyKeys.properties, navTitle))),
        success => {
          for {
            save <- calcConnector.saveFormData(keystoreKeys.annualExemptAmount, success)
            positiveAEA <- positiveAEACheck(success)
            positiveChargeableGain <- positiveChargeableGainCheck
          } yield (positiveAEA, positiveChargeableGain)

          match {
            case (false, true) => Redirect(routes.IncomeController.previousTaxableGains())
            case (_, false) => Redirect(routes.SummaryController.summary())
              //TODO: Update to checkYourAnswers
            case _ => Redirect(routes.IncomeController.currentIncome())
          }
        }
      )
    }
    for {
      disposalDate <- calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(year)
      backLink <- annualExemptAmountBackLink(hc)
      route <- routeRequest(maxAEA.get, backLink)
    } yield route
  }
}
