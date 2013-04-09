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
package org.ow2.proactive_grid_cloud_portal.scheduler.client;

import org.ow2.proactive_grid_cloud_portal.common.client.JSUtil;
import org.ow2.proactive_grid_cloud_portal.common.client.Settings;
import org.ow2.proactive_grid_cloud_portal.scheduler.shared.SchedulerConfig;

import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;


public class TestPortal implements EntryPoint {

    public void onModuleLoad() {

        VLayout rootLayout = new VLayout();
        ToolStrip toolStrip = new ToolStrip();
        toolStrip.setWidth(600);
        toolStrip.setHeight(600);
        ToolStripButton button = new ToolStripButton("Hello very long text to fill space");
//        button.addBitlessDomHandler(new DragEnterHandler() {
//            @Override
//            public void onDragEnter(DragEnterEvent event) {
//                System.out.println("drag eenter" + event);
//            }
//        }, DragEnterEvent.getType());
        toolStrip.addButton(button);

        Canvas panel = new Canvas();
        panel.setWidth("700px");
        panel.setHeight("200px");
//        panel.getElement().getStyle().setBackgroundColor("#0000FF");

        rootLayout.addMember(toolStrip);
        rootLayout.addMember(panel);
        rootLayout.draw();
//        Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
//            @Override
//            public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
//                System.out.println(event.getNativeEvent().getType());
//            }
//        });
        button.addBitlessDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                System.out.println("PANEL DRAG" + event);
            }
        }, DragOverEvent.getType());

    }


}
