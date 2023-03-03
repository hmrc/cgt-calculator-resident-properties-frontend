/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time._

import common.Dates._
import common.KeystoreKeys
import config.AppConfig
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import javax.inject.{Singleton, Inject}
import models.resident.DisposalDateModel
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.resident.properties.whatNext._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatNextSAController @Inject()(
                                      val calcConnector: CalculatorConnector,
                                      val sessionCacheConnector: SessionCacheConnector,
                                      val messagesControllerComponents: MessagesControllerComponents,
                                      whatNextSAFourTimesAEAView: whatNextSAFourTimesAEA,
                                      whatNextSaNoGainView: whatNextSaNoGain,
                                      whatNextSaGainView: whatNextSaGain,
                                      val appConfig: AppConfig
                                    ) extends FrontendController(messagesControllerComponents) with ValidActiveSession with I18nSupport {

  implicit val ec: ExecutionContext = messagesControllerComponents.executionContext

  lazy val backLink: String = routes.SaUserController.saUser.url
  lazy val iFormUrl: String = appConfig.residentIFormUrl

  override lazy val homeLink: String = controllers.routes.PropertiesController.introduction.url
  override lazy val sessionTimeoutUrl: String = homeLink

  def fetchAndParseDateToLocalDate()(implicit hc: HeaderCarrier): Future[LocalDate] = {
    sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.ResidentPropertyKeys.disposalDate).map {
      data => LocalDate.of(data.get.year, data.get.month, data.get.day)
    }
  }

  val whatNextSAOverFourTimesAEA: Action[AnyContent] = ValidateSession.async { implicit request =>
    Future.successful(Ok(whatNextSAFourTimesAEAView(backLink)))
  }

  val whatNextSANoGain: Action[AnyContent] = ValidateSession.async { implicit request =>
    fetchAndParseDateToLocalDate().map {
      date => Ok(whatNextSaNoGainView(backLink, iFormUrl, taxYearOfDateLongHand(date)))
    }.recoverToStart(homeLink, sessionTimeoutUrl)
  }

  val whatNextSAGain: Action[AnyContent] = ValidateSession.async { implicit request =>
    fetchAndParseDateToLocalDate().map {
      date => Ok(whatNextSaGainView(backLink, iFormUrl, taxYearOfDateLongHand(date)))
    }.recoverToStart(homeLink, sessionTimeoutUrl)
  }
}
