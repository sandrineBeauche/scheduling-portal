package org.ow2.proactive_grid_cloud_portal.scheduler.client.model;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class ModelUtils {

	private static final String PLATFORM_INDEPENDENT_LINE_BREAK = "\r\n?|\n";
	
	public static String[] lineByLine(String lines) {
        return lines.split(PLATFORM_INDEPENDENT_LINE_BREAK);
    }
    
    
    public static String formatLine(String str) {
        if (str.matches("\\[.*\\].*")) {
            str = SafeHtmlUtils.htmlEscape(str).replaceFirst("]", "]</span>");
            return "<nobr><span style='color:gray;'>" + str +"</nobr><br>";
        }
        return "";
    }
}
