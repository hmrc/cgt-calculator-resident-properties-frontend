/*
 * Copyright 2020 HM Revenue & Customs
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

package views.resident.properties.gain

import assets.MessageLookup.{AcquisitionCosts => messages, Resident => commonMessages}
import forms.resident.AcquisitionCostsForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.BaseViewSpec
import views.html.calculation.resident.properties.{gain => views}

class AcquisitionCostsViewSpec extends UnitSpec with WithFakeApplication with BaseViewSpec {

  "Acquisition Costs view" should {

    lazy val view = views.acquisitionCosts(acquisitionCostsForm, Some("back-link"))(fakeRequest, testingMessages, mockAppConfig)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a back button that" should {

      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have a link to back-link" in {
        backLink.attr("href") shouldBe "back-link"
      }
    }

    "have a home link to 'home-link'" in {
      doc.getElementById("homeNavHref").attr("href") shouldEqual controllers.routes.PropertiesController.introduction().toString
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("H1")

      s"have the page heading '${messages.pageHeading}'" in {
        h1Tag.text shouldBe messages.pageHeading
      }

      "have the heading-large class" in {
        h1Tag.hasClass("heading-large") shouldBe true
      }
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.GainController.submitAcquisitionCosts().toString}'" in {
        form.attr("action") shouldBe controllers.routes.GainController.submitAcquisitionCosts().toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      "has a label that" should {

        lazy val label = form.select("label")

        s"have the question ${messages.pageHeading}" in {
          label.text should include(messages.pageHeading)
        }

        "have the class 'visuallyhidden'" in {
          label.select("span.visuallyhidden").size shouldBe 1
        }

        "have a div form-hint" which {
          lazy val formHint = form.select("div.form-hint")
          s"has a paragraph with the text ${messages.listTitle}" in {
            doc.getElementById("listTitle").text shouldBe messages.listTitle
          }

            s"has a list" which {
              s"have the first bullet of ${messages.bulletOne}" in {
                doc.body.select("div.form-hint ul li").get(0).text shouldBe messages.bulletOne
              }

              s"have the second bullet of ${messages.bulletTwo}" in {
                doc.body.select("div.form-hint ul li").get(1).text shouldBe messages.bulletTwo
              }

              s"have the third bullet of ${messages.bulletThree}" in {
                doc.body.select("div.form-hint ul li").get(2).text shouldBe messages.bulletThree
              }

            }

          s"has panel text ${messages.panelText}" in {
            doc.select("p.panel-indent").text shouldBe messages.panelText
          }
          }
        }
      }

      "has a numeric input field" which {

        lazy val input = doc.body.getElementsByTag("input")

        "has the id 'amount'" in {
          input.attr("id") shouldBe "amount"
        }

        "has the name 'amount'" in {
          input.attr("name") shouldBe "amount"
        }

        "is of type number" in {
          input.attr("type") shouldBe "number"
        }

        "has a step value of '0.01'" in {
          input.attr("step") shouldBe "0.01"
        }

      }

    "have a continue button that" should {

      lazy val continueButton = doc.select("button#continue-button")

      s"have the button text '${commonMessages.continue}'" in {
        continueButton.text shouldBe commonMessages.continue
      }

      "be of type submit" in {
        continueButton.attr("type") shouldBe "submit"
      }

      "have the class 'button'" in {
        continueButton.hasClass("button") shouldBe true
      }

    }
  }

  "Acquisition Costs View with form with errors" which {

    "is due to mandatory field error" should {

      lazy val form = acquisitionCostsForm.bind(Map("amount" -> ""))
      lazy val view = views.acquisitionCosts(form, Some("back-link"))(fakeRequest, testingMessages, mockAppConfig)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select("#amount-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select(".form-group .error-notification").size shouldBe 1
      }
    }
  }
}
