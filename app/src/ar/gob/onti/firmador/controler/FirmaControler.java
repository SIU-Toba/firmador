package ar.gob.onti.firmador.controler;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.security.KeyStoreException;
import java.util.UUID;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import ar.gob.onti.firmador.controler.PdfControler.OriginType;
import ar.gob.onti.firmador.controler.conection.HttpFileDownLoader;
import ar.gob.onti.firmador.controler.conection.HttpFileUploader;
import ar.gob.onti.firmador.model.Documento;
import ar.gob.onti.firmador.model.PreguntasRespuestas;
import ar.gob.onti.firmador.model.PropsConfig;
import ar.gob.onti.firmador.view.DialogSecure;
import ar.gob.onti.firmador.view.VentanaPrincipal.Estados;
import ar.gob.onti.firmador.view.VentanaPrincipal;
import ar.gob.onti.firmador.view.certificatelist.CertsTreeTable;
import ar.gob.onti.firmador.view.questionlist.QuestionList;
import java.util.HashMap;
import java.util.Map;





/**
 * Controlador que se encarga de manejar las acciones que realiza el usuario
 * @author ocaceres
 *
 */
public class FirmaControler {
	private VentanaPrincipal mainWindow;
	private	ImageIcon okIcon = null;
	private Icon errorIcon = null;
	private PropsConfig myProps=null;
	private String error="";
	private boolean arbolCargado = false;
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * metodo encargado de loguear los mensajes de error y mostrar al usuario
	 * el error que ha ocurrido por el cual no se pudo realizar la accion
	 * @param container
	 * @param mensaje
	 * @param exception
	 */
	public void mostrarMensajesError(Container container,String mensaje,Exception exception){
		String errMsg="";
		if (exception != null) {
			errMsg += "\nMensaje JVM: " + exception.getMessage();
			writeLogFile("excepcion " + FileSystem.getInstance().getTraceExcepcion(exception), 1);
		}
		JOptionPane.showMessageDialog(container,mensaje+errMsg,myProps.getString("error"), JOptionPane.ERROR_MESSAGE,this.errorIcon);
		
	}
	public void mostrarMensajesOk(Container container,String mensaje,String titulo){
		
		JOptionPane.showMessageDialog(container,mensaje,titulo, JOptionPane.INFORMATION_MESSAGE,this.okIcon);
		
	}
	
	/**
	 * Constructor del controler de la firma recibe como parametro
	 * el contnedor de los componentes del Applet
	 * @param mainWindow
	 */
	public FirmaControler(VentanaPrincipal mainWindow) {
		super();

		this.mainWindow = mainWindow;
		this.error="";
		ClassLoader cl = this.getClass().getClassLoader();
		okIcon =  new ImageIcon(cl.getResource("images/OKShield-64.png"));
		errorIcon= new ImageIcon(cl.getResource("images/ErrorCircle-64.png"));
		this.myProps= PropsConfig.getInstance();
	}
	
	
	public void firmarDocumentos(Container container, HashMap<String, Documento> documentos) {
		arbolCargado = false;
		mainWindow.getPdfControler().cleanCurrentKeyStoreData();
		boolean ok = true;
		for (Map.Entry<String, Documento> entry : documentos.entrySet()) {
			if (! firmarDocumento(container, entry.getValue())) {
				ok = false;
				break;
			}
		}
	
		if (ok) {
			//Borra archivos sin firmar			
			for (Map.Entry<String, Documento> doc : documentos.entrySet()) {
				FileSystem.getInstance().borrarArchivo(doc.getValue().getArchivoAFirmar().getPath()); 				
			}
			mainWindow.setEstado(VentanaPrincipal.Estados.FIRMA_DOC_OK);
		} else {
			mainWindow.setEstado(VentanaPrincipal.Estados.FIRMA_DOC_ERROR);
		}
	}

	
	/**
	 * 
	 * Metodo encargado de ralizar la firma digital del archivo pdf con el certificado seleccionado
	 * por el usario firmante
	 * Solo se elimina el documento original en 
	 * caso que el firmado haya sido recibido exitosamente
	 * No se pasa el nombre ya que en ese caso se asume el
	 * archivo por default que es el actualmente seleccionado
	 * @param container recibe como parametro el contenedor de los componentes el Applet
	 */
	public boolean firmarDocumento(Container container, Documento documento) {
		if (!FileSystem.getInstance().isExisteArchivo(documento.getArchivoAFirmar())) {
			System.err.println("No existe el archivo " + documento.getArchivoAFirmar().getAbsolutePath());
			mostrarMensajesError(container, myProps.getString("errorArchivoParaFirmarInexistente"), null);
			return false;
		}
		try {
			if (firmarDocumentoPdf(container, documento)) {
				return true;
			} else {
				return false;
			}
		} catch (java.security.ProviderException e) {
       			e.printStackTrace();
				mostrarMensajesError(container, myProps.getString("errorFirmaProvider"), e);
				return false;

		} catch (Exception e) {
			e.printStackTrace();
			mostrarMensajesError(container, myProps.getString("errorEliminandoSinFirmar"), e);
			return false;
		}
	}
	

	
	/**
	 * Metodo encargado de envar mediando un post el archivo pdf firmado
	 * junto con los parametros de segurdad del applet
	 * 
	 * Se elimina el documento firmado independientemente
	 * si se lo pudo subir
	 * @param evt
	 * @param container recibe como parametro el contenedor de los componentes el Applet
	 */
	public boolean subirDocumento(Container container, Documento documento) {
		if (! documento.getArchivoFirmado().isFile()) {
			mostrarMensajesError(container, myProps.getString("errorArchivoFirmadoInexistente"), null);
			return false;
		}
		if (uploadDoc(container, documento)) {
			try {
				if (FileSystem.getInstance().borrarArchivo(documento.getArchivoFirmado().getPath()) ) {
					return true;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				mostrarMensajesError(container,myProps.getString("errorEliminandoFirmado"), e);
			}
			return true;
		}else{
			return false;
		}
	}
	
	public StringBuffer preguntarPinToken(Container container){
		StringBuffer tokenPin = new StringBuffer();
		char[] c = DialogSecure.getInputSecure( findParentFrame(), myProps.getString("ingreseToken"), true,myProps.getString("tokenPin"), 15);
		for (int i = 0; i < c.length; i++){
			tokenPin.append(c[i]);
		}
		if (tokenPin.length() == 0) {
			mostrarMensajesError(container,myProps.getString("errorPinToken"), null);
			return tokenPin;
		}
		return tokenPin;
	}
	/**
	 * Se inicializa el almacen de certificados,
	 * se recuperan la clave privada y el certificado
	 * para firmar los documentos
	 * @param container
	 * @return
	 */
    public boolean cargarArbolDeCertificados(Container container){
		if (mainWindow.getSignProps().isMultiple() && arbolCargado) {
			return true;
		}
		
    	if (!mainWindow.getPdfControler().existeCertificate()) {
			StringBuffer tokenPin = new StringBuffer();
//			 Se pide el ingreso del PIN en caso de token
/*
			if (mainWindow.getPdfControler().isKeyStoreTokenOpen()) {
				tokenPin=preguntarPinToken(container);
			}
 */
			if (!mainWindow.getPdfControler().cargarKeyStore(tokenPin.toString()) ) {
				mostrarMensajesError(container, mainWindow.getPdfControler().getSignError(), null);

				return false;
			} else{
				if(!mostrarCertificados(tokenPin.toString())){
					return false;
				}
			}

		}	
    	return true;
    }
	/**
	 * Se muestra la lista de preguntas de confirmación
	 * que el usuario deberá responder correctamente
	 * @param container
	 * @return
	 */
    public boolean mostrarListaDePreguntas(Container container){
    	PreguntasRespuestas preguntasRespuestas = this.myProps.getPreguntas();

    	if (preguntasRespuestas == null || preguntasRespuestas.getItems().size() == 0) {
    		return true;
    	}
    	QuestionList questionListDlg = new QuestionList();
		questionListDlg.inicializar(findParentFrame());
		if (!questionListDlg.getIsCancel()) {
			if (questionListDlg.getIsValid()){
				return true;
			}else{
				mostrarMensajesError(mainWindow.getContainer(),PropsConfig.getInstance().getString("errorBadAnswer"), null);
				return false;
			}
		} else {
			return false;		
		}
    }
    /**
	 * Metodo encargado de firmar el archivo Pdf y cargar los vectores del los hash y md5 de los archivos
	 * 
	 * Si no hay condicion de error, entonces se recupera
	 * el PIN del token, siempre que el tipo de almacenamiento
	 * seleccionado sea PKCS11 y el almacen de certificados aun
	 * no ha sido inicializado
	 * @param md5Values vector donde se carga los md5 de los archivos
	 * @param certValues vector donde se carga el hash Del certificado y su numero de serie
	 * @param container recibe como parametro el contenedor de los componentes el Applet
	 * @return
	 * @throws IOException 
	 */
	public boolean firmarDocumentoPdf(Container container, Documento documento) throws IOException {
		if(!mostrarListaDePreguntas(container)){
			return false;
		}
		if(!cargarArbolDeCertificados(container)){
			return false;
		} else {
			arbolCargado = true;
		}
		mainWindow.showProgress(myProps.getString("progresoFirmando"));
		
		String archivoFirmado = getNombreArchivoFirmado(documento.getArchivoAFirmar());
		mainWindow.getPdfControler().setNombreArchivoParaFirmar(documento.getArchivoAFirmar().getName());
		mainWindow.getPdfControler().setNombreArchivoFirmado(archivoFirmado);
		if (mainWindow.getPdfControler().firmarDigitalmenteArchivoPdf()) {
			myProps.getMapaDatosUsuarioFirma().put("MD5_ARCHIVO", mainWindow.getPdfControler().getMessageDig("MD5", documento.getArchivoAFirmar().getName()));
			myProps.getMapaDatosUsuarioFirma().put("MD5_ARCHIVO_FIRMADO", mainWindow.getPdfControler().getMessageDig("MD5", archivoFirmado));
			myProps.getMapaDatosUsuarioFirma().put("SERIAL_CERTIFICADO", mainWindow.getPdfControler().getCertSerial());
			myProps.getMapaDatosUsuarioFirma().put("HASH_CERTIFICADO", mainWindow.getPdfControler().getCertHash("SHA1"));
			File firmado = new File(documento.getArchivoAFirmar().getParent() + "//" + archivoFirmado);
			documento.setArchivoFirmado(firmado);
		} else {
			mostrarMensajesError(container,  myProps.getString("errorFirma") + mainWindow.getPdfControler().getSignError(), null);
			return false;
		}
		return true;
	}
	/**
	 * Metodo encargado de desplegar los certificados en una nueva ventana
	 *  para que el usario del Applet pueda selecionar uno para firmar
	 * Si no se ha cargado el almacen de certificados, y se ha
	 * seleccionado token, entonces se pide la clave de acceso
	 * Se inicializa el almacen de certificados,
	 * se recuperan la clave privada y el certificado
	 * para firmar los documentos
	 * @return
	 */
	public boolean mostrarCertificados (String tokenPin) {
		mainWindow.getCertSelecionado().setText("");
		if (!mainWindow.getPdfControler().cargarKeyStore(tokenPin)) {
				mostrarMensajesError(mainWindow.getContainer(), mainWindow.getPdfControler().getSignError(), null);
		}
		try {
			CertsTreeTable aCertsDlg = new CertsTreeTable();
			aCertsDlg.inicializar(findParentFrame(), mainWindow.getPdfControler().getKeyStores(), mainWindow.getSignProps().getAutoCertificantes());
			if (aCertsDlg.getSelectedCert().length() > 0){
				mainWindow.getCertSelecionado().setText(aCertsDlg.getSelectedCert());
				if(!mainWindow.getPdfControler().cargarClavePrivadaYCadenaDeCertificados( mainWindow.getCertSelecionado().getText().trim(), tokenPin, true)){
					mostrarMensajesError(mainWindow.getContainer(),mainWindow.getPdfControler().getSignError() , null);
					return false;
				}
			}else{
				return false;
			}
		} catch (KeyStoreException e) {
			e.printStackTrace();;
			 mostrarMensajesError(mainWindow.getContainer(),  myProps.getString("errorBrowseCerts"), e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			 mostrarMensajesError(mainWindow.getContainer(),  myProps.getString("errorBrowseCerts"), e);
		}
		return true;
	}
	/**
	 * metodo que busca el frame del applet
	 * @return
	 */
	private Frame findParentFrame(){
		Container c = mainWindow.getContainer();
		while(c != null){
			if (c instanceof Frame){
				return (Frame)c;
			}
			c = c.getParent();
		}
		return (Frame)null;
	} 
	/**
	 * Metodo encargado de conectarse con el servlet o el post e enviar los parametros y el archivo
	 * firmado a la aplicacion que esta ejecutando el Applet
	 * @param md5Values vector donde se carga los md5 de los archivos
	 * @param certValues vector donde se carga el hash cel certificado y su numero de serie
	 * @param container recibe como parametro el contenedor de los componentes el Applet
	 * @return
	 */
	public boolean uploadDoc(Container container, Documento documento) {
		HttpFileUploader fileUp = new HttpFileUploader();
		try {
			if (fileUp.connectURL(mainWindow.getSignProps().getUploadURL())) {

				if (fileUp.doUpload(documento,
										mainWindow.getSignProps(), 
										mainWindow.getCodigo(), 
										mainWindow.getCookie()) ) {
					return true;
				} else {
					mostrarMensajesError(container, myProps.getString("errorEnvioServer") + fileUp.getHttpFileError(), null);
					return false;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			mostrarMensajesError(container, "Método MainWindow.UploadDoc(): "+myProps.getString("errorEnvioDictamenServer") + " "  + documento.getArchivoFirmado().getPath(), e)	;
		}
		return false;
	}
	  
	public String agregarParametroUrl(Container container,String url,HttpFileDownLoader fileDown){
		String nuevaURL=url;
		String concatenar = nuevaURL.indexOf("?") >= 0 ? "&" : "?";
		//try {
            nuevaURL = nuevaURL + concatenar + "codigo=" + this.mainWindow.getCodigo();
			mainWindow.getSignProps().setNombreArchivoTemporal(UUID.randomUUID().toString() + ".pdf");
			fileDown.setLocalFileName(mainWindow.getSignProps().getNombreArchivoTemporal());
		//} catch (UnsupportedEncodingException e) {
			//	mostrarMensajesError(container, myProps.getString("errorUrl"), e);
			//}
		return nuevaURL;
	}
   /**
    * Metodo que se encarga de descargar el Archivo pdf que va a ser firmado de la la 
    * Applicacion que esta ejecutando el Applet y guardarlo en la carpeta de archivos
    * temporales del sistema operativo
    * @param container recibe como parametro el contenedor de los componentes el Applet
    */
	public File descargarDocumentoParaFirmar (Container container, String documentoUrl) {
		HttpFileDownLoader fileDown = new HttpFileDownLoader();
		String url = agregarParametroUrl(container, documentoUrl, fileDown);
        System.out.println("Downloading from " + url);
		if (url.startsWith("http://") || url.startsWith("https://")) {
			if (fileDown.connectURL(url)) {			
				try {
					if (fileDown.doDownload(mainWindow.getSignProps().getSourceDir(), fileDown.getLocalFileName(), mainWindow.getCookie())) {
						File archivoAFirmar = new File(mainWindow.getSignProps().getSourceDir() + File.separator + fileDown.getLocalFileName());
						if (FileSystem.getInstance().isExisteArchivo(archivoAFirmar)) {
							return archivoAFirmar;
						}
					}
				} catch (IOException e) {
                    e.printStackTrace();
					mostrarMensajesError(container, myProps.getString("errorDescargarDoc")+ fileDown.getHttpFileError(), e);
					return null;
				}
			} 
			mostrarMensajesError(container, myProps.getString("errorDescargarDoc")+ fileDown.getHttpFileError(), null);
			return null;
		} else {
			//TODO: Que es esto? permite firmar documentos del disco local? WTF
			mainWindow.setEstado(VentanaPrincipal.Estados.DESCARGA_ERROR);
			System.err.println("No se permite firmar documentos locales");
			return null;
		}
	}
	
	/**
	 * metodo que devuelve el nombre del archivo que va ser firmado
	 *  Se genera el archivo de salida que es igual al nombre
	 * del origen mas sufijo _firmado
	 * @return
	 */
	public String getNombreArchivoFirmado(File file) {

		String destFileName = "";
		destFileName = file.getName().substring(0, file.getName().length()-4) +  "_firmado.pdf";
		return destFileName;
	}
	
	/**
	 * 
	 * Metodo encargado de desplegar el archivo pdf descargado por la applicacion
	 * para que el usario lo pueda visualizar
	 * @param container recibe como parametro el contenedor de los componentes el Applet
	 */
	public void visualizarDocumento(Container container,File archivoAVisualizar) {
		try   
		{
			if (FileSystem.getInstance().isExisteArchivo(archivoAVisualizar)) {
				writeLogFile("File view : " +archivoAVisualizar.getPath(), 0);
				
				String os = System.getProperty("os.name").toLowerCase();
				if (os.contains("win")) {
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + archivoAVisualizar.getPath());
				} else if (os.contains("mac")) {
					Runtime.getRuntime().exec(new String[]{"/usr/bin/open", archivoAVisualizar.getPath()});					
				} else {
					Runtime.getRuntime().exec(new String[]{"acroread", archivoAVisualizar.getPath()});
				}
			} else {
				mostrarMensajesError(container, "visualizarDocumento()"+myProps.getString("errorVerDocumento"), null);
			}
		} catch (IOException e) {
			e.printStackTrace();
			mostrarMensajesError(container, "visualizarDocumento()"+myProps.getString("errorVerDocumento"), e);
		}
	}	
	/**
	 * metodo que cierra el archivo log
	 */
	public void closeLogFile() {
		try {
			// Cerrado de todos los archivos
			mainWindow.getAppLogFile().info("END PROCESS  ------------------");
			mainWindow.getAppLogFile().removeHandler(mainWindow.getHndLog());
			
		} catch (SecurityException e) {
			this.mainWindow.getAppLogFile().info(e.getMessage());
		}
		finally{
			if(mainWindow.getHndLog()!=null){
			mainWindow.getHndLog().close();
			}
		}
	}
    /**
     * metodo encargado de escribir el archivo log los eventos que se registran en los pasos que 
     * se siguen para la firma del docuemnto pdf
     * @param strLogMsg
     * @param typeWrite
     */
	public void writeLogFile(String strLogMsg, int typeWrite) {
	
			if (mainWindow.getAppLogFile() != null) {
				if (typeWrite == 0) {
					mainWindow.getAppLogFile().info(strLogMsg);
				} else {
					mainWindow.getAppLogFile().severe(strLogMsg);
				}
			}	
		
	}
}
