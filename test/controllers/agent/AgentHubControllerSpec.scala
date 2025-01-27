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

package controllers.agent

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import assets.BaseTestConstants
import assets.CustomerDetailsTestConstants.{customerDetailsAllInfo, customerDetailsFnameOnly, customerDetailsAllPending}
import controllers.ControllerBaseSpec
import mocks.services._
import org.jsoup.Jsoup
import play.api.mvc.Result
import play.mvc.Http.Status._
import views.html.agent.AgentHubView

import scala.concurrent.Future

class AgentHubControllerSpec extends ControllerBaseSpec with MockCustomerDetailsService with MockDateService {

  trait Test {
    lazy val controller = new AgentHubController(
      mockAuthAsAgentWithClient,
      mockErrorHandler,
      mockCustomerDetailsService,
      mockDateService,
      mcc,
      inject[AgentHubView],
      mockConfig,
      ec
    )
    implicit val timeout: Timeout = Timeout.apply(60, TimeUnit.SECONDS)
  }

  "AgentHubController.show()" when {

        "the customer is a missing trader" when {

          "they do not have a pending PPOB" should {

            "redirect the customer to manage-vat" in new Test {
              mockAgentAuthorised()
              mockCustomerDetailsSuccess(customerDetailsAllInfo)

              val result: Future[Result] = {
                controller.show()(fakeRequestWithVrnAndRedirectUrl)
              }

              status(result) shouldBe SEE_OTHER
            }
          }

          "they have a pending PPOB" should {

            "render the AgentHubPage" in new Test {
              mockAgentAuthorised()
              mockCustomerDetailsSuccess(customerDetailsAllPending.copy(missingTrader = true))

              val result: Future[Result] = {
                controller.show()(fakeRequestWithVrnAndRedirectUrl)
              }

              status(result) shouldBe OK
              messages(Jsoup.parse(bodyOf(result)).select("h1").text) shouldBe "Your client’s VAT details"
            }
          }
        }

        "the customer isn't a missing trader" should {

          "render the AgentHubPage" in new Test {
            mockAgentAuthorised()
            mockCustomerDetailsSuccess(customerDetailsFnameOnly)

            val result: Future[Result] = {
              controller.show()(fakeRequestWithVrnAndRedirectUrl)
            }

            status(result) shouldBe OK
            messages(Jsoup.parse(bodyOf(result)).select("h1").text) shouldBe "Your client’s VAT details"
          }
        }

    "the customerDetails call fails" should {

      "return an error" in new Test {
        mockAgentAuthorised()
        mockCustomerDetailsError(BaseTestConstants.unexpectedError)

        val result: Future[Result] = {
          controller.show()(fakeRequestWithVrnAndRedirectUrl)
        }

        status(result) shouldBe INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title() shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }
    }
  }

}
