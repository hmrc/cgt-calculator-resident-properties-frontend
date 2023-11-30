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

package common

class TransformersSpec extends CommonPlaySpec {

  "stripCurrencyCharacters" should {
    "return a cleaned currency amount" when {
      "the transform is applied to an amount with commas and pound signs" in {
        val result = Transformers.stripCurrencyCharacters("£1,999")
        result shouldBe "1999"
      }
      "the transform is applied to an amount with commas" in {
        val result = Transformers.stripCurrencyCharacters("1,999")
        result shouldBe "1999"
      }
      "the transform is applied to an amount with pound signs" in {
        val result = Transformers.stripCurrencyCharacters("£1999")
        result shouldBe "1999"
      }
    }
  }

  "stringToBigDecimal" should {
    "return a BigDecimal" when {
      "the transformation fails" in {
        val result = Transformers.stringToBigDecimal("1000")
        result shouldBe BigDecimal(1000)
      }
    }

    "return zero as a BigDecimal" when {
      "the transformation fails" in {
        val result = Transformers.stringToBigDecimal("FAIL")
        result shouldBe BigDecimal(0)
      }
    }
  }

}
