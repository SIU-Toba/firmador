package ar.gob.onti.firmador.view;

import javax.swing.JApplet;

import netscape.javascript.JSObject;
/**
 * Clase encargada de la comunicacion del lenguaje javascripts 
 * con el lenguaje java
 * @author ocaceres
 *
 */
public final class JSCommands
{
   
    
    private static JSObject browserWindow;

    private JSCommands() 
    {     
    }

    public static void init(JApplet owner) 
    {
        	JSCommands.browserWindow = JSObject.getWindow(owner);
    }

   
    
    public static JSObject getWindow()
    {
        return browserWindow;
    }
}
