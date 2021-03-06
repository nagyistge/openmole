/*
 * Copyright (C) 2010 Romain Reuillon
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

package org.openmole.plugin.environment.egi

import org.openmole.core.updater.IUpdatableWithVariableDelay
import org.openmole.core.workflow.execution.ExecutionState._
import org.openmole.core.workflow.job.Job
import org.openmole.core.workspace.{ ConfigurationLocation, Workspace }
import org.openmole.plugin.environment.batch.environment.BatchEnvironment
import org.openmole.tool.logger.Logger
import squants.time.Time

import scala.collection.immutable.TreeSet
import scala.collection.mutable
import scala.collection.mutable.{ HashMap, MultiMap, Set }
import scala.ref.WeakReference

object EagerSubmissionAgent extends Logger {

  case class HistoryPoint(running: Int, total: Int, time: Long = System.currentTimeMillis)

  implicit class FiniteQueue[A <: { def time: Long }](q: mutable.Queue[A]) {
    def enqueueFinite(elem: A, keepTime: Time) = {
      q.enqueue(elem)
      q.dequeueFirst(e ⇒ e.time + keepTime.millis < System.currentTimeMillis)
    }
  }
}

import org.openmole.plugin.environment.egi.EagerSubmissionAgent._
import Log._

class EagerSubmissionAgent(environment: WeakReference[BatchEnvironment], thresholdPreference: ConfigurationLocation[Double]) extends IUpdatableWithVariableDelay {

  @transient lazy val runningHistory = new mutable.Queue[HistoryPoint]

  override def delay = Workspace.preference(EGIEnvironment.EagerSubmissionInterval)

  override def update: Boolean = {
    try {
      logger.log(FINE, "Eager submission started")

      val env = environment.get match {
        case None      ⇒ return false
        case Some(env) ⇒ env
      }

      val jobs = env.jobs

      val executionJobs = jobs.groupBy(_.job)
      val jobSize = jobs.size
      val stillRunning = jobs.count(_.state == RUNNING)
      val stillReady = jobs.count(_.state == READY)

      runningHistory.enqueueFinite(
        HistoryPoint(running = stillRunning, total = jobSize),
        Workspace.preference(EGIEnvironment.RunningHistoryDuration)
      )

      logger.fine("still running " + stillRunning)

      val maxTotal = runningHistory.map(_.total).max
      val shouldBeRunning = runningHistory.map(_.running).max * Workspace.preference(thresholdPreference)

      val minOversub = Workspace.preference(EGIEnvironment.EagerSubmissionMinNumberOfJob)

      var nbRessub =
        if (jobSize < minOversub) minOversub - jobSize
        else if (jobSize < maxTotal) shouldBeRunning - (stillRunning + stillReady) else 0

      val numberOfSimultaneousExecutionForAJobWhenUnderMinJob = Workspace.preference(EGIEnvironment.EagerSubmissionNumberOfJobUnderMin)

      logger.fine("resubmit " + nbRessub)

      if (nbRessub > 0) {
        // Resubmit nbRessub jobs in a fair manner
        val order = new HashMap[Int, Set[Job]] with MultiMap[Int, Job]
        var keys = new TreeSet[Int]

        for (job ← executionJobs.keys) {
          val nb = executionJobs(job).size
          if (nb < numberOfSimultaneousExecutionForAJobWhenUnderMinJob) {
            order.addBinding(nb, job)
            keys += nb
          }
        }

        if (!keys.isEmpty) {
          while (nbRessub > 0 && keys.head < numberOfSimultaneousExecutionForAJobWhenUnderMinJob) {
            val key = keys.head
            val jobs = order(keys.head)
            val job =
              jobs.find(j ⇒ executionJobs(j).isEmpty) match {
                case Some(j) ⇒ j
                case None ⇒
                  jobs.find(j ⇒ !executionJobs(j).exists(_.state != SUBMITTED)) match {
                    case Some(j) ⇒ j
                    case None    ⇒ jobs.head
                  }
              }

            env.submit(job)

            order.removeBinding(key, job)
            if (jobs.isEmpty) keys -= key

            order.addBinding(key + 1, job)
            keys += (key + 1)
            nbRessub -= 1
          }
        }

      }
    }
    catch {
      case e: Throwable ⇒ Log.logger.log(Log.SEVERE, "Exception in oversubmission agen", e)
    }
    true
  }

}
