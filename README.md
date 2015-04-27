# Applet Firmador Digital de documentos PDF 
El applet firmador de PDF es un desarrollo basado en el [firmador pdf de la ONTI](http://cluster.softwarepublico.gob.ar/redmine/projects/firmador-digital) (el link actualmente se encuentra caido, pero ahí estaba el proyecto originalmente publicado con una licencia libre)

## Integración con Aplicaciones Web
```
<applet  id="AppletFirmador" code="ar/gob/onti/firmador/view/FirmaApplet"  scriptable="true"  archive="http://url/al/firmador.jar"  width="640"	height="480" >
   <param name="URL_DESCARGA"	 	value="http://url/al/documento.pdf" />
   <param name="MULTIPLE"	 		value="false" />
   <param name="URL_SUBIR"			value="http://url/de/upload" />
   <param name="MOTIVO"  			value="Texto del motivo de la firma" />
   <param name="STAMP_WATERMARK"  	value="false" />
   <param name="CODIGO" 			value="token para control ad-hoc de la sesion" />
   <param name="COOKIE"             value="clave=valor (valor session id de PHP)" />
   <param name="codebase_lookup"    value='false' />
   <param name="classloader_cache" 	value="false" />
</applet>
```

Existen tres comunicaciones via javascript:
```

        /** Callback que se llama al terminar la carga del Applet **/
		function appletLoaded() {
		}
		
		/** Callback que se llama al terminar la firma **/
		function firmaOk() {
		}
		
		/** Para el caso de firma múltiple, agrega un documento al lote de firma **/
		document.AppletFirmador.agregarDocumento(id, url_descarga);
		
		/** Para el caso de firma múltiple, quita un documento previamente agregado al lote de firma **/
		document.AppletFirmador.quitarDocumento(id);
```

## Integración con SIU-Toba
En caso de utilizar un proyecto Toba al menos versión 2.4, ya se encuentra implementada la integración con el  componente [ei_firma](https://repositorio.siu.edu.ar/trac/toba/wiki/Referencia/Objetos/ei_firma)

## Integración con PHP en general
Para facilitar la integración con aplicaciones PHP, se incluye una clase que tiene algunas utilidades, 
se llama [firmador_pdf.php](https://github.com/SIU-Toba/firmador/blob/master/sample/www/caso_unico_pdf.php) y se utiliza en la Demo del firmador

## Ejecutando la Demo
* Publicar en apache la carpeta sample/www, por ejemplo de esta forma:
```
  ln -s ~/proyectos/firmador/sample/www /var/www/firmador
  ```
* Abrir ```http://localhost/firmador_pdf```

 






