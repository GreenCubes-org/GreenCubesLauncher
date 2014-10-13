package org.greencubes.launcher;

public class AuthError extends Exception {
	
	private static final long serialVersionUID = -4709793962272592369L;
	
	public int errorCode;

	public AuthError(int code) {
		this.errorCode = code;
	}
	
	public AuthError(int code, String message) {
		super(message);
		this.errorCode = code;
	}
	
	public AuthError(int code, Throwable cause) {
		super(cause);
		this.errorCode = code;
	}
	
	public AuthError(int code, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = code;
	}
	
}
