package org.greencubes.launcher;

import java.io.IOException;

public class HTTPResponseError extends IOException {

	private static final long serialVersionUID = -6519057417547883808L;

	public HTTPResponseError(String s) {
		super(s);
	}

	public HTTPResponseError(IOException e) {
		super(e);
	}

}
