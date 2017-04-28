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

package controllers

import common.Dates
import controllers.predicates.ValidActiveSession
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.{IncomeAnswersModel, LossesBroughtForwardModel, PrivateResidenceReliefModel, TaxYearModel}
import models.resident.properties.{ChargeableGainAnswers, PropertyLivedInModel, YourAnswersSummaryModel}
import play.api.mvc.{Action, AnyContent}
import views.html.calculation.resident.properties.checkYourAnswers._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

/**
  * Created by david on 27/04/17.
  */

object ReviewYourAnswersController extends ReviewYourAnswersController

trait ReviewYourAnswersController extends ValidActiveSession {

  lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
    None,
    Some(500),
    whoDidYouGiveItTo = None,
    worthWhenGaveAway = None,
    BigDecimal(10000),
    None,
    worthWhenInherited = None,
    worthWhenGifted = None,
    worthWhenBoughtForLess = Some(3000),
    BigDecimal(10000),
    BigDecimal(30000),
    givenAway = false,
    Some(true),
    ownerBeforeLegislationStart = false,
    None,
    Some("Bought"),
    Some(true)
  )

  lazy val deductionAnswers = ChargeableGainAnswers(
    Some(LossesBroughtForwardModel(false)),
    None,
    Some(PropertyLivedInModel(true)),
    Some(PrivateResidenceReliefModel(false)),
    None,
    None,
    None
  )

  lazy val incomeAnswers = IncomeAnswersModel(Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

  lazy val taxYearModel = TaxYearModel("2013/14", false, "2015/16")

  val checkYourAnswersGain = TODO

  val checkYourAnswersDeductions = TODO

  val checkYourAnswersFinal: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(checkYourAnswers(routes.SummaryController.summary(), "Back link", gainAnswers,
      Some(deductionAnswers), Some(taxYearModel), Some(incomeAnswers))))
  }

}
