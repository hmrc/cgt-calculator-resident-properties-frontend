/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.PropertiesControllerSpec

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import assets.MessageLookup.{IntroductionView => messages}
import controllers.PropertiesController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers._
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.html.calculation.resident.properties.introduction

import scala.concurrent.Future

class IntroductionActionSpec extends CommonPlaySpec with MockitoSugar with FakeRequestHelper with CommonMocks with WithCommonFakeApplication {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()

  class Setup() {

    val controller = new PropertiesController(mockMessagesControllerComponents,
      fakeApplication.injector.instanceOf[introduction])
  }

  "Calling the introduction action" should {
    "return a status of 200" in new Setup() {
      lazy val result: Future[Result] = controller.introduction(fakeRequest)
      status(result) shouldBe 200
    }

    "return some html" in new Setup() {
      lazy val result: Future[Result] = controller.introduction(fakeRequest)
      contentType(result) shouldBe Some("text/html")
    }

    "display the introduction view" in new Setup() {
      lazy val result: Future[Result] = controller.introduction(fakeRequest)
      Jsoup.parse(bodyOf(result)).title shouldBe messages.title
    }
  }
}
