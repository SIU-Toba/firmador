package ar.gob.onti.firmador.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.input.CountingInputStream;
import org.bouncycastle.asn1.DERString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

import ar.gob.onti.firmador.controler.conection.HttpFileConnection;

/**
 * Helper bean for holding CRL related data.
 * 
 * @author Josef Cacek
 * 
 */
public class CRLInfo {

	private CRL[] crls;
	private long byteCount = 0L;
	private Certificate[] certChain;
	private Proxy proxy;

	private Set<X509CRL> crlSet ;
	private Set<String> urls;
    private String errorCRLInfo="";
    private PropsConfig myProps = null;
	public Proxy getProxy() {
		return proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	public String getErrorCRLInfo() {
		return errorCRLInfo;
	}

	public void setErrorCRLInfo(String errorCRLInfo) {
		this.errorCRLInfo = errorCRLInfo;
	}

	public Set<X509CRL> getCrlSet() throws IOException{
		initCrls();
		return crlSet;
	}

	public void setCrlSet(Set<X509CRL> crlSet) {
		this.crlSet = crlSet;
	}

	public CRLInfo(Certificate[] certChain) {
		super();
		this.myProps=PropsConfig.getInstance();
		Security.addProvider(new BouncyCastleProvider());
		this.certChain = certChain.clone();
		this.proxy=null;
		crlSet= new HashSet<X509CRL>();
		urls= new HashSet<String>();
	}

	/**
	 * Returns CRLs for the certificate chain.
	 * 
	 * @return
	 * @throws IOException 
	 */
	public CRL[] getCrls() throws IOException {
		return crls.clone();
	}

	/**
	 * Returns byte count, which should
	 * 
	 * @return
	 * @throws IOException 
	 */
	public long getByteCount() throws IOException {
		return byteCount;
	}
	
	public Set<String> getCrlUrls(){
		initUrls();
		return urls;
	}
	public void initUrls() {
		if(urls.isEmpty()){
			for (Certificate cert : certChain) {
				if (cert instanceof X509Certificate) {
					urls.addAll(getCrlUrls((X509Certificate) cert));
				}
			}
		}
	}
	/**
	 * Initialize CRLs (load URLs from certificates and download the CRLs).
	 * @throws IOException 
	 */
	public void initCrls() throws IOException  {
		InputStream inputStream=null;
		CountingInputStream inStream =null;
		initUrls();
		if(this.crlSet.isEmpty()){
			for (final String urlStr : urls) {
				try {
					final URL tmpUrl = new URL(urlStr);
					inputStream=tmpUrl.openConnection(HttpFileConnection.createProxy(this.proxy)).getInputStream();
					inStream = new CountingInputStream(inputStream);
					final CertificateFactory cf = CertificateFactory.getInstance("X.509","BC");
					final X509CRL crl = (X509CRL) cf.generateCRL(inStream);
					final long tmpBytesRead = inStream.getByteCount();
					if (!crlSet.contains(crl)) {
						byteCount += tmpBytesRead;
						crlSet.add(crl);
					} 
					
				}catch (NoSuchProviderException e) {
					cargarError(e);
				} catch (CertificateException e) {
					cargarError(e);
				} catch (CRLException e) {
					cargarError(e);
				}
				finally{
					if(inStream!=null){
					inStream.close();
					}
				}
			}
		}
		crls = crlSet.toArray(new CRL[crlSet.size()]);
	}
   private void cargarError(Exception e){
	   errorCRLInfo+=myProps.getString("errorInitCrls");
		if (e.getMessage() != null) {
			errorCRLInfo += "\nMensaje JVM: " + e.getMessage();
		}
    }
	/**
	 * Returns (initialized, but maybe empty) set of URLs of CRLs for given
	 * certificate.
	 * 
	 * @param aCert
	 *            X509 certificate.
	 * @return
	 */
	public Set<String> getCrlUrls(final X509Certificate aCert) {
		final Set<String> tmpResult = new HashSet<String>();
		final byte[] crlDPExtension = aCert.getExtensionValue(X509Extensions.CRLDistributionPoints.getId());
		if (crlDPExtension != null) {
			CRLDistPoint crlDistPoints = null;
			try {
				crlDistPoints = CRLDistPoint.getInstance(X509ExtensionUtil.fromExtensionValue(crlDPExtension));
			} catch (IOException e) {
				  errorCRLInfo+=myProps.getString("errorGetCrlUrls");
				if (e.getMessage() != null) {
					errorCRLInfo += "\nMensaje JVM: " + e.getMessage();
				}
				
			}
			if (crlDistPoints != null) {
				getCrlDistPoints(tmpResult, crlDistPoints);
			}
		} 
		return tmpResult;
	}

	private void getCrlDistPoints(final Set<String> tmpResult,
			CRLDistPoint crlDistPoints) {
		final DistributionPoint[] distPoints = crlDistPoints.getDistributionPoints();
		distPoint: for (DistributionPoint dp : distPoints) {
			final DistributionPointName dpName = dp.getDistributionPoint();
			final GeneralNames generalNames = (GeneralNames) dpName.getName();
			if (generalNames != null) {
				final GeneralName[] generalNameArr = generalNames.getNames();
				if (generalNameArr != null) {
					for (final GeneralName generalName : generalNameArr) {
						if (generalName.getTagNo() == GeneralName.uniformResourceIdentifier) {
							final DERString derString = (DERString) generalName.getName();
							final String uri = derString.getString();
							if (uri != null && uri.startsWith("http")) {
								// ||uri.startsWith("ftp")
								tmpResult.add(uri);
								continue distPoint;
							}
						}
					}
				}

			}
		}
	}
	
}
