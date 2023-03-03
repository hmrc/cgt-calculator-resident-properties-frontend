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

package config

import javax.inject.Inject
import models.CGTClientException
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND}
import play.api.i18n.MessagesApi
import play.api.mvc.Results.{BadRequest, NotFound}
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.{ApplicationException, FrontendErrorHandler}

import scala.concurrent.Future

class CgtErrorHandler @Inject()(val messagesApi: MessagesApi,
                                errorTemplateView: views.html.error_template,
                                implicit val config: AppConfig) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html = {
    val url = """^(.*[\/])""".r findFirstIn request.path
    val homeNavLink = url match {
      case Some(path) if path == "/calculate-your-capital-gains/resident/properties/" =>
        controllers.routes.PropertiesController.introduction().url
      case Some(path) if path == "/calculate-your-capital-gains/resident/shares/" =>
        controllers.routes.GainController.disposalDate().url
      case _ => "/calculate-your-capital-gains/"
    }
    errorTemplateView(pageTitle, heading, message, homeNavLink)
  }

  lazy val homeLink: String = controllers.routes.GainController.disposalDate().url

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    statusCode match {
      case BAD_REQUEST => Future.successful(BadRequest(badRequestTemplate(Request(request, ""))))
      case NOT_FOUND   => Future.successful(NotFound(notFoundTemplate(Request(request, ""))))
      case _           => onServerError(request, CGTClientException(s"Client Error Occurred with Status $statusCode and message $message"))
    }
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case ApplicationException(result, _) =>
        Future.successful(result.withHeaders(CACHE_CONTROL -> "no-cache,no-store,max-age=0"))
      case e => Future.successful(resolveError(request, e))
    }
  }

}