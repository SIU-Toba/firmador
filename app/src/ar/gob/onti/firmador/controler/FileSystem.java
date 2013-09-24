package ar.gob.onti.firmador.controler;

import java.io.File;

import ar.gob.onti.firmador.model.PropsConfig;



public final class FileSystem {
	private static FileSystem instance=null;
	public static synchronized  FileSystem getInstance() {
		if (FileSystem.instance == null){
			FileSystem.instance = new FileSystem();
		}
		return FileSystem.instance;
	}
	private FileSystem() {
		super();
	}
	/**
	 * devuelve true o false si existe o no en la carpeta temporal
	 * el archivo descargado por el Applet
	 * @return
	 */
	public boolean isExisteArchivo(File fileDel) {
		if(fileDel!=null){
		return fileDel.exists();
		}
		return false;
	}
	/**
	 * Metodo que recibe como parametro la ruta de un archivo y lo 
	 * elimina del disco  devolviendo true o false si lo pudo borrar
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean borrarArchivo(String path) {
		boolean retValue = false;
		File fileDel=new File(path);		
		// Se asegura que el archivo exista, sino
		// nada que hacer
		if (!fileDel.exists()) {
			return true;
		} 
		//Se asegura que no este protegido contra escritura
		if (fileDel.canWrite()) {
			retValue = fileDel.delete();
		} 
		return retValue;
	}
	/**
	 * Metodo encargado de esribir el el log la exception que  recibe como parametro
	 * @param e
	 */
	public String getTraceExcepcion(Exception e){
		StringBuffer buffer= new StringBuffer();
		StackTraceElement elements[] = e.getStackTrace();
		for (int i = 0, n = elements.length; i < n; i++) {       
			buffer.append(elements[i].getFileName());
			buffer.append(":");
			buffer.append(elements[i].getLineNumber());
			buffer.append(">> ");
			buffer.append(elements[i].getMethodName());
			buffer.append("()\r\n");
		}
		return buffer.toString();
	}
}
