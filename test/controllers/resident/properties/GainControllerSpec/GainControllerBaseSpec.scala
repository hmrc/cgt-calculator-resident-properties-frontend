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

package controllers.resident.properties.GainControllerSpec

import common.WithCommonFakeApplication
import controllers.GainController
import controllers.helpers.CommonMocks
import views.html.calculation.resident.properties.gain._
import views.html.calculation.resident.outsideTaxYear

trait GainControllerBaseSpec {
  self: CommonMocks with WithCommonFakeApplication =>

  lazy val testingGainController: GainController = new GainController(
    mockCalcConnector,
    mockSessionCacheConnector,
    mockSessionCacheService,
    mockMessagesControllerComponents,
    fakeApplication.injector.instanceOf[disposalCosts],
    fakeApplication.injector.instanceOf[disposalDate],
    fakeApplication.injector.instanceOf[disposalValue],
    fakeApplication.injector.instanceOf[worthWhenInherited],
    fakeApplication.injector.instanceOf[worthWhenGifted],
    fakeApplication.injector.instanceOf[worthWhenGaveAway],
    fakeApplication.injector.instanceOf[worthWhenBoughtForLess],
    fakeApplication.injector.instanceOf[worthWhenSoldForLess],
    fakeApplication.injector.instanceOf[sellForLess],
    fakeApplication.injector.instanceOf[buyForLess],
    fakeApplication.injector.instanceOf[ownerBeforeLegislationStart],
    fakeApplication.injector.instanceOf[valueBeforeLegislationStart],
    fakeApplication.injector.instanceOf[acquisitionValue],
    fakeApplication.injector.instanceOf[acquisitionCosts],
    fakeApplication.injector.instanceOf[sellOrGiveAway],
    fakeApplication.injector.instanceOf[whoDidYouGiveItTo],
    fakeApplication.injector.instanceOf[noTaxToPay],
    fakeApplication.injector.instanceOf[howBecameOwner],
    fakeApplication.injector.instanceOf[improvements],
    fakeApplication.injector.instanceOf[outsideTaxYear],
    )

}
