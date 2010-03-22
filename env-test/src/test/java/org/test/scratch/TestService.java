package org.test.scratch;

import java.util.HashMap;
import java.util.Map;


/**
 * {@link Service} with hard-coded input data.
 */
public class TestService implements Service {
	
	private Map<String, String> map = new HashMap<String, String>();
	
	public TestService() {
		map.put("jdbc.type", "TEST");
		map.put("jdbc.user", "testing");
		map.put("remote.url", "file://home/service/test-data.xml");
	}

	public Map<String, String> getMap() {
		return map;
	}

}
