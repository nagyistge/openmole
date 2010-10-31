/*
 * Copyright (C) 2010 reuillon
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

package org.openmole.core.implementation.execution.batch

import org.openmole.core.model.execution.batch.IBatchEnvironment
import org.openmole.core.model.execution.batch.IBatchExecutionJob
import java.util.logging.Level
import java.util.logging.Logger
import org.openmole.commons.exception.InternalProcessingError
import org.openmole.commons.exception.UserBadDataError
import org.openmole.core.model.execution.ExecutionState._
import org.openmole.core.model.job.IJob
import org.openmole.misc.updater.IUpdatable
import scala.collection.mutable.ListBuffer

class BatchJobWatcher(environment: IBatchEnvironment[_]) extends IUpdatable {

    override def update: Boolean = {
        val registry = environment.jobRegistry
        val jobGroupsToRemove = new ListBuffer[IJob]
        
        registry.synchronized  {
            for (val job <- registry.getAllJobs) {

                if (job.allMoleJobsFinished()) {

                    for (val ej <- registry.getExecutionJobsFor(job)) {
                        ej.kill
                    }

                    jobGroupsToRemove += job
                } else {

                    val executionJobsToRemove = new ListBuffer[IBatchExecutionJob[_]]

                    for (ej <- registry.getExecutionJobsFor(job)) {
                        ej.state match {
                            case KILLED => executionJobsToRemove += ej
                            case _ =>
                        }
                    }

                    for (ej <- executionJobsToRemove) {
                        registry.remove(ej)
                    }

                    if (registry.getNbExecutionJobsForJob(job) == 0) {
                        try {
                            environment.submit(job)
                        } catch {
                          case(e) => Logger.getLogger(classOf[BatchJobWatcher].getName).log(Level.SEVERE, "Submission of job failed, job isn't being executed.", e)
                        }
                    }
                }
            }

            for (j <- jobGroupsToRemove) {
                registry.removeJob(j)
            }
        }

        true
    }
}
