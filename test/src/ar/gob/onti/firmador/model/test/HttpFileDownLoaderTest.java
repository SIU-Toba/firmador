package ar.gob.onti.firmador.model.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import ar.gob.onti.firmador.controler.conection.HttpFileDownLoader;
import ar.gob.onti.firmador.model.PropsConfig;
import com.sun.net.httpserver.Authenticator;

public class HttpFileDownLoaderTest {

	@Test
	public final void testDoDownload() {
		System.setProperty("java.net.useSystemProxies","true");
		PropsConfig.getInstance().readProps();
		HttpFileDownLoader downLoader= new HttpFileDownLoader();
		assertTrue(downLoader.connectURL("http://www.google.com.ar"));
		try {
			assertTrue(downLoader.doDownload(PropsConfig.getInstance().getSourceDir(), "test.txt"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		assertTrue(downLoader.getHttpFileError().isEmpty());
	}
	@Test
	public final void testErrorDoDownload() {
		System.setProperty("java.net.useSystemProxies","true");
		PropsConfig.getInstance().readProps();
		HttpFileDownLoader downLoader= new HttpFileDownLoader();
		assertTrue(downLoader.connectURL("http://www.google.com.ar"));
		try {
			assertFalse(downLoader.doDownload("ruta//invaida", "test.txt"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		assertFalse(downLoader.getHttpFileError().isEmpty());
	}
	@Test
	public final void testConnectURL() {
		HttpFileDownLoader downLoader= new HttpFileDownLoader();
		assertTrue(downLoader.connectURL("http://www.google.com.ar"));
		assertTrue(downLoader.getHttpFileError().isEmpty());
	}
	@Test
	public final void testConnectURLMalformedURLException() {
		HttpFileDownLoader downLoader= new HttpFileDownLoader();
		assertFalse(downLoader.connectURL("www.google.com.ar"));
		assertEquals(downLoader.getHttpFileError(),"Error en la creación del objeto URL (www.google.com.ar)\nMensaje JVM: no protocol: www.google.com.ar");
	}
	
}
