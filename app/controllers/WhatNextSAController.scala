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

import controllers.predicates.ValidActiveSession
import play.api.mvc.{Action, AnyContent}
import common.Dates._
import java.time._
import controllers.utils.RecoverableFuture
import common.KeystoreKeys
import config.{AppConfig, ApplicationConfig}
import connectors.CalculatorConnector
import models.resident.DisposalDateModel
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object WhatNextSAController extends WhatNextSAController {
  val calcConnector = CalculatorConnector
  val appConfig = ApplicationConfig
}

trait WhatNextSAController extends ValidActiveSession {

  val calcConnector: CalculatorConnector
  val appConfig: AppConfig

  val backLink: String = routes.SaUserController.saUser().url
  lazy val iFormUrl: String = appConfig.residentIFormUrl

  override val homeLink: String = controllers.routes.PropertiesController.introduction().url
  override val sessionTimeoutUrl: String = homeLink

  def fetchAndParseDateToLocalDate()(implicit hc: HeaderCarrier): Future[LocalDate] = {
    calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.ResidentPropertyKeys.disposalDate).map {
      data => LocalDate.of(data.get.year, data.get.month, data.get.day)
    }
  }

  val whatNextSAOverFourTimesAEA: Action[AnyContent] = ValidateSession.async { implicit request =>
    Future.successful(Ok(views.html.calculation.resident.properties.whatNext.whatNextSAFourTimesAEA(backLink)))
  }

  val whatNextSANoGain: Action[AnyContent] = ValidateSession.async { implicit request =>
    fetchAndParseDateToLocalDate().map {
      date => Ok(views.html.calculation.resident.properties.whatNext.whatNextSaNoGain(backLink, iFormUrl, taxYearOfDateLongHand(date)))
    }.recoverToStart(homeLink, sessionTimeoutUrl)
  }

  val whatNextSAGain: Action[AnyContent] = ValidateSession.async { implicit request =>
    fetchAndParseDateToLocalDate().map {
      date => Ok(views.html.calculation.resident.properties.whatNext.whatNextSaGain(backLink, iFormUrl, taxYearOfDateLongHand(date)))
    }.recoverToStart(homeLink, sessionTimeoutUrl)
  }
}
