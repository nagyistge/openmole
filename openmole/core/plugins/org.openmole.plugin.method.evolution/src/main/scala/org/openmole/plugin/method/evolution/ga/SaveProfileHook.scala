/*
 * Copyright (C) 08/01/13 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.plugin.method.evolution.ga

import org.openmole.core.implementation.data._
import org.openmole.core.implementation.mole._
import org.openmole.core.implementation.tools._
import org.openmole.core.model.data._
import org.openmole.core.model.mole._
import org.openmole.misc.tools.io.FileUtil._

object SaveProfileHook {

  def apply(puzzle: GAPuzzle[GenomeProfile], dir: String): HookBuilder = apply(puzzle, dir, "profile${" + puzzle.generation.name + "}.csv")

  def apply(puzzle: GAPuzzle[GenomeProfile], dir: String, name: String): HookBuilder =
    new HookBuilder {
      addInput(puzzle.population)
      val _puzzle = puzzle
      val _path = dir + "/" + name

      def toHook = new SaveProfileHook with Built {
        val puzzle = _puzzle
        val path = _path
      }
    }

}

abstract class SaveProfileHook extends Hook {

  val puzzle: GAPuzzle[GenomeProfile]
  val path: String

  def process(context: Context, executionContext: ExecutionContext) = {
    val file = executionContext.relativise(VariableExpansion(context, path))
    file.createParentDir
    file.withWriter { w ⇒
      for {
        i ← context(puzzle.population).toIndividuals
      } {
        val scaledGenome = puzzle.evolution.toVariables(i.genome, context)
        w.write("" + scaledGenome(puzzle.evolution.x).value + "," + puzzle.evolution.aggregate(i.fitness) + "\n")
      }
    }
    context
  }

}