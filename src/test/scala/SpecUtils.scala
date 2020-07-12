/*
 * Copyright 2020 zio.crew
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

package zio.arrow
import zio.{ URIO }

object SpecUtils {
  val plusOne  = (_: Int) + 1
  val minusOne = (_: Int) - 1
  val mulTwo   = (_: Int) * 2

  val add1: ZArrow[Nothing, Int, Int] = ZArrow(plusOne)
  val min1: ZArrow[Nothing, Int, Int] = ZArrow(minusOne)
  val mul2: ZArrow[Nothing, Int, Int] = ZArrow(mulTwo)

  val zioAdd1: URIO[Int, Int] = URIO.access(plusOne)

  val greaterThan0 = ZArrow((_: Int) > 0)
  val lessThan10   = ZArrow((_: Int) < 10)

  val thrower = ZArrow.effect[String, Int, Int] { case _: Throwable => "error" }(_ => throw new Exception)
}
