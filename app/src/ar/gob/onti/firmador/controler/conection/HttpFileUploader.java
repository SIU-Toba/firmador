package ar.gob.onti.firmador.controler.conection;
/*
 * HttpFileUpLoader.java
 * author: mpin
 * owner : ONTI
 */
import ar.gob.onti.firmador.model.Documento;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;


import ar.gob.onti.firmador.model.PropsConfig;

/**
 * 
 * @author ocaceres
 *
 */
public class HttpFileUploader extends HttpFileConnection {


	private FileInputStream fileInputStream = null;
	private String lineEnd = "\r\n";
	private  String twoHyphens = "--";
	

	public HttpFileUploader() {
		super();
	}
	/**
	 * Le agrega la mensaje e error que le aparecera al usuario toda la informacion
	 * necesario para saber el motivo del error
	 * @param mensajeDeError
	 * @param errOpera
	 * @param e
	 */
	private void cargarMensajeDeError(String errorOperacion,Exception e){
		String signError=errorOperacion;
		if (e.getMessage() != null) {
			signError += "Mensaje JVM: " + e.getMessage();
		}
		setHttpFileError(signError);
	}
	/**
	 * Se permiten inputs, outputs y no se usa cache
	 * Se configura la conexion
	 * @param conn
	 * @return
	 */
	private DataOutputStream cargarConfiguracionDataOutputStream(HttpURLConnection conn,String  boundary){
		try {
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			return new DataOutputStream(conn.getOutputStream());
		} catch (ProtocolException e) {
			cargarMensajeDeError("ProtocolException", e);
		} catch (IOException e) {
			cargarMensajeDeError("conn.getOutputStream()", e);
		}
		return null;
	}
	private void agregarArchivoAEnviarFile(DataOutputStream dos,String nombreCampo,String fileName,String  boundary){
		try {
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			String contDispos = "Content-Disposition: form-data;name=\"" + nombreCampo + "\";filename=\"" + fileName + "\"" + lineEnd;
			dos.writeBytes(contDispos);
			dos.writeBytes(lineEnd);
			// Lectura archivo y escritura tipo form data 
			int bytesAvailable = fileInputStream.available();
			int maxBufferSize = 1024;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];
			int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize); 
			}
		} catch (IOException e) {
			cargarMensajeDeError("agregarArchivoAEnviar",e);
		}
	}
	private void agregarCampoAEnviar(DataOutputStream dos,String nombreCampo,String valorAEnviar,String boundary){
		
		String contDispos = "Content-Disposition: form-data;name=\"" + nombreCampo + "\"";
		try {			
			dos.writeBytes(contDispos);
			dos.writeBytes(lineEnd);
			dos.writeBytes(lineEnd);
			if(valorAEnviar!=null){
			dos.writeBytes(valorAEnviar);
			}
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary  + lineEnd);
		} catch (IOException e) {
			cargarMensajeDeError(nombreCampo,e);
		}
	}
	/**
	 * 	Recuperar la respuesta desde el servidor
	 * @param conn
	 * @return
	 */
	private String recibirRespuesta(HttpURLConnection conn){
		InputStream is;
		String resp="";
		try {
			is = conn.getInputStream();
			ObjectInputStream inputFromServlet = new ObjectInputStream(is);
			try {
				resp=(String) inputFromServlet.readObject();
			} catch (ClassNotFoundException e) {
				cargarMensajeDeError("recibirRespuesta servidor", e);
			}
			inputFromServlet.close();
			
		} catch (IOException e) {
			cargarMensajeDeError("recibirRespuesta servidor", e);
		}
		return resp;
		
	}
	/**
	 * Una vez firmado digitalmente el documento pdf,  en el último paso envía mediante 
	 * un post el archivo pdf firmado con los siguientes parámetros que pueden ser 
	 * usados por su aplicación para verificar la validez de la firma del documento.
	 *
	 *	md5_file.-  Hash  md5 del archivo descargado a la carpeta temporal para ser firmado.
	 *	md5_fileSigned.- Hash  md5 del archivo pdf firmado digitalmente.
	 *	serialCert.- Serial del certificado con el que se firmo el archivo
	 *	hashCert.- Hash del certificado
	 *	codigo.- este parámetro es un código interno que puede ser usado por su aplicación 
	 * para identificar el documento o el tipo de documento a ser firmado, este código 
	 * es devuelto sin ser modificado, en el post de subida del archivo.
	 * El post debe devolver OK en caso que de que las validaciones de seguridad del 
	 * archivo con los parámetros recibidos  sean exitosas, en caso contrario deberá 
	 * devolver el error ocurrido, que será mostrado al usuario.
	 * @param fileName nombre del archiv a enviar
	 * @param boundary para separea el vio del post
	 * @param codigo codigo que se recibio como parametro del applet
	 * @param md5Values vector de hash md5 del archivo firmado y del descargado 
	 * @param certValues vector de hash md5 del certificado con el que se firmo el arcjhivo pdf
	 * @return
	 * @throws IOException 
	 */
                    public boolean doUpload(Documento documento, PropsConfig myProps,String codigo, String cookie) throws IOException {
                            String errorBuffer="";
                            String fileName = documento.getArchivoFirmado().getPath();
                            try {			
                                    PostMethod post = new PostMethod(myProps.getUploadURL());
                                    Part[] parts = {   new StringPart("codigo", codigo),
                                                                new StringPart("id", documento.getId()),
								new StringPart(myProps.getCrossSiteTokenName(),myProps.getCrossSiteTokenValue()),
                                                                new FilePart("md5_fileSigned", new File(fileName))
								};
                                    
                                    post.setRequestEntity( new MultipartRequestEntity(parts, post.getParams()) );
                                    HttpClient client = new HttpClient();
                                    client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
                                    if (cookie != null && cookie.length() > 0) {
                                        post.setRequestHeader("Cookie", cookie);
                                    }

                                    int status = client.executeMethod(post);
                                    if (status != 200) {
                                        errorBuffer+="Error de recepci�n en el server status" + status;
                                        this.setHttpFileError(errorBuffer);
                                        return false;
                                    }
                                    byte[] response = post.getResponseBody();

                                    String responseString = new String(response, "UTF-8");
                                    post.releaseConnection();


                                    if (post.getStatusCode() == 200) {
                                        return true;
                                    } else {		
                                        errorBuffer+="Error de recepci�n en el server :" +responseString;
                                        this.setHttpFileError(errorBuffer);
                                        return false;
                                    }
                            }
                            catch (SocketException e) {
                                    e.printStackTrace();
                                    return doUploadApache(documento, myProps, codigo);
                            }
                            catch (Exception e) {
                                e.printStackTrace();            
                                cargarMensajeDeError("", e);
                                return false;
                            }
                    }
	
	/**
	 * Una vez firmado digitalmente el documento pdf,  en el último paso envía mediante 
	 * un post el archivo pdf firmado con los siguientes parámetros que pueden ser 
	 * usados por su aplicación para verificar la validez de la firma del documento.
	 *
	 *	md5_file.-  Hash  md5 del archivo descargado a la carpeta temporal para ser firmado.
	 *	md5_fileSigned.- Hash  md5 del archivo pdf firmado digitalmente.
	 *	serialCert.- Serial del certificado con el que se firmo el archivo
	 *	hashCert.- Hash del certificado
	 *	codigo.- este parámetro es un código interno que puede ser usado por su aplicación 
	 * para identificar el documento o el tipo de documento a ser firmado, este código 
	 * es devuelto sin ser modificado, en el post de subida del archivo.
	 * El post debe devolver OK en caso que de que las validaciones de seguridad del 
	 * archivo con los parámetros recibidos  sean exitosas, en caso contrario deberá 
	 * devolver el error ocurrido, que será mostrado al usuario.
	 * Esta version está preparada para funcionar mediante proxies HTTP.
	 * @param fileName nombre del archiv a enviar
	 * @param boundary para separea el vio del post
	 * @param codigo codigo que se recibio como parametro del applet
	 * @param md5Values vector de hash md5 del archivo firmado y del descargado 
	 * @param certValues vector de hash md5 del certificado con el que se firmo el arcjhivo pdf
	 * @return
	 * @throws IOException 
	 */
	public boolean doUploadApache(Documento documento, PropsConfig myProps,String codigo) throws IOException {
		String errorBuffer="";
		try {			
			String fileName = documento.getArchivoFirmado().getPath();
			
			//SocketAddress addr = new InetSocketAddress("210.101.131.231", 8080);
			//Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
			
			URL url = new URL(myProps.getUploadURL());
			HttpURLConnection  conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
		      
			Part[] parts = {
					new StringPart("codigo", codigo),
					new StringPart("id", documento.getId()),
					new StringPart(myProps.getCrossSiteTokenName(),myProps.getCrossSiteTokenValue()),
					new FilePart("md5_fileSigned", new File(fileName))};
			
			String boundary = Part.getBoundary();

			conn.setRequestMethod("POST"); 
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary); 
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Accept-Encoding", "deflate");
			conn.setRequestProperty("Content-Length", String.valueOf(Part.getLengthOfParts(parts)));
			conn.setUseCaches (false);

			OutputStream ws = conn.getOutputStream();
			Part.sendParts(ws, parts);
			ws.flush();
			ws.close();
		
			String line;
			
			int status = conn.getResponseCode();

			InputStream is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			String responseString = "";
			while ((line = reader.readLine()) != null) {
				responseString += line + "\n";
			}
			reader.close(); 

	        //int status = httpclient.execute(post);
	        if (status != 200) {
				errorBuffer+="Error de recepción en el server status" + responseString;
				this.setHttpFileError(errorBuffer);
				return false;
	        } else {
	        	return true;
	        }
		}
		catch (Exception e) {
            cargarMensajeDeError("", e);
            e.printStackTrace();
            return false;
        }
	}

}
