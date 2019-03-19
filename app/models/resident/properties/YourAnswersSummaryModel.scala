/*
 * Copyright 2019 HM Revenue & Customs
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

package models.resident.properties

import java.time.LocalDate

import common.Dates._
import play.api.libs.json._

case class YourAnswersSummaryModel(
  disposalDate: LocalDate,
  disposalValue: Option[BigDecimal],
  worthWhenSoldForLess: Option[BigDecimal],
  whoDidYouGiveItTo: Option[String],
  worthWhenGaveAway: Option[BigDecimal],
  disposalCosts: BigDecimal,
  acquisitionValue: Option[BigDecimal],
  worthWhenInherited: Option[BigDecimal],
  worthWhenGifted: Option[BigDecimal],
  worthWhenBoughtForLess: Option[BigDecimal],
  acquisitionCosts: BigDecimal,
  improvements: BigDecimal,
  givenAway: Boolean,
  sellForLess: Option[Boolean],
  ownerBeforeLegislationStart: Boolean,
  valueBeforeLegislationStart: Option[BigDecimal],
  howBecameOwner: Option[String],
  boughtForLessThanWorth: Option[Boolean]) {

  val displayWorthWhenSold: Boolean = !givenAway && !sellForLess.get
  val displayWorthWhenSoldForLess: Boolean = !givenAway && sellForLess.get
  val displayBoughtForLessThanWorth: Boolean = !ownerBeforeLegislationStart && howBecameOwner.get.equals("Bought")
  val displayWorthWhenBought: Boolean = displayBoughtForLessThanWorth && !boughtForLessThanWorth.get
  val displayWorthWhenBoughtForLess: Boolean = displayBoughtForLessThanWorth && boughtForLessThanWorth.get
  val displayWorthWhenGifted: Boolean = !ownerBeforeLegislationStart && howBecameOwner.get.equals("Gifted")
  val displayWorthWhenInherited: Boolean = !ownerBeforeLegislationStart && howBecameOwner.get.equals("Inherited")
}

object YourAnswersSummaryModel {

  implicit val localDateFormat = new Format[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] = json.validate[String].map(LocalDate.parse(_,formatter))
    override def writes(o: LocalDate): JsValue = Json.toJson(o.toString)
  }

  implicit val format = Json.format[YourAnswersSummaryModel]
}
