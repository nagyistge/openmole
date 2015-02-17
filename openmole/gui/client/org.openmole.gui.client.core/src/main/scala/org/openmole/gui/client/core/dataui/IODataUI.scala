package org.openmole.gui.client.core.dataui

/*
 * Copyright (C) 13/02/15 // mathieu.leclaire@openmole.org
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
import rx._

trait IODataUI {

  def inputExtraFieldsFactory: IOMappingsFactory = IOMappingsFactory.default

  def outputExtraFieldsFactory: IOMappingsFactory = IOMappingsFactory.default

  lazy val inputDataUI: Var[InputDataUI] = Var(new InputDataUI(inputExtraFieldsFactory))

  lazy val outputDataUI: Var[OutputDataUI] = Var(new OutputDataUI(outputExtraFieldsFactory))

  def reset = {
    inputDataUI() = new InputDataUI(inputExtraFieldsFactory)
    outputDataUI() = new OutputDataUI(outputExtraFieldsFactory)
  }

}