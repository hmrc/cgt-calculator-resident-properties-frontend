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

package controllers.resident.properties.DeductionsControllerSpec

import common.WithCommonFakeApplication
import controllers.DeductionsController
import controllers.helpers.CommonMocks
import views.html.calculation.resident._
import views.html.calculation.resident.properties.deductions._


trait DeductionsControllerBaseSpec  {
  self: CommonMocks & WithCommonFakeApplication =>

  lazy val testingDeductionsController: DeductionsController = new DeductionsController(
    mockCalcConnector,
    mockSessionCacheService,
    mockMessagesControllerComponents,
    fakeApplication.injector.instanceOf[propertyLivedIn],
    fakeApplication.injector.instanceOf[privateResidenceRelief],
    fakeApplication.injector.instanceOf[privateResidenceReliefValue],
    fakeApplication.injector.instanceOf[lettingsRelief],
    fakeApplication.injector.instanceOf[lettingsReliefValue],
    fakeApplication.injector.instanceOf[lossesBroughtForward],
    fakeApplication.injector.instanceOf[lossesBroughtForwardValue])

}
