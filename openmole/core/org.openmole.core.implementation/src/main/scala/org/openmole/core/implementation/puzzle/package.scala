/*
 * Copyright (C) 2012 reuillon
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
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.core.implementation

import org.openmole.core.implementation.task.TaskBuilder
import org.openmole.core.model.mole.ICapsule
import mole._
import org.openmole.core.model.task.ITask

package object puzzle {
  
  implicit def capsuleToPuzzle(capsule: ICapsule): PuzzleFirstAndLast = new PuzzleFirstAndLast(capsule, capsule) 
  //implicit def taskToPuzzle(task: ITask): PuzzleFirstAndLast = capsuleToPuzzle(new Capsule(task)) 
  //implicit def builderToPuzzle(builder: TaskBuilder): PuzzleFirstAndLast = taskToPuzzle(builder.toTask) 
  
}