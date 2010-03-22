package org.test.scratch;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * {@link Service} with hard-coded input data.
 */
@Component
public class ExampleService implements Service {
	
	private Map<String, String> map = new HashMap<String, String>();
	
	@Value("${jdbc.type}")
	public void setJdbcType(String value) {
		map.put("jdbc.type", value);
	}

	@Value("${jdbc.user}")
	public void setJdbcUser(String value) {
		map.put("jdbc.user", value);
	}

	@Value("${remote.url}")
	public void setRemoteUrl(String value) {
		map.put("remote.url", value);
	}

	public Map<String, String> getMap() {
		return map;
	}

}
