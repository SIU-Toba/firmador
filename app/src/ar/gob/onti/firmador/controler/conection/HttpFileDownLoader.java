package ar.gob.onti.firmador.controler.conection;
/*
 * HttpFileDownLoader.java
 * author: mpin
 * owner : ONTI
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;


public class HttpFileDownLoader extends HttpFileConnection{


	private String localFileName="";

	public HttpFileDownLoader() {
		super();
	}
	public String getNombreArchivo(String urlString){
		// Se recupera el nombre del archivo de la URL informada
		int lastSlashIndex = urlString.lastIndexOf('/');
		if ((lastSlashIndex < 0) || (lastSlashIndex >= urlString.length() - 1)) {
			this.setHttpFileError("Método HttpFileDownLoader.connectURL(): Error de parámetros.\nNo se ha podido recupear el nombre del archivo de la URL (" + urlString + ")");
			return "ERROR";
		}
		return urlString.substring(lastSlashIndex + 1);
	}



	public String getLocalFileName() {
		return localFileName;
	}	
	public void setLocalFileName(String localFileName) {
		this.localFileName = localFileName;
	}
	/**
	 * metodo que se encarga de conectarse con la url que el
	 * Applet recibe como parametro y con el nombre de archivo y direcctorio que recibe 
	 * como parametro escribir en disco en la carpeta de archivos temporales del sistema
	 * operativo el archivo pdf a ser firmado por la aplicacion
	 * @param srcDirectory
	 * @param nombreArchivo
	 * @return
	 * @throws IOException 
	 */
	public boolean doDownload(String srcDirectory,String nombreArchivo, String cookie) throws IOException {
		localFileName=nombreArchivo;
		BufferedOutputStream outStream = null;
		InputStream  inStream = null;
		FileOutputStream stream=null;
		String downloadError= "";

		try {
			HttpURLConnection httpconn = getConnectURL();
			if (cookie != null && cookie.length() > 0) {
				httpconn.setRequestProperty("Cookie", cookie);
			}

			inStream = httpconn.getInputStream();
			stream=new FileOutputStream(srcDirectory + File.separator + localFileName);
			outStream = new BufferedOutputStream(stream);
			byte[] buffer = new byte[1024];
			int bytesRead;
			int bytesWritten = 0;
			while ((bytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
				bytesWritten += bytesRead;
			}
			return true;
		} catch (FileNotFoundException e) {
		
			if (inStream == null) {
				downloadError += "Archivo no encontrado en el servidor (" + localFileName + ")";
			} else if (outStream == null) {
				downloadError += "Error al intentar crear el archivo en el directorio local (" + srcDirectory + File.separator + localFileName + ")";
			} else {
				downloadError += "No se ha encontrado el archivo (" +  localFileName + ")";
			}
			setHttpFileError(downloadError);
			return false;
		}catch (IOException e) {

			downloadError += "Error al intentar leer el archivo del servidor"; 
			setHttpFileError(downloadError);
			return false;
		} finally {

		if (inStream != null) {
			inStream.close();
		}
		if (outStream != null) {
			outStream.close();
		}
		if (stream != null) {
			stream.close();
		}				

	}

}

}
