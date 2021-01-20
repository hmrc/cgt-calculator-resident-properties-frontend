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

package views.resident.properties

import assets.MessageLookup.{IntroductionView => messages, Resident => commonMessages}
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.{properties => views}

class IntroductionViewSpec extends UnitSpec with WithFakeApplication with BaseViewSpec {

  "Introduction view" should {

    lazy val view = views.introduction()(fakeRequest, testingMessages, mockAppConfig)
    lazy val doc = Jsoup.parse(view.body).select("article.content__body")

    "have the correct title" in {
      doc.select("h1").text shouldBe messages.title
    }

    "have the correct sub-heading" in {
      doc.select("h2").text.trim shouldBe messages.subheading
    }

    "have the correct paragraph text" in {
      doc.select("p:nth-of-type(1)").text.trim shouldBe messages.paragraph
    }

    "have a hyperlink to GOV.UK that" should {

      lazy val hyperlink = doc.select("a:nth-of-type(1)")

      "have the correct text" in {
        hyperlink.text.trim shouldBe messages.entitledLinkText
      }

      "link to the correct GOV.UK page" in {
        hyperlink.attr("href") shouldBe "https://www.gov.uk/tax-relief-selling-home"
      }
    }
    "the link should have a set of attributes" which {

      "has the external link class" in {
        doc.select("#privateResidenceReliefStartPageLink").hasClass("external-link") shouldEqual true
      }

      "has the attribute rel" in {
        doc.select("#privateResidenceReliefStartPageLink").hasAttr("rel") shouldEqual true
      }

      "rel has the value of external" in {
        doc.select("#privateResidenceReliefStartPageLink").attr("rel") shouldEqual "external"
      }

      "has a target attribute" in {
        doc.select("#privateResidenceReliefStartPageLink").hasAttr("target") shouldEqual true
      }

      "has a target value of _blank" in {
        doc.select("#privateResidenceReliefStartPageLink").attr("target") shouldEqual "_blank"
      }
    }

    "record GA statistics" which {
      "has a data-journey-click attribute" in {
        doc.select("#privateResidenceReliefStartPageLink").hasAttr("data-journey-click") shouldEqual true
      }

      "with the GA value of help:govUK:rtt-properties-privateResidenceReliefStartPageLink" in {
        doc.select("#privateResidenceReliefStartPageLink").attr("data-journey-click") shouldEqual "help:govUK:rtt-properties-privateResidenceReliefStartPageLink"
      }
    }

    "have the correct continuation instructions" in {
      doc.select("p:nth-of-type(2)").text.trim shouldBe messages.continuationInstructions
    }

    "have a continue link that" should {

      lazy val hyperlink = doc.select("a:nth-of-type(2)")

      "have the correct text" in {
        hyperlink.text.trim shouldBe commonMessages.continue
      }

      "take the user to disposal date page" in {
        hyperlink.attr("href") shouldBe controllers.routes.GainController.disposalDate().url
      }

      "have the id continue-button" in {
        hyperlink.attr("id") shouldBe "continue-button"
      }
    }

  }
}
