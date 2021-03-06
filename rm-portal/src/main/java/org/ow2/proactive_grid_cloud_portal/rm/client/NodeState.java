/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.rm.client;

/**
 * Enumeration of all states of a RMNode :<BR>
 * -deploying : node deployment is on going. <BR>
 * -lost : node deployment failed. <BR>
 * -free : node is ready to perform a task.<BR>
 * -configuring: node has been added to the RM but is in configuring state, not ready to perform a task.<BR>
 * -busy : node is executing a task.<BR>
 * -to be removed : node is busy and have to be removed at the end of its current task.<BR>
 * -down : node is broken, and not anymore able to perform tasks.<BR>
 *
 *
 */
public enum NodeState {
    /**
     * a node that can be provided to a RM user, and able to perform a task
     */
    FREE("Free"),
    /**
     * a node provided to a RM user.
     */
    BUSY("Busy"),
    /**
     * a node that has been detected down.
     */
    DOWN("Down"),
    /**
     * a busy node which must be removed from resource manager when the user
     * will give back the node.
     */
    TO_BE_REMOVED("To_be_removed"),
    /**
     * a node for which one the deployment process has been triggered but which
     * is not registered yet.
     */
    DEPLOYING("Deploying"),
    /**
     * a node for which one the deployment process failed
     */
    LOST("Lost"),
    /**
     * a node cannot be provided to a RM user, it is under configuration
     */
    CONFIGURING("Configuring"),

    /**
     * a node state "locked" means that node cannot be used for computations by anyone
     * and the main difference with busy is that it remains locked after a user disconnects
     * from the resource manager (busy node becomes free in this case).
     */
    LOCKED("Locked");

    private String desc;

    /**
     * Constructor
     * @param nb state to specify.
     */
    NodeState(String desc) {
        this.desc = desc;
    }

    /**
     * @param value value returned by NodeState.toString()
     * @return enum instance corresponding the String representation provided
     * @throws IllegalArgumentException provided value is no good
     */
    public static NodeState parse(String value) {
        if (value.equalsIgnoreCase(FREE.toString()))
            return NodeState.FREE;
        else if (value.equalsIgnoreCase(BUSY.toString()))
            return NodeState.BUSY;
        else if (value.equalsIgnoreCase(DOWN.toString()))
            return NodeState.DOWN;
        else if (value.equalsIgnoreCase(TO_BE_REMOVED.toString()))
            return NodeState.TO_BE_REMOVED;
        else if (value.equalsIgnoreCase(DEPLOYING.toString()))
            return NodeState.DEPLOYING;
        else if (value.equalsIgnoreCase(LOST.toString()))
            return NodeState.LOST;
        else if (value.equalsIgnoreCase(CONFIGURING.toString()))
            return NodeState.CONFIGURING;
        else if (value.equalsIgnoreCase(LOCKED.toString()))
            return NodeState.LOCKED;
        else
            throw new IllegalArgumentException("'" + value + "' is not a valid NodeState");
    }

    /**
     * @param state arbitrary node state
     * @return icon name corresponding the state
     */
    public String getIcon() {
        switch (this) {
            case BUSY:
                return RMImages.instance.node_busy_16().getSafeUri().asString();
            case CONFIGURING:
                return RMImages.instance.node_configuring_16().getSafeUri().asString();
            case DEPLOYING:
                return RMImages.instance.node_deploying_16().getSafeUri().asString();
            case DOWN:
                return RMImages.instance.node_down_16().getSafeUri().asString();
            case FREE:
                return RMImages.instance.node_free_16().getSafeUri().asString();
            case LOCKED:
                return RMImages.instance.node_locked_16().getSafeUri().asString();
            case LOST:
                return RMImages.instance.node_lost_16().getSafeUri().asString();
            case TO_BE_REMOVED:
                return RMImages.instance.node_torelease_16().getSafeUri().asString();
        }
        return "";
    }

    /**
     * Gives a string representation of the state.
     * @return String representation of the state.
     */
    @Override
    public String toString() {
        return desc;
    }
}
