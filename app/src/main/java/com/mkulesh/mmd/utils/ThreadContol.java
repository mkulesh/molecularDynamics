/*
 * **************************************************************************
 * Molecular Dynamics - Particles under the microscope
 * **************************************************************************
 * Copyright (C) 2014-2017 Mikhail Kulesh
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
 * **************************************************************************
 */

package com.mkulesh.mmd.utils;

public class ThreadContol
{
    private int pauseStack = 0;
    private boolean isInterrupted = false;

    public ThreadContol()
    {
        super();
    }

    /**
     * Check whether the thread is paused
     */
    public boolean isPaused()
    {
        return pauseStack > 0;
    }

    /**
     * Check whether the thread is interrupted
     */
    public boolean isInterrupted()
    {
        return isInterrupted;
    }

    /**
     * Adds a new pause event into the stack
     */
    public void pause()
    {
        synchronized (this)
        {
            pauseStack++;
        }
    }

    /**
     * Sets an interruption flag
     */
    public void setInterrupted(boolean flag)
    {
        synchronized (this)
        {
            isInterrupted = flag;
        }
    }

    /**
     * Resumes one or all pause events from the stack
     */
    public void resume(boolean resumeAll)
    {
        synchronized (this)
        {
            if (resumeAll)
            {
                pauseStack = 0;
            }
            else if (pauseStack > 0)
            {
                pauseStack--;
            }
            if (pauseStack == 0)
            {
                this.notifyAll();
            }
        }
    }

}
