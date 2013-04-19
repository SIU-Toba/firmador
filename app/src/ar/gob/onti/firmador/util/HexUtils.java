package ar.gob.onti.firmador.util;

public class HexUtils {
	
	private static String hexValues[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9","a", "b", "c", "d", "e", "f" };

	/**
	 * Funcion utilizada para pasar de un arrey de bytes a
	 * una cadena con representacion hexadecimal
	 * @param in
	 * @return
	 */

	public static String byteArrayToHexString(byte in[]) {

		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0){
			return null;
		}

		StringBuffer out = new StringBuffer(in.length * 2);
		while (i < in.length) {
			// Se elimina high nibble
			ch = (byte) (in[i] & 0xF0);
			// Se reacomodan los bits
			ch = (byte) (ch >> 4);
			// Si high order bit esta en on, entonces off
			ch = (byte) (ch & 0x0F);
			// Convertir a String Char
			out.append(hexValues[(int) ch]);
			// Se elimina low nibble
			ch = (byte) (in[i] & 0x0F);
			// Convertir a String Char
			out.append(hexValues[(int) ch]);
			i++;
		}
		return out.toString();
	}
}
