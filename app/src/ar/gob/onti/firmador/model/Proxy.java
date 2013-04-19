package ar.gob.onti.firmador.model;

import java.net.Proxy.Type;


public class Proxy {

	private String hostName;
	private Integer port;
	private java.net.Proxy.Type proxyType=java.net.Proxy.Type.HTTP;

	
	public Proxy(String hostName, Integer port, Type proxyType) {
		super();
		this.hostName = hostName;
		this.port = port;
		this.proxyType = proxyType;
	}
	public String getHostName() {
		return hostName;
	}
	
	public Integer getPort() {
		return port;
	}
	
	public java.net.Proxy.Type getProxyType() {
		return proxyType;
	}
	
	

}
