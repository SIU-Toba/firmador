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
 *< param  name="ID_APPLICACION"  value="ecom.ar">
 * < param  name="CODIGO"  value="23|233|PLIEGO|19" />
 * <param  name="NOMBRE_ARCHIVO" value="Acto administrativo "/>
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
		String idApplicacion = this.getParameter("ID_APPLICACION");
		String codigo = this.getParameter("CODIGO");
		String nombreArchivo = this.getParameter("NOMBRE_ARCHIVO");
		String userName = this.getParameter("USERNAME");
		String objetoDominio = this.getParameter("ID_OBJETO_DOMINIO");
		String tipoArchivo = this.getParameter("TIPO_ARCHIVO");
		String cookie = this.getParameter("COOKIE");
		
		PreguntasRespuestas preguntas = new PreguntasRespuestas();
		preguntas.parse(URLDecoder.decode(this.getParameter("PREGUNTAS")));
			
		VentanaPrincipal myMainWin= new VentanaPrincipal(this,browser);
		myMainWin.inicializar();
		setSize(600, 300);
		myMainWin.setIdApplicacion(idApplicacion);
		myMainWin.setCodigo(codigo);
		myMainWin.setObjetoDominio(objetoDominio);
		myMainWin.setTipoArchivo(tipoArchivo);
		myMainWin.setCookie(cookie);
		PropsConfig config= myMainWin.getSignProps();
		config.setNombreArchivo(nombreArchivo);
		config.setUploadURL(uploadURL);
		config.setDownloadURL(downloadURL);
		config.setLocation(localidad);
		config.setReason(motivo);
		config.setUserName(userName);
		config.setPreguntas(preguntas);
		config.setVisible(true);
		myMainWin.initProps(this);
		myMainWin.initLogFile(this);
		myMainWin.initSigner(this);

		myMainWin.getfirmaControler().descargarDocumentoParaFirmar( this);
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