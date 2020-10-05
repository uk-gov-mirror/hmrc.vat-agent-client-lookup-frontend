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

package pages.agent

import forms.PasscodeForm
import helpers.IntegrationTestConstants.notificationsEmail
import pages.BasePageISpec
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{OK, SEE_OTHER, BAD_REQUEST}

class VerifyEmailPinPageSpec extends BasePageISpec {

  val path = "/email-enter-code"

  "Calling the .show action" when {

    "there is no notification email in session" should {

      "Render the Capture Preference view" in {

        def show(): WSResponse = get(path, formatNotificationsEmail(None))

        given.agent.isSignedUpToAgentServices

        When("I call the verify email pin page with no notification email in session")

        val res = show()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(controllers.agent.routes.CapturePreferenceController.show().url)
        )
      }
    }

    "there is a notification email in session" should {

      "Render the Verify Email Pin view" in {

        def show(): WSResponse = get(path, formatNotificationsEmail(Some(notificationsEmail)))

        given.agent.isSignedUpToAgentServices

        When("I call the Verify email pin page with a notification email in session")
        val res = show()

        res should have(
          httpStatus(OK),
          elementText("h1")("Enter code to confirm your email address")
        )
      }
    }
  }

  "Calling the .submit action" when {

    def submit(passcode: String): WSResponse = post(
      path, formatNotificationsEmail(Some(notificationsEmail)))(toFormData(PasscodeForm.form, passcode))

    "there is a verified notification email in session" when {

      "the correct passcode is submitted" should {

        "redirect to manage customer details" in {
          given.agent.isSignedUpToAgentServices

          When("I submit the Email verification passcode page with the correct passcode")
          val res = submit("123456")

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI("/vat-through-software/representative/client-vat-account")
          )
        }
      }

      "an incorrect passcode is submitted" should {

        "redirect to manage customer details" in {
          given.agent.isSignedUpToAgentServices

          When("I submit the Email verification passcode page with the correct passcode")
          val res = submit("1234567890")

          res should have(
            httpStatus(BAD_REQUEST),

            //Error Summary
            isElementVisible("#error-summary-display")(isVisible = true),
            isElementVisible("#passcode-error-summary")(isVisible = true),
            elementText("#passcode-error-summary")("Enter the 6 character confirmation code"),
            elementWithLinkTo("#passcode-error-summary")("#passcode"),

            //Error against Input Label
            isElementVisible(".form-field--error .error-message")(isVisible = true),
            elementText(".form-field--error .error-message")("Error: Enter the 6 character confirmation code")
          )
        }
      }
    }
  }

}
