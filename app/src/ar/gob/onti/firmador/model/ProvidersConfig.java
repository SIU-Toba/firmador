package ar.gob.onti.firmador.model;
/*
 * ProvidersConfig.java
 * author: frc
 * owner : ONTI
 */
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Clase encargada de modelizar el archivo de configuracion de la applicacion
 * @author ocaceres
 *
 */
public final class ProvidersConfig {
	public ArrayList<Provider> getProviders(String os) {
		ArrayList<Provider> result = new ArrayList<Provider>();
	      try {
		      ResourceBundle bundle = ResourceBundle.getBundle("properties.providers-" + os);
		      
		      Enumeration<String> keys = bundle.getKeys();
		      ArrayList<String> temp = new ArrayList<String>();

		      // get the keys and add them in a temporary ArrayList
		      for (Enumeration<String> e = keys; keys.hasMoreElements();) {
		         String key = e.nextElement();
		         temp.add(key);
		      }
		      // store the bundle Strings in the StringArray
		      for (int i = 0; i < temp.size(); i++) {
		    	 Provider provider = new Provider();
		    	 provider.setName(temp.get(i));
		    	 provider.setLibrary(bundle.getString(temp.get(i)));
		    	 result.add(provider);
		      }
//                                          //Prueba de agregado del token diabolico
//                                          Provider provider = new Provider();
//                                          provider.setName("Token");
//                                          provider.setLibrary("/lib64/libASEP11.so");
//                                          result.add(provider);
	      } catch (Exception e) {

	      }
	      
	      return result;
	}	
}
