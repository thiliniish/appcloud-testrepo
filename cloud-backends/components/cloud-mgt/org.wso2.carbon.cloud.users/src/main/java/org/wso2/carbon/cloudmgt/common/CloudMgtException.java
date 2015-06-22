package org.wso2.carbon.cloudmgt.common;

public class CloudMgtException extends Exception {
	public CloudMgtException() {
	}

	public CloudMgtException(String s) {
		super(s);
	}

	public CloudMgtException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public CloudMgtException(Throwable throwable) {
		super(throwable);
	}

}
