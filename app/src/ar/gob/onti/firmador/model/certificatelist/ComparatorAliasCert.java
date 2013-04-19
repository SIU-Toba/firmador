package ar.gob.onti.firmador.model.certificatelist;

import java.io.Serializable;
import java.util.Comparator;

public class ComparatorAliasCert implements Comparator<String> ,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(String o1, String o2) {
		return o1.compareToIgnoreCase(o2);
	}

}
