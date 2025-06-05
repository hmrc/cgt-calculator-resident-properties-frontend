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

package assets

import common.Dates
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.properties._

/**
  * Created by david on 27/04/17.
  */
object ModelsAsset {

  val gainAnswersMostPossibles :YourAnswersSummaryModel= YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
    None,
    None,
    whoDidYouGiveItTo = Some("Other"),
    worthWhenGaveAway = Some(10000),
    BigDecimal(10000),
    None,
    worthWhenInherited = None,
    worthWhenGifted = None,
    worthWhenBoughtForLess = None,
    BigDecimal(10000),
    BigDecimal(30000),
    givenAway = true,
    None,
    ownerBeforeLegislationStart = true,
    Some(BigDecimal(5000)),
    None,
    None
  )

  val gainLargeDisposalValue :YourAnswersSummaryModel= YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
    None,
    None,
    whoDidYouGiveItTo = Some("Other"),
    worthWhenGaveAway = Some(1000000),
    BigDecimal(1000000),
    None,
    worthWhenInherited = None,
    worthWhenGifted = None,
    worthWhenBoughtForLess = None,
    BigDecimal(10000),
    BigDecimal(30000),
    givenAway = true,
    None,
    ownerBeforeLegislationStart = true,
    Some(BigDecimal(5000)),
    None,
    None
  )

  val deductionAnswersMostPossibles = ChargeableGainAnswers(
    Some(LossesBroughtForwardModel(true)),
    Some(LossesBroughtForwardValueModel(10000)),
    Some(PropertyLivedInModel(true)),
    Some(PrivateResidenceReliefModel(true)),
    Some(PrivateResidenceReliefValueModel(4000)),
    Some(LettingsReliefModel(true)),
    Some(LettingsReliefValueModel(4500))
  )

  val deductionAnswersLeastPossibles = ChargeableGainAnswers(
    Some(LossesBroughtForwardModel(false)),
    None,
    Some(PropertyLivedInModel(false)),
    None,
    None,
    None,
    None
  )

  val incomeAnswers = IncomeAnswersModel(Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

  val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

}
