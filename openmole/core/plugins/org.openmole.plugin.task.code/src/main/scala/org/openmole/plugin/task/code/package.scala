/*
 * Copyright (C) 2012 Romain Reuillon
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

package org.openmole.plugin.task

import java.io.File

import org.openmole.core.implementation.builder._
import org.openmole.misc.macros.Keyword._

package object code {

  lazy val imports = add[{ def addImport(s: String) }]
  lazy val libraries = add[{ def addLibrary(l: File) }]

  trait CodePackage extends external.ExternalPackage {
    lazy val imports = code.imports
    lazy val libraries = code.libraries
  }

}