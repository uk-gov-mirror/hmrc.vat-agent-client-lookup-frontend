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

package controllers.agent

import common.SessionKeys
import controllers.ControllerBaseSpec
import mocks.MockAuth
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SelectClientVrnControllerSpec extends ControllerBaseSpec with MockAuth with BeforeAndAfterEach {

  object TestClientVrnController extends SelectClientVrnController(
    messagesApi,
    mockAgentOnlyAuthPredicate,
    serviceErrorHandler,
    mockConfig
  )

  override def beforeEach(): Unit = {
    mockConfig.features.preferenceJourneyEnabled(true)
    mockConfig.features.whereToGoFeature(false)
  }

  "Calling the .show() action" when {

    val testRedirectUrl = "/manage-vat-account"
    val testYesPreference = "yes"
    val testNoPreference = "no"
    val testEmail = "test@example.com"

    "whereToGoFeature is enabled" when {

      "redirect URL is supplied" should {

        lazy val result = {
          mockConfig.features.whereToGoFeature(true)
          TestClientVrnController.show(testRedirectUrl)(request)
        }

        "return 200" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.OK
        }

        "render selectClientVrn page" in {
          Jsoup.parse(bodyOf(result)).title() shouldBe "What is your client’s VAT number? - Your client’s VAT details - GOV.UK"
        }

        "add redirectURL to session" in {
          session(result).get(SessionKeys.redirectUrl) shouldBe Some(testRedirectUrl)
        }
      }

      "redirect URL is not supplied" should {

        lazy val result = {
          mockConfig.features.whereToGoFeature(true)
          TestClientVrnController.show("")(request)
        }

        "return 200" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.OK
        }

        "render selectClientVrn page" in {
          Jsoup.parse(bodyOf(result)).title() shouldBe "What is your client’s VAT number? - Your client’s VAT details - GOV.UK"
        }

        "not add redirectURL to session" in {
          session(result).get(SessionKeys.redirectUrl) shouldBe None
        }
      }
    }

    "whereToGoFeature is disabled" when {

      "the preference feature switch is on" when {

        "the user has a preference of 'yes' in session" when {

          "the user has a verified email in session" when {

            "a valid redirect URL is provided" when {

              "there is a redirect URL currently in session" should {

                lazy val result = TestClientVrnController.show(testRedirectUrl)(request.withSession(
                  SessionKeys.redirectUrl -> testRedirectUrl,
                  SessionKeys.preference -> testYesPreference,
                  SessionKeys.verifiedAgentEmail -> testEmail
                ))

                "return 200" in {
                  mockAgentAuthorised()
                  status(result) shouldBe Status.OK
                }

                "return HTML" in {
                  contentType(result) shouldBe Some("text/html")
                  charset(result) shouldBe Some("utf-8")
                }

                "not add the requested redirect URL to the session" in {
                  session(result).get(SessionKeys.redirectUrl) shouldBe None
                }
              }

              "there is no redirect URL currently in session" should {

                lazy val result = TestClientVrnController.show(testRedirectUrl)(request.withSession(
                  SessionKeys.preference -> testYesPreference,
                  SessionKeys.verifiedAgentEmail -> testEmail
                ))

                "return 200" in {
                  mockAgentAuthorised()
                  status(result) shouldBe Status.OK
                }

                "return HTML" in {
                  contentType(result) shouldBe Some("text/html")
                  charset(result) shouldBe Some("utf-8")
                }

                "add the requested redirect URL to the session" in {
                  session(result).get(SessionKeys.redirectUrl) shouldBe Some(testRedirectUrl)
                }
              }
            }

            "an invalid redirect URL is provided" when {

              "there is a redirect URL currently in session" should {

                lazy val result = TestClientVrnController.show("www.google.com")(request.withSession(
                  SessionKeys.redirectUrl -> testRedirectUrl,
                  SessionKeys.preference -> testYesPreference,
                  SessionKeys.verifiedAgentEmail -> testEmail
                ))

                "return 200" in {
                  mockAgentAuthorised()
                  status(result) shouldBe Status.OK
                }

                "return HTML" in {
                  contentType(result) shouldBe Some("text/html")
                  charset(result) shouldBe Some("utf-8")
                }

                "not add the requested redirect URL to the session" in {
                  session(result).get(SessionKeys.redirectUrl) shouldBe None
                }
              }

              "there is no redirect URL currently in session" should {

                lazy val result = TestClientVrnController.show("www.google.com")(request.withSession(
                  SessionKeys.preference -> testYesPreference,
                  SessionKeys.verifiedAgentEmail -> testEmail
                ))

                "return 200" in {
                  mockAgentAuthorised()
                  status(result) shouldBe Status.OK
                }

                "return HTML" in {
                  contentType(result) shouldBe Some("text/html")
                  charset(result) shouldBe Some("utf-8")
                }

                "add the default redirect URL (ChoC overview) to the session" in {
                  session(result).get(SessionKeys.redirectUrl) shouldBe Some(mockConfig.manageVatCustomerDetailsUrl)
                }
              }
            }
          }

          "the user has no verified email in session" should {

            lazy val result = TestClientVrnController.show(testRedirectUrl)(request.withSession(
              SessionKeys.preference -> testYesPreference
            ))

            "return 303" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the Capture Preference controller action" in {
              redirectLocation(result) shouldBe Some(controllers.agent.routes.CapturePreferenceController.show().url)
            }

            "add the requested redirect URL to the session" in {
              session(result).get(SessionKeys.redirectUrl) shouldBe Some(testRedirectUrl)
            }
          }
        }

        "the user has a preference of 'no' in session" when {

          "a valid redirect URL is provided" when {

            "there is a redirect URL currently in session" should {

              lazy val result = TestClientVrnController.show(testRedirectUrl)(request.withSession(
                SessionKeys.redirectUrl -> testRedirectUrl,
                SessionKeys.preference -> testNoPreference
              ))

              "return 200" in {
                mockAgentAuthorised()
                status(result) shouldBe Status.OK
              }

              "return HTML" in {
                contentType(result) shouldBe Some("text/html")
                charset(result) shouldBe Some("utf-8")
              }

              "not add the requested redirect URL to the session" in {
                session(result).get(SessionKeys.redirectUrl) shouldBe None
              }
            }

            "there is no redirect URL currently in session" should {

              lazy val result = TestClientVrnController.show(testRedirectUrl)(request.withSession(
                SessionKeys.preference -> testNoPreference
              ))

              "return 200" in {
                mockAgentAuthorised()
                status(result) shouldBe Status.OK
              }

              "return HTML" in {
                contentType(result) shouldBe Some("text/html")
                charset(result) shouldBe Some("utf-8")
              }

              "add the requested redirect URL to the session" in {
                session(result).get(SessionKeys.redirectUrl) shouldBe Some(testRedirectUrl)
              }
            }
          }

          "an invalid redirect URL is provided" when {

            "there is a redirect URL currently in session" should {

              lazy val result = TestClientVrnController.show("www.google.com")(request.withSession(
                SessionKeys.redirectUrl -> testRedirectUrl,
                SessionKeys.preference -> testNoPreference
              ))

              "return 200" in {
                mockAgentAuthorised()
                status(result) shouldBe Status.OK
              }

              "return HTML" in {
                contentType(result) shouldBe Some("text/html")
                charset(result) shouldBe Some("utf-8")
              }

              "not add the requested redirect URL to the session" in {
                session(result).get(SessionKeys.redirectUrl) shouldBe None
              }
            }

            "there is no redirect URL currently in session" should {

              lazy val result = TestClientVrnController.show("www.google.com")(request.withSession(
                SessionKeys.preference -> testNoPreference
              ))

              "return 200" in {
                mockAgentAuthorised()
                status(result) shouldBe Status.OK
              }

              "return HTML" in {
                contentType(result) shouldBe Some("text/html")
                charset(result) shouldBe Some("utf-8")
              }

              "add the default redirect URL (ChoC overview) to the session" in {
                session(result).get(SessionKeys.redirectUrl) shouldBe Some(mockConfig.manageVatCustomerDetailsUrl)
              }
            }
          }
        }

        "the user does not have a preference in session, but does have a redirect URL in session" should {

          lazy val result = TestClientVrnController.show("/homepage")(request.withSession(
            SessionKeys.redirectUrl -> testRedirectUrl
          ))

          "return 303" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Capture Preference controller action" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.CapturePreferenceController.show().url)
          }

          "not add the requested redirect URL to the session" in {
            session(result).get(SessionKeys.redirectUrl) shouldBe None
          }
        }

        "the user does not have a preference or a redirect URL in session" should {

          lazy val result = TestClientVrnController.show(testRedirectUrl)(request)

          "return 303" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the Capture Preference controller action" in {
            redirectLocation(result) shouldBe Some(controllers.agent.routes.CapturePreferenceController.show().url)
          }

          "add the requested redirect URL to the session" in {
            session(result).get(SessionKeys.redirectUrl) shouldBe Some(testRedirectUrl)
          }
        }
      }

      "the preference feature switch is off" when {

        "the user does not have a preference in session" should {

          lazy val result = {
            mockConfig.features.preferenceJourneyEnabled(false)
            TestClientVrnController.show(testRedirectUrl)(request.withSession(
              SessionKeys.redirectUrl -> testRedirectUrl
            ))
          }

          "return 200" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }
        }
      }
    }
  }

  "Calling the .submit action" when {

    "the user is an authorised Agent" when {

      "valid data is posted" should {

        lazy val request = FakeRequest("POST", "/")
          .withFormUrlEncodedBody(("vrn", "999969202"))
          .withSession(SessionKeys.clientMandationStatus -> "Non MTDfB")

        "original request should contain mandation status in cookie" in {
          request.headers.get("Cookie").get should include("mtdVatMandationStatus=Non+MTDfB")
        }

        lazy val result = TestClientVrnController.submit(request)

        "return 303" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.SEE_OTHER
        }

        "contain the correct location header" in {
          redirectLocation(result) shouldBe Some(controllers.agent.routes.ConfirmClientVrnController.show().url)
        }

        "add Client VRN to session cookie" in {
          result.header.headers("Set-Cookie") should include("CLIENT_VRN=999969202")
        }

        "remove mandation status from session cookie" in {
          result.header.headers("Set-Cookie") shouldNot include("mtdVatMandationStatus=Non+MTDfB")
        }
      }

      "invalid data is posted" should {

        lazy val request = FakeRequest("POST", "/").withFormUrlEncodedBody(("vrn", "123456789"))
        lazy val result = TestClientVrnController.submit(request)

        "return 400" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.BAD_REQUEST
        }
      }
    }
  }

  "The extractRedirectUrl() function" when {

    "a valid relative redirect URL is provided" should {

      "return the URL" in {
        val result = TestClientVrnController.extractRedirectUrl("/homepage")
        result shouldBe Some("/homepage")
      }
    }

    "a valid absolute redirect URL is provided" should {

      "return the URL" in {
        val result = TestClientVrnController.extractRedirectUrl("http://localhost:9149/homepage")
        result shouldBe Some("http://localhost:9149/homepage")
      }
    }

    "an invalid redirect URL is provided" should {

      "return None" in {
        val result = TestClientVrnController.extractRedirectUrl("http://www.google.com")
        result shouldBe None
      }
    }

    "an exception is thrown when trying to construct a continue URL" should {

      "return None" in {
        val result = TestClientVrnController.extractRedirectUrl("99")
        result shouldBe None
      }
    }
  }
}
