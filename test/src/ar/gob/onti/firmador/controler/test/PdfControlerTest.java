package ar.gob.onti.firmador.controler.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Locale;

import javax.security.auth.login.LoginException;

import org.junit.Before;
import org.junit.Test;

import ar.gob.onti.firmador.controler.KeyStoreData;
import ar.gob.onti.firmador.controler.PdfControler;
import ar.gob.onti.firmador.controler.PdfControler.OriginType;
import ar.gob.onti.firmador.model.Mozilla;
import ar.gob.onti.firmador.model.PropsConfig;
import ar.gob.onti.firmador.view.FirmaApplet;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfControlerTest {
   private PdfControler  pdfControler;
   private Certificate[] chain ;
   private File file;
   private void crearPdf(String nombreFile){
		 try {
	            OutputStream file = new FileOutputStream(nombreFile);
	            Document document = new Document();
	            PdfWriter.getInstance(document, file);
	            document.open();
	            document.add(new Paragraph("Hola a todos!"));
	            document.add(new Paragraph(new Date().toString()));
	            document.close();
	            file.close();
	        } catch (Exception e) {
	        	fail(e.getMessage());
	        }
	}
	@Before
	public void setUp() throws Exception {
		PropsConfig.getInstance().readProps();
		pdfControler=new PdfControler();
		ClassLoader cl = this.getClass().getClassLoader();
		URL url=  cl.getResource("caceresCer.pfx");
		file= new File(url.toURI());
		KeyStore ks = KeyStore.getInstance("pkcs12");
		ks.load(new FileInputStream(file), "caycay".toCharArray());
		String alias = (String)ks.aliases().nextElement();
	    chain = ks.getCertificateChain(alias);
	    PrivateKey key = (PrivateKey)ks.getKey(alias, "caycay".toCharArray());
	    PropsConfig.getInstance().setBrowser(FirmaApplet.IEXPLORER);
	    KeyStoreData keyStoreData = new KeyStoreData();
	    pdfControler.setKeyStoreData(OriginType.browser, keyStoreData);
	    pdfControler.setCurrentKeyStoreData(OriginType.browser);
	    keyStoreData.setKeyStore(ks);
	    keyStoreData.setKeySign(key);
	    keyStoreData.setChain(chain.clone());
	    pdfControler.setProps(PropsConfig.getInstance());
	    pdfControler.setNombreArchivoParaFirmar("test.pdf");
	    pdfControler.setNombreArchivoFirmado("test_firmado.pdf");
	    crearPdf(PropsConfig.getInstance().getSourceDir()+File.separator+"test.pdf");
	    new FileOutputStream(PropsConfig.getInstance().getSourceDir()+File.separator+"test_MD5.pdf");
	}
  

	/*
	@Test
	public final void testCargarClavePrivadaYCadenaDeCertificados() {
		try {
			assertTrue(pdfControler.cargarClavePrivadaYCadenaDeCertificados("Oscar Caceres - (SN:25134 - Alias:d6bb1f0eef38e14f0b418bc0cca7d1d7_25148026-6e42-4e54-9c3b-f560b843ca89 - Origin:0)", "caycay", false));
		} catch (KeyStoreException e) {
			fail(e.getMessage());
		}
	}
	*/
	
	@Test
	public final void testErrorKeyStoreCargarClavePrivadaYCadenaDeCertificados() {
		try {
			assertFalse(pdfControler.cargarClavePrivadaYCadenaDeCertificados("Oscar Caceres - (SN:25134 - Alias:d6bb1f0eef38e14f0b418bc0cca7d1d7_25148026-6e42-4e54-9c3b-f560b843ca89 - Origin:0)", "", false));
		} catch (KeyStoreException e) {
			fail(e.getMessage());
		} 
	}

	/*
	@Test
	public final void testValidarCRL() {
		try {
			pdfControler= new PdfControler();
			KeyStoreData customKeyStoreData = new KeyStoreData()
			{
				@Override
				public BigInteger getSerialNumber() {		
					BigInteger strSerialNum = new BigInteger("6130");
					return strSerialNum;

				}
			};

			pdfControler.setKeyStoreData(OriginType.browser, customKeyStoreData);
			pdfControler.setCurrentKeyStoreData(OriginType.browser);
			
			customKeyStoreData.setChain(chain);
			ClassLoader cl = this.getClass().getClassLoader();
			URL url=  cl.getResource("Cert_test.pfx");
			KeyStore ks = KeyStore.getInstance("pkcs12");
			ks.load(new FileInputStream(new File(url.toURI())), "caycay".toCharArray());
			String alias = (String)ks.aliases().nextElement();
			Certificate[] chain=ks.getCertificateChain(alias);
			customKeyStoreData.setChain(chain);
			pdfControler.validarCRL(chain, false);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (KeyStoreException e) {
			fail(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			fail(e.getMessage());
		} catch (CertificateException e) {
			fail(e.getMessage());
		}
	}
	*/
	
	@Test
	public final void testExisteCertificate() {
		assertTrue(pdfControler.getCurrentKeyStoreData().existeCertificate());
	}
	@Test
	public final void testValidarVigenciaCertificado() {
		ClassLoader cl = this.getClass().getClassLoader();
		URL url=  cl.getResource("secoptesteo.pfx");
		KeyStore ks;
		try {
			ks = KeyStore.getInstance("pkcs12");
			ks.load(new FileInputStream(new File(url.toURI())), "caycay".toCharArray());
			String alias = (String)ks.aliases().nextElement();
			Certificate[] certificado = ks.getCertificateChain(alias);
			assertFalse(pdfControler.validarVigenciaCertificado((X509Certificate) certificado[0]));
		} catch (KeyStoreException e) {
			fail(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			fail(e.getMessage());
		} catch (CertificateException e) {
			fail(e.getMessage());
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
		
	}
	@Test
	public final void testGetMessageDig() {
		try {
			assertEquals(pdfControler.getMessageDig("MD5", "test_MD5.pdf"),"d41d8cd98f00b204e9800998ecf8427e");
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	@Test
	public final void testErrorGetMessageDig() {
		try {
			assertEquals(pdfControler.getMessageDig("MD23344", "test_MD5.pdf"),"00000000000000000000");
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	@Test
	public final void testGetCertSerial() {
		assertEquals(pdfControler.getCertSerial(),"25134");
	}

	@Test
	public final void testGetSerialNumber() {
		assertEquals(pdfControler.getCurrentKeyStoreData().getSerialNumber(),new BigInteger("25134"));
	}

	@Test
	public final void testGetIssuerCNSeleccionado() {
		assertEquals(pdfControler.getCurrentKeyStoreData().getIssuerCNSeleccionado(),"AC de la Subsecretaría de la Gestión Pública para Certificados de Correo Electrónico");
	}

	@Test
	public final void testGetCertHash() {
		assertEquals(pdfControler.getCertHash("SHA1"),"5e372b3d2ae09e9e07222c761c11054182391cf1");
	}
	@Test
	public final void testErrorGetCertHash() {
		assertEquals(pdfControler.getCertHash("SHAe2333"),"00000000000000000000");
	}
	@Test
	public final void testIsKeyStoreOpen() {
		assertTrue(pdfControler.getCurrentKeyStoreData().isKeyStoreOpen());
	}

	/*
	@Test
	public final void testCargarConfiguracionProviderToken() {
		assertEquals(pdfControler.cargarConfiguracionProviderToken()[0].toLowerCase(new Locale("es","AR")),"name=eToken\nlibrary=C:\\Windows\\system32\\eTPKCS11.dll".toLowerCase(new Locale("es","AR")));
	}
	*/

	@Test
	public final void testCargarKeyStoreWindows() {
		try {
			pdfControler= new PdfControler();
		} catch (NoSuchAlgorithmException e1) {
			fail(e1.getMessage());
		}
		try {
			pdfControler.cargarKeyStoreWindows();
		} catch (KeyStoreException e) {
			fail(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			fail(e.getMessage());
		} catch (CertificateException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	
	

	@Test
    public final void testCargarKeyStoreIExplorer() {
		    PropsConfig.getInstance().setBrowser(FirmaApplet.IEXPLORER);
            pdfControler.getCurrentKeyStoreData().setKeyStore(null);
            assertTrue(pdfControler.cargarKeyStore(""));
    }

	/*
	@Test
    public final void testErrorCargarKeyStore() {
		   PropsConfig.getInstance().setBrowser("Navegador_Invalido");
            pdfControler.getCurrentKeyStoreData().setKeyStore(null);
            PropsConfig.getInstance().setStoreType("TOKEN");
            assertFalse(pdfControler.cargarKeyStore(""));
            System.out.println(pdfControler.getSignError());
            assertTrue(pdfControler.getSignError().contains("Método FirmaPdfHandler.keyStoreOpen():Error en la apertura del almacén de certificados"));
    }
    */
	@Test
	public final void testFirmarDigitalmenteArchivoPdf() {
		try {
			pdfControler.firmarDigitalmenteArchivoPdf();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	@Test
	public final void testErrorFirmarDigitalmenteArchivoPdf() {
		try {
			pdfControler.setNombreArchivoParaFirmar("invalido.pdf");
			pdfControler.firmarDigitalmenteArchivoPdf();
			assertTrue(pdfControler.getSignError().contains("Método PdfControler.SignFile(): Error en la firma del documento"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	/*
	@Test
    public final void tesCargarKeyStorePKSC11() {
             Mozilla mozilla= new Mozilla();
         try {
        	 		 mozilla.loadMozillaLibraries();
        	 		 String[] provs = new String[1];
        	 		 provs[0] = mozilla.getPKCS11CfgInputStream();
                     pdfControler.cargarKeyStorePKSC11(provs, OriginType.browser, null);
            } catch (FileNotFoundException e) {
                    fail(e.getMessage());
            } catch (LoginException e) {
                    fail(e.getMessage());
            } catch (KeyStoreException e) {
                    fail(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                    fail(e.getMessage());
            } catch (CertificateException e) {
                    fail(e.getMessage());
            } catch (IOException e) {
                    fail(e.getMessage());
            } catch (Throwable e) {
            		fail(e.getMessage());
			}
    }
    */
	@Test
    public final void testCargarKeyStoreFirefox() {
		   PropsConfig.getInstance().setBrowser(FirmaApplet.FIREFOX);
           //pdfControler.getCurrentKeyStoreData().setKeyStore(null);
           assertTrue(pdfControler.cargarKeyStore(""));
    }
}
