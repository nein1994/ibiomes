/*
 * iBIOMES - Integrated Biomolecular Simulations
 * Copyright (C) 2014  Julien Thibault, University of Utah
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
package edu.utah.bmi.ibiomes.parse.chem;

import java.util.List;

import edu.utah.bmi.ibiomes.experiment.ExperimentTask;
import edu.utah.bmi.ibiomes.parse.LocalFile;

/**
 * Method parameter file interface
 * @author Julien Thibault, University of Utah
 *
 */
public interface MethodParameterFile extends LocalFile {

	/**
	 * Get tasks
	 * @return Tasks
	 */
	public List<ExperimentTask> getTasks();
}
