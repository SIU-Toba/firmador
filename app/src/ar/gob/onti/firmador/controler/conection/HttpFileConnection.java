package ar.gob.onti.firmador.controler.conection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import ar.gob.onti.firmador.model.PropsConfig;
import ar.gob.onti.firmador.model.Proxy;

import com.sun.java.browser.net.ProxyService;
import java.net.URI;


public class HttpFileConnection  {
	private String httpFileError="";
	private  HttpURLConnection connectURL=null;
	public String getHttpFileError() {
		return httpFileError;
	}
	public void setHttpFileError(String httpFileError) {
		this.httpFileError = httpFileError;
	}
	public HttpURLConnection getConnectURL() {
		return connectURL;
	}
	public void setConnectURL(HttpURLConnection connectURL) {
		this.connectURL = connectURL;
	}
	
	/**
	 * Creates and returns Proxy object, which should be used for URL
	 * connections in JSignPdf.
	 * 
	 * @return initialized Proxy object.
	 */
	public static java.net.Proxy createProxy(Proxy proxy) {
		java.net.Proxy tmpResult = java.net.Proxy.NO_PROXY;
		if(proxy!=null){
			tmpResult = new java.net.Proxy(proxy.getProxyType(), new InetSocketAddress(proxy.getHostName(), proxy.getPort()));
		}
			return tmpResult;
	}
	/**
	 * metodo que verifica la connecion con el url de archivo
	 * @param urlString
	 * @return
	 */
	public boolean connectURL(String urlString) {

		boolean retValue = false;
      
			try {
				  Proxy proxy=this.detectProxy(urlString);
				final URL tmpUrl = new URL(urlString);
				connectURL = (HttpURLConnection)tmpUrl.openConnection(HttpFileConnection.createProxy(proxy));
				retValue = true;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				cargarMensajeError(e,urlString);
			} 
			catch (IOException e) {
				e.printStackTrace();
				cargarMensajeError(e,urlString);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				cargarMensajeError(e,urlString);
			}
		return retValue;
	}
	private void cargarMensajeError(Exception e,String urlString){
		httpFileError += PropsConfig.getInstance().getString("errorURL") + urlString + ")";
		if (e.getMessage() != null) {
			httpFileError += "\nMensaje JVM: " + e.getMessage();
		}
	}
	/**
	 * detecta el proxy de la configuracion para la coneecion a internet si no detecta 
	 * ninguno devuelve null
	 * @param url
	 * @return
	 * @throws URISyntaxException
	 */
	public Proxy detectProxy(String url) throws URISyntaxException   {
			java.net.InetSocketAddress addr=null;
			try{
				com.sun.java.browser.net.ProxyInfo[] pi = ProxyService.getProxyInfo(new URL(url));
				return new Proxy(pi[0].getHost(), pi[0].getPort(), java.net.Proxy.Type.HTTP);
			}catch(IOException e){
				if (e.getMessage().equalsIgnoreCase("Proxy service provider is not yet set")){
					System.setProperty("java.net.useSystemProxies","true"); // I don't know if this part is always necessary
					java.util.List<java.net.Proxy> listProxy = ProxySelector.getDefault().select(new java.net.URI(url));

					for (Iterator<java.net.Proxy> iter = listProxy.iterator(); iter.hasNext(); ) {
						java.net.Proxy proxy = (java.net.Proxy) iter.next();
						addr = (java.net.InetSocketAddress)proxy.address();
						if(addr == null) {

							return null;
						} else {

							return new Proxy(addr.getHostName(),addr.getPort(),proxy.type());
						}
					}
				}
			}
			return null;
		}


		
	}
