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

import config.AppConfig
import controllers.predicates.ValidActiveSession
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import scala.concurrent.ExecutionContext
import views.html.calculation.resident.properties.whatNext._

@Singleton
class WhatNextNonSaController @Inject()(
                                       val messagesControllerComponents: MessagesControllerComponents,
                                       val sessionCacheService: SessionCacheService,
                                       whatNextNonSaGainView: whatNextNonSaGain,
                                       whatNextNonSaLossView: whatNextNonSaLoss,
                                       val appConfig: AppConfig
                                       ) extends FrontendController(messagesControllerComponents) with ValidActiveSession with I18nSupport {

  implicit val ec: ExecutionContext = messagesControllerComponents.executionContext

  val whatNextNonSaGain: Action[AnyContent] = ValidateSession.async { implicit request =>
    for {
      selfAssessmentRequired <- sessionCacheService.shouldSelfAssessmentBeConsidered()
    } yield {
      selfAssessmentRequired match {
        case true => {
          Ok(whatNextNonSaGainView(appConfig.residentIFormUrl, controllers.routes.SaUserController.saUser.url))
        }
        case false => {
          Ok(whatNextNonSaGainView(appConfig.capitalGainsReportingFormUrl, controllers.routes.SummaryController.summary.url))
        }
      }
    }
  }

  val whatNextNonSaLoss: Action[AnyContent] = ValidateSession.async { implicit request =>
    for {
      selfAssessmentRequired <- sessionCacheService.shouldSelfAssessmentBeConsidered()
    } yield {
      selfAssessmentRequired match {
        case true => Ok(whatNextNonSaLossView(appConfig.residentIFormUrl, controllers.routes.SaUserController.saUser.url))
        case false => Ok(whatNextNonSaLossView(appConfig.capitalGainsReportingFormUrl, controllers.routes.SummaryController.summary.url))
      }
    }
  }
}
