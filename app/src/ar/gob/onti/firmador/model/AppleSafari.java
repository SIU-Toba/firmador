package ar.gob.onti.firmador.model;

import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

public class AppleSafari {

	private static final String KEYSTORE_NAME = "KeychainStore";
	private static final String APPLE_KEYSTORE = "Apple";
	private KeyStore ks;
	
	public KeyStore initialize() {
		ks = null;
/*		
		try {
			Security.insertProviderAt((Provider)Class.forName("com.apple.crypto.provider.Apple").newInstance(), 0);
 		} catch(Throwable e) {
 			System.out.println("[AppleSafari]::No se ha podido abrir el almacén de claves del usuario de Safari: " + e.getMessage());
 		}
 */
		
 		//-- Obtener el keystore
 		try {
 			ks = KeyStore.getInstance(KEYSTORE_NAME, APPLE_KEYSTORE);
 		} catch(Throwable e) {
 			System.out.println("[AppleSafari]::No se ha podido obtener un keystore de tipo " + KEYSTORE_NAME + ": " + e.getMessage());
 		}
 
 		//-- Cargar el keystore de Safari
 		try {
 			ks.load(null, null);
 		} catch(Throwable e) {
 			System.out.println("[AppleSafari]::No se ha podido cargar el keystore: " + e.getMessage());
 		}		
 		 		
 		return ks;
	}
	
}
