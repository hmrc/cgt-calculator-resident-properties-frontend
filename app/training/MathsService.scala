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

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object MathsService extends MathsService {
  override val mathsConnector: MathsConnector = MathsConnector
}

trait MathsService {

  val mathsConnector: MathsConnector

  def nextStringIsLowerCase(): Boolean = {
    val str = mathsConnector.getNextString
    str.toLowerCase == str
  }

  def validateThing(str: String): Future[String] = {
    mathsConnector.validate(str).map {
      case true  => "valid"
      case false => "invalid"
    }
  }

  def addMaxFive(x: Int, y: Int): Future[Int] = {
    mathsConnector.add(x, y).map {
      num => if(num > 5) 5 else num
    }
  }

}
