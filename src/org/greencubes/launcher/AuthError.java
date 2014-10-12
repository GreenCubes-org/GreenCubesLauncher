package org.greencubes.launcher;

public class AuthError extends Exception {
	
	private static final long serialVersionUID = -4709793962272592369L;

	public AuthError() {
	}
	
	public AuthError(String message) {
		super(message);
	}
	
	public AuthError(Throwable cause) {
		super(cause);
	}
	
	public AuthError(String message, Throwable cause) {
		super(message, cause);
	}
	
}
