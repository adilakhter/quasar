/*
 * Copyright 2014–2016 SlamData Inc.
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

package quasar.specs2

import quasar.Predef._
import quasar.RenderTree, RenderTree.ops._
import quasar.fp._

import org.specs2.matcher._
import scalaz._, Scalaz._

trait ValidationMatchers extends org.specs2.scalaz.ValidationMatchers {
  def beEqualIfSuccess[E, A](expected: Validation[E, A]) =
    new Matcher[Validation[E, A]] {
      def apply[S <: Validation[E, A]](s: Expectable[S]) = {
        val v = s.value

        v.fold(
          κ(result(
            expected.fold(κ(true), κ(false)),
            "both failed",
            s"$v is not $expected",
            s)),
            a => expected.fold(
              κ(result(false, "", "expected failure", s)),
              ex => result(a == ex, "both are equal", s"$a is not $ex", s)))
    }
  }
}

trait DisjunctionMatchers extends org.specs2.scalaz.DisjunctionMatchers {
  def beRightDisjOrDiff[A, B: Equal](expected: B)(implicit rb: RenderTree[B]): Matcher[A \/ B] = new Matcher[A \/ B] {
    def apply[S <: A \/ B](s: Expectable[S]) = {
      val v = s.value
      v.fold(
        a => result(false, s"$v is right", s"$v is not right", s),
        b => {
          val d = (b.render diff expected.render).shows
          result(b ≟ expected,
            s"\n$v is right and equals:\n$d",
            s"\n$v is right but does not equal:\n$d",
            s)
        })
    }
  }
}

object QuasarMatchers extends ValidationMatchers with DisjunctionMatchers
