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

package controllers

import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest

class TimeoutControllerSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with CommonMocks with MockitoSugar {
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)
  val homeLink = controllers.routes.PropertiesController.introduction.url

  class fakeRequestTo(url : String, controllerAction : Action[AnyContent]) {
    val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/" + url)
    val result = controllerAction(fakeRequest)
    val jsoupDoc = Jsoup.parse(bodyOf(result))
  }

  lazy val timeoutController = new TimeoutController(
    mockMessagesControllerComponents,
    fakeApplication.injector.instanceOf[views.html.warnings.sessionTimeout])

  "TimeoutController.timeout" should {
    "when called with no session" should {
      object timeoutTestDataItem extends fakeRequestTo("", timeoutController.timeout())

      implicit lazy val messages: Messages = mockMessagesControllerComponents.messagesApi.preferred(fakeRequest).messages

      "return a 200" in {
        status(timeoutTestDataItem.result) shouldBe 200
      }

      s"have the home link too test2" in {
        timeoutTestDataItem.jsoupDoc.select("body > header > section > div > div > span.govuk-service-navigation__service-name > a").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/"
      }

      "have the title" in {
        timeoutTestDataItem.jsoupDoc.title shouldEqual s"${Messages("session.timeout.message")} - Calculate your Capital Gains Tax - GOV.UK"
      }

      "contain the heading 'Your session has timed out." in {
        timeoutTestDataItem.jsoupDoc.select("h1").text shouldEqual Messages("session.timeout.message")
      }

      "have a restart link to href of 'test'" in {
        timeoutTestDataItem.jsoupDoc.getElementById("startAgain").attr("href") shouldEqual homeLink
      }
    }
  }
}
