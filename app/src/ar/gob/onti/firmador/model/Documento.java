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

	public Documento(File archivo) {
		this.archivoAFirmar = archivo;
	}

	public File getArchivoAFirmar() {
		return archivoAFirmar;
	}

	public File getArchivoFirmado() {
		return archivoFirmado;
	}

	public void setArchivoFirmado(File archivoFirmado) {
		this.archivoFirmado = archivoFirmado;
	}
	
}
