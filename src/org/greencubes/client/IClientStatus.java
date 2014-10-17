package org.greencubes.client;

public interface IClientStatus {
	
	public Status getStatus();
	
	public String getStatusTitle();
	
	public float getStatusProgress();	
	
	public enum Status {
		READY, NEED_UPDATE, UPDATING, LOADING, RUNNING;
	}
}
