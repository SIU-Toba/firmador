package ar.gob.onti.firmador.model.certificatelist.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.junit.Test;

import ar.gob.onti.firmador.controler.PdfControler;
import ar.gob.onti.firmador.controler.StoreDctnry;
import ar.gob.onti.firmador.controler.PdfControler.OriginType;
import ar.gob.onti.firmador.model.PropsConfig;

public class StoreDctnryTest {


	@Test
	public final void testAddAlias() {
		StoreDctnry dctnry= new StoreDctnry();
		dctnry.addAlias("Oficina Nacional de Tecnologías de Información","Maria Cecilia Sava", "13919", "14/05/2011","{607B0DDD-4D1C-41D7-BA7E-702119455BE1}", OriginType.browser);
		assertEquals("[Maria Cecilia Sava###***13919###***14/05/2011###***{607B0DDD-4D1C-41D7-BA7E-702119455BE1}###***0]",dctnry.getmDictionary().get(dctnry.itrKeys().next()).toString());
	 
	}

	@Test
	public final void testIterator() {
		StoreDctnry dctnry= new StoreDctnry();
		dctnry.addAlias("Oficina Nacional de Tecnologías de Información","Maria Cecilia Sava", "13919", "14/05/2011","{607B0DDD-4D1C-41D7-BA7E-702119455BE1}", OriginType.browser);
		assertNull(dctnry.iterator("O.N.T.I."));
		assertEquals("Maria Cecilia Sava###***13919###***14/05/2011###***{607B0DDD-4D1C-41D7-BA7E-702119455BE1}###***0",dctnry.iterator("Oficina Nacional de Tecnologías de Información").next().toString());
	}

	@Test
	public final void testItrSize() {
		StoreDctnry dctnry= new StoreDctnry();
		dctnry.addAlias("Oficina Nacional de Tecnologías de Información","Maria Cecilia Sava", "13919", "14/05/2011","{607B0DDD-4D1C-41D7-BA7E-702119455BE1}", OriginType.browser);
		dctnry.addAlias("Oficina Nacional de Tecnologías de Información","Oscar Caceres", "45612", "24/05/2012","{607B0DDD-4D1C-41D7-BA7E-WER45461545151}", OriginType.browser);
		assertEquals(dctnry.itrSize("Oficina Nacional de Tecnologías de Información"),2);
	}

	@Test
	public final void testItrKeys() {
		StoreDctnry dctnry= new StoreDctnry();
		dctnry.addAlias("Oficina Nacional de Tecnologías de Información","Maria Cecilia Sava", "13919", "14/05/2011","{607B0DDD-4D1C-41D7-BA7E-702119455BE1}", OriginType.browser);
		assertEquals(dctnry.getmDictionary().get(dctnry.itrKeys().next()).toString(),"[Maria Cecilia Sava###***13919###***14/05/2011###***{607B0DDD-4D1C-41D7-BA7E-702119455BE1}###***0]");

	}

	@Test
	public final void testItrKeySize() {
		StoreDctnry dctnry= new StoreDctnry();
		dctnry.addAlias("Oficina Nacional de Tecnologías de Información","Maria Cecilia Sava", "13919", "14/05/2011","{607B0DDD-4D1C-41D7-BA7E-702119455BE1}", OriginType.browser);
		dctnry.addAlias("Autoridad Certificante de Firma Digital","Oscar Caceres", "45612", "24/05/2012","{607B0DDD-4D1C-41D7-BA7E-WER45461545151}", OriginType.browser);
		assertEquals(dctnry.itrKeySize(),2);

	}

	@Test
	public final void testLoadKeyStore() {
		if (System.getProperty("os.name").toLowerCase().indexOf("win") == -1) {
			return;
		}

		StoreDctnry dctnry= new StoreDctnry();
		PdfControler controler = null;
		try {
			controler = new PdfControler();
		} catch (NoSuchAlgorithmException e1) {
			fail(e1.getMessage());
		}
		try { 
			controler.cargarKeyStoreWindows();
			dctnry.setKeyStore(controler.getKeyStores());
			dctnry.setIssuers(PropsConfig.getInstance().getAutoCertificantes());
			dctnry.loadKeyStore();
		} catch (KeyStoreException e) {
			fail(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			fail(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			fail(e.getMessage());
		} catch (CertificateException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
