package ar.gob.onti.firmador.controler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.bouncycastle.ocsp.OCSPReq;

import sun.security.pkcs11.SunPKCS11;
import sun.security.provider.certpath.OCSP;
import sun.security.provider.certpath.OCSP.RevocationStatus;
import sun.security.provider.certpath.OCSP.RevocationStatus.CertStatus;
import ar.gob.onti.firmador.model.CRLInfo;
import ar.gob.onti.firmador.model.PropsConfig;
import ar.gob.onti.firmador.util.HexUtils;

import com.itextpdf.text.pdf.PdfPKCS7;

public class KeyStoreData {
	private KeyStore keyStore;
	private PrivateKey keySign;
	private CRLInfo crlInfo=null;
	private Certificate[] chain;		
	private SunPKCS11 pkcs11Provider;
	private String signError="";

	public SunPKCS11 getPkcs11Provider() {
		return this.pkcs11Provider;
	}

	public void setPkcs11Provider(SunPKCS11 pkcs11Provider) {
		this.pkcs11Provider = pkcs11Provider;
	}

	public CRLInfo getCrlInfo() {
		return this.crlInfo;
	}

	public void setCrlInfo(CRLInfo crlInfo) {
		this.crlInfo = crlInfo;
	}

	public PrivateKey getKeySign() {
		return this.keySign;
	}

	public void setKeySign(PrivateKey keySign) {
		this.keySign = keySign;
	}

	public Certificate[] getChain() {
		return this.chain;
	}

	public void setChain(Certificate[] chain) {
		this.chain = chain.clone();
	}

	/**
	 * devuelve el KeyStore del navegador donde se ejecuta el Applet
	 * @return
	 */
	public KeyStore getKeyStore() {
		return keyStore;
	}

	/**
	 * 
	 * @param keyStore
	 */
	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}

	/**
	 * metodo que devulve true o false si existe la cadena  de certificados
	 * @return
	 */
	public boolean existeCertificate() {
		return (this.chain != null);
	}

	/**
	 * metodo que devulve true o false si existe la cadena  de certificados
	 * @return
	 */
	public void limpiar() {
		this.chain = null;
		//this.keyStore = null;
	}

	/**
	 * devulve true o false si esta o no cargado el keystore
	 * @return
	 */
	public boolean isKeyStoreOpen()
	{
		return keyStore != null;
	}

	/**
	 * metodo que devuelve el numero de serial del ceritficado selecionado 
	 * por el usario firmante
	 * @return
	 */
	public String getCertSerial() {

		String strSerialNum = "000000";
		if (this.chain != null) {
			strSerialNum = ((X509Certificate) this.chain[0]).getSerialNumber().toString();
		}	
		return strSerialNum;

	}
	public BigInteger getSerialNumber() {		
		BigInteger strSerialNum = null;
		if (this.chain != null) {
			strSerialNum = ((X509Certificate) this.chain[0]).getSerialNumber();
		}	
		return strSerialNum;

	}
	public String getIssuerCNSeleccionado() {
		return PdfPKCS7.getIssuerFields((X509Certificate) this.chain[0]).getField("CN");
	}
	public  Map<String, String> getMapaSubjectDN(){
		String nombre="no se encontro el nombre";
		Map<String, String> mapa= new HashMap<String, String>();
		String datosUsuarioFirmante=((X509Certificate) this.chain[0]).getSubjectDN().toString();
		StringTokenizer  elementos = new StringTokenizer(datosUsuarioFirmante,",");
		while(elementos.hasMoreTokens()){
			nombre = elementos.nextToken();		
			String[] lista=nombre.split("=");
			mapa.put(lista[0].trim(), PdfPKCS7.getSubjectFields((X509Certificate) this.chain[0]).getField(lista[0].trim()));
			if(nombre.contains("EMAIL")){
				mapa.put(lista[0].trim(), PdfPKCS7.getSubjectFields((X509Certificate) this.chain[0]).getField("E"));
			}
		}

		return mapa;
	}
	/**
	 * metodo que devuelve el hash md5 de del ceritficado selecionado 
	 * por el usario firmante
	 * @return
	 */
	public String getCertHash(String mdAlgorithm) {

		String strHashNum = "00000000000000000000";
		if (this.chain != null) {
			try {
				MessageDigest md = MessageDigest.getInstance(mdAlgorithm);
				md.update(((X509Certificate)this.chain[0]).getEncoded());
				byte[] fingerPrint = md.digest();
				strHashNum = HexUtils.byteArrayToHexString(fingerPrint);
			} catch (NoSuchAlgorithmException e) {
				cargarMensajeDeError("", "getCertHash", e);
			} catch (CertificateEncodingException e) {
				cargarMensajeDeError("", "getCertHash", e);
			} 
		}	
		return strHashNum;
	}

	/**
	 * Le agrega la mensaje e error que le aparecera al usuario toda la informacion
	 * necesario para saber el motivo del error
	 * @param mensajeDeError
	 * @param errOpera
	 * @param e
	 */
	private void cargarMensajeDeError(String mensajeDeError,String errOpera,Exception e){
		if(!mensajeDeError.isEmpty()){
			signError += mensajeDeError;
		}
		if (errOpera.length() > 0) {
			signError += "\r\nOperación " + errOpera;
		}
		if (e.getMessage() != null) {
			signError += "\r\nMensaje JVM: " + e.getMessage();
		}
		if (e.getCause() != null) {
			signError += "\r\nError Cause:" + e.getCause().getClass().getName();
		}
	}

	public boolean validarCertificadoOCSP() {
		try {
			X509Certificate cert = (X509Certificate)this.chain[0];
			X509Certificate issuerCert = (X509Certificate)this.chain[1];
			
			RevocationStatus revocationStatus = OCSP.check(cert, issuerCert);
		
			CertStatus status = revocationStatus.getCertStatus();

			return status.equals(CertStatus.GOOD);
		} catch (CertPathValidatorException e) {
			System.out.println("OCSP CERT EXCEPTION:" + e.getMessage());
			// TODO Auto-generated catch block
			return false;
		} catch (IOException e) {
			System.out.println("OCSP IO EXCEPTION:" + e.getMessage());
			// TODO Auto-generated catch block
			return false;
		}		
	}
	
}
