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
 * Mozilla.java
 * author: mpin
 * owner : ONTI
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import netscape.javascript.JSObject;

import ar.gob.onti.firmador.exception.MozillaNotFoundException;
import ar.gob.onti.firmador.util.Util;
import ar.gob.onti.firmador.view.JSCommands;

/**
 * clase que modeliza el acceso a la keystore de firefox
 * @author ocaceres
 *
 */
public class Mozilla {

	private String execName;
	private String profileDir;
	private String lockFile;
	private String secDB;

	/*
	 * Nombre del provider
	 */
	String PROVIDER_NAME = "NSS";

	public Mozilla()  {
		RegQuery rq = new RegQuery();
		String userAppDataDir = rq.getCurrentUserPath();
		profileDir = userAppDataDir + "\\Mozilla\\Firefox\\Profiles\\";
		execName = "firefox.exe";
		lockFile = "parent.lock";
		secDB = "secmod.db";
	}

	/**
	 * Obtiene la ruta a la carpeta donde se encuentra instalado Mozilla
	 * 
	 * @return Ruta a la carpeta donde se encuentra instalado Mozilla
	 * @throws MozillaNotFoundException No se ha encontrado una instalación de Mozilla
	 */
	public File getMozillaFolder() throws MozillaNotFoundException {
		//-- Si el SO es Windows ...
		if (System.getProperty("os.name").contains("indows")) {
			System.out.println("[Mozilla]::Buscando la carpeta de instalación de Mozilla en Windows");
			File mozillaFolder = new File (new File (System.getenv("PROGRAMFILES")), "Mozilla Firefox");
			if (mozillaFolder.exists()) {	
				System.out.println("[Mozilla]::Se ha encontrado la carpeta de instalación de Mozilla Firefox en Windows: " + mozillaFolder.getAbsolutePath());
				
				//-- Si el path al directorio contiene caracteres extraños no funcionará la carga del
				//-- provider PKCS#11 de SUN
				if (mozillaFolder.getAbsolutePath().contains(")") || 
						mozillaFolder.getAbsolutePath().contains("(") || 
						mozillaFolder.getAbsolutePath().contains("\u007E")) { 
					
					//-- Copiar las DLLs a una ubicación temporal que será la que utilizará a partir de ahora
					try {
						System.out.println("[Mozilla]::La carpeta de instalación de Mozilla Firefox en Windows contiene caracteres incompatibles con provider PKCS11: " + mozillaFolder.getAbsolutePath());
						File mozillaTempFolder = File.createTempFile("mozillaTemp", null);
						mozillaTempFolder.delete();
						mozillaTempFolder.mkdirs();
						Util.copyFile(new File (mozillaFolder, "softokn3.dll"), new File (mozillaTempFolder, "softokn3.dll"));
						Util.copyFile(new File (mozillaFolder, "plc4.dll"), new File (mozillaTempFolder, "plc4.dll"));
						Util.copyFile(new File (mozillaFolder, "plds4.dll"), new File (mozillaTempFolder, "plds4.dll"));
						Util.copyFile(new File (mozillaFolder, "nspr4.dll"), new File (mozillaTempFolder, "nspr4.dll"));
						
						try { 
							Util.copyFile(new File (mozillaFolder, "mozutils.dll"), new File (mozillaTempFolder, "mozutils.dll"));
						} catch (FileNotFoundException e) {}
						try { 
							Util.copyFile(new File (mozillaFolder, "mozglue.dll"), new File (mozillaTempFolder, "mozglue.dll"));
						} catch (FileNotFoundException e) {}
						try { 
							Util.copyFile(new File (mozillaFolder, "mozsqlite3.dll"), new File (mozillaTempFolder, "mozsqlite3.dll"));
						} catch (FileNotFoundException e) {}
						try { 
							Util.copyFile(new File (mozillaFolder, "sqlite3.dll"), new File (mozillaTempFolder, "sqlite3.dll"));
						} catch (FileNotFoundException e) {}
						try { 
							Util.copyFile(new File (mozillaFolder, "mozcrt19.dll"), new File (mozillaTempFolder, "mozcrt19.dll"));
						} catch (FileNotFoundException e) {}
						try { 
							Util.copyFile(new File (mozillaFolder, "nssutil3.dll"), new File (mozillaTempFolder, "nssutil3.dll"));
						} catch (FileNotFoundException e) {}
						try { 
							Util.copyFile(new File (mozillaFolder, "freebl3.dll"), new File (mozillaTempFolder, "freebl3.dll"));
						} catch (FileNotFoundException e) {}
						try { 
							Util.copyFile(new File (mozillaFolder, "nssdbm3.dll"), new File (mozillaTempFolder, "nssdbm3.dll"));
						} catch (FileNotFoundException e) {}
						
						return mozillaTempFolder;
					} catch (Exception e) {
						System.out.println("[MozillaKeyStoreManager]::No se han podido cargar las DLLs de Mozilla Firefoxen una carpeta temporal " +  e.getMessage());
						throw new MozillaNotFoundException("No se han podido cargar las DLLs de Mozilla Firefox en una carpeta temporal", e);
					}
				}
				
				return mozillaFolder;
			}
		}
		
		//-- Si el SO es Linux / Unix ...
		if(System.getProperty("os.name").contains("inux") || System.getProperty("os.name").contains("SunOS") || 
				System.getProperty("os.name").contains("olaris")){
			System.out.println("[Mozilla]::Buscando la carpeta con las librerías de Mozilla en Linux/Unix");

			JSObject document = (JSObject) JSCommands.getWindow().getMember("navigator");
			String userAgent = ((String) document.getMember("userAgent")).toLowerCase();

			//if (userAgent.contains("x86_64")) {
				if (new File("/lib64/libsoftokn3.so").exists()) {
					System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Linux/Unix: /lib64");
					return new File("/lib64");
				}
				if (new File("/usr/lib64/libsoftokn3.so").exists()) {
					System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Linux/Unix: /usr/lib64");
					return new File("/usr/lib64");
				}
				if (new File("/usr/lib64/firefox/libsoftokn3.so").exists()) {
					System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Linux/Unix: /usr/lib64/firefox");
					return new File("/usr/lib64/firefox");
				}
				if (new File("/usr/lib64/mozilla/libsoftokn3.so").exists()) {
					System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Linux/Unix: /usr/lib64/mozilla");
					return new File("/usr/lib64/mozilla");
				}
				if (new File("/usr/lib64/nss/libsoftokn3.so").exists()) {
					System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Linux/Unix: /usr/lib64/nss");
					return new File("/usr/lib64/nss");
				}				
			//} else {
				if (new File("/lib/libsoftokn3.so").exists()) {
					System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Linux/Unix: /lib");
					return new File("/lib");
				}
				if (new File("/usr/lib/libsoftokn3.so").exists()) {
					System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Linux/Unix: /usr/lib");
					return new File("/usr/lib");
				}
				if (new File("/usr/lib/firefox/libsoftokn3.so").exists()) {
					System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Linux/Unix: /usr/lib/firefox");
					return new File("/usr/lib/firefox");
				}
				if (new File("/usr/lib/mozilla/libsoftokn3.so").exists()) {
					System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Linux/Unix: /usr/lib/mozilla");
					return new File("/usr/lib/mozilla");
				}
				if (new File("/usr/lib/nss/libsoftokn3.so").exists()) {
					System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Linux/Unix: /usr/lib/nss");
					return new File("/usr/lib/nss");
				}				
			//}
		}
		
		//-- Si el SO es Mac OS X
		if(System.getProperty("os.name").startsWith("Mac OS X")) {
			System.out.println("[Mozilla]::Buscando la carpeta con las librerías de Mozilla en Mac OS X");
			File tmpFile = new File("/Applications/Firefox.app/Contents/MacOS/libsoftokn3.dylib"); 
			if (tmpFile.exists()) {
				System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Mac OS X: /Applications/Firefox.app/Contents/MacOS");
				return new File("/Applications/Firefox.app/Contents/MacOS");
			}
			tmpFile = new File("/lib/libsoftokn3.dylib"); 
			if (tmpFile.exists()) {
				System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Mac OS X: /lib");
				return new File("/lib");
			}
			tmpFile = new File("/usr/lib/libsoftokn3.dylib"); 
			if (tmpFile.exists()) {
				System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Mac OS X: /usr/lib");
				return new File("/usr/lib");
			}
			tmpFile = new File("/usr/lib/nss/libsoftokn3.dylib"); 
			if (tmpFile.exists()) {
				System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Mac OS X: /usr/lib/nss");
				return new File ("/usr/lib/nss");
			}
			// Las versiones Alpha de Firefox se llaman Minefield
			tmpFile = new File("/Applications/Minefield.app/Contents/MacOS/libsoftokn3.dylib"); 
			if (tmpFile.exists()) {
				System.out.println("[Mozilla]::Se ha encontrado la carpeta con las librerías de Mozilla Firefox en Mac OS X: /Applications/Minefield.app/Contents/MacOS");
				return new File ("/Applications/Minefield.app/Contents/MacOS");
			}
		}
		
		System.out.println("[Mozilla]::No se ha podido obtener la carpeta de instalación de Mozilla Firefox");
		throw new MozillaNotFoundException("No se ha podido obtener la carpeta de instalación de Mozilla Firefox");
	}

	/**
	 * Obtiene la ruta donde se encuentran los ficheros con el almacén de claves de Mozilla
	 * 
	 * @return Ruta donde se encuentran los ficheros con el almacén de claves de Mozilla
	 * @throws MozillaNotFoundException No se ha encontrado una instalación de Mozilla
	 */
	private File getSecModFolder() throws MozillaNotFoundException {
		
		if (System.getProperty("os.name").contains("indows")) {
			System.out.println("[Mozilla]::Buscando el almacén de claves de Mozilla en Windows");
			
			//-- Buscar carpeta de mozilla en Datos de Programa del usuario
			File secModFolder = new File (new File (System.getenv("APPDATA")), "Mozilla/Firefox");
			if (!secModFolder.exists() || secModFolder.list() == null || secModFolder.list().length == 0) {
				System.out.println("[Mozilla]::No se ha podido obtener la carpeta con el perfil del usuario de Mozilla Firefox: " + secModFolder.getAbsolutePath());
				throw new MozillaNotFoundException("No se ha podido obtener la carpeta con el perfil del usuario de Mozilla Firefox: " + secModFolder.getAbsolutePath());
			}
			System.out.println("[Mozilla]::La carpeta de usuario de Mozilla Firefox es: " + secModFolder.getAbsolutePath());
			
			//-- Obtener el fichero de inicio que contiene la referencia de los perfiles
			File iniFile = new File (secModFolder, "profiles.ini");
			if (!iniFile.exists() || !iniFile.isFile()) {
				System.out.println("[Mozilla]::No se ha podido obtener el fichero de inicio con los perfiles de Mozilla: " + iniFile.getAbsolutePath());
				throw new MozillaNotFoundException("No se ha podido obtener el fichero de inicio con los perfiles de Mozilla: " + iniFile.getAbsolutePath());
			}
			
			//-- Obtener la carpeta del perfil
			try {
				return getFirefoxProfileDirectory (iniFile);
			} catch (IOException e) {
				System.out.println("[Mozilla]::No se ha podido obtener la carpeta con el perfil del usuario en Windows " +  e.getMessage());
				throw new MozillaNotFoundException("No se ha podido obtener la carpeta con el perfil del usuario en Windows", e);
			}
		}
		
		//-- Si estamos en un Linux...
		System.out.println("[Mozilla]::Buscando el almacén de claves de Mozilla en Linux");
		File regFile = new File(new File (System.getProperty("user.home")), "/.mozilla/firefox/profiles.ini");
		if (regFile.exists()) {
			try {
				return getFirefoxProfileDirectory(regFile);
			} catch (Exception e) {
				System.out.println("[Mozilla]::No se ha podido obtener la carpeta con el perfil del usuario en Linux " + e.getMessage());
				throw new MozillaNotFoundException("No se ha podido obtener la carpeta con el perfil del usuario en Linux", e);
			}
		}
		
		//-- Si es un Mac OS X...
		regFile = new File(new File (System.getProperty("user.home")), "/Library/Application Support/Firefox/profiles.ini");
		if (regFile.exists()) {
			try {
				return getFirefoxProfileDirectory(regFile);
			} catch (Exception e) {
				System.out.println("[Mozilla]::No se ha podido obtener la carpeta con el perfil del usuario en Mac OS X" + e.getMessage());
				throw new MozillaNotFoundException("No se ha podido obtener la carpeta con el perfil del usuario en Mac OS X", e);
			}
		}
		
		//-- Intento con el registro clasico de Mozilla
		regFile = new File(new File (System.getProperty("user.home")), "/.mozilla/appreg");
		if (regFile.exists()) {
			try {
				return getFirefoxProfileDirectory(regFile);
			} catch (Exception e) {
				System.out.println("[Mozilla]::No se ha podido obtener la carpeta con el perfil del usuario con el registro clásico de Mozilla " + e.getMessage());
				throw new MozillaNotFoundException("No se ha podido obtener la carpeta con el perfil del usuario con el registro clásico de Mozilla", e);
			}
		}

		System.out.println("[Mozilla]::No se ha podido obtener la carpeta con el perfil del usuario de Mozilla Firefox");
		throw new MozillaNotFoundException("No se ha podido obtener la carpeta con el perfil del usuario de Mozilla Firefox");
	}

	/**
	* Devuelve el directorio del perfil activo de Firefox. Si no hubiese perfil activo,
	* devolver&iacute;a el directorio del perfil por defecto y si tampoco lo hubiese
	* el del primer perfil encontrado. Si no hubiese perfiles configurados,
	* devolver&iacute;a {@code null}.
	* 
	* @param iniFile Fichero con la informaci&oacute;n de los perfiles de Firefox.
	* @return Directorio con la informaci&oacute;n del perfil.
	* @throws IOException Cuando ocurre un error abriendo o leyendo el fichero.
	*/
	private File getFirefoxProfileDirectory(File iniFile) throws IOException {

		String currentProfilePath = null;
		
		// Leemos el fichero con la informacion de los perfiles y buscamos el activo(el que esta bloqueado)
		FirefoxProfile[] profiles = readProfiles(iniFile);
		for (FirefoxProfile profile : profiles) {
			if (isProfileLocked(profile)) {
				currentProfilePath = profile.absolutePath;
				System.out.println("[Mozilla]::Perfil actual activo: " + currentProfilePath);
				return new File (currentProfilePath);
			}	
		}

		// Si no hay ninguno actualmente activo, tomamos el por defecto
		if (currentProfilePath == null) {
			for (FirefoxProfile profile : profiles) {
				if (profile.isDefault) {
				currentProfilePath = profile.absolutePath;
				System.out.println("[Mozilla]::Perfil actual por defecto: " + currentProfilePath);
				return new File (currentProfilePath);
				}
			}
		}

		// Si no hay ninguno por defecto, se toma el primero
		if (profiles.length > 0) {
			currentProfilePath = profiles[0].absolutePath;
			System.out.println("[Mozilla]::Primer perfil: " + currentProfilePath);
				return new File (currentProfilePath);
		}

		return null;
	}

	/**
	* Parsea la informacion de los perfiles declarada en el fichero "profiles.ini".
	* Para identificar correctamente los perfiles es necesario que haya al menos una
	* l&iacute;nea de separaci&oacute;n entre los bloques de informaci&oacute;n de
	* cada perfil.
	* @param iniFile Fichero con lainformaci&oacute;n de los perfiles.
	* @return Listado de perfiles completos encontrados.
	* @throws IOException Cuando se produce un error durante la lectura de la
	* configuraci&oacute;n.
	*/
	private static FirefoxProfile[] readProfiles(File iniFile) throws IOException {
		
		final String NAME_ATR = "name=";
		final String IS_RELATIVE_ATR = "isrelative=";
		final String PATH_PROFILES_ATR = "path=";
		final String IS_DEFAULT_ATR = "default=";
		
		String line = null;
		Vector<FirefoxProfile> profiles = new Vector<FirefoxProfile>();
		BufferedReader in = new BufferedReader(new FileReader(iniFile));
		try {
			while ((line = in.readLine()) != null) {
				
				// Buscamos un nuevo bloque de perfil
				if (!line.trim().toLowerCase().startsWith("[profile"))
					continue;
				
				FirefoxProfile profile = new FirefoxProfile();
				while((line = in.readLine()) != null && line.trim().length() > 0 && !line.trim().toLowerCase().startsWith("[profile")) {
					if(line.trim().toLowerCase().startsWith(NAME_ATR))
						profile.name = line.trim().substring(NAME_ATR.length());
					else if(line.trim().toLowerCase().startsWith(IS_RELATIVE_ATR))
						profile.isRelative = line.trim().substring(IS_RELATIVE_ATR.length()).equals("1");
					else if(line.trim().toLowerCase().startsWith(PATH_PROFILES_ATR))
						profile.path = line.trim().substring(PATH_PROFILES_ATR.length());
					else if(line.trim().toLowerCase().startsWith(IS_DEFAULT_ATR))
						profile.isDefault = line.trim().substring(IS_DEFAULT_ATR.length()).equals("1");
					else
						break;
				}
				
				// Debemos encontrar al menos el nombre y la ruta del perfil
				if (profile.name != null || profile.path != null) {
					profile.absolutePath =
						profile.isRelative ? 
							new File(iniFile.getParent(), profile.path).toString() :
							profile.path;
							
					profiles.add(profile);
				}
			}
		} catch (Exception e) {
			throw new IOException("Ocurrio un error al leer la configuracion de los perfiles de Firefox: " + e);
		} finally {
			try { in.close(); } catch (Exception e) {}
		}
		
		return profiles.toArray(new FirefoxProfile[profiles.size()]);
		}
	
	/**
	* Comprueba que un perfil de Firefox est&eacute; bloqueado. Un perfil esta
	* bloqueado cuando en su directorio se encuentra el fichero "parent.lock".
	* @param profile Informaci&oacute;n del perfil de Firefox.
	* @return Devuelve {@code true} si el perfil esta bloqueado, {@code false}
	* en caso contrario.
	*/
	private static boolean isProfileLocked(FirefoxProfile profile) {
		return 	new File(profile.absolutePath, "parent.lock").exists() ||	// En Windows
						new File(profile.absolutePath, "lock").exists();					// En Linux
	}

	public void loadMozillaLibraries() throws Throwable {
		File mozillaFolder= this.getMozillaFolder();
		
		if (System.getProperty("os.name").contains("indows") || System.getProperty("os.name").startsWith("Mac OS X")) {
			try {
				System.load(new File (mozillaFolder, System.mapLibraryName("mozcrt19")).getAbsolutePath());
			} catch (UnsatisfiedLinkError e) {}
			try {
				System.load(new File (mozillaFolder, System.mapLibraryName("mozutils")).getAbsolutePath());				
			} catch (UnsatisfiedLinkError e) {}
			try {
				System.load(new File (mozillaFolder, System.mapLibraryName("mozglue")).getAbsolutePath());				
			} catch (UnsatisfiedLinkError e) {}
			try {
				System.load(new File (mozillaFolder, System.mapLibraryName("sqlite3")).getAbsolutePath());				
			} catch (UnsatisfiedLinkError e) {
				System.load(new File (mozillaFolder, System.mapLibraryName("mozsqlite3")).getAbsolutePath());
			}
			System.load(new File (mozillaFolder, System.mapLibraryName("nspr4")).getAbsolutePath());
			System.load(new File (mozillaFolder, System.mapLibraryName("plds4")).getAbsolutePath());
			System.load(new File (mozillaFolder, System.mapLibraryName("plc4")).getAbsolutePath());
			System.load(new File (mozillaFolder, System.mapLibraryName("nssutil3")).getAbsolutePath());
			System.load(new File (mozillaFolder, System.mapLibraryName("nssdbm3")).getAbsolutePath());
			System.load(new File (mozillaFolder, System.mapLibraryName("freebl3")).getAbsolutePath());
		}
	}
	
	/**
	* Obtiene el nombre de la librería del provider NSS dependiendo del SO
	* 
	* @return Nombre de la librería del provider NSS
	*/
	public String getMozillaNSSLibraryName () {
			String nssLib = "libsoftokn3.so";
			if(System.getProperty("os.name").contains("indows")) {
				nssLib = "softokn3.dll";
			}
			else if (System.getProperty("os.name").startsWith("Mac OS X")) {
				nssLib = "libsoftokn3.dylib";
			}

			return nssLib;
	}

	public String getPKCS11CfgInputStream() throws FileNotFoundException {
		File mozillaFolder = null;
		File secModFolder = null;
		try {
			mozillaFolder = this.getMozillaFolder();
			secModFolder = this.getSecModFolder();

			String providerConfig = "name = " + PROVIDER_NAME + "\r" + 
			   "library = " + new File (mozillaFolder, getMozillaNSSLibraryName()).getCanonicalPath() + "\r" + 
			   "attributes= compatibility" + "\r" +
			   "slot=2\r" + 
			   "nssArgs=\"" + 
			   "configdir='" + secModFolder.getCanonicalPath() + "' " +
			   "certPrefix='' " + 
			   "keyPrefix='' " + 
			   "secmod=' secmod.db' " + 
			   "flags=readWrite\"\r";
            
            /*String providerConfig = "name = " + PROVIDER_NAME + "\r" + 
              "nssLibraryDirectory = " + new File (mozillaFolder, getMozillaNSSLibraryName()).getCanonicalPath() + "\r" + 
              "attributes= compatibility" + "\r" +
              "nssSecmodDirectory='" + secModFolder.getCanonicalPath() + "'\r" +
              "nssModule = keystore";*/

            //System.out.println("Firefox cfg orishinal: " + providerConfig);
			providerConfig = providerConfig.replaceAll("\\\\", "/");
            //System.out.println("Firefox cfg orishinal after replace: " + providerConfig);
			return providerConfig;
		
		} catch (MozillaNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FileNotFoundException("Mozilla.getPKCS11CfgInputStream(): No se ha podido recuperar el directorio del usuario relacionado a Firefox");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new FileNotFoundException("Mozilla.getPKCS11CfgInputStream(): No se ha podido recuperar el directorio del usuario relacionado a Mozilla Firefox");
		}
	}
	
	public String getApplicationPath() {
		try {
			File mozillaFolder = this.getMozillaFolder();
			return mozillaFolder.getAbsolutePath();
		} catch (MozillaNotFoundException e) {
			// TODO Auto-generated catch block
			return null;
		}
		
	}

	private static class FirefoxProfile {
		String name = null;
		boolean isRelative = true;
		String path = null;
		String absolutePath = null;
		boolean isDefault = false;
	}
}

