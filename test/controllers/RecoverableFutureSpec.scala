/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.utils

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.mvc.Result
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecoverableFutureSpec extends AnyWordSpec with ScalaFutures with Matchers with IntegrationPatience with Status {

  ".recoverToStart" should {
    "convert a `NoSuchElementException` into an `ApplicationException`" in {


      val future: Future[Result] = Future.failed(new NoSuchElementException("test message")).recoverToStart()
      val url = controllers.routes.TimeoutController.timeout().url

      whenReady(future.failed) {
        case ApplicationException(result, message) =>
          result.header.headers should contain("Location" -> url)
          result.header.status shouldBe SEE_OTHER
          message should equal("cgt-calculator-resident-properties-frontendtest message")
        case e => throw e
      }
    }

    "not convert any other exception into an `ApplicationException`" in {

      val ex = new IllegalArgumentException("test message")

      val future: Future[Result] = Future.failed(ex).recoverToStart()

      whenReady(future.failed) {
        _ shouldBe ex
      }
    }
  }
}
