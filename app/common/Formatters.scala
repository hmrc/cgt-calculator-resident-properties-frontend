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

package common

import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError}

object Formatters {
  def text(errorKey: String, args: String*): FieldMapping[String] = of(using stringFormatter(errorKey, args*))

  def stringFormatter(errorKey: String, args: String*): Formatter[String] = new Formatter[String] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key).toRight(Seq(FormError(key, errorKey, args)))
    def unbind(key: String, value: String)           = Map(key -> value)
  }
}
