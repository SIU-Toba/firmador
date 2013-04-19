package ar.gob.onti.firmador.model.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import ar.gob.onti.firmador.model.PropsConfig;

public class PropsConfigTest {

	@Test
	public final void testGetString() {
		assertEquals(PropsConfig.getInstance().getString("UploadBoundary"),"*****");
	}

	@Test
	public final void testCargarIssuers() {
		PropsConfig.getInstance().cargarIssuers();
		 assertEquals(PropsConfig.getInstance().getAutoCertificantes().get(0),"AC de la Subsecretaría de la Gestión Pública para Certificados de Correo Electrónico");
		 assertEquals(PropsConfig.getInstance().getAutoCertificantes().get(1),"Oficina Nacional de Tecnologías de Información");
		 assertEquals(PropsConfig.getInstance().getAutoCertificantes().get(2),"Autoridad Certificante de Firma Digital");
		
	}

	@Test
	public final void testCargarDirectorioTemporal() {
		try {
			PropsConfig.getInstance().cargarDirectorioTemporal();
			 assertNotNull(PropsConfig.getInstance().getSourceDir());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public final void testReadProps() {
		assertTrue(PropsConfig.getInstance().readProps());
	}

}
