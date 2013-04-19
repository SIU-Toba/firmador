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
package ar.gob.onti.firmador.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;

/**
* Diversas utilidades necesarias en los proyectos que integran Arangi
*
* @author <a href="mailto:jgutierrez@accv.es">José M Gutiérrez</a>
*/
public class Util {


	/**
	* Guarda en disco un array de bytes.
	*
	* @param file Fichero donde se guardará el contenido
	* @param contenido Contenido a guardar
	* @throws Exception No se puede escribir
	*/
	public static void saveFile(File file, byte[] contenido) throws IOException {

		try {
			// Los guardamos a disco.
			RandomAccessFile lObjSalida = new RandomAccessFile(file, "rw");
	lObjSalida.write(contenido,0,contenido.length);
			lObjSalida.close();
		} catch (IOException e){
				System.out.println("Error saving file at " + file + " " + e.getMessage());
			  throw e;
		}
	}

	/**
	* Lee un stream de lectura y escribe en el fichero destino
	*
	* @param file Fichero destino
	* @param iStream Stream de lectura
	* @throws Exception No se puede leer o escribir
	*/
	public static void saveFile(File file, InputStream iStream) throws IOException {

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream (file);
	    	byte[] buffer = new byte [1024];
	        int len;
	        while ((len = iStream.read(buffer)) > -1) {
	        	fos.write(buffer, 0, len);
	        }
		} catch (IOException e){
			System.out.println ("Error saving file at " + file + " " + e.getMessage());
			  throw e;
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	/**
	* Guarda en un stream de escritura lo contenido en un stream de lectura
	*
	* @param out Stream de escritura
	* @param iStream Stream de lectura
	* @throws Exception No se puede leer o escribir
	*/
	public static void save(OutputStream out, InputStream iStream) throws IOException {

		try {
	    	byte[] buffer = new byte [1024];
	        int len;
	        while ((len = iStream.read(buffer)) > -1) {
	        	out.write(buffer, 0, len);
	        }
		} catch (IOException e){
			  System.out.println("Error guardando en stream de escritura" + e.getMessage());
			  throw e;
		}
	}

	/**
	* Guarda en un stream de escritura un array de bytes
	*
	* @param out Stream de escritura
	* @param contenido Contenido a guardar
	* @throws Exception No se puede leer o escribir
	*/
	public static void save(OutputStream out, byte[] contenido) throws IOException {

		try {
	out.write(contenido, 0, contenido.length);
		} catch (IOException e){
			  System.out.println ("Error guardando en stream de escritura" + e.getMessage());
			  throw e;
		}
	}

	/**
	* Lee un fichero en el classpath y escribe en el fichero destino
	*
	* @param file Fichero destino
	* @param classPathFile Path al fichero dentro del classpath. Recordar que estos
	* 	path tienen separadores '/' y no '.'. O sea que si el recurso se encuentra
	* 	en el paquete org.java y se llama recurso.rec el path sería org/java/recurso.rec.
	* @throws FileNotFoundException No es posible leer el recurso dentro del classpath
	* @throws IOException No se puede escribir
	*/
	public static void saveFileFromClasspath(File file, String classPathFile) throws FileNotFoundException, IOException {

		InputStream iStream = new Util().getClass().getClassLoader().getResourceAsStream(classPathFile);
		if (iStream == null) {
			throw new FileNotFoundException("No es posible leer el fichero '" + classPathFile + "'dentro del classpath");
		}
		saveFile(file, iStream);
	}

/**
	* Carga en un array de bytes la información contenida en el fichero.
	*
	* @param file Fichero que contiene la información que se va a cargar
	* @return Array de bytes que contienen la información guardada en el fichero
	* @throws IOException No se puede leer
	*/
	public static byte[] loadFile(File file) throws IOException {

		try {
			// Leemos el fichero de disco.
			RandomAccessFile lObjFile = new RandomAccessFile(file, "r");
			byte lBytDatos[] = new byte[(int)lObjFile.length()];
lObjFile.read(lBytDatos);
lObjFile.close();

return lBytDatos;

		} catch (IOException e) {
			  System.out.println ("Error cargando el fichero de " + file + " " + e.getMessage());
			  throw e;
		}
	}

/**
	* Carga en un array de bytes la información contenida en un stream de lectura
	*
	* @param is Stream de lectura que contiene la información que se va a cargar
	* @return Array de bytes que contienen la información guardada en el fichero
	* @throws IOException No se puede leer
	*/
	public static byte[] readStream(InputStream is) throws IOException {

		try {
	    	//-- Leer el mensaje SOAP
	    	byte[] buffer = new byte [1024];
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        int len;
	        while ((len = is.read(buffer)) > -1) {
	            baos.write(buffer, 0, len);
	        }

return baos.toByteArray();

		} catch (IOException e) {
			System.out.println ("Error cargando el stream de lectura " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Copia de ficheros
	 *
	 * @param srcFile Fichero origen
	 * @param dstFile Fichero destino
	 * @throws FileNotFoundException No existe el fichero origen
	 * @throws IOException Error de entrada / salida
	 */
	public static void copyFile (File srcFile, File dstFile) throws FileNotFoundException, IOException {

		InputStream in = null;
	    OutputStream out = null;
	    try {
	    	in = new FileInputStream(srcFile);
	    	out = new FileOutputStream(dstFile);
		    // Transfer bytes from in to out
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
	    } finally {
	    	if (in != null) { in.close(); }
	    	if (out != null) { out.close(); }
	    }
	}
}