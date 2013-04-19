package ar.gob.onti.firmador.controler.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ar.gob.onti.firmador.controler.FirmaControler;
import ar.gob.onti.firmador.controler.KeyStoreData;
import ar.gob.onti.firmador.controler.PdfControler;
import ar.gob.onti.firmador.controler.PdfControler.OriginType;
import ar.gob.onti.firmador.controler.conection.HttpFileDownLoader;
import ar.gob.onti.firmador.model.PropsConfig;
import ar.gob.onti.firmador.view.FirmaApplet;
import ar.gob.onti.firmador.view.VentanaPrincipal;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class FirmaControlerTest {
	private FirmaControler firmaControler;
	@Before
	public void setUp() throws Exception {
		
		firmaControler=inicializar(true);
	}
	 @After
	   public void tearDown() throws Exception {
		 firmaControler.closeLogFile();
	   }
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
	private FirmaControler getControler(){
		return firmaControler;
	}
	private PdfControler getPdfControler(){
		PdfControler pdfControler = null;
		try {
			pdfControler = new PdfControler();
		} catch (NoSuchAlgorithmException e1) {
			fail(e1.getMessage());
		}
		ClassLoader cl = this.getClass().getClassLoader();
		URL url=  cl.getResource("Cert_test.pfx");
		
		try {
			KeyStore ks = KeyStore.getInstance("pkcs12");
			ks.load(new FileInputStream(new File(url.toURI())), "caycay".toCharArray());
			String alias = (String)ks.aliases().nextElement();
			Certificate[] chain = ks.getCertificateChain(alias);
			PrivateKey key = (PrivateKey)ks.getKey(alias, "caycay".toCharArray());
			KeyStoreData keyStoreData = new KeyStoreData();
			pdfControler.setKeyStoreData(OriginType.browser, keyStoreData);
			pdfControler.setCurrentKeyStoreData(OriginType.browser);
			keyStoreData.setKeyStore(ks);
			keyStoreData.setKeySign(key);
			keyStoreData.setChain(chain.clone());
			pdfControler.setProps(PropsConfig.getInstance());
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
		} catch (KeyStoreException e) {
			fail(e.getMessage());
		} catch (UnrecoverableKeyException e) {
			fail(e.getMessage());
		}
     return pdfControler;
	}
	private FirmaControler inicializar(boolean pdfControler){
		JFrame jFrame= new JFrame();
		  PropsConfig.getInstance().setBrowser(FirmaApplet.IEXPLORER);
		JFrame container= new JFrame();
		VentanaPrincipal principal=new VentanaPrincipal(container, "IEXPLORER");
		principal.inicializar();
		if(pdfControler){
		principal.setpdfControler(getPdfControler());
		principal.initLogFile(jFrame );
		principal.initProps(jFrame );
		principal.initLogFile(jFrame );
		}
		else{
			PdfControler controler = null;
			try {
				controler = new PdfControler();
			} catch (NoSuchAlgorithmException e) {
				fail(e.getMessage());
			}
			controler.setProps(PropsConfig.getInstance());
			PropsConfig.getInstance().setVisible(false);
			principal.setpdfControler(controler);
		}

		principal.setCodigo("RQVMJGPTU589IWSDdfdknv");
		principal.setObjetoDominio("RQVMJGPTU589IWSDdfdknv");
		principal.setTipoArchivo("SDLFKJSDF675878sjdlsdj");
		PropsConfig.getInstance().readProps();
		PropsConfig.getInstance().setDownloadURL("http://www.sundaycrosswords.com/ccpuz/ShockTreatment.pdf");
		PropsConfig.getInstance().setUploadURL("http://assertserver.dyndns.org:8280/ecom/resources/file/upload");
		crearPdf(PropsConfig.getInstance().getSourceDir()+File.separator+"test.pdf");
		crearPdf(PropsConfig.getInstance().getSourceDir()+File.separator+"test_firmado.pdf");
		principal.setArchivoParaFirmar(new File(PropsConfig.getInstance().getSourceDir()+File.separator+"test.pdf"));
		principal.setArchivoFirmado(new File(PropsConfig.getInstance().getSourceDir()+File.separator+"test_firmado.pdf"));
		FirmaControler controler= new FirmaControler(principal){
			@Override
			public void mostrarMensajesError(Container container,String mensaje,Exception exception){
				this.setError(mensaje);		
			}
			@Override
			public void mostrarMensajesOk(Container container,String mensaje,String titulo){
				this.setError("");	
			}
		};
		
		return controler;
	}
	@Test
	public final void testFirmarDocumento() {
		FirmaControler firmaControler=	getControler();
		firmaControler.firmarDocumento(new JFrame());
		assertTrue(firmaControler.getError().isEmpty());
	}
	@Test
	public final void testPreguntarPinToken() {
		FirmaControler firmaControler=	getControler();
		PropsConfig.getInstance().setVisible(false);
		firmaControler.preguntarPinToken(new JFrame());
		assertEquals(firmaControler.getError(),"No se ha ingresado el PIN del token");
	}
	/*
	@Test
	public final void testErrorUrlValidaSubirDocumento() {
		FirmaControler firmaControler=	getControler();
		firmaControler.subirDocumento(new JFrame());
		assertTrue(firmaControler.getError().contains("Se han producido errores al enviar el archivo al server"));
	}
	@Test
	public final void testErrorUrlInValidaSubirDocumento() {
		FirmaControler firmaControler=	getControler();
		PropsConfig.getInstance().setUploadURL("http://www.invalida.url");
		firmaControler.subirDocumento(new JFrame());
		assertTrue(firmaControler.getError().contains("Se han producido errores al enviar el archivo al server"));
	}
	*/
	@Test
	public final void testCargarArbolDeCertificados() {
		FirmaControler firmaControler=	inicializar(false);
		PropsConfig.getInstance().setVisible(false);
		assertFalse(firmaControler.cargarArbolDeCertificados(new JFrame()));
		assertTrue(firmaControler.getError().isEmpty());
	}

	
/*
	@Test
	public final void testUploadDoc() {
		FirmaControler firmaControler=	getControler();
		assertFalse(firmaControler.uploadDoc(new JFrame()));
		assertTrue(firmaControler.getError().contains("Se han producido errores al enviar el archivo al server"));
				}
*/
	@Test
	public final void testAgregarParametroUrl() {
		FirmaControler firmaControler=	getControler();
		HttpFileDownLoader fileDownLoader=new HttpFileDownLoader();
		assertEquals(firmaControler.agregarParametroUrl(new JFrame(), "http://localhost:8080/appletServlet",fileDownLoader),"http://localhost:8080/appletServlet?idDominio=RQVMJGPTU589IWSDdfdknv&tipoDeArchivo=SDLFKJSDF675878sjdlsdj");
	}

	@Test
	public final void testErrorUrlValidaDescargarDocumentoParaFirmar() {
		FirmaControler firmaControler=	getControler();
		PropsConfig.getInstance().setNombreArchivo(null);
		firmaControler.descargarDocumentoParaFirmar(new JFrame());
		firmaControler.getError().contains("No se ha podido descargar el documento a firmar"); 
	}
	@Test
	public final void testErrorUrlInValidaDescargarDocumentoParaFirmar() {
		FirmaControler firmaControler=	getControler();
		PropsConfig.getInstance().setNombreArchivo(null);
		PropsConfig.getInstance().setDownloadURL("http://www.invalida.url");
		firmaControler.descargarDocumentoParaFirmar(new JFrame());
		assertTrue(firmaControler.getError().contains("No se ha podido descargar el documento a firmar")); 
	}
	@Test
	public final void testGetNombreArchivoFirmado() {
		FirmaControler firmaControler=	getControler();
		assertEquals(firmaControler.getNombreArchivoFirmado(),"test_firmado.pdf");
	}
	@Test
	public final void testVisualizarDocumento() {
		FirmaControler firmaControler=	getControler();
		firmaControler.visualizarDocumento(new JFrame(), new File(""));
		assertEquals(firmaControler.getError(),"visualizarDocumento()Ha ocurrido un error al intentar visualizar el documento");
	}

}
