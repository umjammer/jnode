/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.util;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * @author epr
 */
public class QueueProcessorThread<T> extends Thread {

    private static final Logger bootlog = System.getLogger("bootlog");

    /**
     * The queue i'm processing
     */
    private final Queue<T> queue;

    /**
     * The actual processor
     */
    private final QueueProcessor<T> processor;

    private boolean stop;

    /**
     * Create a new instance
     *
     * @param name the name
     * @param queue the queue
     * @param processor the processor
     */
    public QueueProcessorThread(String name, Queue<T> queue, QueueProcessor<T> processor) {
        super(name);
        this.queue = queue;
        this.processor = processor;
        this.stop = false;
    }

    /**
     * Create a new instance. A new queue is automatically created.
     *
     * @param name the name
     * @param processor the processor
     * @see #getQueue()
     */
    public QueueProcessorThread(String name, QueueProcessor<T> processor) {
        this(name, new Queue<>(), processor);
    }

    /**
     * Stop the processor
     */
    public void stopProcessor() {
        this.stop = true;
        this.queue.close();
//        this.interrupt();
    }

    /**
     * Handle an exception thrown during the processing of the object.
     *
     * @param ex the exception
     */
    protected void handleException(Exception ex) {
        bootlog.log(Level.ERROR, "Exception in QueueProcessor: " + getName(), ex);
    }

    /**
     * Handle an exception thrown during the processing of the object.
     *
     * @param ex the exception
     */
    protected void handleError(Error ex) {
        bootlog.log(Level.ERROR, "Error in QueueProcessor: " + getName(), ex);
    }

    /**
     * Thread runner
     */
    @Override
    public void run() {
        while (!stop) {
            try {
                final T object = queue.get(false);
                if (object != null) {
                    processor.process(object);
                }
            } catch (Exception ex) {
                handleException(ex);
            } catch (Error ex) {
                handleError(ex);
            }
        }
    }

    /**
     * Gets this queue this thread works on.
     *
     * @return The queue
     */
    public Queue<T> getQueue() {
        return queue;
    }
}
