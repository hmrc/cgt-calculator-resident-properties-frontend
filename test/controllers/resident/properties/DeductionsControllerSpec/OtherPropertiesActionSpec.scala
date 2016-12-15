/*
 * Copyright 2016 HM Revenue & Customs
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

package controllers.DeductionsControllerSpec

import assets.MessageLookup.{OtherProperties => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import config.AppConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.DeductionsController
import models.resident.PrivateResidenceReliefModel
import models.resident.properties.{LettingsReliefModel, PropertyLivedInModel}
import models.resident.{DisposalDateModel, OtherPropertiesModel, TaxYearModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class OtherPropertiesActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[OtherPropertiesModel],
                  disposalDate: Option[DisposalDateModel],
                  taxYear: Option[TaxYearModel],
                  propertyLivedInModel: Option[PropertyLivedInModel] = Some(PropertyLivedInModel(false)),
                  privateResidenceReliefModel: Option[PrivateResidenceReliefModel] = None,
                  lettingsReliefModel: Option[LettingsReliefModel] = None
                 ): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAppConfig = mock[AppConfig]

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](ArgumentMatchers.eq(keystoreKeys.otherProperties))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
    .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.eq(keystoreKeys.disposalDate))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(disposalDate)

    when(mockCalcConnector.fetchAndGetFormData[PropertyLivedInModel](ArgumentMatchers.eq(keystoreKeys.propertyLivedIn))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(propertyLivedInModel)

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](ArgumentMatchers.eq(keystoreKeys.privateResidenceRelief))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(privateResidenceReliefModel)

    when(mockCalcConnector.fetchAndGetFormData[LettingsReliefModel](ArgumentMatchers.eq(keystoreKeys.lettingsRelief))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(lettingsReliefModel)

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(taxYear)

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config = mockAppConfig
    }
  }

  "Calling .otherProperties from the DeductionsController" when {
    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None,
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.otherProperties(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"have a title of ${messages.title("2015/16")}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title("2015/16")
      }
    }

    "request has no session" should {

      lazy val target = setupTarget(None,
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.otherProperties(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }
    }

    "the property was not lived in" should {
      lazy val target = setupTarget(
        None,
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.otherProperties(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))
      lazy val backLink = doc.select("a#back-link")

      "have a back link to the Property Lived In page" in {
        backLink.attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/property-lived-in"
      }
    }

    "the property was lived in but without prr" should {
      lazy val target = setupTarget(
        None,
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")),
        Some(PropertyLivedInModel(true)),
        Some(PrivateResidenceReliefModel(false)))
      lazy val result = target.otherProperties(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))
      lazy val backLink = doc.select("a#back-link")

      "have a back link to the Private Residence Relief page" in {
        backLink.attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/private-residence-relief"
      }
    }

    "prr was claimed with no lettings relief" should {
      lazy val target = setupTarget(
        None,
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")),
        Some(PropertyLivedInModel(true)),
        Some(PrivateResidenceReliefModel(true)),
        Some(LettingsReliefModel(false)))
      lazy val result = target.otherProperties(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))
      lazy val backLink = doc.select("a#back-link")

      "have a back link to the Lettings Relief page" in {
        backLink.attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/lettings-relief"
      }
    }

    "prr was claimed with  lettings relief" should {
      lazy val target = setupTarget(
        None,
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")),
        Some(PropertyLivedInModel(true)),
        Some(PrivateResidenceReliefModel(true)),
        Some(LettingsReliefModel(true)))
      lazy val result = target.otherProperties(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))
      lazy val backLink = doc.select("a#back-link")

      "have a back link to the Lettings Relief page" in {
        backLink.attr("href") shouldBe "/calculate-your-capital-gains/resident/properties/lettings-relief-value"
      }
    }
  }

  "Calling .submitOtherProperties from the DeductionsController" when {
    "a valid form 'Yes' is submitted" should {

      lazy val target = setupTarget(None,
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("hasOtherProperties", "Yes"))
      lazy val result = target.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the allowable losses page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/allowable-losses")
      }
    }

    "a valid form 'No' is submitted" should {

      lazy val target = setupTarget(None,
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("hasOtherProperties", "No"))
      lazy val result = target.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the allowable losses page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/losses-brought-forward")
      }
    }

    "an invalid form is submitted" should {

      lazy val target = setupTarget(None,
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("hasOtherProperties", ""))
      lazy val result = target.submitOtherProperties(request)

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the other properties page" in {
        Jsoup.parse(bodyOf(result)).title() shouldEqual messages.title("2015/16")
      }
    }
  }
}
