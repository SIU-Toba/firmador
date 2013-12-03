/*
 * StoreDctnry.java
 * author: mpin
 * owner : ONTI
 */

package ar.gob.onti.firmador.controler;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ar.gob.onti.firmador.controler.PdfControler.OriginType;
import ar.gob.onti.firmador.model.certificatelist.ComparatorAliasCert;

import com.itextpdf.text.pdf.PdfPKCS7;
import java.math.BigInteger;

public class StoreDctnry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8223224339880703789L;
	private  Map<String,List<String>> mDictionary=new HashMap<String, List<String>>();
	private KeyStore[] keyStrToken=null;
	private  List <String> issuers= new ArrayList<String>();
	public static final String SEP_CER_FIELDS ="###***";
	


	public Map<String, List<String>> getmDictionary() {
		return mDictionary;
	}
	public void setKeyStore(KeyStore[] aKeyStore) {
		keyStrToken = aKeyStore;
	}

	public void unsetKeyStore() {
		keyStrToken = null;
	}

	public void setIssuers(List <String> anACList){
		issuers = anACList;
	}

	public void unsetIssuers() {
		issuers = null;
	}

	/**
	 * Agrega un nuevo alias de certificado para la AC referenciada Si es el
	 * primer certificado para la AC,entonces se agrega el nombre de la AC como
	 * clave del diccionario y el alias en el array list asociado Si la AC ya
	 * cuenta con alias, entonces solo se agrega el nuevo
	 * @param issuerCN
	 * @param subjCN
	 * @param nroSerie
	 * @param datCaducidad
	 * @param aliasCer
	 */
	public void addAlias(String issuerCN, String subjCN, String nroSerie,
			String datCaducidad, String aliasCer, OriginType originType) {
		String alias="";
		List<String> aliasCerts = (List<String>) mDictionary.get(issuerCN);
		if (aliasCerts == null) {
			aliasCerts = new ArrayList<String>();
			mDictionary.put(issuerCN, aliasCerts);
		}
		alias=subjCN + SEP_CER_FIELDS + nroSerie + SEP_CER_FIELDS + datCaducidad + SEP_CER_FIELDS + aliasCer + SEP_CER_FIELDS + originType.ordinal();
		aliasCerts.add(alias);
	}

	/*
	 * Retorna un iterador que recorre la coleccion de alias para una
	 * determinada AC
	 */
	public Iterator<String> iterator(String issuerCN) {
		List<String> aliasCerts = (List<String>) mDictionary.get(issuerCN);
		if (aliasCerts == null){
			return null;
		}
		Collections.sort(aliasCerts, new ComparatorAliasCert());
		return aliasCerts.iterator();
	}

	public int itrSize(String issuerCN) {
		return (((List<String>) mDictionary.get(issuerCN)).size());
	}

	public Iterator<String> itrKeys() {
		return (mDictionary.keySet().iterator());
	}

	public int itrKeySize() {
		return (mDictionary.keySet().size());
	}


	/**
	 *  Se recuperan todos los alias existentes en el repositorio que tengan asociada una clave privada
	 * Debe ser una entrada que posea clave privada  para poder firmar el documento
	 * Se recupera el certificado y se toman solo  aquellos que cumplen con el standard X509
	 * Se verifica que el issuer sea uno de los admitidos o si no fue 
	 * especificado, si al menos se acepta cualquier emisor (*)
	 * Se verifica que la fecha de caducidad sea anterior a la fecha del dia actual y 
	 * la fecha de activacion sea posterior
	 *  Si todo OK, entonces se agrega al diccionario
	 * @throws KeyStoreException
	 * @throws UnsupportedEncodingException
	 */
	public void loadKeyStore() throws KeyStoreException, UnsupportedEncodingException  {
		X509Certificate cerONTI = null;
		Certificate cerKeyStore = null;
		String aliasCer = "";
		String issuerCN= "";
		String subjCN= "";
		String nroSerie= "";
		String datCaducidad= "";
		int index;
		ArrayList<BigInteger> serials = new ArrayList<BigInteger>();
		for(int n = 0; n < keyStrToken.length; n++) {
			if ( keyStrToken[n] == null) {
				continue;	//Keystore invalido, no problem
			}
			Enumeration<String> aliasEnm = keyStrToken[n].aliases();
			for (; aliasEnm.hasMoreElements();) {
				aliasCer =  aliasEnm.nextElement();
				if (!keyStrToken[n].isKeyEntry(aliasCer)) {
					continue;
				}
				cerKeyStore = keyStrToken[n].getCertificate(aliasCer);
				if (!(cerKeyStore instanceof X509Certificate)) {
					continue;
				}
				cerONTI = (X509Certificate) cerKeyStore;
				if (serials.contains(cerONTI.getSerialNumber())) {
					continue;   //Avoid to add the same serial# twice
				}
                serials.add(cerONTI.getSerialNumber());
				issuerCN= PdfPKCS7.getIssuerFields(cerONTI).getField("CN");
	
				index = issuers.indexOf(issuerCN);
				if (index == -1) {
					index = issuers.indexOf("*");
					if (index == -1) {
						continue;
					}
				} 
	
				subjCN= PdfPKCS7.getSubjectFields(cerONTI).getField("CN");
                                if (subjCN == null) {
                                    subjCN = PdfPKCS7.getSubjectFields(cerONTI).getField("E");
                                }

				nroSerie = cerONTI.getSerialNumber().toString();
				datCaducidad = (new SimpleDateFormat("dd/MM/yyyy")).format(cerONTI.getNotAfter());
				Date upDate = cerONTI.getNotAfter();
				Date lowDate = cerONTI.getNotBefore();
				Date now = new Date();
				if ((now.getTime() >= lowDate.getTime()) && (now.getTime() <= upDate.getTime())) {
					// Si todo OK, entonces se agrega al diccionario
					/*System.out.println("issuerCN " + issuerCN + " subjCN " + subjCN + " nroSerie " + nroSerie + " datCaducidad " + datCaducidad + " aliasCer " + aliasCer
								 + " origin " + OriginType.values()[n].toString());*/
					addAlias(issuerCN, subjCN, nroSerie, datCaducidad, aliasCer, OriginType.values()[n]);
				}  
			}
		}
	}
}
