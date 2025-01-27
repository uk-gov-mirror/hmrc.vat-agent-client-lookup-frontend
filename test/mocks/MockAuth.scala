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

package mocks

import controllers.predicates._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import _root_.services.EnrolmentsAuthService
import audit.AuditService
import config.ErrorHandler
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import utils.TestUtil
import assets.BaseTestConstants._
import org.scalatestplus.mockito.MockitoSugar
import views.html.errors.agent.UnauthorisedNoEnrolmentView
import views.html.errors.{SessionTimeoutView, StandardErrorView}

import scala.concurrent.Future

trait MockAuth extends TestUtil with BeforeAndAfterEach with MockitoSugar {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }

  lazy val mockAuthConnector: AuthConnector = mock[AuthConnector]

  def setupAuthResponse(authResult: Future[~[Option[AffinityGroup], Enrolments]]): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] = {
    when(mockAuthConnector.authorise(
      ArgumentMatchers.any(), ArgumentMatchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(
      ArgumentMatchers.any(), ArgumentMatchers.any())
    ).thenReturn(authResult)
  }

  val mockEnrolmentsAuthService: EnrolmentsAuthService = new EnrolmentsAuthService(mockAuthConnector)

  val mockErrorHandler: ErrorHandler = new ErrorHandler(messagesApi, inject[StandardErrorView], mockConfig)

  val mockAuthAsAgentWithClient: AuthoriseAsAgentWithClient =
    new AuthoriseAsAgentWithClient(
      mockEnrolmentsAuthService,
      inject[AuditService],
      inject[ErrorHandler],
      mcc,
      inject[SessionTimeoutView],
      mockConfig,
      ec
    )

  val mockAgentOnlyAuthPredicate: AuthoriseAsAgentOnly =
    new AuthoriseAsAgentOnly(
      mockEnrolmentsAuthService,
      inject[ErrorHandler],
      mcc,
      inject[UnauthorisedNoEnrolmentView],
      inject[SessionTimeoutView],
      mockConfig
    )

  val mockPreferencePredicate: PreferencePredicate = new PreferencePredicate(
    mcc,
    mockConfig,
    ec
  )

  def mockIndividualAuthorised(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.successful(
      new ~(Some(AffinityGroup.Individual),
        Enrolments(Set(Enrolment("HMRC-MTD-VAT",
          Seq(EnrolmentIdentifier("VRN", vrn)),
          "Activated"
        )))
      )
    ))

  def mockAgentAuthorised(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.successful(
      new ~(Some(AffinityGroup.Agent),
        Enrolments(Set(Enrolment("HMRC-AS-AGENT",
          Seq(EnrolmentIdentifier("AgentReferenceNumber", arn)),
          "Activated",
          Some("mtd-vat-auth")
        )))
      )
    ))

  def mockAgentWithoutEnrolment(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.successful(
      new ~(Some(AffinityGroup.Agent),
        Enrolments(Set(Enrolment("OTHER_ENROLMENT",
          Seq(EnrolmentIdentifier("", "")),
          "Activated"
        )))
      )
    ))

  def mockUserWithoutAffinity(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.successful(
      new ~(None,
        Enrolments(Set(Enrolment("HMRC-MTD-VAT",
          Seq(EnrolmentIdentifier("VRN", vrn)),
          "Activated"
        )))
      )
    ))

  def mockAgentWithoutAffinity(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.successful(
      new ~(None,
        Enrolments(Set(Enrolment("HMRC-AS-AGENT",
          Seq(EnrolmentIdentifier("AgentReferenceNumber", arn)),
          "Activated",
          Some("mtd-vat-auth")
        )))
      )
    ))

  def mockMissingBearerToken()(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.failed(MissingBearerToken()))

  def mockUnauthorised()(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] =
    setupAuthResponse(Future.failed(InsufficientEnrolments()))

}
