package org.ow2.proactive_grid_cloud_portal.fixture;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public interface JSONFixture {

	public JSONObject toJSON() throws JSONException;
}
