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

package v1.controllers.requestParsers

import support.UnitSpec
import uk.gov.hmrc.domain.Vrn
import v1.controllers.requestParsers.validators.Validator
import v1.models.errors.{BadRequestError, ErrorWrapper, VrnFormatError, RuleIncorrectOrEmptyBodyError}
import v1.models.request.RawData

class RequestParserSpec extends UnitSpec {

  private val vrn = "123456789"
  case class Raw(vrn: String) extends RawData
  case class Request(vrn: Vrn)

  trait Test {
    test =>

    val validator: Validator[Raw]

    val parser: RequestParser[Raw, Request] = new RequestParser[Raw, Request] {
      val validator: Validator[Raw] = test.validator

      protected def requestFor(data: Raw): Request = Request(Vrn(data.vrn))
    }
  }

  "parse" should {
    "return a Request" when {
      "the validator returns no errors" in new Test {
        lazy val validator: Validator[Raw] = (_: Raw) => Nil

        parser.parseRequest(Raw(vrn)) shouldBe Right(Request(Vrn(vrn)))
      }
    }

    "return a single error" when {
      "the validator returns a single error" in new Test {
        lazy val validator: Validator[Raw] = (_: Raw) => List(VrnFormatError)

        parser.parseRequest(Raw(vrn)) shouldBe Left(ErrorWrapper(None, VrnFormatError, None))
      }
    }

    "return multiple errors" when {
      "the validator returns multiple errors" in new Test {
        lazy val validator: Validator[Raw] = (_: Raw) => List(VrnFormatError, RuleIncorrectOrEmptyBodyError)

        parser.parseRequest(Raw(vrn)) shouldBe Left(ErrorWrapper(None, BadRequestError, Some(Seq(VrnFormatError, RuleIncorrectOrEmptyBodyError))))
      }
    }
  }

}
