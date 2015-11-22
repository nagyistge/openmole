/*
 * Copyright (C) 2014 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.core.workflow.tools

import java.io.File

import org.openmole.core.tools.io.FromString
import org.openmole.core.workflow.data._

object FromContext {

  implicit def fromTToContext[T](t: T): FromContext[T] = FromContext.value[T](t)

  implicit def fromStringToContext[T: FromString](code: String): FromContext[T] =
    new FromContext[T] {
      @transient lazy val proxy = ScalaWrappedCompilation.dynamic(code)
      override def from(context: ⇒ Context)(implicit rng: RandomProvider): T =
        implicitly[FromString[T]].apply(proxy.run(context).toString)
    }

  def value[T](t: T): FromContext[T] =
    new FromContext[T] {
      def from(context: ⇒ Context)(implicit rng: RandomProvider): T = t
    }

  def apply[T](f: (Context, RandomProvider) => T) =
    new FromContext[T] {
      def from(context: ⇒ Context)(implicit rng: RandomProvider) = f(context, rng)
    }

}

trait FromContext[+T] {
  def flatMap[U](f: T => FromContext[U]) =  FromContext { (context, rng) =>
    val res = from(context)(rng)
    f(res).from(context)(rng)
  }
  def map[U](f: T => U) = FromContext((context, rng) => f(from(context)(rng)))
  def from(context: ⇒ Context)(implicit rng: RandomProvider): T
}

object ExpandedString {

  implicit def fromStringToExpandedString(s: String) = ExpandedString(s)
  implicit def fromStringToExpandedStringOption(s: String) = Some[ExpandedString](s)
  implicit def fromTraversableOfStringToTraversableOfExpandedString[T <: Traversable[String]](t: T) = t.map(ExpandedString(_))
  implicit def fromFileToExpandedString(f: File) = ExpandedString(f.getPath)

  def apply(s: String) =
    new ExpandedString {
      override def string = s
    }
}

trait ExpandedString <: FromContext[String] {
  @transient lazy val expansion = VariableExpansion(string)
  def +(s: ExpandedString): ExpandedString = string + s.string
  def string: String
  def from(context: ⇒ Context)(implicit rng: RandomProvider) = expansion.expand(context)
}

