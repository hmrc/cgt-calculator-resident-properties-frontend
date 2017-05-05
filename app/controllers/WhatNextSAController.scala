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
import common.KeystoreKeys
import connectors.CalculatorConnector
import models.resident.DisposalDateModel
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object WhatNextSAController extends WhatNextSAController {
  val calcConnector = CalculatorConnector
}

trait WhatNextSAController extends ValidActiveSession {

  val calcConnector: CalculatorConnector

  //TODO link to the correct page
  val backLink: String = "back-link"

  def fetchAndParseDate()(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[DisposalDateModel] (KeystoreKeys.ResidentPropertyKeys.disposalDate).map {
      case Some(data) => datePageFormatNoZero.format(constructDate(31, 1, data.year + 1))
      case _ => "Disposal Date Page Not Visited"
    }
  }

  val whatNextSAOverFourTimesAEA: Action[AnyContent] = Action.async { implicit request =>
    fetchAndParseDate() map {
      date => Ok(views.html.calculation.resident.properties.whatNext.whatNextSAFourTimesAEA("back-link", date))
    }
  }

  val whatNextSANoGain = TODO

  val whatNextSAGain = TODO

}
