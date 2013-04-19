package ar.gob.onti.firmador.model.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;

import org.junit.Test;

import ar.gob.onti.firmador.model.Mozilla;

public class MozillaTest {
	
	@Test
	public final void testGetMozillaNSSLibraryName() {
		  Mozilla mozilla= new Mozilla(); 
		  assertTrue(mozilla.getMozillaNSSLibraryName().contains("softokn3.dll"));
	}

	@Test
	public final void testGetPKCS11CfgInputStream() {
		 Mozilla mozilla= new Mozilla();
	     try {
			 assertTrue(mozilla.getPKCS11CfgInputStream().contains("nssDbMode = readOnly\nnssModule = keystore"));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (NullPointerException e) {
			// TODO: handle exception
		}
		
	}

}
