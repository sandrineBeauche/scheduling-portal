package functionaltests.monitor;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * Scheduler event receiver for functional tests
 * Receives all scheduler events and
 * forward them a SchedulerMonitorsHandler object to handle them.
 *
 * @author ProActive team
 *
 */
public class MonitorEventReceiver implements SchedulerEventListener {

    private SchedulerMonitorsHandler monitorsHandler;

    /**
     * ProActive Empty constructor
     */
    public MonitorEventReceiver() {
    }

    /**
     * @param monitor SchedulerMonitorsHandler object which is notified
     * of Schedulers events.
     */
    public MonitorEventReceiver(SchedulerMonitorsHandler monitor) {
        this.monitorsHandler = monitor;
    }

    //---------------------------------------------------------------//
    //Methods inherited form SchedulerEventListener
    //---------------------------------------------------------------//

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        switch (eventType) {
            case STARTED:
            case STOPPED:
            case KILLED:
            case FROZEN:
            case PAUSED:
            case RM_DOWN:
            case RM_UP:
            case RESUMED:
            case SHUTDOWN:
            case SHUTTING_DOWN:
            case POLICY_CHANGED:
                monitorsHandler.handleSchedulerStateEvent(eventType);
                break;
        }

    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        switch (notification.getEventType()) {
            case JOB_PENDING_TO_RUNNING:
            case JOB_RUNNING_TO_FINISHED:
            case JOB_REMOVE_FINISHED:
            case JOB_CHANGE_PRIORITY:
            case JOB_PAUSED:
            case JOB_RESUMED:
                monitorsHandler.handleJobEvent(notification.getEventType(), notification.getData());
                break;
        }

    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        switch (notification.getEventType()) {
            case TASK_PENDING_TO_RUNNING:
            case TASK_RUNNING_TO_FINISHED:
            case TASK_WAITING_FOR_RESTART:
                monitorsHandler.handleTaskEvent(notification.getEventType(), notification.getData());
                break;
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.JobState)
     */
    public void jobSubmittedEvent(JobState jState) {
        monitorsHandler.handleJobEvent(SchedulerEvent.JOB_SUBMITTED, jState);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.USERS_UPDATE);
    }

}