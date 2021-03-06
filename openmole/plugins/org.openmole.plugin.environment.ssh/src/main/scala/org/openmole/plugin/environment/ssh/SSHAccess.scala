/*
 * Copyright (C) 2012 reuillon
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

package org.openmole.plugin.environment.ssh

import fr.iscpif.gridscale.ssh.{ SSHHost, SSHAuthentication ⇒ SA }
import org.openmole.core.workspace.Workspace
import squants.time.TimeConversions._

trait SSHAccess extends SSHHost { s ⇒

  def user: String
  def credential: SA
  def maxConnections = Workspace.preference(SSHEnvironment.MaxConnections)
  override def timeout = Workspace.preference(SSHService.timeout)

}
