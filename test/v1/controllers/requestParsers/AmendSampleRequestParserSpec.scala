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

import play.api.libs.json.{JsObject, Json}
import support.UnitSpec
import uk.gov.hmrc.domain.Vrn
import v1.mocks.validators.MockAmendSampleValidator
import v1.models.errors._
import v1.models.request.amendSample.{AmendSampleRawData, AmendSampleRequest, AmendSampleRequestBody}

class AmendSampleRequestParserSpec extends UnitSpec {
  val vrn = "123456789"
  val taxYear = "2017-18"
  val calcId = "someCalcId"

  private val requestBodyJson = Json.parse(
    """{
      |  "data" : "someData"
      |}
    """.stripMargin)

  val inputData: AmendSampleRawData = AmendSampleRawData(vrn, requestBodyJson)

  trait Test extends MockAmendSampleValidator {
    lazy val parser = new AmendSampleRequestParser(mockAmendSampleValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendSampleValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(AmendSampleRequest(Vrn(vrn), AmendSampleRequestBody("someData")))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendSampleValidator.validate(inputData)
          .returns(List(VrnFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, VrnFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendSampleValidator.validate(inputData.copy(body = JsObject.empty))
          .returns(List(VrnFormatError, RuleIncorrectOrEmptyBodyError))

        parser.parseRequest(inputData.copy(body = JsObject.empty)) shouldBe
          Left(ErrorWrapper(None, BadRequestError, Some(Seq(VrnFormatError, RuleIncorrectOrEmptyBodyError))))
      }
    }
  }
}
