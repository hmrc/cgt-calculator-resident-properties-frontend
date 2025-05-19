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

package controllers.resident.properties

import config.ApplicationConfig
import controllers.FeedbackSurveyController
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{DefaultAwaitTimeout, FakeRequest}

import scala.concurrent.Future

class FeedbackSurveyControllerSpec
  extends AsyncWordSpec
    with Matchers
    with MockitoSugar
    with DefaultAwaitTimeout {

  "FeedbackSurveyController" should {

    "redirect to the exit survey with new session" in {
      val mockAppConfig = mock[ApplicationConfig]
      val mockMessagesControllerComponents = stubMessagesControllerComponents()

      when(mockAppConfig.signOutUrl).thenReturn("http://localhost:9553/bas-gateway/sign-out-without-state")
      when(mockAppConfig.feedbackSurvey).thenReturn("http://localhost:9514/feedback/CGT-RP")

      val controller = new FeedbackSurveyController(mockMessagesControllerComponents, mockAppConfig)

      val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/resident/properties/feedback-survey")
      val result: Future[Result] = controller.redirectExitSurvey.apply(fakeRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("http://localhost:9553/bas-gateway/sign-out-without-state?continue=http://localhost:9514/feedback/CGT-RP")
    }
  }
}