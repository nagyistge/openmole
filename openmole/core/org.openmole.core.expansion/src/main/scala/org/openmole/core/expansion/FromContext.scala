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

package org.openmole.core.expansion

import org.openmole.core.context._
import org.openmole.core.exception._
import org.openmole.tool.cache._
import org.openmole.tool.random._
import org.openmole.tool.file._

import cats._
import cats.implicits._

trait LowPriorityToFromContext {
  implicit def fromTToContext[T] = ToFromContext[T, T](t ⇒ FromContext.value[T](t))
}

object ToFromContext extends LowPriorityToFromContext {
  import FromContext._

  def apply[F, T](func: F ⇒ FromContext[T]): ToFromContext[F, T] = new ToFromContext[F, T] {
    def apply(f: F) = func(f)
  }

  implicit def functionToFromContext[T] =
    ToFromContext[(Context ⇒ T), FromContext[T]](f ⇒ FromContext((c, _) ⇒ f(c)))

  implicit def codeToFromContextFloat = ToFromContext(codeToFromContext[Float])
  implicit def codeToFromContextDouble = ToFromContext(codeToFromContext[Double])
  implicit def codeToFromContextLong = ToFromContext(codeToFromContext[Long])
  implicit def codeToFromContextInt = ToFromContext(codeToFromContext[Int])
  implicit def codeToFromContextBigDecimal = ToFromContext(codeToFromContext[BigDecimal])
  implicit def codeToFromContextBigInt = ToFromContext(codeToFromContext[BigInt])
  implicit def codeToFromContextBoolean = ToFromContext(codeToFromContext[Boolean])

  implicit def fileToString = ToFromContext[File, String](f ⇒ ExpandedString(f.getPath))
  implicit def stringToString = ToFromContext[String, String](s ⇒ ExpandedString(s))
  implicit def stringToFile = ToFromContext[String, File](s ⇒ ExpandedString(s).map(s ⇒ File(s)))
  implicit def fileToFile = ToFromContext[File, File](f ⇒ ExpandedString(f.getPath).map(s ⇒ File(s)))

  implicit def booleanToCondition = ToFromContext[Boolean, Boolean](b ⇒ FromContext.value(b))
  implicit def booleanPrototypeIsCondition: ToFromContext[Val[Boolean], Boolean] = ToFromContext[Val[Boolean], Boolean](p ⇒ prototype(p))
}

trait ToFromContext[F, T] {
  def apply(f: F): FromContext[T]
}

trait LowPriorityFromContext {
  def contextConverter[F, T](f: F)(implicit tfc: ToFromContext[F, T]): FromContext[T] = tfc(f)
  implicit def fromTToContext[T](t: T): FromContext[T] = contextConverter(t)
}

object FromContext extends LowPriorityFromContext {

  implicit val applicative: Applicative[FromContext] = new Applicative[FromContext] {
    override def pure[A](x: A): FromContext[A] = FromContext.value(x)
    override def ap[A, B](ff: FromContext[(A) ⇒ B])(fa: FromContext[A]): FromContext[B] =
      new FromContext[B] {
        override def from(context: ⇒ Context)(implicit rng: RandomProvider): B = {
          val res = fa.from(context)(rng)
          ff.from(context)(rng)(res)
        }

        override def validate(inputs: Seq[Val[_]]): Seq[Throwable] =
          fa.validate(inputs) ++ ff.validate(inputs)
      }
  }

  def codeToFromContext[T: Manifest](code: String): FromContext[T] =
    new FromContext[T] {
      val proxy = Cache(ScalaWrappedCompilation.dynamic[T](code))
      override def from(context: ⇒ Context)(implicit rng: RandomProvider): T = proxy()().from(context)
      override def validate(inputs: Seq[Val[_]]): Seq[Throwable] = proxy().validate(inputs).toSeq
    }

  implicit def functionToFromContext[T](f: (Context ⇒ T)) = contextConverter(f)
  implicit def codeToFromContextFloat(s: String) = contextConverter[String, Float](s)
  implicit def codeToFromContextDouble(s: String) = contextConverter[String, Double](s)
  implicit def codeToFromContextLong(s: String) = contextConverter[String, Long](s)
  implicit def codeToFromContextInt(s: String) = contextConverter[String, Int](s)
  implicit def codeToFromContextBigDecimal(s: String) = contextConverter[String, BigDecimal](s)
  implicit def codeToFromContextBigInt(s: String) = contextConverter[String, BigInt](s)
  implicit def codeToFromContextBoolean(s: String) = contextConverter[String, Boolean](s)

  implicit def fileToString(f: File) = contextConverter[File, String](f)
  implicit def stringToString(s: String) = contextConverter[String, String](s)
  implicit def stringToFile(s: String) = contextConverter[String, File](s)
  implicit def fileToFile(f: File) = contextConverter[File, File](f)

  implicit def booleanToCondition(b: Boolean) = contextConverter[Boolean, Boolean](b)
  implicit def booleanPrototypeIsCondition(p: Val[Boolean]) = contextConverter[Val[Boolean], Boolean](p)

  def prototype[T](p: Val[T]) =
    new FromContext[T] {
      override def from(context: ⇒ Context)(implicit rng: RandomProvider) = context(p)
      def validate(inputs: Seq[Val[_]]): Seq[Throwable] = {
        if (inputs.exists(_ == p)) Seq.empty else Seq(new UserBadDataError(s"Prototype $p not found"))
      }
    }

  def value[T](t: T): FromContext[T] =
    new FromContext[T] {
      def from(context: ⇒ Context)(implicit rng: RandomProvider): T = t
      def validate(inputs: Seq[Val[_]]): Seq[Throwable] = Seq.empty
    }

  def apply[T](f: (Context, RandomProvider) ⇒ T) =
    new FromContext[T] {
      def from(context: ⇒ Context)(implicit rng: RandomProvider) = f(context, rng)
      def validate(inputs: Seq[Val[_]]): Seq[Throwable] = Seq.empty
    }

  implicit class ConditionDecorator(f: Condition) {
    def unary_! = f.map(v ⇒ !v)

    def &&(d: Condition): Condition =
      new FromContext[Boolean] {
        override def from(context: ⇒ Context)(implicit rng: RandomProvider): Boolean = f.from(context) && d.from(context)
        override def validate(inputs: Seq[Val[_]]): Seq[Throwable] = f.validate(inputs) ++ d.validate(inputs)
      }

    def ||(d: Condition): Condition =
      new FromContext[Boolean] {
        override def from(context: ⇒ Context)(implicit rng: RandomProvider): Boolean = f.from(context) || d.from(context)
        override def validate(inputs: Seq[Val[_]]): Seq[Throwable] = f.validate(inputs) ++ d.validate(inputs)
      }
  }

  implicit class ExpandedStringOperations(s1: FromContext[String]) {
    def +(s2: FromContext[String]) = (s1 map2 s2)(_ + _)
  }

  implicit class FromContextFileDecorator(f: FromContext[File]) {
    def exists = f.map(_.exists)
    def isEmpty = f.map(_.isEmpty)
    def /(path: FromContext[String]) = (f map2 path)(_ / _)
  }

}

trait FromContext[+T] {
  def from(context: ⇒ Context)(implicit rng: RandomProvider): T
  def validate(inputs: Seq[Val[_]]): Seq[Throwable]
}

object Expandable {
  def apply[S, T](f: S ⇒ FromContext[T]) = new Expandable[S, T] {
    override def expand(s: S): FromContext[T] = f(s)
  }

  implicit def stringToString = Expandable[String, String](s ⇒ s: FromContext[String])
  implicit def stringToFile = Expandable[String, File](s ⇒ s: FromContext[File])
  implicit def fileToFile = Expandable[File, File](f ⇒ f: FromContext[File])
}

trait Expandable[S, T] {
  def expand(s: S): FromContext[T]
}

