/**
 * 
 */
package ar.gob.onti.firmador.model.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import ar.gob.onti.firmador.controler.conection.HttpFileUploader;
import ar.gob.onti.firmador.model.PropsConfig;
import ar.gob.onti.firmador.model.Proxy;

/**
 * @author oscar
 *
 */
public class HttpFileUploaderTest {

	/**
	 * Test method for {@link ar.gob.onti.firmador.controler.conection.HttpFileUploader#doUpload(java.lang.String, ar.gob.onti.firmador.model.PropsConfig, java.lang.String)}.
	 */
	/*
	@Test
	public final void testDoUpload() {
		HttpFileUploader fileUploader= new HttpFileUploader();
		assertTrue(fileUploader.connectURL("http://www.google.com.ar"));
		PropsConfig.getInstance().readProps();
		try {
			assertFalse(fileUploader.doUpload(PropsConfig.getInstance().getSourceDir()+File.separator+"test.txt", PropsConfig.getInstance(), "codigo de prueba", "objeto dominio de prueba", "tipo de archivo de prueba"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		assertEquals(fileUploader.getHttpFileError(),"Error de recepción en el server :");
	}
	 */
	
	/**
	 * Test method for {@link ar.gob.onti.firmador.controler.conection.HttpFileConnection#connectURL(java.lang.String)}.
	 */
	@Test
	public final void testConnectURL() {
		HttpFileUploader fileUploader= new HttpFileUploader();
		assertTrue(fileUploader.connectURL("http://www.google.com.ar"));
	}

	/**
	 * Test method for {@link ar.gob.onti.firmador.controler.conection.HttpFileConnection#detectProxy(java.lang.String)}.
	 */
	@Test
	public final void testDetectProxy() {
		HttpFileUploader fileUploader= new HttpFileUploader();
		Proxy proxy=new Proxy(null, null, java.net.Proxy.Type.DIRECT);
	     try {
			if(fileUploader.detectProxy("http://www.google.com.ar")==null){
				assertNull(proxy.getHostName());
				assertNull(proxy.getPort());
				assertEquals(proxy.getProxyType(),  java.net.Proxy.Type.DIRECT);
			}else{
				proxy=fileUploader.detectProxy("http://www.google.com.ar");
				assertNotNull(proxy.getHostName());
				assertNotNull(proxy.getPort());
				assertNotNull(proxy.getProxyType());
			}
			
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
	}

}
