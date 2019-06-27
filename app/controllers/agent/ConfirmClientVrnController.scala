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

import audit.AuditService
import audit.models.{AuthenticateAgentAuditModel, GetClientBusinessNameAuditModel}
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthoriseAsAgentWithClient
import javax.inject.{Inject, Singleton}
import models.errors._
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.CustomerDetailsService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

@Singleton
class ConfirmClientVrnController @Inject()(val messagesApi: MessagesApi,
                                           val authenticate: AuthoriseAsAgentWithClient,
                                           val customerDetailsService: CustomerDetailsService,
                                           val errorHandler: ErrorHandler,
                                           val auditService: AuditService,
                                           implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def show: Action[AnyContent] = authenticate.async {
    implicit user =>
      customerDetailsService.getCustomerDetails(user.vrn) map {
        case Right(customerDetails) =>
          auditService.extendedAudit(
            AuthenticateAgentAuditModel(user.arn.get, user.vrn, isAuthorisedForClient = true),
            Some(controllers.agent.routes.ConfirmClientVrnController.show().url)
          )
          auditService.extendedAudit(
            GetClientBusinessNameAuditModel(user.arn.get, user.vrn, customerDetails.clientName),
            Some(controllers.agent.routes.ConfirmClientVrnController.show().url)
          )

          Ok(views.html.agent.confirmClientVrn(user.vrn, customerDetails))

        case Left(Migration)   => PreconditionFailed(views.html.errors.accountMigration())
        case Left(NotSignedUp) => NotFound(views.html.errors.notSignedUp())
        case _ =>
          Logger.warn("[ConfirmClientVrnController][show] Error returned from GetCustomerDetails")
          errorHandler.showInternalServerError
      }
  }

  def changeClient: Action[AnyContent] = authenticate {
    implicit user =>
      val redirectUrl = user.session.get(SessionKeys.redirectUrl).getOrElse {
        if (appConfig.features.whereToGoFeature()) "" else appConfig.manageVatCustomerDetailsUrl
      }
      Redirect(controllers.agent.routes.SelectClientVrnController.show(redirectUrl))
        .removingFromSession(SessionKeys.clientVRN)
  }

  def redirect: Action[AnyContent] = authenticate {
    implicit user =>

      if(appConfig.features.whereToGoFeature()) {
        user.session.get(SessionKeys.redirectUrl) match {
          case Some(redirectUrl) =>
            user.session.get(SessionKeys.preference) match {
              case Some(_) => Redirect(redirectUrl).removingFromSession(SessionKeys.redirectUrl)
              case None => Redirect(controllers.agent.routes.CapturePreferenceController.show())
            }
          case _ =>
            Logger.debug("[ConfirmClientVrnController][redirect] User has come from portal. Redirecting to 'What To Do' page.")
            Redirect(controllers.agent.routes.WhatToDoController.show())
        }
      //TODO: all remaining logic will become redundant and should be removed when whereToGoFeature permanently on
      } else {
        user.session.get(SessionKeys.redirectUrl) match {
          case Some(redirectUrl) => Redirect(redirectUrl)
            .removingFromSession(SessionKeys.redirectUrl)

          case _ =>
            Logger.debug("[ConfirmClientVrnController][show] - No redirect URL was found in session")
            errorHandler.showInternalServerError
        }
      }
  }
}
