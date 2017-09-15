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

package training

import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers
import org.scalatest.mockito.MockitoSugar
import scala.concurrent.Future

class MathsServiceSpec extends UnitSpec with MockitoSugar {

  val mockMathsConnector = mock[MathsConnector]

  class Setup {
    val testService = new MathsService {
      override val mathsConnector = mockMathsConnector
    }
  }

  "nextStringIsLowerCase" should {
    "return true if lower case" in new Setup {
      when(mockMathsConnector.getNextString).thenReturn("lower")
      testService.nextStringIsLowerCase() shouldBe true
    }
  }

  "nextStringIsLowerCase" should {
    "return false if upper case" in new Setup {
      when(mockMathsConnector.getNextString).thenReturn("UPPER")
      testService.nextStringIsLowerCase() shouldBe false
    }
  }

    "validateThing" should{
      "return valid if something something" in new Setup{
        when(mockMathsConnector
          .validate(ArgumentMatchers.anyString()))
          .thenReturn(Future.successful(true))
        await(testService.validateThing("test")) shouldBe "valid"
      }
    }

    "validateThing" should{
      "return invalid if something something" in new Setup{
        when(mockMathsConnector
          .validate(ArgumentMatchers.anyString()))
          .thenReturn(Future.successful(false))
        await(testService.validateThing("test")) shouldBe "invalid"
      }
    }

  "addMaxFive" should {
    "return 5 if num is greater than 5" in new Setup {
      when(mockMathsConnector
        .add(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt()))
        .thenReturn(Future.successful(5))
      await(testService.addMaxFive(5, 4)) shouldBe 5
    }
  }
}
