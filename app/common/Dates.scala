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

import play.api.i18n.Messages

import java.time._
import java.time.format.{DateTimeFormatter, ResolverStyle}
import scala.concurrent.Future

object Dates {

  val taxYearEnd: String = "04-05"
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT)
  val requestFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)
  val datePageFormatNoZero: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuuu").withResolverStyle(ResolverStyle.STRICT)

  def constructDate(day: Int, month: Int, year: Int): LocalDate = LocalDate.parse(s"$day/$month/$year", formatter)

  def getDay(date: LocalDate): Int = date.getDayOfMonth

  def getMonth(date: LocalDate): Int = date.getMonthValue

  def getYear(date: LocalDate): Int = date.getYear

  def taxYearToString(input: Int): String = {
    val startYear = input - 1
    val endYear = input.toString.takeRight(2)
    s"$startYear/$endYear"
  }

  def taxYearOfDateLongHand(date: LocalDate)(implicit messages: Messages): String = {
    if (date.isAfter(LocalDate.parse(s"${date.getYear.toString}-$taxYearEnd"))) {
      Messages("calc.whatToDoNext.longTaxYear", date.getYear.toString, date.plusYears(1L).getYear.toString)
    }
    else {
      Messages("calc.whatToDoNext.longTaxYear", date.minusYears(1L).getYear.toString, date.getYear.toString)
    }
  }

  def getCurrentTaxYear: Future[String] = {
    val now = ZonedDateTime.now(ZoneId.of("Europe/London"))
    val year = now.getYear
    if (now.isAfter(LocalDate.parse(s"${year.toString}-$taxYearEnd").atStartOfDay(ZoneId.of("Europe/London")))) {
      Future.successful(taxYearToString(year + 1))
    }
    else {
      Future.successful(taxYearToString(year))
    }
  }

  object TemplateImplicits {
    implicit class RichDate(date: LocalDate) {
      def localFormat(pattern: String)(implicit messages: Messages): String = {
        if(messages.lang.language == "cy") {
          val monthNum = date.getMonthValue
          val welshFormatter = DateTimeFormatter.ofPattern(s"""d '${messages(s"calc.month.$monthNum")}' yyyy""")
          date.format(welshFormatter)
        } else {
          val localFormatter = DateTimeFormatter.ofPattern(pattern, messages.lang.toLocale)
          date.format(localFormatter)
        }
      }
    }
  }
}
