package ar.gob.onti.firmador.view;


import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JApplet;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;

import netscape.javascript.JSObject;
import ar.gob.onti.firmador.model.PreguntasRespuestas;
import ar.gob.onti.firmador.model.PropsConfig;
import ar.gob.onti.firmador.model.PreguntaRespuesta;
import java.io.Console;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 *   El Applet de firma digital de la Subsecretaria de Tecnologías de Gestión está 
 *   desarrollado en Java y se ejecuta desde un navegador web para realizar la firma 
 *   digital de una archivo pdf, siendo este Applet capaz de emplear directamente 
 *   los certificados accesibles desde el almacén de claves de Windows o vía PKCS11, 
 *   accediendo directamente al repositorio de Mozilla Firefox, ofreciendo una gestión 
 *   transparente para el usuario.
 *  
 *  Clase principal donde se llama al metodo init()
 *  en donde se ejecuta el Applet
 *  
 *   Ejemplo de configuracion del Apllet de firma digital
 *   Configuración del Applet en la página web donde se  ejecutara.
 *< applet  code="ar.gov.firmador.FirmaApplet.class" codebase="resource/applet/"	 
 *   archive="FirmaApplet-1.0-jar-with-dependencies.jar"  width="700"	height="410" >
 * < param  name="URL_DESCARGA"	 value=”http://ip:puerto /rutaApplicacion/downloadServlet” >
 * < param  name="URL_SUBIR"	value=" http://ip:puerto /rutaApplicacion/post_subir_archivo">
 * < param  name="MOTIVO"  value="Firmar Dictamen">
 * < param  name="CODIGO"  value="23|233|PLIEGO|19" />
 *   </applet>
 *
 * @author ocaceres
 */
public class FirmaApplet extends JApplet{
	private static final long serialVersionUID = 1L;
	public static final String IEXPLORER = "IEXPLORER";
	public static final String FIREFOX = "FIREFOX";
	public static final String CHROME_LINUX = "CHROME_LINUX";
	public static final String LINUX_MAC = "LINUX_MAC";
	
	private VentanaPrincipal myMainWin;
	
	/*************************************************
	 *****		PUBLIC INTERFACE
	 **************************************************/
	
	public boolean agregarDocumento(String id, String url) {
		PropsConfig props = myMainWin.getSignProps();
		props.agregarDocumento(id, url);
		myMainWin.setEstado(VentanaPrincipal.Estados.DOCUMENTO_AGREGADO);
		return true;
    }
	
	public boolean quitarDocumento(String id) {
		PropsConfig props = myMainWin.getSignProps();
		if (props.existeDocumento(id)) {
			props.borrarDocumento(id);
		}
		myMainWin.setEstado(VentanaPrincipal.Estados.DOCUMENTO_QUITADO);		
		return true;
    }
	
	/**
	 * Metodo principal del Applet a partir del cual el este se 
	 * inicializa y  se ejecutara
	 */
	public void init() {
		
		System.setProperty("java.net.useSystemProxies","true");
		String browser=getBrowser();
		
		System.out.println("File.pathSeparator:" + File.pathSeparator);
		System.out.println("File.separator:" + File.separator);
		
		String uploadURL = this.getParameter("URL_SUBIR");
		String motivo = this.getParameter("MOTIVO");
		String localidad = this.getParameter("LOCALIDAD");
		String downloadURL = this.getParameter("URL_DESCARGA");
		String codigo = this.getParameter("CODIGO");
		String nombreArchivo = this.getParameter("NOMBRE_ARCHIVO");
		String userName = this.getParameter("USERNAME");
		String cookie = this.getParameter("COOKIE");
		String multiple = this.getParameter("MULTIPLE");

		
		PreguntasRespuestas preguntas = new PreguntasRespuestas();
		preguntas.parse(URLDecoder.decode(this.getParameter("PREGUNTAS")));
			
		myMainWin= new VentanaPrincipal(this,browser);
		myMainWin.inicializar();
		setSize(400, 120);
		myMainWin.setCodigo(codigo);
		myMainWin.setCookie(cookie);
		PropsConfig config= myMainWin.getSignProps();
		config.setNombreArchivo(nombreArchivo);
		config.setUploadURL(uploadURL);
		config.setLocation(localidad);
		config.setReason(motivo);
		config.setUserName(userName);
		config.setPreguntas(preguntas);
		config.setMultiple(multiple != null && multiple.equalsIgnoreCase("true"));
		config.setVisible(true);
		myMainWin.initProps(this);
		myMainWin.initLogFile(this);
		myMainWin.initSigner(this);
		
		if (! config.isMultiple())  {
			config.agregarDocumentoUnico(downloadURL);
			myMainWin.showProgress(PropsConfig.getInstance().getString("progresoBajandoArchivo"));			
			File file = myMainWin.getfirmaControler().descargarDocumentoParaFirmar(this, downloadURL);
			if (file != null) {
				myMainWin.setEstado(VentanaPrincipal.Estados.DESCARGA_OK);
				config.getDocumentoUnico().setArchivoAFirmar(file);
			} else {
				myMainWin.setEstado(VentanaPrincipal.Estados.DESCARGA_ERROR);
			}
		} else {
			config.borrarDocumentos();
		}
		
		try {
			getAppletContext().showDocument(new URL("javascript:appletLoaded()"));
		} catch (MalformedURLException e) {
			System.err.println("Failed to call JavaScript function appletLoaded()");
		}		
	}	
	
	/**
	 * Metodo encargado de ejecutar metodos javascripts 
	 * y preguntar al Applet el navegador en el que se esta ejecutando
	 * @return
	 */
	private String getBrowser(){
		//FIREFOX/IEXPLORER
		String browser=IEXPLORER;
			try {
				JSCommands.init(this);
	
				JSObject document = (JSObject) JSCommands.getWindow().getMember("navigator");
	
				String userAgent = ((String) document.getMember("userAgent")).toLowerCase();
				System.out.println("userAgent: " + userAgent);
				System.out.println("os.name: " + System.getProperty("os.name"));
				
				if (System.getProperty("os.name").toLowerCase().contains("linux") || 
						System.getProperty("os.name").toLowerCase().contains("sunos") || 
						System.getProperty("os.name").toLowerCase().contains("mac os x") || 
						System.getProperty("os.name").toLowerCase().contains("solaris")) {
					if (userAgent.indexOf("firefox") > -1 || 
							userAgent.indexOf("seamonkey") > -1 ||
							userAgent.indexOf("netscape") > -1)
					{
						browser=FIREFOX;
					}
					else if (userAgent.indexOf("chrome") > -1) 
					{
						browser=CHROME_LINUX;						 
					} else {
						browser=LINUX_MAC;
					}
				} 
				else if (userAgent != null)
				{
					userAgent = userAgent.toLowerCase(new Locale("es_AR"));
	
					if ((userAgent.indexOf("explorer") > -1) || 
							(userAgent.indexOf("msie") > -1)||
							(userAgent.indexOf("chrome") > -1))
					{
						browser=IEXPLORER;
					}
					else if (userAgent.indexOf("firefox") > -1 || 
							userAgent.indexOf("seamonkey") > -1 ||
							userAgent.indexOf("netscape") > -1)
					{
						browser=FIREFOX;
					}
					else  
					{
						browser=IEXPLORER;
					}					
				}
			} catch (netscape.javascript.JSException e) {

			}
		

		return browser;
	}
}