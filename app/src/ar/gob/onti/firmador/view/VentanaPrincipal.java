package ar.gob.onti.firmador.view;
/*
 * MainWindow.java
 * author: mpin
 * owner : ONTI
 */
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.applet.Applet;
import netscape.javascript.*; // add plugin.jar to classpath during compilation

import ar.gob.onti.firmador.controler.FileSystem;
import ar.gob.onti.firmador.controler.FirmaControler;
import ar.gob.onti.firmador.controler.PdfControler;
import ar.gob.onti.firmador.model.Documento;
import ar.gob.onti.firmador.model.PropsConfig;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JProgressBar;



/**
 * Clase contenedora de los componetes de la Applicacion
 * @author ocaceres
 *
 */
public class VentanaPrincipal implements Runnable  {

	
	public enum Estados {
		DESCARGA_ERROR,
		DESCARGA_OK,
		FIRMA_DOC_ERROR,
		FIRMA_DOC_OK,
		SUBIDA_DOC_OK,
		SUBIDA_DOC_ERROR,
		
		DOCUMENTO_AGREGADO,
		DOCUMENTO_QUITADO
	}
	
	private static final long serialVersionUID = 1L;
	private JLabel jLblPDFile;
	private JLabel mensajeFirmaOk;
	private JPanel panelPrincipal =null;

	private JLabel labelTitulo;
	private JLabel labelIconoOk;
	private JButton botonVerPdf;
	private JButton botonFirmar;
	private JProgressBar progressBar;
	private JTextField certSelecionado;
	private Container container;
	private PropsConfig myProps=null;
	private Logger 		appLogFile;
	private FileHandler hndLog=null;
	private PdfControler   pdfControler;
	private FirmaControler firmaControler;
	private ImageIcon tituloIcon =  null;	
	private ImageIcon okIcon =  null;
	private String codigo;
	private String letra="Arial";
	private String cookie;
	
	private Thread downloadThread;

	/**
	 * Controlador que se encarga de manejar las acciones que realiza el usuario
	 * @return
	 */
	public FirmaControler getfirmaControler() {
		return firmaControler;
	}

	/**
	 * JTextField del certificado selecionado
	 * @return
	 */
	public JTextField getCertSelecionado() {
		if(certSelecionado==null){
			certSelecionado = new javax.swing.JTextField();
			certSelecionado.setText("");
			certSelecionado.setEnabled(false);
		}
		return certSelecionado;
	}
	/**
	 * 
	 * @param certSelecionado
	 */
	public void setCertSelecionado(JTextField certSelecionado) {
		this.certSelecionado = certSelecionado;
	}
	/**
	 * Codigo que recibe el Applet de la aplicaion que lo esta jecutando
	 * @return
	 */
	public String getCodigo() {
		return codigo;
	}
	/**
	 * 
	 * @param codigo
	 */
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getCookie() {
		return cookie;
	}


	/**
	 * Constructor de la ventanaprincipal
	 * debe recibir como paramtro el Applet que se esta ejecutando 
	 * y el navegador que lo contiene
	 * @param container Applet que s esta ejecutando
	 * @param browser navegador que lo contiene al Applet
	 */
	public VentanaPrincipal(Container container,String browser) {
		super();
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			letra = "Arial";
		} else {
			letra = "Droid Sans";
		}
		myProps =PropsConfig.getInstance();
		myProps.setBrowser(browser);
		appLogFile = null;
		hndLog = null;
		this.container=container;
	}
	
    public void inicializar(){
    	// Se instancia el objeto de firma y se trabaja sobre
		// el documento origen, generando en el documento de
		// salida la firma digital
		try {
			pdfControler = new PdfControler();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			PropsConfig.getInstance().getAppLogFile().info(e.getMessage());
		}
		pdfControler.setProps(PropsConfig.getInstance());
		firmaControler= new FirmaControler(this);
		ClassLoader cl = this.getClass().getClassLoader();
		tituloIcon =  new ImageIcon(cl.getResource("images/Logosiu1.png"));
		okIcon =  new ImageIcon(cl.getResource("images/ok.png"));
		
		crearGroupLayout();

		container.add(panelPrincipal);
    }

	/**
	 * Etiqueta del titulo
	 * @return
	 */
	public JLabel getjLblPDFile() {
		// Seleccion de archivo
		if(jLblPDFile==null){
			jLblPDFile = new JLabel(myProps.getString("titulo"));
			jLblPDFile.setFont(new java.awt.Font(letra, Font.PLAIN, 11));
		}
		return jLblPDFile;
	}
	/**
	 * Etiqueta del titulo
	 * @return
	 */
	public JLabel getMensajeFirmaOk() {
		// Seleccion de archivo
		if(mensajeFirmaOk==null){
			mensajeFirmaOk = new JLabel(myProps.getString("mensajeFirmaOk"));
			mensajeFirmaOk.setHorizontalAlignment(JLabel.CENTER);
			mensajeFirmaOk.setVerticalAlignment(JLabel.CENTER);
		}
		return mensajeFirmaOk;
	}
	/**
	 *  Etiqueta del titulo
	 * @return
	 */
	public JLabel getLabelIconoOk() {
		if(labelIconoOk==null){
			labelIconoOk = new JLabel(okIcon);
		}
		return labelIconoOk;
	}
	/**
	 *  Etiqueta del titulo
	 * @return
	 */
	public JLabel getLabelTitulo() {
		if(labelTitulo==null){
			labelTitulo = new JLabel(tituloIcon);
		}
		return labelTitulo;
	}

	/**
	 *  Git animado "procesando" para indicar puntos de procesamiento
	 * @return
	 */
	public JProgressBar getProgressBar() {
		if(progressBar==null){
			progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
			progressBar.setStringPainted(true);
			progressBar.setString("Procesando");
			//progressBar.setSize(100, 30);
		}
		return progressBar;
	}


	protected void subirPdfs()
	{
		showProgress(myProps.getString("progresoSubiendoArchivo"));
		boolean ok = true;
		for (Map.Entry<String, Documento> entry : getSignProps().getDocumentos().entrySet()) {
			if (!firmaControler.subirDocumento(container, entry.getValue())){
				ok = false;
				break;
			}
		}
		if (ok) {
			setEstado(Estados.SUBIDA_DOC_OK);
			try {
				JSObject window = JSObject.getWindow((Applet) container);
				window.call("firmaOk", new Object[] {})   ;
			} catch (Exception e) {
				e.printStackTrace();
			}			
		} else {
			setEstado(Estados.SUBIDA_DOC_ERROR);
		}
		hideProgress();
	}


	/**
	 * Boton encargado de recibir el evento de mostrar mostrar el documento
	 * @return
	 */
	public JButton getBotonVerPdf() {
		if(botonVerPdf==null){
			// Operaciones
			botonVerPdf = new javax.swing.JButton();
			botonVerPdf.setText(myProps.getString("visualizar"));
			botonVerPdf.setFont(new java.awt.Font(letra, Font.PLAIN, 11));
			botonVerPdf.setToolTipText("Visualizar el PDF en un programa externo, por ejemplo Adobe Reader");
			botonVerPdf.setVisible(false);			
			botonVerPdf.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					//getSignProps().getAppLogFile()
					firmaControler.visualizarDocumento(container, getSignProps().getDocumentoUnico().getArchivoAFirmar());
				}
			});
		}
		return botonVerPdf;
	}
			
	/**
	 * Boton encargado de recibir el evento de firmar el documento
	 * @return
	 */
	public JButton getBotonFirmar() {
		if(botonFirmar==null){
			botonFirmar = new javax.swing.JButton();
			botonFirmar.setText(myProps.getString("firmar"));
			botonFirmar.setFont(new java.awt.Font(letra, Font.BOLD, 11));
			botonFirmar.setEnabled(false);
			AccessController.doPrivileged(new PrivilegedAction<Object>() {
				public Object run()
				{	
					botonFirmar.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							if (getSignProps().isMultiple() && ! getSignProps().getDocumentosDescargados()) {
								descargarDocumentos();
							} else {
								firmarDocumentos();
							}
						}
					});
					return null;
				}
			});
		}
		return botonFirmar;
	}
	
	public void descargarDocumentos() {
		getBotonFirmar().setEnabled(false);
		showProgress(myProps.getString("progresoBajandoArchivo"));
		downloadThread = new Thread(this);
		downloadThread.start();
	}
	
		@Override
	public void run() {
		//Download Thread			
		boolean ok = true;
		for (Map.Entry<String, Documento> entry : getSignProps().getDocumentos().entrySet()) {
			File file = getfirmaControler().descargarDocumentoParaFirmar(
														getContainer(), 
														entry.getValue().getUrl());
			if (file == null) {
				ok = false;
				break;
			}
			entry.getValue().setArchivoAFirmar(file);
		}				
		if (ok) {
			getSignProps().setDocumentosDescargados(true);
			setEstado(VentanaPrincipal.Estados.DESCARGA_OK);
		} else {
			getSignProps().setDocumentosDescargados(false);
			setEstado(VentanaPrincipal.Estados.DESCARGA_ERROR);
		}
	}

	
	public void firmarDocumentos() {
		showProgress(myProps.getString("progresoAccediendoToken"));
		firmaControler.firmarDocumentos(container, getSignProps().getDocumentos());
	}
	
	/**
	 * crea el GroupLayout de como se mostrara los componetes en el applet
	 */
	public void crearGroupLayout(){
		panelPrincipal = new PanelPrincipal();
		//Crear espacio entre componentes
		GroupLayout myLayout = new GroupLayout(panelPrincipal);

		panelPrincipal.setLayout(myLayout);
		panelPrincipal.setBackground(Color.white);
		panelPrincipal.setForeground(Color.decode("#565656"));

		int marginLeft = 20;
		myLayout.setHorizontalGroup( 
				myLayout.createParallelGroup(GroupLayout.Alignment.LEADING)

				.addGroup(myLayout.createSequentialGroup()
						.addGap(10,10,10)
                		.addComponent(getLabelTitulo(),GroupLayout.PREFERRED_SIZE, 70,GroupLayout.PREFERRED_SIZE)
						.addGap(10,10,10)
						.addComponent(getjLblPDFile(),GroupLayout.PREFERRED_SIZE, 120,GroupLayout.PREFERRED_SIZE)
						.addGap(10,10,10)
						.addComponent(getProgressBar(),GroupLayout.PREFERRED_SIZE, 180,GroupLayout.PREFERRED_SIZE))
				.addGroup(myLayout.createSequentialGroup()
						.addGap(marginLeft,marginLeft,marginLeft)
						.addComponent(getBotonFirmar(),GroupLayout.PREFERRED_SIZE, 240,GroupLayout.PREFERRED_SIZE)
						.addGap(20,20,20)
						.addComponent(getBotonVerPdf(),GroupLayout.PREFERRED_SIZE, 180,GroupLayout.PREFERRED_SIZE))				

		);


		myLayout.setVerticalGroup(myLayout.createSequentialGroup()

				.addGroup(myLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
       						.addComponent(getjLblPDFile(),GroupLayout.PREFERRED_SIZE, 50,GroupLayout.PREFERRED_SIZE)
                        	.addComponent(getLabelTitulo(),GroupLayout.PREFERRED_SIZE, 50,GroupLayout.PREFERRED_SIZE)
							.addComponent(getProgressBar(),GroupLayout.PREFERRED_SIZE, 30,GroupLayout.PREFERRED_SIZE))
				.addGap(10,10,10)

				.addGroup(myLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(getBotonFirmar(),GroupLayout.PREFERRED_SIZE, 40,GroupLayout.PREFERRED_SIZE)
							.addComponent(getBotonVerPdf(),GroupLayout.PREFERRED_SIZE, 40,GroupLayout.PREFERRED_SIZE))
				.addGap(10,10,10)				

		);
		hideProgress();

	}


	/**
	 * metodo encargado de Habilitar o desabilitar los botones del Applet
	 *  deacuerdo a las acciones ralizadas
	 * @param operation
	 */
	public void setEstado(Estados estado) {
		System.out.println("Set Estado: " + estado.name());
		switch (estado) {
			case DOCUMENTO_AGREGADO:
			case DOCUMENTO_QUITADO:
				int cantidad = getSignProps().getCantidadDocumentos();
				updateBotonFirmar(cantidad);
				break;
			case DESCARGA_ERROR:
				hideProgress();
				botonVerPdf.setEnabled(false);
				botonFirmar.setEnabled(false);
				break;
			case DESCARGA_OK:
				hideProgress();
				if (! getSignProps().isMultiple()) {
					botonFirmar.setEnabled(true);
				}
				botonFirmar.setVisible(true);
				if (! getSignProps().isMultiple()) {
					botonVerPdf.setVisible(true);
					botonVerPdf.setEnabled(true);
				} else {
					//En firma masiva, luego de descargarlos, hay que firmarlos
					firmarDocumentos();
				}
				break;
			case FIRMA_DOC_OK:
				hideProgress();
				botonVerPdf.setEnabled(false);
				botonFirmar.setEnabled(false);//false
				subirPdfs();
				break;
			case FIRMA_DOC_ERROR:
				hideProgress();
				break;
			case SUBIDA_DOC_OK:
				botonFirmar.setEnabled(false);
				botonFirmar.setText(myProps.isMultiple() ? myProps.getString("firmados") : myProps.getString("firmado"));			
				botonVerPdf.setVisible(false);
				break;

		}

	}
	/**
	 * inicializa los  controler y la configuarcion necesaria
	 *  que se usaran en la firma de los documentos
	 * @param container
	 * @return
	 */
	public boolean initSigner(Container container) {
		boolean retValue = true;
		try {
			// Se setea la configuracion necesaria para seleccionar
			// certificados y presentacion de la firma
			pdfControler.setProps(myProps);
			// Se setea archivo de log para permitir loggeo
			// de las acciones propias de firma
			pdfControler.setLogFile(appLogFile);
		} catch (FileNotFoundException e) {
			retValue = false;
			String userMsg = "Método MainWindow.initSigner(): Error al inicializar el objeto de firma";
			if (e.getMessage() != null) {
				userMsg += "\nMensaje JVM: " + e.getMessage();
			}	
			firmaControler.writeLogFile(userMsg, 1);
			JOptionPane.showMessageDialog(container, userMsg, "Firma Digital", JOptionPane.ERROR_MESSAGE);
			return retValue;
		}	
		return retValue;
	}

	/**
	 * levanta del archivo properties las variables que se usaran 
	 * para la firma de los documnentos
	 * @param container
	 * @return
	 */
	public boolean initProps(Container container) {
		boolean retValue = true;
		if (getSignProps().isMultiple()) {
			updateBotonFirmar(0);
		}
		if (!myProps.readProps() ) {
			JOptionPane.showMessageDialog(container,
					"Problemas en la configuración de la aplicación\n"
					+ myProps.getPropsError(), "Error",
					JOptionPane.ERROR_MESSAGE);
			retValue = false;
		}
		return retValue;
	}
	/**
	 * inicializa el archivo que se utilizara para loguear los eventos que sucedan en la firma del docuemnto
	 * @param container
	 * @return
	 */
	public boolean initLogFile(Container container) {
		boolean retValue = true;
		String msgErrInitLog ="";
		if (myProps == null) {
			msgErrInitLog = "Método MainWindow.initLogFile: Error en lectura previa de configuración";
			retValue = false;
		} else {
			// Creacion del archivo de log
			
			Calendar calendar = Calendar.getInstance();    
			calendar.set(Calendar.MILLISECOND, 0); // Clear the millis part. Silly API.
			calendar.set(2010, 8, 14, 0, 0, 0); // Note that months are 0-based
			Date date = calendar.getTime();
			long millis = date.getTime(); 
			
			String logFileName = myProps.getSourceDir() + File.separator + "pdfControler" + millis + ".log";
			System.out.println("logFileName: " + logFileName);
			try {
				hndLog = new FileHandler(logFileName, true);
				hndLog.setFormatter(new SimpleFormatter());
				appLogFile = Logger.getLogger(logFileName);
				appLogFile.addHandler(hndLog);
				// Se elimina output a la consola
				Logger rootLogger = Logger.getLogger("");
				Handler[] rootHandlers = rootLogger.getHandlers();
				if (rootHandlers.length > 0 && rootHandlers[0] instanceof ConsoleHandler) {
					
						rootLogger.removeHandler(rootHandlers[0]);
					
				}
				PropsConfig.getInstance().setAppLogFile(appLogFile);
				// Incio loggeo acciones
				firmaControler.writeLogFile("INIT PROCESS  ------------------", 0);
			} catch (IOException e){
				msgErrInitLog = "Método MainWindow.initLogFile(): Error al inicializar el archivo de log"; 
				if (e.getMessage() != null) {
					msgErrInitLog += "\nMensaje JVM: " + e.getMessage();
				}
				retValue = false;
			}
		}
		if (!retValue) {
			JOptionPane.showMessageDialog(container, msgErrInitLog, "Error", JOptionPane.ERROR_MESSAGE);
		}
		return retValue;
	}
	/**
	 * 
	 */
	public void closeLogFile() {
		firmaControler.closeLogFile();

	}

	/**
	 * Container es el contenedor de los componetes en este caso el Applet
	 * @return
	 */
	public Container getContainer() {
		return container;
	}
	/**
	 * 
	 * @param container
	 */
	public void setContainer(Container container) {
		this.container = container;
	}

	/**+
	 * 
	 * @param signProps
	 */
	public void setSignProps(PropsConfig signProps) {
		this.myProps = signProps;
	}
	
	/**
	 * 
	 * @param appLogFile
	 */
	public void setAppLogFile(Logger appLogFile) {
		this.appLogFile = appLogFile;
	}
	/**
	 * 
	 * @param hndLog
	 */
	public void setHndLog(FileHandler hndLog) {
		this.hndLog = hndLog;
	}
	/**
	 * 
	 * @param pdfControler
	 */
	public void setpdfControler(PdfControler pdfControler) {
		this.pdfControler = pdfControler;
	}
	/**
	 * 
	 * @return
	 */
	public PropsConfig getSignProps() {
		return myProps;
	}
	
	/**
	 * 
	 * @return
	 */
	public Logger getAppLogFile() {
		return appLogFile;
	}
	/**
	 * 
	 * @return
	 */
	public FileHandler getHndLog() {
		return hndLog;
	}
	/**
	 * 
	 * 
	 * @return
	 */
	public PdfControler getPdfControler() {
		return pdfControler;
	}

	public void showProgress(String message) {
		getProgressBar().setVisible(true);
		getProgressBar().setString(message);
	}
	
	public void hideProgress() {
		progressBar.setVisible(false);
	}

	public void updateBotonFirmar(int cantidad) {
		if (cantidad > 0) {
			botonFirmar.setText(myProps.getString("firmarMultiple") + " (" + cantidad + ")");
			botonFirmar.setEnabled(true);
		} else {
			botonFirmar.setText(myProps.getString("firmarMultipleEsperar"));
			botonFirmar.setEnabled(false);
		}
	}
			




}
