package ar.gob.onti.firmador.view;


/*
 * PDFSigner.java
 * author: mpin
 * owner : ONTI
 */

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import ar.gob.onti.firmador.model.PropsConfig;

public class DialogSecure extends JDialog {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String prompt;
	private final int columns;
	private final JPasswordField secureField = new JPasswordField();
	

	public static final char[] getInputSecure(Frame parent, String title, boolean modal, String prompt, int columns) 
	{  
	    return new DialogSecure(parent, title, modal, prompt, columns).getInput(); 
	}     



	public DialogSecure(Frame parent, String title, boolean modal, String prompt, int columns) {
	
	    super(parent, title, modal);
 
		this.prompt = prompt;
		this.columns = columns;
 
		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		//setLocationRelativeTo(parent);
		getContentPane().add( buildGui() );
		pack();
		setLocationRelativeTo(null);
		this.setVisible(PropsConfig.getInstance().isVisible());
	}
	
	private  char[] getInput() { return secureField.getPassword(); }
	 
	private JComponent buildGui() {
		JPanel jpanel = new JPanel();
		jpanel.setBorder( BorderFactory.createEmptyBorder(20, 20, 20, 20) );
		jpanel.add( buildSecureEntry() );
		jpanel.add( Box.createVerticalStrut(20) );
		jpanel.add( buildWarning() );
		jpanel.add( Box.createVerticalStrut(20) );
		jpanel.add( buildOKAndCancelButtons() );
		return jpanel;
	}
 
 
	private JComponent buildSecureEntry() {
		secureField.setColumns(columns);
		secureField.setEchoChar('*');	
		JPanel jpanel = new JPanel();
		jpanel.add( new JLabel(prompt) );
		jpanel.add( Box.createHorizontalStrut(10) );
		jpanel.add( secureField );
		return jpanel;
	}
 
 
	private JComponent buildWarning() {
		return new JPanel();
	}
 
 
	private JComponent buildOKAndCancelButtons() {

		JPanel jpanel = new JPanel();
 		JButton okButton = new JButton("OK");
		okButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				DialogSecure.this.dispose();
			}
		} );
		this.getRootPane().setDefaultButton(okButton);
		
		jpanel.add( okButton );
 
		jpanel.add( Box.createHorizontalStrut(20) );
 
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				secureField.setText("");	
				DialogSecure.this.dispose();
			}
		} );
		jpanel.add( cancelButton );
		return jpanel;
	} 

}
