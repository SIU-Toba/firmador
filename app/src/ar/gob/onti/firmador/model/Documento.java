/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ar.gob.onti.firmador.model;

import java.io.File;

/**
 *
 * @author smarconi
 */
public class Documento {
	private File archivoAFirmar;
	private File archivoFirmado;
	private String url;
	private String id;

	public Documento(String id, String url) {
		this.url = url;
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public String getUrl() {
		return url;
	}
	
	public File getArchivoAFirmar() {
		return archivoAFirmar;
	}

	public File getArchivoFirmado() {
		return archivoFirmado;
	}
	
	public void setArchivoAFirmar(File archivoAFirmar) {
		this.archivoAFirmar = archivoAFirmar;
	}

	public void setArchivoFirmado(File archivoFirmado) {
		this.archivoFirmado = archivoFirmado;
	}
	
}
