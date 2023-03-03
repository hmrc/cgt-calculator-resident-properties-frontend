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

package config

import java.time.LocalDate
import common.Dates.formatter
import javax.inject.Inject
import play.api.Environment
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {
  val assetsPrefix: String
  val contactFormServiceIdentifier: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val residentIFormUrl: String
  val capitalGainsReportingFormUrl: String
  val urBannerLink: String
  val feedbackSurvey: String
  val selfAssessmentActivateDate: LocalDate
  def isWelshEnabled: Boolean
}

class ApplicationConfig @Inject()(servicesConfig: ServicesConfig,
                                  environment: Environment) extends AppConfig {

  private def loadConfig(key: String) = servicesConfig.getString(key)

  lazy val contactHost = servicesConfig.getConfString("contact-frontend.www", "")

  override lazy val assetsPrefix = loadConfig(s"assets.url") + loadConfig(s"assets.version")

  override val selfAssessmentActivateDate = LocalDate.parse(loadConfig(s"selfAssessmentActivate.date"), formatter)

  override lazy val contactFormServiceIdentifier = "CGT"
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override lazy val feedbackSurvey: String = loadConfig(s"feedback-frontend.url")

  override lazy val urBannerLink =
    "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=CGT_resident_properties_summary&utm_source=Survey_Banner&utm_medium=other&t=HMRC&id=117"
  override lazy val residentIFormUrl: String = loadConfig(s"resident-iForm.url")

  override lazy val capitalGainsReportingFormUrl: String = loadConfig(s"resident-iForm.capitalGainsReportingFormUrl")

  def isWelshEnabled: Boolean = servicesConfig.getBoolean("features.welsh-translation")


}
