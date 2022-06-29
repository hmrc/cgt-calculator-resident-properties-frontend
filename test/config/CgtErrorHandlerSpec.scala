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

package config

import org.scalatest.matchers.must.Matchers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import common.WithCommonFakeApplication
import org.scalatest.wordspec.AnyWordSpecLike

class CgtErrorHandlerSpec extends AnyWordSpecLike with WithCommonFakeApplication with Matchers {

  lazy val errorHandler: CgtErrorHandler = fakeApplication.injector.instanceOf[CgtErrorHandler]

  "Handle bad request" in {
    val response = errorHandler.onClientError(FakeRequest(), 400, "Bad Request")

    status(response) must equal(BAD_REQUEST)
  }

  "Handle not found" in {
    val response = errorHandler.onClientError(FakeRequest(), 404, "Not Found")

    status(response) must equal(NOT_FOUND)
  }

  "Handle internal server errors" in {
    val response = errorHandler.onClientError(FakeRequest(), 503, "Internal server error")

    status(response) must equal(INTERNAL_SERVER_ERROR)
  }
}
