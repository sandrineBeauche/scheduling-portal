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
package org.ow2.proactive_grid_cloud_portal.scheduler.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.ow2.proactive_grid_cloud_portal.common.client.Listeners.LogListener;
import org.ow2.proactive_grid_cloud_portal.common.client.Listeners.StatsListener;
import org.ow2.proactive_grid_cloud_portal.common.client.Model.StatHistory.Range;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.Job;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.JobOutput;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.JobStatus;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.JobUsage;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerStatus;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerUser;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.Task;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.JobOutputListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.JobSelectedListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.JobsUpdatedListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.RemoteHintListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.SchedulerStatusListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.StatisticsListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.TagSuggestionListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.TasksUpdatedListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.ThirdPartyCredentialsListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.UsageListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.UsersListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.VisualizationListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.controller.PrefixWordSuggestOracle.TagSuggestion;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.model.SchedulerModel.RemoteHint;
import org.ow2.proactive_grid_cloud_portal.scheduler.shared.JobVisuMap;
import org.ow2.proactive_grid_cloud_portal.scheduler.shared.SchedulerConfig;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;


/**
 * Writable Model, should only be used by the Controller
 *
 *
 * @author mschnoor
 *
 */
public class SchedulerModelImpl extends SchedulerModel implements SchedulerEventDispatcher {

    
    

    private JobsModel jobsModel;
    
    private TaskModel tasksModel;
    
    
    private boolean logged = false;
    private String login = null;
    private String sessionId = null;
    private SchedulerStatus schedulerStatus = SchedulerStatus.STARTED;
    
    
    
    
    
    
    private List<SchedulerUser> users = null;
    private List<SchedulerUser> usersWithJobs = null;
    private HashMap<String, String> schedulerStats = null;
    private HashMap<String, String> accountStats = null;
   
    private Map<String, StatHistory> statistics = null;
    private Map<String, Range> requestedStatRange = null;
    private List<JobUsage> usage = null;
    
    private ArrayList<SchedulerStatusListener> schedulerStateListeners = null;
    
    private ArrayList<LogListener> logListeners = null;
    private ArrayList<UsersListener> usersListeners = null;
    private ArrayList<UsersListener> usersWithJobsListeners = null;
    private ArrayList<StatisticsListener> statisticsListeners = null;
    
    
    private ArrayList<StatsListener> statsListeners = null;
    private ArrayList<SchedulerListeners.UsageListener> usageListeners = null;
    private SchedulerListeners.ThirdPartyCredentialsListener thirdPartyCredentialsListener;

    public SchedulerModelImpl() {
        super();

        this.schedulerStateListeners = new ArrayList<SchedulerStatusListener>();
        
        this.logListeners = new ArrayList<LogListener>();
        this.usersListeners = new ArrayList<UsersListener>();
        this.usersWithJobsListeners = new ArrayList<UsersListener>();
        this.statisticsListeners = new ArrayList<StatisticsListener>();
        
        this.statsListeners = new ArrayList<StatsListener>();
        this.usageListeners = new ArrayList<SchedulerListeners.UsageListener>();
        
        this.requestedStatRange = new HashMap<String, Range>();
    }
    
    
    
    @Override
    public boolean isLoggedIn() {
        return this.logged;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.logged = loggedIn;
        this.sessionId = null;
    }

    @Override
    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String id) {
        this.sessionId = id;
    }

    @Override
    public SchedulerStatus getSchedulerStatus() {
        return this.schedulerStatus;
    }

    /**
     * Set a new scheduler status,
     * notify listeners
     * 
     * @param status the new scheduler status
     */
    public void setSchedulerStatus(SchedulerStatus status) {
        this.schedulerStatus = status;

        for (SchedulerStatusListener listener : this.schedulerStateListeners) {
            listener.statusChanged(this.schedulerStatus);
        }
    }


    @Override
    public List<SchedulerUser> getSchedulerUsers() {
        return this.users;
    }

    /**
     * Change the local users list, notify listeners
     * 
     * @param users new users
     */
    public void setSchedulerUsers(List<SchedulerUser> users) {
        this.users = users;

        for (UsersListener list : this.usersListeners) {
            list.usersUpdated(this.users);
        }
    }

    @Override
    public List<SchedulerUser> getSchedulerUsersWithJobs() {
        return this.usersWithJobs;
    }

    /**
     * Change the local users list, notify listeners
     * 
     * @param usersWithJobs new users
     */
    public void setSchedulerUsersWithJobs(List<SchedulerUser> usersWithJobs) {
        this.usersWithJobs = usersWithJobs;
        for (UsersListener list : this.usersWithJobsListeners) {
            list.usersUpdated(this.usersWithJobs);
        }
    }

    /**
     * Set local model, notify listeners
     * 
     */
    public void setAccountStatistics(HashMap<String, String> stats) {
        this.accountStats = stats;
        for (StatisticsListener list : this.statisticsListeners) {
            list.accountStatsUpdated(stats);
        }
    }

    @Override
    public HashMap<String, String> getAccountStatistics() {
        return this.accountStats;
    }

    public void setSchedulerStatistics(HashMap<String, String> stats) {
        this.schedulerStats = stats;
        for (StatisticsListener list : this.statisticsListeners) {
            list.schedulerStatsUpdated(stats);
        }
    }

    @Override
    public HashMap<String, String> getSchedulerStatistics() {
        return this.schedulerStats;
    }

    @Override
    public List<JobUsage> getUsage() {
        return this.usage;
    }

    public void setUsage(List<JobUsage> usage) {
        this.usage = usage;
        for (SchedulerListeners.UsageListener list : this.usageListeners) {
            list.usageUpdated(usage);
        }
    }
    
    
    

    public void setThirdPartyCredentialsKeys(Set<String> thirdPartyCredentialsKeys) {
        thirdPartyCredentialsListener.keysUpdated(thirdPartyCredentialsKeys);
    }

    @Override
    public StatHistory getStatHistory(String source) {
        return this.statistics.get(source);
    }

    @Override
    public Map<String, StatHistory> getStatHistory() {
        return this.statistics;
    }

    @Override
    public Range getRequestedStatHistoryRange(String source) {
        Range r = this.requestedStatRange.get(source);
        if (r == null)
            return Range.MINUTE_1;
        return r;
    }
    
    

    @Override
    public void logMessage(String message) {
        for (LogListener list : this.logListeners) {
            list.logMessage(getLogStamp() + message);
        }
    }

    @Override
    public void logImportantMessage(String error) {
        for (LogListener list : this.logListeners) {
            list.logImportantMessage(getLogStamp() + "<span style='color:#8f7601;'>" + error + "</span>");
        }
    }

    @Override
    public void logCriticalMessage(String error) {
        for (LogListener list : this.logListeners) {
            list.logCriticalMessage(getLogStamp() + "<span style='color:red;'>" + error + "</span>");
        }
    }

    private String getLogStamp() {
        String date = DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG).format(new Date());
        return "<span style='color:gray'>" + date + "</span> ";
    }

    

    

    /*
     * (non-Javadoc)
     * @see org.ow2.proactive_grid_cloud_portal.client.EventDispatcher#addSchedulerStateListener(org.ow2.proactive_grid_cloud_portal.client.Listeners.SchedulerStateListener)
     */
    public void addSchedulerStatusListener(SchedulerStatusListener listener) {
        this.schedulerStateListeners.add(listener);
    }

    

    /*
     * (non-Javadoc)
     * @see org.ow2.proactive_grid_cloud_portal.client.EventDispatcher#addLogListener(org.ow2.proactive_grid_cloud_portal.client.Listeners.LogListener)
     */
    public void addLogListener(LogListener listener) {
        this.logListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * @see org.ow2.proactive_grid_cloud_portal.client.EventDispatcher#addUsersListener(org.ow2.proactive_grid_cloud_portal.client.Listeners.UsersListener)
     */
    public void addUsersListener(UsersListener listener) {
        this.usersListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * @see org.ow2.proactive_grid_cloud_portal.client.EventDispatcher#addUsersWithJobsListener(org.ow2.proactive_grid_cloud_portal.client.Listeners.UsersListener)
     */
    public void addUsersWithJobsListener(UsersListener listener) {
        this.usersWithJobsListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * @see org.ow2.proactive_grid_cloud_portal.client.EventDispatcher#addStatisticsListener(org.ow2.proactive_grid_cloud_portal.client.Listeners.StatisticsListener)
     */
    public void addStatisticsListener(StatisticsListener listener) {
        this.statisticsListeners.add(listener);
    }

 

    @Override
    public void addStatsListener(StatsListener listener) {
        this.statsListeners.add(listener);
    }

    @Override
    public void addUsageListener(SchedulerListeners.UsageListener listener) {
        this.usageListeners.add(listener);
    }

    @Override
    public void setThirdPartyCredentialsListener(
      SchedulerListeners.ThirdPartyCredentialsListener thirdPartyCredentialsListener) {
        this.thirdPartyCredentialsListener = thirdPartyCredentialsListener;
    }



	public JobsModel getJobsModel() {
		return jobsModel;
	}



	public TaskModel getTasksModel() {
		return tasksModel;
	}
    
    
    
}
