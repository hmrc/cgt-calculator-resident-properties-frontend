/*
 * Copyright 2019 HM Revenue & Customs
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

package config

import com.typesafe.config.Config
import javax.inject.Inject
import models.CGTClientException
import net.ceedubs.ficus.Ficus._
import play.api.Play.current
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND}
import play.api.i18n.MessagesApi
import play.api.mvc.Results.{BadRequest, NotFound}
import play.api.mvc.{Request, RequestHeader, Result}
import play.api.{Configuration, DefaultGlobal, Play}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import uk.gov.hmrc.play.config.ControllerConfig

import scala.concurrent.Future

class CgtErrorHandler @Inject()(val messagesApi: MessagesApi,
                                val configuration: Configuration) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html = {
    val url = """^(.*[\/])""".r findFirstIn request.path
    val homeNavLink = url match {
      case Some(path) if path == "/calculate-your-capital-gains/resident/properties/" =>
        controllers.routes.PropertiesController.introduction().url
      case Some(path) if path == "/calculate-your-capital-gains/resident/shares/" =>
        controllers.routes.GainController.disposalDate().url
      case _ => "/calculate-your-capital-gains/"
    }
    views.html.error_template(pageTitle, heading, message, homeNavLink)
  }

  val homeLink: String = controllers.routes.GainController.disposalDate().url

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    statusCode match {
      case BAD_REQUEST => Future.successful(BadRequest(badRequestTemplate(Request(request, ""))))
      case NOT_FOUND   => Future.successful(NotFound(notFoundTemplate(Request(request, ""))))
      case _           => DefaultGlobal.onError(request, CGTClientException(s"Client Error Occurred with Status $statusCode and message $message"))
    }
  }
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs: Config = Play.current.configuration.underlying.as[Config]("controllers")
}