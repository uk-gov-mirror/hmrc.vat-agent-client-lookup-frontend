/*
 * Copyright 2021 HM Revenue & Customs
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

package helpers

import java.util.UUID

import models.CustomerDetails
import play.api.libs.json.{JsObject, Json}

object IntegrationTestConstants {
  val sessionId = s"stubbed-${UUID.randomUUID}"
  val userId = s"/auth/oid/1234567890"

  val clientVRN = "999969202"

  val nonMTDfB = "Non MTDfB"
  val mandated = "MTDfB Mandated"

  val notificationsEmail = "notifyme@email.com"

  val website = "www.test.com"

  val phoneNumber = "01234 567890"
  val mobileNumber = "07700 123456"
  val faxNumber = "01234 098765"
  val email = "test@test.com"
  val emailVerified = true

  val organisation: CustomerDetails = CustomerDetails(
    firstName = None,
    lastName = None,
    tradingName = Some("Vatmobile Taxi"),
    organisationName = Some("Vatmobile Taxi LTD"),
    mandationStatus = nonMTDfB,
    deregistration = None,
    partyType = Some("2"),
    isInsolvent = false
  )

  val individual: CustomerDetails = CustomerDetails(
    firstName = Some("Pepsi"),
    lastName = Some("Mac"),
    tradingName = Some("PepsiCo"),
    organisationName = None,
    mandationStatus = nonMTDfB,
    deregistration = None,
    partyType = Some("2"),
    isInsolvent = false
  )

  val individualMandatedJson: JsObject = Json.obj(
    "firstName" -> "Pepsi",
    "lastName" -> "Mac",
    "tradingName" -> "PepsiCo",
    "mandationStatus" -> mandated,
    "missingTrader" -> false,
    "isInsolvent" -> false
  )

  val individualNonMTDfBJson: JsObject = Json.obj(
    "firstName" -> "Pepsi",
    "lastName" -> "Mac",
    "tradingName" -> "PepsiCo",
    "mandationStatus" -> nonMTDfB,
    "missingTrader" -> false
  )
}
