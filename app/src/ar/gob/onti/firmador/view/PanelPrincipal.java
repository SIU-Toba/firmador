package ar.gob.onti.firmador.view;

import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
/**
 * Clase que hereda de Jpanel utilizada como imagen de fondo del Applet
 * @author ocaceres
 *
 */
public class PanelPrincipal extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PanelPrincipal() {
		super();
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		/*ClassLoader cl = this.getClass().getClassLoader();
		ImageIcon imagen =  new ImageIcon(cl.getResource("images/fondo.jpg"));
		g.drawImage(imagen.getImage(),0,0,1000,300,null);
		setOpaque(false);*/
		super.paintComponent(g);
    }

}
