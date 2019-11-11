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

package views.agent.partials

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.ViewBaseSpec
import assets.messages.partials.ClientDetailsPartialMessages

class ClientDetailsPartialsSpec extends ViewBaseSpec {

  "ClientDetailsPartials" should {

    lazy implicit val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")
    lazy val view = views.html.agent.partials.clientDetailsPartials()(messages,mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    s"have the correct card heading of ${ClientDetailsPartialMessages.heading}" in {
      elementText(".heading-medium") shouldBe ClientDetailsPartialMessages.heading
    }

    s"display the correct line 1 of ${ClientDetailsPartialMessages.paragraphOne}" in {
      elementText("p") shouldBe ClientDetailsPartialMessages.paragraphOne
    }

    s"display the correct link text of ${ClientDetailsPartialMessages.linkText}" in {
      elementText("a") shouldBe ClientDetailsPartialMessages.linkText
    }

    s"display the correct link of ${mockConfig.manageVatCustomerDetailsUrl}" in {
      element("a").attr("href") shouldBe mockConfig.manageVatCustomerDetailsUrl
    }
  }

}
