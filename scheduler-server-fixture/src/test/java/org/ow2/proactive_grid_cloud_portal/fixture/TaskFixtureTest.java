package org.ow2.proactive_grid_cloud_portal.fixture;


import objectFaker.DataFaker;

import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

public class TaskFixtureTest{

	@Test
	public void testToJSON() throws JSONException {
		DataFaker<TaskFixture> taskFixtureFaker = new DataFaker<>(TaskFixture.class);
		TaskFixture fixture = taskFixtureFaker.fake();
		String fixtureJSON = fixture.toJSON();
		
		System.out.println(fixtureJSON);
	}

}
