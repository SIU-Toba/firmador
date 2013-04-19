package ar.gob.onti.firmador.model;
/*
 * RegQuery.java
 * author: mpin
 * owner : ONTI
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
/**
 * 
 * @author ocaceres
 *
 */
public class RegQuery
{
	public static final  String REGQUERY_UTIL = "reg query ";
	public static final  String REGSTR_TOKEN = "REG_SZ";
	public static final  String APP_DATA_CMD = REGQUERY_UTIL + "\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v APPDATA";
	private   String appPathCmd = REGQUERY_UTIL + "\"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\";

	public String getAppPathCmd() {
		return appPathCmd;
	}

	public void setAppPathCmd(String appPathCmd) {
		this.appPathCmd = appPathCmd;
	}

	private String execAndGetValue(String cmd)
	{
		int c;
		String res = null;
		StringWriter sw = new StringWriter();

		try	{
			InputStream in = Runtime.getRuntime().exec(cmd).getInputStream();
			while ((c = in.read()) != -1) {
				sw.write(c);
			}
			in.close();
			res = sw.toString();
			int p = res.indexOf(REGSTR_TOKEN);
			if (p == -1) {
				return null;
			}
			return res.substring(p + REGSTR_TOKEN.length()).trim();
		}
		catch (IOException e) {
			return null;
		}
	}

	public String getCurrentUserPath() {
		return execAndGetValue(APP_DATA_CMD);
	}

	public String getApplicationPath(String execName) {
		appPathCmd += execName + "\"" + " /v Path";
		return execAndGetValue(appPathCmd);
	}
}