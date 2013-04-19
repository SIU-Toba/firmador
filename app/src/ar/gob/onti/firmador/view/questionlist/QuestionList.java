package ar.gob.onti.firmador.view.questionlist;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ar.gob.onti.firmador.model.PreguntaRespuesta;
import ar.gob.onti.firmador.model.PreguntasRespuestas;
import ar.gob.onti.firmador.model.PropsConfig;

public class QuestionList {
	private PropsConfig myProps=null;
	/* Dialogo que contiene los demas controles */
	private JDialog mainDlg=null;
	private JButton jbtnCancel;
	private JButton jbtnAccept;
	private boolean _isValid = false;
	private boolean _isCancel = false;
	private ArrayList<JTextField> fields = new ArrayList<JTextField>();

	public QuestionList() {
		myProps = PropsConfig.getInstance();
	}
	
	public void inicializar(Frame mainWndw) {
		_isValid = false;
		_isCancel = false;
		mainDlg = getCreateDialog(mainWndw);
		mainDlg.pack();
		
		PreguntasRespuestas preguntas = myProps.getPreguntas();
		mainDlg.getPreferredSize().setSize(0, preguntas.getItems().size() * 65 + 80);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension formSize = mainDlg.getPreferredSize();
		mainDlg.setLocation(screenSize.width / 2 - (formSize.width / 2),
				screenSize.height / 2 - (formSize.height / 2));
		mainDlg.setVisible(PropsConfig.getInstance().isVisible());
	}
	
	/* Crea el JDialog principal que contiene los demas elementos */
	private JDialog getCreateDialog(Frame mainWndw) {
        if(mainDlg==null){
			mainDlg = new JDialog(mainWndw,"Preguntas de confirmación", true);
			WindowListener wndListenClose = new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
	
				}
			};
	
			PreguntasRespuestas preguntas = myProps.getPreguntas();
			PreguntaRespuesta preguntaRespuesta;
			JPanel panelRespuesta;
			mainDlg.addWindowListener(wndListenClose);
			mainDlg.getContentPane().setLayout(new GridLayout(preguntas.getItems().size() + 1,2));
			for(int p = 0; p < preguntas.getItems().size(); p++) {
				preguntaRespuesta = preguntas.getItems().get(p);
				panelRespuesta = getPanelRespuesta();
				mainDlg.getContentPane().add(getPanelPregunta(preguntaRespuesta.getPregunta()));
				mainDlg.getContentPane().add(panelRespuesta);
			}
			mainDlg.getContentPane().add(getPanelAccept());
			mainDlg.getContentPane().add(getPanelCancel());
			
			mainDlg.setResizable(false);
        }
		return mainDlg;
	}

	private JPanel getPanelPregunta(String pregunta) {
		JPanel panel = new JPanel();
		JLabel label = new JLabel();
		label.setLocation(20, 40);
		label.setText(pregunta);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(label);
		return panel;
	}

	private JPanel getPanelRespuesta() {
		JPanel panel = new JPanel();
		JTextField text = new JTextField();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		text.setPreferredSize(new Dimension((int)(screenSize.getSize().getWidth() / 4) - 40, 25));
		text.setAlignmentX(Component.CENTER_ALIGNMENT);
		text.setLocation(0, 40);
		panel.add(text);
		fields.add(text);
		return panel;
	}

	private JPanel getPanelAccept() {
		JPanel panel = new JPanel();
		panel.add(getJbtnAccept());
		panel.setLocation(20, 20);
		return panel;
	}

	private JPanel getPanelCancel() {
		JPanel panel = new JPanel();
		panel.add(getJbtnCancel());
		panel.setLocation(20, 20);
		return panel;
	}

	public JButton getJbtnCancel() {
		// Cancelacion
		if(jbtnCancel==null){
			jbtnCancel = new javax.swing.JButton();
			jbtnCancel.setAlignmentX(Component.RIGHT_ALIGNMENT);
			jbtnCancel.setAlignmentX(Component.BOTTOM_ALIGNMENT);
			jbtnCancel.setText(myProps.getString("cancelar"));
			jbtnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					cancel();
				}
			});
		}
		return jbtnCancel;
	}

	public void setJbtnCancel(JButton jbtnCancel) {
		this.jbtnCancel = jbtnCancel;
	}

	public JButton getJbtnAccept() {
		// Aceptar
		if(jbtnAccept==null){
			jbtnAccept = new javax.swing.JButton();
			jbtnAccept.setAlignmentX(Component.LEFT_ALIGNMENT);
			jbtnAccept.setText(myProps.getString("aceptar"));
			jbtnAccept.setAlignmentX(Component.BOTTOM_ALIGNMENT);
			jbtnAccept.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					validate();
					close();
				}
			});
		}
		return jbtnAccept;
	}

	public void setJbtnAccept(JButton jbtnAccept) {
		this.jbtnAccept = jbtnAccept;
	}

	public boolean getIsValid() {
		return _isValid;
	}

	public boolean getIsCancel() {
		return _isCancel;
	}

	private void cancel() {
		_isValid = false;
		_isCancel = true;
		mainDlg.dispatchEvent(new WindowEvent(mainDlg, WindowEvent.WINDOW_CLOSING));
	}
	
	private void close() {
		mainDlg.dispatchEvent(new WindowEvent(mainDlg, WindowEvent.WINDOW_CLOSING));		
	}
	
	private void validate() {
		String respuestaActual = "";
		String respuestaActualUsuario = "";
		_isValid = true;
		for(int i = 0; i < fields.size(); i++) {
			respuestaActual = myProps.getPreguntas().getItems().get(i).getRespuesta();
			respuestaActualUsuario = fields.get(i).getText();
			if (!respuestaActual.equals(respuestaActualUsuario)) {
				_isValid = false;
			}
		}		
	}
	
}

