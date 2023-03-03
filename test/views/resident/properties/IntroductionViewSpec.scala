/*
 * Copyright 2023 HM Revenue & Customs
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
import common.{CommonPlaySpec,WithCommonFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.introduction

class IntroductionViewSpec extends CommonPlaySpec with WithCommonFakeApplication with BaseViewSpec {

  "Introduction view" should {
    lazy val introductionView = fakeApplication.injector.instanceOf[introduction]
    lazy val view = introductionView()(fakeRequest, testingMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title shouldBe messages.title
    }

    "have the correct heading" in {
      doc.select(".govuk-heading-xl").text shouldBe messages.heading
    }

    "have the correct sub-heading" in {
      doc.select(".govuk-heading-m").text.trim shouldBe messages.subheading
    }

    "have the correct paragraph text" in {
      doc.select("#main-content > div > div > p:nth-child(3)").text.trim shouldBe messages.paragraph
    }

    "have a hyperlink to GOV.UK that" should {

      lazy val hyperlink = doc.select("#privateResidenceReliefStartPageLink")

      "have the correct text" in {
        hyperlink.text.trim shouldBe messages.entitledLinkText
      }

      "link to the correct GOV.UK page" in {
        hyperlink.attr("href") shouldBe "https://www.gov.uk/tax-relief-selling-home"
      }
    }
    "the link should have a set of attributes" which {

      lazy val hyperlink = doc.select("#privateResidenceReliefStartPageLink")

      "has correct link class" in {
        hyperlink.hasClass("govuk-link") shouldEqual true
      }

      "has the attribute rel" in {
        hyperlink.hasAttr("rel") shouldEqual true
      }

      "rel has the value of noreferrer noopener" in {
        hyperlink.attr("rel") shouldEqual "noreferrer noopener"
      }

      "has a target attribute" in {
        hyperlink.hasAttr("target") shouldEqual true
      }

      "has a target value of _blank" in {
        hyperlink.attr("target") shouldEqual "_blank"
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
      doc.select("#main-content > div > div > p:nth-child(5)").text.trim shouldBe messages.continuationInstructions
    }

    "have a continue link that" should {

      lazy val hyperlink = doc.select("#main-content > div > div > a.govuk-button.govuk-button--start")

      "have the correct text" in {
        hyperlink.text.trim shouldBe commonMessages.continue
      }

      "take the user to disposal date page" in {
        hyperlink.attr("href") shouldBe controllers.routes.GainController.disposalDate().url
      }

    }

  }
}
