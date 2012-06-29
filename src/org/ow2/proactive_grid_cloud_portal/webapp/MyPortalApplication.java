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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.CacheNotYetInitializedMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ConnectionExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.IOExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.InternalSchedulerExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.JobAlreadyFinishedExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.JobCreationExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.KeyExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.LoginExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.NotConnectedExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.PermissionExceptionExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ProActiveRuntimeExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.RuntimeExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.SchedulerExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.SubmissionClosedExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ThrowableMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.UnknownJobExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.UnknownTaskExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.scheduler.UpdatablePropertiesConverter;


public class MyPortalApplication extends Application {
    HashSet<Object> singletons = new HashSet<Object>();

    public MyPortalApplication() {
        //    singletons.add(new NotConnectedExceptionMapper()); 
        //    singletons.add(new PermissionExceptionExceptionMapper());
        //      singletons.add(new LoggingExecutionInterceptor());
    }

    @Override
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> set = new HashSet<Class<?>>();
        set.add(ConnectionExceptionMapper.class);
        set.add(InternalSchedulerExceptionMapper.class);
        set.add(IOExceptionMapper.class);
        set.add(JobAlreadyFinishedExceptionMapper.class);
        set.add(JobCreationExceptionMapper.class);
        set.add(LoginExceptionMapper.class);
        set.add(KeyExceptionMapper.class);
        set.add(NotConnectedExceptionMapper.class);
        set.add(PermissionExceptionExceptionMapper.class);
        set.add(ProActiveRuntimeExceptionMapper.class);
        set.add(RuntimeExceptionMapper.class);
        set.add(SchedulerExceptionMapper.class);
        set.add(SubmissionClosedExceptionMapper.class);
        set.add(UnknownJobExceptionMapper.class);
        set.add(UnknownTaskExceptionMapper.class);
        set.add(UpdatablePropertiesConverter.class);
        set.add(LoggingExecutionInterceptor.class);
        set.add(ThrowableMapper.class);
        set.add(LoggingInterceptor.class);
        set.add(CacheNotYetInitializedMapper.class);
        return set;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
