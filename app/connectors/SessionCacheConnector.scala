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

package connectors

import config.CalculatorSessionCache
import javax.inject.Inject
import play.api.Logging
import play.api.libs.json.Format
import play.shaded.ahc.org.asynchttpclient.exception.RemotelyClosedException
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionCacheConnectorImpl @Inject()(val sessionCache: CalculatorSessionCache) extends SessionCacheConnector {
  lazy val homeLink: String = controllers.routes.GainController.disposalDate.url
}

trait SessionCacheConnector extends Logging{
  val sessionCache: SessionCache
  val homeLink: String

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

  def saveFormData[T](key: String, data: T)(implicit hc: HeaderCarrier, formats: Format[T]): Future[CacheMap] = {
    sessionCache.cache(key, data) recoverWith {
      case e: Exception => logger.warn(s"Keystore failed to save data: $data to this key: $key with message: ${e.getMessage}", e)
        throw e
    }
  }

  def fetchAndGetFormData[T](key: String)(implicit hc: HeaderCarrier, formats: Format[T]): Future[Option[T]] = {
    sessionCache.fetchAndGetEntry(key) recoverWith {
      case e: RemotelyClosedException => logger.warn(s"Remotely closed exception from keystore on fetch: ${e.getMessage}", e)
        throw e
    }
  }

  def clearKeystore(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    sessionCache.remove()
  }
}