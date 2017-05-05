/*
 * Copyright 2017 HM Revenue & Customs
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

package views.resident.properties.whatNext

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{whatNext => views}

class WhatNextSANoGainViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The whatNextSANoGain view" should {

    lazy val view = views.whatNextSAFourTimesAEA("back-link", "31 January 2018")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body).select("article.content__body")

    "have the correct title" in {

    }

    "have a bullet point ..." which {

      "has the title ..." in {

      }

      "has a first bullet point of ..." in {

      }

      "has a second bullet point of ..." in {

      }
    }

    "have an important information section with the text ..." in {

    }

    "have a notice section with the text ..." in {

    }

    "have a Report now button" which {

      "has the styling/id ..." in {

      }

      "has a link to the ... page" in {

      }
    }

    "have a Finish link" which {

      "has a link to the ... page" in {

      }
    }
  }
}