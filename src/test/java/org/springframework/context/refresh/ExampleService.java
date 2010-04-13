package org.springframework.context.refresh;


/**
 * {@link Service} with hard-coded input data.
 */
public class ExampleService implements Service {
	
	private String message = "Hello world!";
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;	
	}

}
