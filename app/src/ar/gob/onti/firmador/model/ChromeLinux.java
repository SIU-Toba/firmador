/**
* LICENCIA LGPL:
* 
* Esta librería es Software Libre; Usted puede redistribuirla y/o modificarla
* bajo los términos de la GNU Lesser General Public License (LGPL) tal y como 
* ha sido publicada por la Free Software Foundation; o bien la versión 2.1 de 
* la Licencia, o (a su elección) cualquier versión posterior.
* 
* Esta librería se distribuye con la esperanza de que sea útil, pero SIN 
* NINGUNA GARANTÍA; tampoco las implícitas garantías de MERCANTILIDAD o 
* ADECUACIÓN A UN PROPÓSITO PARTICULAR. Consulte la GNU Lesser General Public 
* License (LGPL) para más detalles
* 
* Usted debe recibir una copia de la GNU Lesser General Public License (LGPL) 
* junto con esta librería; si no es así, escriba a la Free Software Foundation 
* Inc. 51 Franklin Street, 5º Piso, Boston, MA 02110-1301, USA o consulte
* <http://www.gnu.org/licenses/>.
*
* Copyright 2011 Agencia de Tecnología y Certificación Electrónica
*/

package ar.gob.onti.firmador.model;
/*
 * ChromeLinux.java
 * author: mpin
 * owner : ONTI
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import netscape.javascript.JSObject;

import ar.gob.onti.firmador.exception.ChromeNotFoundException;
import ar.gob.onti.firmador.view.JSCommands;

/**
 * clase que modeliza el acceso a la keystore de firefox
 * @author ocaceres
 *
 */
public class ChromeLinux {
	/*
	 * Nombre del provider
	 */
	String PROVIDER_NAME = "NSS";

	public ChromeLinux()  {
		RegQuery rq = new RegQuery();
		String userAppDataDir = rq.getCurrentUserPath();
	}

	/**
	 * Obtiene la ruta a la carpeta donde se encuentra instalado ChromeLinux
	 * 
	 * @return Ruta a la carpeta donde se encuentra instalado ChromeLinux
	 * @throws MozillaNotFoundException No se ha encontrado una instalación de Mozilla
	 */
	public File getChromeFolder() throws ChromeNotFoundException {
		//-- Si el SO es Linux / Unix ...
		if(System.getProperty("os.name").contains("inux") || System.getProperty("os.name").contains("SunOS") || 
				System.getProperty("os.name").contains("olaris")){
			System.out.println("[ChromeLinux]::Buscando la carpeta con las librerías de Mozilla en Linux/Unix");

			JSObject document = (JSObject) JSCommands.getWindow().getMember("navigator");
			String userAgent = ((String) document.getMember("userAgent")).toLowerCase();

			if (new File("/opt/google/chrome/libnss3.so.ld").exists()) {
				System.out.println("[ChromeLinux]::Se ha encontrado la carpeta con las librerías de Google Chrome en Linux/Unix: /opt/google/chrome");
				return new File("/opt/google/chrome");
			}
		}
		
		System.out.println("[ChromeLinux]::No se ha podido obtener la carpeta de instalación de Mozilla Firefox");
		throw new ChromeNotFoundException("No se ha podido obtener la carpeta de instalación de Mozilla Firefox");
	}

	public void loadChromeLibraries() throws Throwable {
		//File chromeFolder= this.getChromeFolder();
	}
	
	/**
	* Obtiene el nombre de la librería del provider NSS dependiendo del SO
	* 
	* @return Nombre de la librería del provider NSS
	*/
	public String getChromeNSSLibraryName () {
			String nssLib = "libnss3.so.ld";
			return nssLib;
	}

	public String getPKCS11CfgInputStream() throws FileNotFoundException {
		String line = null;
		String providerConfig = "";
		File regFile = new File(new File (System.getProperty("user.home")), "/.pki/nssdb/pkcs11.txt");
		BufferedReader in = new BufferedReader(new FileReader(regFile));

		try {
			while ((line = in.readLine()) != null) {
				if (!line.isEmpty()) {
					providerConfig += line + "\r";
				}
			}
		} catch (Exception e) {
			System.out.println("Ocurrio un error al leer la configuracion de los perfiles de Firefox: " + e);
		} finally {
			try { in.close(); } catch (Exception e) {}
		}
		
		if (providerConfig.length() >= 2) {
			providerConfig = providerConfig.substring(0, providerConfig.length() - 1);
		}
		
		System.out.println("providerConfig: " + providerConfig);
		return providerConfig;
	}
	
	public String getApplicationPath() {
		try {
			File chromeFolder = this.getChromeFolder();
			return chromeFolder.getAbsolutePath();
		} catch (ChromeNotFoundException e) {
			// TODO Auto-generated catch block
			return null;
		}
		
	}
}

