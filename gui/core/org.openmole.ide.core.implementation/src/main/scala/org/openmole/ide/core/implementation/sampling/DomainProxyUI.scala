/*
 * Copyright (C) 2011 <mathieu.Mathieu Leclaire at openmole.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
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
package org.openmole.ide.core.implementation.sampling

import org.openmole.ide.core.model.data.{ IFactorDataUI, IDomainDataUI }
import org.openmole.ide.core.model.sampling.{ IFactorProxyUI, IDomainProxyUI }
import org.openmole.ide.core.implementation.data.FactorDataUI
import org.openmole.ide.core.model.dataproxy.IPrototypeDataProxyUI

class DomainProxyUI(var dataUI: IDomainDataUI) extends IDomainProxyUI {

  def testTypes = true

  /*def testTypes = {

 println("COMPARE ON " + id)
 factorDataUI match {
   case Some(f: IFactorDataUI) ⇒ f.prototype match {
     case p: IPrototypeDataProxyUI ⇒
       println("-- " + p.dataUI.protoType.toString.split('.').last)
       println("-- " + dataUI.domainType.toString.split('.').last)
       p.dataUI.protoType.toString.split('.').last == dataUI.domainType.toString.split('.').last
     case _ ⇒ println("no proto"); true
   }
   case _ ⇒ println("no factor"); true
 }
}   */
}