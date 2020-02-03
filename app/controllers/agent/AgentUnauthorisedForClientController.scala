/*
 * Copyright 2020 HM Revenue & Customs
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

import audit.AuditService
import audit.models.AuthenticateAgentAuditModel
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthoriseAsAgentOnly
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import views.html.errors.agent.NotAuthorisedForClientView

import scala.concurrent.ExecutionContext

@Singleton
class AgentUnauthorisedForClientController @Inject()(val authenticate: AuthoriseAsAgentOnly,
                                                     val serviceErrorHandler: ErrorHandler,
                                                     val auditService: AuditService,
                                                     mcc: MessagesControllerComponents,
                                                     notAuthorisedForClientView: NotAuthorisedForClientView,
                                                     implicit val executionContext: ExecutionContext,
                                                     implicit val appConfig: AppConfig) extends BaseController(mcc) {

  def show(redirectUrl: String = ""): Action[AnyContent] = authenticate {
    implicit agent => {
      val redirectLink = extractRedirectUrl(redirectUrl).getOrElse("")
      agent.session.get(SessionKeys.clientVRN) match {
        case Some(vrn) =>
          auditService.extendedAudit(
            AuthenticateAgentAuditModel(agent.arn, vrn, isAuthorisedForClient = false),
            Some(controllers.agent.routes.ConfirmClientVrnController.show().url)
          )
          Ok(notAuthorisedForClientView(vrn, redirectLink))

        case _ =>
          Redirect(controllers.agent.routes.SelectClientVrnController.show(redirectLink))
      }
    }
  }
}
