package org.greencubes.client;

public interface IClientStatus {
	
	public Status getStatus();
	
	public String getStatusTitle();
	
	public float getStatusProgress();	
	
	public enum Status {
		//@formatter:off
		CHECK("client.status.check", null),
		READY("client.status.ready", "client.status.start"),
		NEED_UPDATE("client.status.needupdate", "client.status.update"),
		UPDATING("client.status.updating", "client.status.abort"),
		LOADING("client.status.loading", null),
		RUNNING("client.status.running", null),
		ERROR("client.status.error", null),
		;
		//@formatter:on
		
		public final String statusName;
		public final String statusActionName;
		/**
		 * 
		 * @param statusName
		 * @param statusAction - null to display statusName and
		 * disallow actions
		 */
		private Status(String statusName, String statusActionName) {
			this.statusActionName = statusActionName;
			this.statusName = statusName;
		}
	}
}
