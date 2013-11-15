package ar.gob.onti.firmador.controler;

import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.swing.JOptionPane;

import sun.security.pkcs11.Secmod;
import sun.security.pkcs11.Secmod.Module;
import sun.security.pkcs11.Secmod.ModuleType;
import sun.security.pkcs11.SunPKCS11;
import ar.gob.onti.firmador.controler.conection.HttpFileDownLoader;
import ar.gob.onti.firmador.exception.MozillaKeyStoreException;
import ar.gob.onti.firmador.model.AppleSafari;
import ar.gob.onti.firmador.model.CRLInfo;
import ar.gob.onti.firmador.model.ChromeLinux;
import ar.gob.onti.firmador.model.Mozilla;
import ar.gob.onti.firmador.model.PropsConfig;
import ar.gob.onti.firmador.model.Provider;
import ar.gob.onti.firmador.model.Proxy;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfLiteral;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPKCS7;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSigGenericPKCS;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.sun.security.auth.callback.DialogCallbackHandler;
import ar.gob.onti.firmador.model.ProvidersConfig;
import ar.gob.onti.firmador.util.HexUtils;

/**
 * Controler encargado de realizar la logica necesaria para la firma 
 * digital de un archivo pdf
 * @author ocaceres
 *
 */

public class PdfControler {

	public enum OriginType {
		browser,
		token
	}

	private KeyStoreData[] keyStoreDataCollection;
	private KeyStoreData currentKeyStoreData;
	private SecureRandom myRandomGen;
	private PropsConfig signProps;
	private String signError="";
	private String nombreArchivoParaFirmar="";
	private String nombreArchivoFirmado="";
	private Logger pdfLogFile;
	public static final  String ERROR_ALMACEN="errorAlmacen";

	public KeyStoreData getKeyStoreData(OriginType originType) {
		return keyStoreDataCollection[originType.ordinal()];
	}

	public void setKeyStoreData(OriginType originType, KeyStoreData keyStoreData) {
		keyStoreDataCollection[originType.ordinal()] = keyStoreData;
	}

	public KeyStoreData getCurrentKeyStoreData() {
		return currentKeyStoreData;
	}

	public void cleanCurrentKeyStoreData() {
		if (this.currentKeyStoreData != null) {
			this.currentKeyStoreData.limpiar();
		}
		this.currentKeyStoreData = null; 
	}
	
	public void setCurrentKeyStoreData(OriginType originType) {
		currentKeyStoreData = keyStoreDataCollection[originType.ordinal()];
	}

	public void setNombreArchivoParaFirmar(String nombreArchivoParaFirmar) {
		this.nombreArchivoParaFirmar = nombreArchivoParaFirmar;
	}

	public void setNombreArchivoFirmado(String nombreArchivoFirmado) {
		this.nombreArchivoFirmado = nombreArchivoFirmado;
	}
	/**+
	 * Constructor de el controler encargado de realizar la firma digital del documento
	 * @throws NoSuchAlgorithmException 
	 * @throws Exception
	 */
	public PdfControler() throws NoSuchAlgorithmException {

			myRandomGen = SecureRandom.getInstance("SHA1PRNG");
			keyStoreDataCollection = new KeyStoreData[2];
			keyStoreDataCollection[0] = new KeyStoreData();
			keyStoreDataCollection[1] = new KeyStoreData();
		pdfLogFile = null;
	}
	/**
	 * Se verifica si el certificado encontrado emitido
	 *  por la AC Raíz de la ONTI está vigente
	 * @param certificado
	 * @return
	 */
	public boolean validarVigenciaCertificado(X509Certificate certificado){		
		if ( certificado != null) {
			Date upDate = certificado.getNotAfter();
			Date lowDate = certificado.getNotBefore();
			Date now = new Date();
			if ((now.getTime() < lowDate.getTime()) || (now.getTime() > upDate.getTime())) {
				SimpleDateFormat dtFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				// DST dtFormat.setTimeZone(TimeZone.getTimeZone("GMT-03:00"));
				dtFormat.setTimeZone(TimeZone.getDefault());
				signError += PropsConfig.getInstance().getString("errorcertificadoExpirado") + dtFormat.format(lowDate) + " , " + dtFormat.format(upDate) + " )";
				return false;
			}
		}
		return true;
	}
	/**
	 * metodo donde se setea al controler la PrivateKey y la cadena de certificados 
	 * del certificado seleccionado por el usario firmante 
	 * @param idCert
	 * @param passToken
	 * @param propsConfig 
	 * @return
	 */
	public boolean cargarClavePrivadaYCadenaDeCertificados(String idCert, String passToken, boolean validateOCSP) throws KeyStoreException {
		String alias = "";
		String originType = "";
		boolean result=true;
		X509Certificate cerONTI = null;
		Certificate cerKeyStore = null;
		try {
			// es necesario separar el alias del aliasCer, porque para Crypto API es lo mismo, 
			// pero para PKCS11, no
			alias = idCert.substring(idCert.indexOf("Alias:") + 6).trim();
			// eliminar el ultimo caracter de la cadena que es un parentesis cerrado
			alias = alias.substring(0, alias.indexOf(" - Origin:")).trim();  

			originType = idCert.substring(idCert.indexOf("Origin:") + 7).trim();
			originType = originType.substring(0 , originType.length() - 1 ).trim();  
			
			currentKeyStoreData = this.keyStoreDataCollection[Integer.parseInt(originType)];

			cerKeyStore = currentKeyStoreData.getKeyStore().getCertificate(alias);	
			if (cerKeyStore instanceof X509Certificate){
				cerONTI = (X509Certificate)cerKeyStore;
			}
			validarVigenciaCertificado(cerONTI);
			if (passToken != null && OriginType.values()[Integer.parseInt(originType)] == OriginType.token) {
				currentKeyStoreData.setKeySign((PrivateKey) currentKeyStoreData.getKeyStore().getKey(alias, passToken.toCharArray()));
			} else {
				currentKeyStoreData.setKeySign((PrivateKey) currentKeyStoreData.getKeyStore().getKey(alias, null));				
			}
			currentKeyStoreData.setChain(currentKeyStoreData.getKeyStore().getCertificateChain(alias));
			
			signError="";
			result=validarCRL(currentKeyStoreData.getChain(), validateOCSP);
			if(!result){
				//currentKeyStoreData.setKeySign(null);
				//currentKeyStoreData.setChain(null);
				return false;
			}
		} catch (KeyStoreException e) {
			e.printStackTrace();
			loguearExcepcion(e);
			return false;
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
			loguearExcepcion(e);
			return false;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			loguearExcepcion(e);
			return false;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			loguearExcepcion(e);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			loguearExcepcion(e);
			return false;
		}
		return true;
	}
	public String toString(Date fecha) {
		return java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG,new java.util.Locale("es","ES")).format(fecha.getTime()); 
	}
	private boolean cargarProxyDePuntosDeDistribucionDelCertificadoPadre() throws URISyntaxException{
		Proxy proxy=null;
		HttpFileDownLoader fileDown = new HttpFileDownLoader();
		CRLInfo crlInfo = currentKeyStoreData.getCrlInfo();
		Set<String> setUrls=crlInfo.getCrlUrls();
		if(!crlInfo.getErrorCRLInfo().isEmpty()){
			signError+=crlInfo.getErrorCRLInfo();
			return false;
		}

		for (Iterator<String> iterator = setUrls.iterator(); iterator.hasNext();) {
			String url = (String) iterator.next();	
			if(fileDown.connectURL(url)){
				proxy=fileDown.detectProxy(url);
				if(proxy!=null){
					crlInfo.setProxy(proxy);
				}
			}else{
				signError += PropsConfig.getInstance().getString("errorURLCrlCA")+" "+currentKeyStoreData.getIssuerCNSeleccionado();
				return false;
			}
		}
		return true;
	}
	public boolean  validarCRL(Certificate[] chain, boolean validateOCSP)throws URISyntaxException, IOException {
		CRLInfo crlInfo = new CRLInfo(chain.clone());
		currentKeyStoreData.setCrlInfo(crlInfo);

		if(!cargarProxyDePuntosDeDistribucionDelCertificadoPadre()){
			return false;
		}
		Set<X509CRL> x509crls=crlInfo.getCrlSet();
		if(!crlInfo.getErrorCRLInfo().isEmpty()){
			signError+=crlInfo.getErrorCRLInfo();
			return false;
		}
		try {
			for (Iterator<X509CRL> iteratorCRL = x509crls.iterator(); iteratorCRL.hasNext();) {
				X509CRL crl = (X509CRL) iteratorCRL.next();
				X509CRLEntry entry = crl.getRevokedCertificate(currentKeyStoreData.getSerialNumber());
//				X509CRLEntry entry = crl.getRevokedCertificate(new BigInteger("6130"));
				if(entry!=null){
					signError+=PropsConfig.getInstance().getString("errorCertRevocado")+" ";
					signError+=currentKeyStoreData.getIssuerCNSeleccionado()+"\n";
					signError+=PropsConfig.getInstance().getString("errorCertRevocadoFecha")+" ";
					signError+=toString(entry.getRevocationDate());
					JOptionPane.showMessageDialog(null, signError);
					return false;
				} 

			}
			if (! currentKeyStoreData.validarCertificateChain()) {
                            signError+=PropsConfig.getInstance().getString("errorValidarCertChain")+" ";
                            return false;
                        }
			if (validateOCSP) {
				if (!currentKeyStoreData.validarCertificadoOCSP()) {
					signError+=PropsConfig.getInstance().getString("errorCertRevocado")+" ";
					signError+=PropsConfig.getInstance().getString("errorCertRevocadoOCSP")+" ";
					//JOptionPane.showMessageDialog(null, signError);
					return false;
				}
			}
		}catch (Exception e) {
            e.printStackTrace();
			cargarMensajeDeError(PropsConfig.getInstance().getString("errorvalidarCRL"), "validarCRL", e);
			loguearExcepcion(e);
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param signConfig
	 * @throws FileNotFoundException 
	 * @throws Exception
	 */
	public void setProps(PropsConfig signConfig){
			signProps = signConfig;
	}
	
	/**
	 * 
	 * @param logFile
	 * @throws FileNotFoundException 
	 * @throws Exception
	 */
	public void setLogFile(Logger logFile) throws FileNotFoundException {
		if (logFile == null) {
			throw new FileNotFoundException("Archivo de configuración no inicializado");	
		} else {
			pdfLogFile = logFile;
		}	
	}
	/**
	 * 
	 */
	public void unsetLogFile() {
		pdfLogFile = null;
	}
	/**
	 * metodo encargado de escribir el archivo log los eventos que se registran en los pasos que 
	 * se siguen para la firma del docuemnto pdf
	 * @param strLogMsg
	 * @param typeWrite
	 */
	public void writeLogFile(String strLogMsg, int typeWrite) {

		if (pdfLogFile != null) {
			if (typeWrite == 0) {
				pdfLogFile.info(strLogMsg);
			} else {
				pdfLogFile.severe(strLogMsg);
			}
		}	

	}


	/**
	 * Se calcula el valor de hash del documento que quiere firmarse
	 * utilizando el algoritmo informado 
	 * 
	 * @param mdAlgorithm
	 * @param fileMD
	 * @return
	 * @throws IOException 
	 */
	public String getMessageDig(String mdAlgorithm, String  nombreArchivo) throws IOException {
		FileInputStream fileIS=null;
		String mdNumber = "00000000000000000000";	
		try {
			byte buffer[] = new byte[2048];
			int bytesRead;
			MessageDigest prcMsgDig = MessageDigest.getInstance(mdAlgorithm);
			fileIS = new FileInputStream(PropsConfig.getInstance().getSourceDir()+File.separator+nombreArchivo);
			while ((bytesRead = fileIS.read(buffer)) > 0) {
				prcMsgDig.update(buffer,0, bytesRead);
			}	
			byte hash[] = prcMsgDig.digest();
			mdNumber = HexUtils.byteArrayToHexString(hash);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			loguearExcepcion(e);
			cargarMensajeDeError( PropsConfig.getInstance().getString("errorHash"), "getMessageDig", e);
		} 
		finally{
			if(fileIS!=null){
				fileIS.close();
			}
		}
		return mdNumber;
	}

	public void cargarKeyStoreWindows() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		// MS-Internet Explorer
		KeyStore keyStore = KeyStore.getInstance("Windows-MY");
		this.keyStoreDataCollection[0].setKeyStore(keyStore);
		//this.keyStore = KeyStore.getInstance("Windows-ROOT");
		keyStore.load(null, null);
	}
	public void cargarKeyStorePKSC11(String[] cfgProvider, OriginType originType, String passToken) throws IOException, LoginException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		KeyStoreData keyStoreData;
		keyStoreData = this.keyStoreDataCollection[originType.ordinal()];
		
		Secmod secmod = Secmod.getInstance();
		ArrayList<SunPKCS11> sunpkcs = new ArrayList<SunPKCS11>(); 
		if(!secmod.isInitialized()){
			for (int n = 0; n < cfgProvider.length; n++) {
				SunPKCS11 provider = new SunPKCS11(new ByteArrayInputStream(cfgProvider[n].getBytes()));
				Security.addProvider(provider);
				sunpkcs.add(provider);
			}
		}
		else {
			Module nssModule = secmod.getModule(ModuleType.KEYSTORE);
			SunPKCS11 provider=(SunPKCS11) nssModule.getProvider();
			Security.addProvider(provider);
			sunpkcs.add(provider);
		}

		KeyStore currentKeyStore;
		for (int n = 0; n < sunpkcs.size(); n++) {
			try {
				sunpkcs.get(n).login(new Subject(), new DialogCallbackHandler());
				currentKeyStore = KeyStore.getInstance("PKCS11",sunpkcs.get(n));
				if (passToken != null) {
					currentKeyStore.load(null, passToken.toCharArray());
				} else {			
					currentKeyStore.load(null, null);
				}
				keyStoreData.setKeyStore(currentKeyStore);
				keyStoreData.setPkcs11Provider(sunpkcs.get(n));
				break;
			} catch (Exception e) {
				e.printStackTrace();
				cargarMensajeDeError("", "cargarKeyStorePKSC11", e);								
			}
		}
	}
	
	public void descargarProvider()
	{
		for(int n = 0; n < this.keyStoreDataCollection.length; n++) {
			if (this.keyStoreDataCollection[n].getPkcs11Provider() != null) {
				Security.removeProvider(this.keyStoreDataCollection[n].getPkcs11Provider().getName());
				this.keyStoreDataCollection[n].setPkcs11Provider(null);
			}
		}
	}
	
	private String[] cargarConfiguracionProviderFirefox() throws FileNotFoundException {
		Mozilla mzFirefox = new Mozilla();
		signError +=PropsConfig.getInstance().getString("errorAlmacenFirefox") ;

		//-- Cargar las librerías de Mozilla (sólo para Windows)
		try {
			mzFirefox.loadMozillaLibraries();
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("[MozillaKeyStoreManager.initKeystores]::No se han podido cargar las librerías para Mozilla Firefox " + e.getMessage());
			//throw new MozillaKeyStoreException("No se han podido cargar las librerías para Mozilla Firefox", e);
		}

		String[] providers = new String[1];
		providers[0] = mzFirefox.getPKCS11CfgInputStream();
		return providers;
	}
	private String[] cargarConfiguracionProviderChromeLinux() throws FileNotFoundException {
		ChromeLinux mzChromeLinux = new ChromeLinux();
		signError +=PropsConfig.getInstance().getString("errorAlmacenChromeLinux");

		//-- Cargar las librerías de Chrome (sólo para Linux)
		try {
			mzChromeLinux.loadChromeLibraries();
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("[ChromeLinuxKeyStoreManager.initKeystores]::No se han podido cargar las librerías para Chrome " + e.getMessage());
			//throw new MozillaKeyStoreException("No se han podido cargar las librerías para Mozilla Firefox", e);
		}

		String[] providers = new String[1];
		providers[0] = mzChromeLinux.getPKCS11CfgInputStream();
		return providers;
	}
	private void cargarConfiguracionProviderLinuxMac() {
		AppleSafari appleSafari = new AppleSafari();
		this.keyStoreDataCollection[0].setKeyStore(appleSafari.initialize());
	}
	public String[] cargarConfiguracionProviderToken(){
		ProvidersConfig providerBundle = new ProvidersConfig();

		String type = "";
		if (System.getProperty("os.name").toLowerCase().contains("linux") || 
				System.getProperty("os.name").toLowerCase().contains("sunos") || 
				System.getProperty("os.name").toLowerCase().contains("solaris")) {
			type = "linux";
		} 
		else if (System.getProperty("os.name").toLowerCase().contains("mac os x")) 
		{
			type = "mac";
		} else {
			type = "windows";			
		}
		
		ArrayList<String> configs = new ArrayList<String>();
		ArrayList<Provider> providers = providerBundle.getProviders(type);
		for (int n = 0; n < providers.size(); n++) {
			try {
				File libraryFile = new File(providers.get(n).getLibrary());
				if (libraryFile.exists()) {
					configs.add("name=" + providers.get(n).getName() + "\nlibrary=" + providers.get(n).getLibrary());
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("cargarConfiguracionProviderToken error: " + e.getMessage());
				cargarMensajeDeError("", "cargarConfiguracionProviderToken", e);				
			}
		}
		return configs.toArray(new String[configs.size()]);
	}
	
	/**
	 * metodo encargado de cargar la keystore del navegador que esta 
	 * ejecutando el Applet
	 *  La password fue controlada en el momento de ingreso
	 * que no fuera nula ni de longitud 0
	 * Se carga el almacenamiento de claves y certificados
	 * @param passToken
	 * @return
	 */
	public boolean cargarKeyStore(String passToken) {
		String[] cfgProvider = null;
		boolean blnReturn = true;
		signError = PropsConfig.getInstance().getString("errorKeyStore");
		String errOpera = "";
		try {
			if(!this.keyStoreDataCollection[1].isKeyStoreOpen()){
				errOpera = "cargarConfiguracionProviderToken";
				cfgProvider =cargarConfiguracionProviderToken();
				errOpera = "cargarKeyStorePKSC11";
				cargarKeyStorePKSC11(cfgProvider, OriginType.token, passToken);
				blnReturn = true;
			}	

			if(!this.keyStoreDataCollection[0].isKeyStoreOpen()){
				if (this.signProps.getBrowser().equalsIgnoreCase("IEXPLORER")) {
					errOpera = "cargarKeyStoreWindows";
					cargarKeyStoreWindows();
					blnReturn = true;
				} 
				if (this.signProps.getBrowser().equalsIgnoreCase("FIREFOX")) {
					errOpera = "cargarConfiguracionProviderFirefox";
					cfgProvider =cargarConfiguracionProviderFirefox();
					errOpera = "cargarKeyStorePKSC11";
					cargarKeyStorePKSC11(cfgProvider, OriginType.browser, null);
					blnReturn = true;
				}
				if (this.signProps.getBrowser().equalsIgnoreCase("CHROME_LINUX")) {
					errOpera = "cargarConfiguracionProviderChromeLinux";
					cfgProvider =cargarConfiguracionProviderChromeLinux();
					errOpera = "cargarKeyStorePKSC11";
					cargarKeyStorePKSC11(cfgProvider, OriginType.browser, null);
					blnReturn = true;
				}
				if (this.signProps.getBrowser().equalsIgnoreCase("LINUX_MAC")) {
					errOpera = "cargarConfiguracionProviderLinuxMac";
					cargarConfiguracionProviderLinuxMac();
					blnReturn = true;
				}
			}
		} catch (KeyStoreException e) {
			e.printStackTrace();
			cargarMensajeDeError(PropsConfig.getInstance().getString(ERROR_ALMACEN),errOpera,e);
			loguearExcepcion(e);
			blnReturn = false;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			cargarMensajeDeError(PropsConfig.getInstance().getString(ERROR_ALMACEN),errOpera,e);
			loguearExcepcion(e);
			blnReturn = false;
		} catch (CertificateException e) {
			e.printStackTrace();
			cargarMensajeDeError(PropsConfig.getInstance().getString(ERROR_ALMACEN),errOpera,e);
			loguearExcepcion(e);
			blnReturn = false;
		} catch (LoginException e) {
			e.printStackTrace();
			cargarMensajeDeError(PropsConfig.getInstance().getString(ERROR_ALMACEN),errOpera,e);
			loguearExcepcion(e);
			blnReturn = false;
		} catch (IOException e) {
			e.printStackTrace();
			cargarMensajeDeError(PropsConfig.getInstance().getString(ERROR_ALMACEN),errOpera,e);
			loguearExcepcion(e);
			blnReturn = false;
		}
		return blnReturn;
	}

	/**
	 * 
	 * @return
	 */
	public String getSignError() {
		return signError;
	}
	/**
	 * Metodo encargado de esribir el el log la exception que  recibe como parametro
	 * @param e
	 */
	private void loguearExcepcion(Exception e){

		writeLogFile("excepcion " + FileSystem.getInstance().getTraceExcepcion(e), 1);
	}
	/**
	 * Le agrega la mensaje e error que le aparecera al usuario toda la informacion
	 * necesario para saber el motivo del error
	 * @param mensajeDeError
	 * @param errOpera
	 * @param e
	 */
	private void cargarMensajeDeError(String mensajeDeError,String errOpera,Exception e){
		if(!mensajeDeError.isEmpty()){
			signError += mensajeDeError;
		}
		if (errOpera.length() > 0) {
			signError += "\r\nOperación " + errOpera;
		}
		if (e.getMessage() != null) {
			signError += "\r\nMensaje JVM: " + e.getMessage();
		}
		if (e.getCause() != null) {
			signError += "\r\nError Cause:" + e.getCause().getClass().getName();
		}
	}
	/**
	 * Se aplica una firma digital a un documento,  posiblemente como una nueva revisión, 
	 * lo que hace posible varias firmas. si retorna null es que el docuemnto no tiene
	 * firmas previas entonces se cera una instancia sin firmas previas sino es que el pdf
	 * tiene firmas previas entonces se agregara a la existente la nuevo firma
	 * @param fout
	 * @param reader
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	private PdfStamper getPdfStamper(FileOutputStream fout,PdfReader reader) throws IOException, DocumentException{
		PdfStamper stp=null;
		try {
			stp = PdfStamper.createSignature(reader, fout, '\0',null, true);
		} catch (com.itextpdf.text.DocumentException e) {
			e.printStackTrace();
			loguearExcepcion(e);
		}
		if(stp==null){
			PdfReader newReader = new PdfReader(PropsConfig.getInstance().getSourceDir() +File.separator+ nombreArchivoFirmado);
			stp = PdfStamper.createSignature(newReader, fout, '\0');

			//Marca de agua
	        int number_of_pages = newReader.getNumberOfPages();
	        int i = 0;
	        ClassLoader cl = this.getClass().getClassLoader();
	        
	        Image watermark_image = Image.getInstance(cl.getResource("images/watermark.jpg"));
	        
	        watermark_image.setAbsolutePosition(200, 400);
	        PdfContentByte add_watermark;            
	        while (i < number_of_pages) {
	          i++;
	          add_watermark = stp.getUnderContent(i);
	          add_watermark.addImage(watermark_image);
	        }
		}
		return stp;
	}
	/**
	 * setea los valores necesarios para agregar el archivo pdf
	 * @param sap
	 */
	private void cargarConfiguracionPdfSignatureAppearance(PdfSignatureAppearance sap){
		byte myRandomNum[] = new byte[12];
		// Se setean los valores de motivo y localidad del
		// archivo de configuracion
		// Motivo y localidad al seleccionar propiedades de la firma
		sap.setReason(signProps.getReason());
		sap.setLocation(signProps.getLocation());
		// Remove this big tick when verified by Adobe
		sap.setAcro6Layers(true);
		// Se obtiene un numero aleatorio para ser utilizado
		// como clave de acceso al documento
		myRandomGen.nextBytes(myRandomNum);
		// Transformacion a hexa de los bytes
		// generados aleatoriamente
		// buffer para la firma y el valor de hash
		sap.setExternalDigest(null, new byte[20], null);
	}
	/**
	 *  Crea MessageDigest para calcular el valor de hash
	 *  Obtiene el contenido sobre el que puede realizarse
	 *	 el calculo del hash al utilizar external signature
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 */
	private byte[] getMessageDigestSHA(PdfSignatureAppearance sap) throws NoSuchAlgorithmException, IOException{
		//
		byte buf[] = new byte[2048];
		int n;
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
		InputStream inp = sap.getRangeStream();
		while ((n = inp.read(buf)) > 0) {
			messageDigest.update(buf, 0, n);
		}
		// Calculo de hash
		return messageDigest.digest();
	}
	private void firmaYverificacionPdf(PdfSignatureAppearance sap) throws NoSuchAlgorithmException, IOException, DocumentException{
		// Obtiene la instancia que se encarga del
		// firmado y verificacion del documento
		PdfPKCS7 pdfSigPKCS7 = sap.getSigStandard().getSigner();
		// Se setea el valor de hash obtenido
		pdfSigPKCS7.setExternalDigest(null, getMessageDigestSHA(sap), null);
		// Signature Dictionary
		PdfSigGenericPKCS sg = sap.getSigStandard();
		PdfLiteral slit = (PdfLiteral) sg.get(PdfName.CONTENTS);
		byte[] outc = new byte[(slit.getPosLength() - 2) / 2];
		byte[] ssig = pdfSigPKCS7.getEncodedPKCS7();
		System.arraycopy(ssig, 0, outc, 0, ssig.length);
		// Se crea el nuevo contenido
		PdfDictionary dic = new PdfDictionary();
		dic.put(PdfName.CONTENTS, new PdfString(outc).setHexWriting(true));
		sap.close(dic);
	}
	/**
	 * Metodo encargado de firmar el pdf descargado que se encuentra enn la carpeta
	 * de archivos temporales del sistema operativo y generar uno nuevo en la cerpeta temporal 
	 * y firmarlo digitalmente con el certificado seleccionado por el usario firmante
	 * *Se crea una instancia de PdfStamper que permite
	 * agregar cualquier tipo de contenido a un Pdf existente
	 * Aplica firma al documento original. Las modificaciones
	 * son mantenidas en memoria y se mantiene la version
	 * original del pdf
	 * Se setea la clave privada, la ruta del certificado
	 * la lista de certificados revocados y el tipo de filtro
	 *	WINCER_SIGNED genera una firma PKCS#7
	 * @return
	 * @throws IOException 
	 */
	public boolean firmarDigitalmenteArchivoPdf() throws IOException  {
		boolean retValue = false;
		FileOutputStream fout = null;
		PdfReader reader=null;
		signError = "";
		String errOpera="";
		String path=PropsConfig.getInstance().getSourceDir() +File.separator;
		try {
			// Se crea el archivo destino
			errOpera = "(new FileOutputStream)"; 
			fout = new FileOutputStream(path+nombreArchivoFirmado);
			// Se crea el apuntador al archivo de entrada 
			errOpera = "(PdfReader new)"; 
			reader = new PdfReader(path+nombreArchivoParaFirmar);
			errOpera = "(PdfStamper.createSignature)"; 
			PdfStamper stp=getPdfStamper(fout,reader);
			errOpera = "(PdfStamper.getSignatureAppearence)"; 
			PdfSignatureAppearance sap = stp.getSignatureAppearance();
			errOpera = "(PdfSignatureAppearence.getNumberOfPages)";

			int number_of_pages = reader.getNumberOfPages();
	        int i = 0;
	        ClassLoader cl = this.getClass().getClassLoader();
			errOpera = "(PdfSignatureAppearence.setAbsolutePosition)";
	        PdfContentByte add_watermark;            
			errOpera = "(PdfSignatureAppearence.number_of_pages)";

            Image watermark_image = Image.getInstance(cl.getResource("images/watermark.png"));
	        while (i < number_of_pages) {
	          i++;
	          add_watermark = stp.getOverContent(i);
 			  Rectangle size = reader.getPageSize(i);
 		      errOpera = "(PdfSignatureAppearence.Image)";


 	          for (int y = 0; y < size.getHeight(); y += watermark_image.getHeight()) {
 	        	  for (int x = 0; x < size.getWidth(); x += watermark_image.getWidth()) {
					  watermark_image.setAbsolutePosition(x, y);
			          add_watermark.addImage(watermark_image);
	 	          }
 	          }
	        }

			errOpera = "(PdfSignatureAppearence.setCrypto)";
			if(currentKeyStoreData.getCrlInfo()!=null){
				sap.setCrypto(currentKeyStoreData.getKeySign(), currentKeyStoreData.getChain(), currentKeyStoreData.getCrlInfo().getCrls(), PdfSignatureAppearance.WINCER_SIGNED);
			}else{
				sap.setCrypto(currentKeyStoreData.getKeySign(), currentKeyStoreData.getChain(), null,PdfSignatureAppearance.WINCER_SIGNED);
			}
			errOpera = "sap.preClose()";
			cargarConfiguracionPdfSignatureAppearance(sap);
			errOpera = "sap.preClose()";
			sap.preClose();
			
			errOpera = "(firmaYverificacionPdf)";
			firmaYverificacionPdf(sap);
			retValue = true;
			PropsConfig.getInstance().setMapaDatosUsuarioFirma(currentKeyStoreData.getMapaSubjectDN());
			descargarProvider();
		}	catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			cargarMensajeDeError(PropsConfig.getInstance().getString("errorFirma"),errOpera,e);
			loguearExcepcion(e);
			if (!retValue ) {
				FileSystem.getInstance().borrarArchivo(PropsConfig.getInstance().getSourceDir() + File.separator  + nombreArchivoFirmado);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
			cargarMensajeDeError(PropsConfig.getInstance().getString("errorFirma"),errOpera,e);
			loguearExcepcion(e);
			if (!retValue ) {
				FileSystem.getInstance().borrarArchivo(PropsConfig.getInstance().getSourceDir() + File.separator  + nombreArchivoFirmado);
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
			cargarMensajeDeError(PropsConfig.getInstance().getString("errorFirma"),errOpera,e);
			loguearExcepcion(e);
		} 
		catch (Exception e) {
			e.printStackTrace();
			cargarMensajeDeError(PropsConfig.getInstance().getString("errorFirma"),errOpera,e);
			loguearExcepcion(e);
		} 
		finally {			
			if(reader!=null){
				reader.close();
			}
			if(fout!=null){
				fout.close();
			}
		}
		return retValue;
	}	
	
	public void finalize () {
	    descargarProvider();
	}

	public boolean isKeyStoreOpen(OriginType originType) {
		return this.keyStoreDataCollection[originType.ordinal()].isKeyStoreOpen();
	}
	
	public KeyStore[] getKeyStores() {
		ArrayList<KeyStore> list = new ArrayList<KeyStore>();
		for(int n = 0; n < this.keyStoreDataCollection.length; n++) {
			if (this.keyStoreDataCollection[n].getKeyStore() != null) {
				list.add(this.keyStoreDataCollection[n].getKeyStore());
			}
		}
		return list.toArray(new KeyStore[list.size()]);
	}
	
	public boolean existeCertificate() {
		boolean result = false;
		for(int n = 0; n < this.keyStoreDataCollection.length; n++) {
			result = result || this.keyStoreDataCollection[n].existeCertificate();
		}
		return result;		
	}

	public boolean isKeyStoreTokenOpen() {
		return this.keyStoreDataCollection[OriginType.token.ordinal()].isKeyStoreOpen();
	}

	public String getCertSerial() {
		return currentKeyStoreData.getCertSerial();
	}
	
	public String getCertHash(String mdAlgorithm) {
		return currentKeyStoreData.getCertHash(mdAlgorithm);		
	}
	
} // end class
