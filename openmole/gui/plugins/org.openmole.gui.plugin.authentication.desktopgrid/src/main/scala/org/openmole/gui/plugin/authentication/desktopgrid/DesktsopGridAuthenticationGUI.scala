/**
 * Created by Romain Reuillon on 28/11/16.
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
 *
 */
package org.openmole.gui.plugin.authentication.desktopgrid

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import org.openmole.gui.ext.data.{ AuthenticationPlugin, AuthenticationPluginFactory }
import org.openmole.gui.ext.tool.client.OMPost
import fr.iscpif.scaladget.api.{ BootstrapTags ⇒ bs }
import fr.iscpif.scaladget.stylesheet.{ all ⇒ sheet }
import autowire._
import sheet._
import bs._
import org.scalajs.dom.raw.HTMLElement
import org.openmole.gui.ext.data._

import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._

@JSExport
class DesktopGridAuthenticationFactory extends AuthenticationPluginFactory {
  type AuthType = DesktopGridAuthenticationData

  def buildEmpty: AuthenticationPlugin = new DesktsopGridAuthenticationGUI()

  def build(data: AuthType): AuthenticationPlugin = new DesktsopGridAuthenticationGUI(data)

  def name = "Desktop grid"

  def getData: Future[Seq[AuthType]] = OMPost()[DesktopGridAuthenticationAPI].desktopGridAuthentications().call()
}

@JSExport
class DesktsopGridAuthenticationGUI(val data: DesktopGridAuthenticationData = DesktopGridAuthenticationData()) extends AuthenticationPlugin {
  type AuthType = DesktopGridAuthenticationData

  def factory = new DesktopGridAuthenticationFactory

  def remove(onremove: () ⇒ Unit) = OMPost()[DesktopGridAuthenticationAPI].removeAuthentication().call().foreach { _ ⇒
    onremove()
  }

  val passwordInput = bs.input(data.password)(placeholder := "Password", passwordType).render

  def panel: TypedTag[HTMLElement] = hForm(
    passwordInput.withLabel("Password")
  )

  def save(onsave: () ⇒ Unit): Unit = {
    OMPost()[DesktopGridAuthenticationAPI].updateAuthentication(DesktopGridAuthenticationData(passwordInput.value)).call().foreach { b ⇒
      onsave()
    }
  }

  def test: Future[Seq[Test]] = Future(Seq(Test.passed()))
}