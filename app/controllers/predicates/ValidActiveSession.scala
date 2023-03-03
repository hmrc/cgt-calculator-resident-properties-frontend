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

package controllers.predicates

import controllers.routes
import play.api.mvc._

import scala.concurrent.Future
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

trait ValidActiveSession extends FrontendController {

  lazy val homeLink: String = controllers.routes.GainController.disposalDate().url
  lazy val sessionTimeoutUrl: String = homeLink

  private type AsyncRequest = Request[AnyContent] => Future[Result]

  class ValidateSession {

    def async(action: AsyncRequest): Action[AnyContent] = {
      Action.async { implicit request =>
        if (request.session.get(SessionKeys.sessionId).isEmpty) {
          Future.successful(Redirect(routes.TimeoutController.timeout(sessionTimeoutUrl, homeLink)))
        } else {
          action(request)
        }
      }
    }
  }

  object ValidateSession extends ValidateSession()

}
