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

package controllers.IncomeControllerSpec

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import assets.MessageLookup.{PersonalAllowance => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.helpers.{CommonMocks, FakeRequestHelper}
import controllers.{IncomeController, routes}
import models.resident.income.PersonalAllowanceModel
import models.resident.{DisposalDateModel, TaxYearModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import common.{CommonPlaySpec, WithCommonFakeApplication}
import views.html.calculation.resident.personalAllowance
import views.html.calculation.resident.properties.income.currentIncome

import scala.concurrent.Future

class PersonalAllowanceActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar with CommonMocks {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()

  def setupTarget(getData: Option[PersonalAllowanceModel],
                  maxPersonalAllowance: Option[BigDecimal] = Some(BigDecimal(11100)),
                  disposalDateModel: DisposalDateModel,
                  taxYearModel: TaxYearModel): IncomeController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockSessionCacheConnector = mock[SessionCacheConnector]

    when(mockSessionCacheConnector.fetchAndGetFormData[PersonalAllowanceModel](ArgumentMatchers.eq(keystoreKeys.personalAllowance))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.getPA(ArgumentMatchers.any(), ArgumentMatchers.eq(true))(ArgumentMatchers.any()))
          .thenReturn(Future.successful(maxPersonalAllowance))

    when(mockCalcConnector.getPA(ArgumentMatchers.any(), ArgumentMatchers.eq(false))(ArgumentMatchers.any()))
      .thenReturn(Future.successful(maxPersonalAllowance))

    when(mockSessionCacheConnector.saveFormData[PersonalAllowanceModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    when(mockSessionCacheConnector.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.eq(keystoreKeys.disposalDate))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(disposalDateModel)))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(taxYearModel)))

    new IncomeController(mockCalcConnector, mockSessionCacheConnector, mockMessagesControllerComponents,
      fakeApplication.injector.instanceOf[currentIncome],
      fakeApplication.injector.instanceOf[personalAllowance])
  }

  "Calling .personalAllowance from the IncomeController" when {

    "there is no keystore data" should {

      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val target = setupTarget(None, disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
      lazy val result = target.personalAllowance(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Personal Allowance view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.question("2015/16")
      }
    }

    "there is some keystore data" should {

      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2016/17", true, "2016/17")
      lazy val target = setupTarget(Some(PersonalAllowanceModel(1000)), disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
      lazy val result = target.personalAllowance(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Personal Allowance view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.question()
      }
    }
  }

  "request has an invalid session" should {

    lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
    lazy val taxYearModel = TaxYearModel("2016/17", false, "2017/18")
    lazy val target = setupTarget(None, disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
    lazy val result = target.personalAllowance(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/properties/session-timeout")
    }
  }

  "Calling .submitPersonalAllowance from the IncomeController" when {

    "a valid form is submitted" should {

      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val target = setupTarget(Some(PersonalAllowanceModel(1000)), disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
      lazy val result = target.submitPersonalAllowance(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the review your answers page" in {
        redirectLocation(result).get shouldBe routes.ReviewAnswersController.reviewFinalAnswers().url
      }
    }

    "an invalid form is submitted" should {

      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val target = setupTarget(None, disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitPersonalAllowance(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the personal allowance page" in {
        doc.title() shouldEqual messages.question("2015/16")
      }
    }
  }
}
