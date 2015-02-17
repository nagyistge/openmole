package org.openmole.gui.client.core

/*
 * Copyright (C) 16/12/14 // mathieu.leclaire@openmole.org
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

import org.openmole.gui.ext.dataui.PanelUI
import rx._

package object dataui {

  def inputUI(protoDataBagUI: PrototypeDataBagUI, default: Var[Option[String]] = Var(None)) =
    InputUI(protoDataBagUI.uuid, protoDataBagUI, default, Var(IOMappingsFactory.default.build))

  def outputUI(protoDataBagUI: PrototypeDataBagUI) =
    OutputUI(protoDataBagUI.uuid, protoDataBagUI)

  case class InputUI(id: String, protoDataBagUI: PrototypeDataBagUI, default: Var[Option[String]] = Var(None), extraInputFields: Var[IOMappingsUI])

  case class OutputUI(id: String, protoDataBagUI: PrototypeDataBagUI)

}