package ar.gob.onti.firmador.view.certificatelist;

/*
 * CertsTreeTable.java
 * author: mpin
 * owner : ONTI
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.NumberFormat;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ar.gob.onti.firmador.controler.PdfControler.OriginType;
import ar.gob.onti.firmador.model.PropsConfig;
import ar.gob.onti.firmador.model.certificatelist.CertifsModel;

import com.itextpdf.text.pdf.PdfPKCS7;


public class CertsTreeTable {
	private PropsConfig myProps=null;
	/* Dialogo que contiene los demas controles */
	private JDialog mainDlg=null;
	private  KeyStore[] keyStore;
	/* Modelo utilizado por el TreeTable construido a partir
	 * de los certificados */
	private CertifsModel    certsModel;
	/* Utilizada para mostrar la jerarquia presente en el modelo */
	private JTreeTable      treeTable;
	/* Buttons para permitir la accion de seleccion/cancelacion */
	private JButton         jbtnSelCert=null;
	private JButton 		  jbtnCancel=null;
	private JPanel panelArriba=null;
	private JPanel panelAbajo=null;
	private JPanel panelBotones=null;
	private JLabel labelInformation= null;
	private JLabel labelSelectCertTop = null;
	private JLabel informationLabelField = null;
	private JScrollPane contentScrollPane = null;
	private JFormattedTextField contentTextField = null;
	public JButton getJbtnSelCert() {
		if(jbtnSelCert==null){
			// Seleccion certificados
			jbtnSelCert = new javax.swing.JButton();
			jbtnSelCert.setText(myProps.getString("seleccionar"));
			jbtnSelCert.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					selCertificate(evt);
				}
			});
			jbtnSelCert.setEnabled((treeTable.getRowCount()>1));
		}
		return jbtnSelCert;
	}

	public void setJbtnSelCert(JButton jbtnSelCert) {
		this.jbtnSelCert = jbtnSelCert;
	}



	public JButton getJbtnCancel() {
		// Cancelacion
		if(jbtnCancel==null){
			jbtnCancel = new javax.swing.JButton();
			jbtnCancel.setText(myProps.getString("cancelar"));
			jbtnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					cancelCertificate(evt);
				}
			});
		}
		return jbtnCancel;
	}

	public void setJbtnCancel(JButton jbtnCancel) {
		this.jbtnCancel = jbtnCancel;
	}
	// Conserva el alias del certificado seleccionado
	private String          aliasSelCert ;

	public CertsTreeTable() {
		myProps=PropsConfig.getInstance();
		aliasSelCert = "";
	}
	public void inicializar(Frame mainWndw, KeyStore[] aKeyStr, List <String> anACList)throws KeyStoreException, UnsupportedEncodingException{
		keyStore=aKeyStr;
		certsModel = new CertifsModel(aKeyStr, anACList);
		treeTable = new JTreeTable(certsModel);
		treeTable.inicializar();
		treeTable.getColumnModel().getColumn(1).setCellRenderer(new IndicatorRenderer());
		mainDlg = getCreateDialog(mainWndw);
		mainDlg.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension formSize = mainDlg.getPreferredSize();
		mainDlg.setLocation(screenSize.width / 2 - (formSize.width / 2),
				screenSize.height / 2 - (formSize.height / 2));
		treeTable.changeSelection(0, 1, false, false);
		mainDlg.setVisible(PropsConfig.getInstance().isVisible());
	}
	/* Crea el JDialog principal que contiene los demas elementos */
	private JDialog getCreateDialog(Frame mainWndw) {
        if(mainDlg==null){
		mainDlg = new JDialog(mainWndw,myProps.getString("repoCert"), true);
		WindowListener wndListenClose = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				certsModel.unsetKeyStore();
				certsModel.unsetIssuers();
			}
		};

		mainDlg.addWindowListener(wndListenClose);
		mainDlg.getContentPane().setLayout(new GridLayout(2,0));
		mainDlg.getContentPane().add(getPanelArriba());
		mainDlg.getContentPane().add(getPanelAbajo());
		mainDlg.setResizable(false);
        }
		return mainDlg;
	}
	public JPanel getPanelBotones(){
		if(panelBotones==null){
			panelBotones= new JPanel();

			javax.swing.GroupLayout aLayout = new javax.swing.GroupLayout(panelBotones);
			panelBotones.setLayout(aLayout);
			aLayout.setHorizontalGroup(
					aLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addComponent(getInformationLabelField())
					.addGroup(aLayout.createSequentialGroup()
							.addGap(320,320,320)	   
							.addComponent(getJbtnSelCert())
							.addGap(13,13,13)
							.addComponent(getJbtnCancel())
							.addGap(13,13,13)));
			aLayout.setVerticalGroup(
					aLayout.createSequentialGroup()
					.addComponent(getInformationLabelField())
					.addGap(13,13,13)		
					.addGroup(aLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
							.addComponent(getJbtnSelCert())
							.addComponent(getJbtnCancel()))
							.addGap(13,13,13));

		}
		return panelBotones;
	}
	public JPanel getPanelAbajo(){
		if(panelAbajo==null){
			panelAbajo= new JPanel();  		  
			GridBagLayout bagLayout= new GridBagLayout();
			panelAbajo.setLayout(bagLayout);
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.5;
			c.gridx = 0;
			c.gridy = 0;
			c.weighty = 0.1;
			panelAbajo.add(getLabelInformation(), c);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.ipady = 80;      //make this component tall
			c.weightx = 0.0;
			c.gridwidth = 3;
			c.gridx = 0;
			c.gridy = 1;
			panelAbajo.add(getContentScrollPane(), c);


			c.fill = GridBagConstraints.HORIZONTAL;
			c.ipady = 0;       //reset to default
			c.weighty = 1.0;   //request any extra vertical space
			c.anchor = GridBagConstraints.PAGE_END; //bottom of space
			c.insets = new Insets(10,0,0,0);  //top padding
			c.gridx = 1;       //aligned with button 2
			c.gridwidth = 2;   //2 columns wide
			c.gridy = 2;       //third row
			panelAbajo.add(getPanelBotones(), c);

		}
		return panelAbajo;
	}

	/**
	 * This method initializes informationTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	protected JLabel getInformationLabelField()
	{
		if (informationLabelField == null)
		{
			informationLabelField = new JLabel();
			informationLabelField.setText(myProps.getString("selectCertifYFirmar"));
		}
		return informationLabelField;
	}
	public JLabel getLabelSelectCertTop(){
		if(labelSelectCertTop==null){
			labelSelectCertTop = new JLabel();
			labelSelectCertTop.setText(myProps.getString("selectCertificado"));
		}
		return labelSelectCertTop;
	}

	public JLabel getLabelInformation(){
		if(labelInformation==null){
			labelInformation = new JLabel();
			labelInformation.setText(myProps.getString("informacion"));
		}
		return labelInformation;
	}
	/**
	 * This method initializes informationTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	protected JFormattedTextField getContentTextField()
	{
		if (contentTextField == null)
		{
			contentTextField = new JFormattedTextField();  
			contentTextField.setEditable(false);
			contentTextField.setBackground(Color.white);
			contentTextField.setForeground(new Color(128, 128, 128));
			contentTextField.setValue(myProps.getString("informacionCertificado"));
		}
		return contentTextField;
	}
	/**
	 * This method initializes informationScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getContentScrollPane()
	{
		if (contentScrollPane == null)
		{
			contentScrollPane = new JScrollPane();
			contentScrollPane.setBackground(Color.WHITE);
			contentScrollPane.setViewportView(getContentTextField());
		}
		return contentScrollPane;
	}
	private void addComponente(JPanel panel,GridBagLayout gridbag, Component comp,int gridx,int gridy,
			int gridw,int gridh ) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = gridw;
		gbc.gridheight = gridh;
		gbc.weightx = 0.5;
		gridbag.setConstraints( comp,gbc );
		panel.add( comp );
	}


	public JPanel getPanelArriba(){
		if(panelArriba==null){
			// Disposicion de controles en el formulario
			panelArriba= new JPanel();
			JScrollPane jscrTreeTable = new JScrollPane(treeTable);	
			GridBagLayout bagLayout= new GridBagLayout();
			panelArriba.setLayout(bagLayout);
			addComponente(panelArriba, bagLayout, getLabelSelectCertTop(), 0, 0, 1, 1);
			addComponente(panelArriba, bagLayout, jscrTreeTable, 0, 1, 2, 2);

		}
		return panelArriba;
	}
	protected void selCertificate(java.awt.event.ActionEvent evt) {


			int selRow = treeTable.getSelectedRow();
			// Nodo raiz - Certificados
			if (selRow == 0) {
				return;
			}
			String selectedCert = (treeTable.getValueAt(selRow, 0)).toString();
			String selSerial = (treeTable.getValueAt(treeTable.getSelectedRow(), 1)).toString().trim();
			String selAlias = certsModel.getCerAlias(selSerial, null);
			String selOriginType = certsModel.getOriginType(selSerial, null);
			if (selSerial.length() == 0) {
				JOptionPane.showMessageDialog(null,myProps.getString("errorCertPersonal1").replace("CERTIFICADO", selectedCert) ,myProps.getString("certificados") , JOptionPane.ERROR_MESSAGE);
			} else {
				aliasSelCert = selectedCert + "##" + selSerial  + "##" + selAlias + "##" + selOriginType;
				mainDlg.dispatchEvent(new WindowEvent(mainDlg, WindowEvent.WINDOW_CLOSING));
			}

		return;
	}	

	protected void cancelCertificate(java.awt.event.ActionEvent evt) {
		aliasSelCert = "";
		mainDlg.dispatchEvent(new WindowEvent(mainDlg, WindowEvent.WINDOW_CLOSING));
	}

	public String getSelectedCert() {

		String aliasCert = "";
		if (aliasSelCert.trim().length() > 0) {
			aliasSelCert = aliasSelCert.trim();
			aliasCert = aliasSelCert.replaceFirst("##", " - (SN:");
			aliasCert = aliasCert.replaceFirst("##", " - Alias:");
			aliasCert = aliasCert.replaceFirst("##", " - Origin:") + ")";
		}
		return aliasCert;
	}


	public void cargarInfoCetificado(int certificadoSelecionado ){
		String serialN = (treeTable.getValueAt(certificadoSelecionado, 1)).toString().trim();
		String selAlias = certsModel.getCerAlias(serialN, null);
		String originType = certsModel.getOriginType(serialN, null);
		int origin = 0;
		if (originType != null && originType != "") {
			origin = Integer.parseInt(originType);
		}
		X509Certificate cerONTI = null;
		Certificate cerKeyStore = null;
		try {
			cerKeyStore=keyStore[origin].getCertificate(selAlias);
			if (cerKeyStore instanceof X509Certificate) {
				cerONTI = (X509Certificate)cerKeyStore;	
				String subject=PdfPKCS7.getSubjectFields(cerONTI).toString().replace("{", " ");
				subject=subject.replace("}", " ");
				subject=subject.replace("[", " ");
				subject=subject.replace("]", " ");
				this.getContentTextField().setText(getInformacion(subject));
			}
		} catch (KeyStoreException e) {
			PropsConfig.getInstance().getAppLogFile().info(e.getMessage());				
		}
	}
	class IndicatorRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private NumberFormat       formatter;

		IndicatorRenderer() {
			setHorizontalAlignment(JLabel.RIGHT);
			formatter = NumberFormat.getInstance();
		}
		/* Invoked as part of DefaultTableCellRenderers implemention. 
		 * Sets the text of the label. */
		public void setValue(Object value) {
			cargarInfoCetificado(treeTable.getSelectedRow());
			if (value instanceof String) {
				setText(value.toString());  
			} else {
				setText((value == null) ? "---" : formatter.format(value));
			}   
		}
		/* Returns this. */
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			return this;
		}

	}
	private String getInformacion(String subjectDN){
		StringBuffer informacion=new StringBuffer();
		StringTokenizer extensionTokens= new StringTokenizer(subjectDN,",");    	 
		while (extensionTokens.hasMoreTokens()) {  
			informacion.append(extensionTokens.nextToken());
			informacion.append("\n");
		}
		return informacion.toString();
	}
}

