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

package controllers.resident.properties.DeductionsControllerSpec

import assets.MessageLookup.{LettingsRelief => messages, Resident => commonMessages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.DeductionsController
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import models.resident.properties.LettingsReliefModel
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._

import scala.concurrent.Future

class LettingsReliefActionSpec extends CommonPlaySpec with WithCommonFakeApplication
  with FakeRequestHelper with CommonMocks with MockitoSugar with DeductionsControllerBaseSpec {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)
  lazy val title = s"${messages.title} - ${commonMessages.homeText} - GOV.UK"

  def setupTarget(getData: Option[LettingsReliefModel]): DeductionsController = {
    when(mockSessionCacheService.fetchAndGetFormData[LettingsReliefModel](ArgumentMatchers.eq(keystoreKeys.lettingsRelief))
      (using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.saveFormData[LettingsReliefModel](ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    testingDeductionsController
  }

  "Calling .lettingsRelief from the resident DeductionsController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.lettingsRelief(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of $title" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual title
      }

      "have a back link to the PRR value page" in {
        doc.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(LettingsReliefModel(true)))
      lazy val result = target.lettingsRelief(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual title
      }

      "have a back link to PRR value page" in {
        doc.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }
  }

  "Calling .submitLettingsRelief from the DeductionsController" when {

    "a valid form 'Yes' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "Yes"))
      lazy val result = target.submitLettingsRelief(request.withMethod("POST"))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the lettings relief value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/lettings-relief-value")
      }
    }

    "a valid form 'No' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "No"))
      lazy val result = target.submitLettingsRelief(request.withMethod("POST"))

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the brought forward losses page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.DeductionsController.lossesBroughtForward.url)
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", ""))
      lazy val result = target.submitLettingsRelief(request.withMethod("POST"))
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the lettings-relief page" in {
        doc.title() shouldEqual s"Error: $title"
      }
    }
  }
}
