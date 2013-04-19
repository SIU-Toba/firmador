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

import ar.gob.onti.firmador.controler.FileSystem;
import ar.gob.onti.firmador.controler.FirmaControler;
import ar.gob.onti.firmador.controler.PdfControler;
import ar.gob.onti.firmador.model.PropsConfig;
/**
 * Clase contenedora de los componetes de la Applicacion
 * @author ocaceres
 *
 */
public class VentanaPrincipal  {
	private static final long serialVersionUID = 1L;
	private JLabel jLblPDFile;
	private JLabel mensajeFirmaOk;
	private JPanel panelPrincipal =null;
	private PanelPrincipal panelFirmaExitosa=null;
	private JLabel labelTitulo;
	private JLabel labelIconoOk;
	private JButton botonSubirPdf;
	private JButton botonFlechaDerecha;
	private JButton botonVerPdf;
	private JButton botonFirmar;
	private JButton botonAyuda;
	private JTextField certSelecionado;
	private Container container;
	private PropsConfig myProps=null;
	private File 		archivoParaFirmar;
	private File 		archivoFirmado;
	private Logger 		appLogFile;
	private FileHandler hndLog=null;
	private PdfControler   pdfControler;
	private FirmaControler firmaControler;
	private	ImageIcon examinarIcon = null;
	private ImageIcon visualizarIcon = null;
	private ImageIcon flechaIcon = null;
	private ImageIcon firmarIcon =  null;
	private ImageIcon tituloIcon =  null;
	private ImageIcon okIcon =  null;
	private ImageIcon ayudaIcon =  null;
	private String idApplicacion;
	private String codigo;
	private String objetoDominio;
	private String tipoArchivo;
	private String letra="Arial";
	private String color="#ffffff";
	/**
	 * Controlador que se encarga de manejar las acciones que realiza el usuario
	 * @return
	 */
	public FirmaControler getfirmaControler() {
		return firmaControler;
	}
	public File getArchivoParaFirmar() {
		return archivoParaFirmar;
	}
	public void setArchivoParaFirmar(File archivoParaFirmar) {
		this.archivoParaFirmar = archivoParaFirmar;
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

	/**
	 * Objeto Dominio que recibe el Applet de la aplicaion que lo esta jecutando
	 * @return
	 */
	public String getObjetoDominio() {
		return objetoDominio;
	}

	/**
	 * 
	 * @param objetoDominio
	 */
	public void setObjetoDominio(String objetoDominio) {
		this.objetoDominio = objetoDominio;
	}
	
	/**
	 * Tipo Archivo que recibe el Applet de la aplicaion que lo esta jecutando
	 * @return
	 */
	public String getTipoArchivo() {
		return tipoArchivo;
	}

	/**
	 * 
	 * @param tipoArchivo
	 */
	public void setTipoArchivo(String tipoArchivo) {
		this.tipoArchivo = tipoArchivo;
	}

	/**
	 * Id de la Applicaicon que esta ejecutando el Applet
	 * @return
	 */
	public String getIdApplicacion() {
		return idApplicacion;
	}
	/**
	 * 
	 * @param idApplicacion
	 */
	public void setIdApplicacion(String idApplicacion) {
		this.idApplicacion = idApplicacion;
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
		myProps =PropsConfig.getInstance();
		myProps.setBrowser(browser);
		archivoParaFirmar = null;
		appLogFile = null;
		hndLog = null;
		archivoFirmado=null;
		this.container=container;
	}
    public void inicializar(){
    	// Se instancia el objeto de firma y se trabaja sobre
		// el documento origen, generando en el documento de
		// salida la firma digital
		try {
			pdfControler = new PdfControler();
		} catch (NoSuchAlgorithmException e) {
			PropsConfig.getInstance().getAppLogFile().info(e.getMessage());
		}
		pdfControler.setProps(PropsConfig.getInstance());
		firmaControler= new FirmaControler(this);
		ClassLoader cl = this.getClass().getClassLoader();
		examinarIcon =  new ImageIcon(cl.getResource("images/folder_new.png"));
		visualizarIcon =  new ImageIcon(cl.getResource("images/view.png"));	
		firmarIcon =  new ImageIcon(cl.getResource("images/sign.png"));
		tituloIcon =  new ImageIcon(cl.getResource("images/logo_jgm.gif"));
		ayudaIcon =  new ImageIcon(cl.getResource("images/Help-32.png"));
		flechaIcon =  new ImageIcon(cl.getResource("images/flecha-derecha.png"));
		okIcon =  new ImageIcon(cl.getResource("images/ok.png"));
		crearGroupLayout();
		panelFirmaExitosa= new PanelPrincipal();
		panelFirmaExitosa.setLayout(new GridLayout(2,1));
		panelFirmaExitosa.add(getLabelIconoOk());
		panelFirmaExitosa.add(getMensajeFirmaOk());
		this.container.add(panelPrincipal);
    }
	/**
	 * Archivo pdf  que se ha firmado
	 * @return
	 */
	public File getArchivoFirmado() {
		return archivoFirmado;
	}
	/**
	 * 
	 * @param archivoFirmado
	 */
	public void setArchivoFirmado(File archivoFirmado) {
		this.archivoFirmado = archivoFirmado;
	}
	/**
	 * Etiqueta del titulo
	 * @return
	 */
	public JLabel getjLblPDFile() {
		// Seleccion de archivo
		if(jLblPDFile==null){
			jLblPDFile = new JLabel(myProps.getString("titulo"));
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
	 * Boton encargado de recibir el evento de subir el pdf al servidor
	 * @return
	 */
	public JButton getBotonSubirPdf() {
		if(botonSubirPdf==null){
			botonSubirPdf = new javax.swing.JButton();
			botonSubirPdf.setBackground(Color.decode(color));
			botonSubirPdf.setFont(new java.awt.Font(letra, Font.BOLD, 14));
			botonSubirPdf.setForeground(Color.decode("#565656"));
			botonSubirPdf.setHorizontalTextPosition(AbstractButton.CENTER);
			botonSubirPdf.setVerticalTextPosition(AbstractButton.BOTTOM);
			botonSubirPdf.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode(color)));
			botonSubirPdf.setIcon(examinarIcon);
			botonSubirPdf.setEnabled(false);
			botonSubirPdf.setText(myProps.getString("subirPdf"));

			botonSubirPdf.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {

					//int repuesta = JOptionPane.showConfirmDialog(container,
					//		myProps.getString("mensajeConfirmacionEnvio"),
					//		myProps.getString("confirmacionEnvio"),
					//		JOptionPane.YES_NO_OPTION);					
					
					//if original
					//if(repuesta==0 && firmaControler.subirDocumento(container)){
					if(firmaControler.subirDocumento(container)){
						
							panelPrincipal.setVisible(false);
							container.add(panelFirmaExitosa);
						
					}
				}
			}
			);
		}
		return botonSubirPdf;
	}






	/**
	 * Boton encargado de recibir el evento de mostrar la ayuda
	 * @return
	 */
	public JButton getBotonAyuda() {
		if(botonAyuda==null){
			botonAyuda = new javax.swing.JButton();
			botonAyuda.setText(myProps.getString("tituloAyuda"));
			botonAyuda.setFont(new java.awt.Font(letra, Font.BOLD, 14));
			botonAyuda.setBackground(Color.decode(color));
			botonAyuda.setForeground(Color.decode("#888888"));
			botonAyuda.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode(color)));
			botonAyuda.setIcon(ayudaIcon);
			botonAyuda.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					JOptionPane.showMessageDialog(container,myProps.getString("textoDeAyuda"), myProps.getString("tituloAyuda"), JOptionPane.INFORMATION_MESSAGE,ayudaIcon);
				}
			});



		}

		return botonAyuda;
	}



	/**
	 * Boton encargado de recibir el evento de mostrar el icono de la flecha
	 * @return
	 */
	public JButton getBotonFlechaDerecha() {
		if(botonFlechaDerecha==null){
			botonFlechaDerecha = new JButton();
			botonFlechaDerecha.setIcon(flechaIcon);
			botonFlechaDerecha.setBackground(Color.decode(color));
			botonFlechaDerecha.setForeground(Color.decode("#565656"));
			botonFlechaDerecha.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode(color)));
		}
		return botonFlechaDerecha;
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
			botonVerPdf.setFont(new java.awt.Font(letra, Font.BOLD, 14));
			botonVerPdf.setIcon(visualizarIcon);
			botonVerPdf.setBackground(Color.decode(color));
			botonVerPdf.setForeground(Color.decode("#888888"));
			botonVerPdf.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode(color)));
			//botonVerPdf.setToolTipText("Visualizar el documento seleccionado");
			botonVerPdf.setEnabled(true);
			botonVerPdf.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					if(FileSystem.getInstance().isExisteArchivo(archivoParaFirmar)){
						firmaControler.visualizarDocumento(container, archivoParaFirmar);
					}else{
						firmaControler.visualizarDocumento(container,archivoFirmado);
					}
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
			botonFirmar.setIcon(firmarIcon);
			botonFirmar.setFont(new java.awt.Font(letra, Font.BOLD, 14));
			botonFirmar.setHorizontalTextPosition(AbstractButton.CENTER);
			botonFirmar.setVerticalTextPosition(AbstractButton.BOTTOM);
			botonFirmar.setBackground(Color.decode(color));
			botonFirmar.setForeground(Color.decode("#888888"));
			botonFirmar.setBorder(javax.swing.BorderFactory.createLineBorder(Color.decode(color)));
			AccessController.doPrivileged(new PrivilegedAction<Object>() {
				public Object run()
				{	
					botonFirmar.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							firmaControler.firmarDocumento(container);
						}
					});
					return null;
				}
			});
		}
		return botonFirmar;
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

		myLayout.setHorizontalGroup( 
				myLayout.createParallelGroup(GroupLayout.Alignment.LEADING)

				.addGroup(myLayout.createSequentialGroup().
						addGap(15,15,15).
						addComponent(getjLblPDFile(),GroupLayout.PREFERRED_SIZE, 360,GroupLayout.PREFERRED_SIZE).
						addComponent(getLabelTitulo()))

						.addGroup(myLayout.createSequentialGroup().
								addGap(50,50,50).
								addComponent(getBotonFirmar(),GroupLayout.PREFERRED_SIZE, 200,GroupLayout.PREFERRED_SIZE).
								//addGap(4,4,4).
								//addComponent(getBotonFlechaDerecha(),GroupLayout.PREFERRED_SIZE, 50,GroupLayout.PREFERRED_SIZE).
								addGap(4,4,4).
								addComponent(getBotonSubirPdf(),GroupLayout.PREFERRED_SIZE, 230,GroupLayout.PREFERRED_SIZE))

								.addGroup(myLayout.createSequentialGroup().
										addGap(190,190,190).
										addComponent(getBotonVerPdf(),GroupLayout.PREFERRED_SIZE, 200,GroupLayout.PREFERRED_SIZE).
										//addGap(100,100,100).
										//addComponent(getBotonAyuda(),GroupLayout.PREFERRED_SIZE, 80,GroupLayout.PREFERRED_SIZE).
										addGap(20,20,20))					
		);


		myLayout.setVerticalGroup(myLayout.createSequentialGroup()

				.addGroup(myLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(getjLblPDFile(),GroupLayout.PREFERRED_SIZE, 100,GroupLayout.PREFERRED_SIZE)
						.addComponent(getLabelTitulo(),GroupLayout.PREFERRED_SIZE, 100,GroupLayout.PREFERRED_SIZE))

						.addGroup(myLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
								addComponent(getBotonFirmar(),GroupLayout.PREFERRED_SIZE, 90,GroupLayout.PREFERRED_SIZE).
								//addComponent(getBotonFlechaDerecha(),GroupLayout.PREFERRED_SIZE, 90,GroupLayout.PREFERRED_SIZE).
								addComponent(getBotonSubirPdf(),GroupLayout.PREFERRED_SIZE, 90,GroupLayout.PREFERRED_SIZE)
						).
						addGap(50,50,50).
						addGroup(myLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
								addComponent(getBotonVerPdf(),GroupLayout.PREFERRED_SIZE, 40,GroupLayout.PREFERRED_SIZE)

								//addComponent(getBotonAyuda(),GroupLayout.PREFERRED_SIZE, 40,GroupLayout.PREFERRED_SIZE)
								)


		);



	}


	/**
	 * metodo encargado de Habilitar o desabilitar los botones del Applet
	 *  deacuerdo a las acciones ralizadas
	 * @param operation
	 */
	public void setCtrls(String operation) {

		if (operation.equals("errorDescarga")) {
			botonVerPdf.setEnabled(false);
			botonFirmar.setEnabled(false);
			botonSubirPdf.setEnabled(false);
		} else if (operation.equals("firmaDocOk")) {
			botonFirmar.setEnabled(false);//false
			botonSubirPdf.setEnabled(true);
		} else if (operation.equals("subidaDocOk")) {
			botonFirmar.setEnabled(false);
			botonVerPdf.setEnabled(false);
			botonSubirPdf.setEnabled(false);
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
			JOptionPane.showMessageDialog(container, userMsg, "Firma Dictámenes", JOptionPane.ERROR_MESSAGE);
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

	/**
	 * 
	 * @param jLblPDFile
	 */
	public void setjLblPDFile(JLabel jLblPDFile) {
		this.jLblPDFile = jLblPDFile;
	}

	/**
	 * 
	 * @param botonSubirPdf
	 */
	public void setBotonSubirPdf(JButton botonSubirPdf) {
		this.botonSubirPdf = botonSubirPdf;
	}
	/**
	 * 
	 * @param botonVerPdf
	 */
	public void setBotonVerPdf(JButton botonVerPdf) {
		this.botonVerPdf = botonVerPdf;
	}
	/**
	 * 
	 * @param botonFirmar
	 */
	public void setBotonFirmar(JButton botonFirmar) {
		this.botonFirmar = botonFirmar;
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







}
