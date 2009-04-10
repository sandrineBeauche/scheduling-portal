/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.util.logforwarder;

import java.io.Serializable;

import org.apache.log4j.Appender;


/**
 * An appender provider is a container that can be sent over the network and that contains a log appender.
 * Actual creation and activation of the contained appender should be performed by the getAppender() method
 * on receiver side.
 */
public interface AppenderProvider extends Serializable {

    /**
     * Create and return the contained appender. Note that several call to getAppender() return the same appender instance is not specified.
     * @return an instance of the contained log appender.
     * @throws LogForwardingException if the appender cannot be created or activated.
     */
    public Appender getAppender() throws LogForwardingException;

}