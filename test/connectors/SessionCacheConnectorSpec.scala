/*
 * Copyright 2022 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.shaded.ahc.org.asynchttpclient.exception.RemotelyClosedException
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import common.CommonPlaySpec

import scala.concurrent.Future

class SessionCacheConnectorSpec extends CommonPlaySpec with MockitoSugar{
  val mockSessionCache = mock[SessionCache]

  val mockSessionCacheConnector = new SessionCacheConnector {
    override val sessionCache: SessionCache = mockSessionCache
    override val homeLink: String = "testString"
  }

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

  val testCache: CacheMap = CacheMap.apply("testId", Map("someString" -> Json.toJson[String]("someJsValue")))
  val testHttpResponse: AnyRef with HttpResponse = HttpResponse.apply(200, "")

  "calling saveFormData" should{
    "return a Future.Successful CacheMap" in{
      when(mockSessionCache.cache(ArgumentMatchers.eq("testKey"), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(testCache)

      val result = mockSessionCacheConnector.saveFormData("testKey", "testData")
      await(result) shouldBe testCache
    }

    "throw an exception" in{
      when(mockSessionCache.cache(ArgumentMatchers.eq("testFailingKey"), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(Future.failed(new Exception("Keystore failed to save data: failData to this key: testFailingKey with message: test")))

      the[Exception] thrownBy await(mockSessionCacheConnector.saveFormData("testFailingKey", "testData")) should have message
        "Keystore failed to save data: failData to this key: testFailingKey with message: test"
    }
  }

  "calling fetchAndGetFormData" should{
    "return a Future.Successful Option[T]" in{
      when(mockSessionCache.fetchAndGetEntry[String](ArgumentMatchers.eq("testKey"))(ArgumentMatchers.any(), ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(Future.successful[Option[String]](Some("String")))

      val result = mockSessionCacheConnector.fetchAndGetFormData[String]("testKey")
      await(result) shouldBe Some("String")
    }

    "throw an exception" in{
      when(mockSessionCache.fetchAndGetEntry(ArgumentMatchers.eq("testFailingKey"))(ArgumentMatchers.any(), ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(Future.failed(RemotelyClosedException.INSTANCE))

      the[RemotelyClosedException] thrownBy await(mockSessionCacheConnector.fetchAndGetFormData[String]("testFailingKey")) should have message
        "Remotely closed"
    }
  }

  "calling clearKeystore" should{
    "clear the cache and return a 200(OK)" in{
      when(mockSessionCache.remove()(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful[HttpResponse](testHttpResponse))

      val result = mockSessionCacheConnector.clearKeystore
      await(result).status shouldBe 200
    }
  }
}
