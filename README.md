# Applet Firmador Digital de documentos PDF 
El applet firmador de PDF es un desarrollo basado en el [firmador pdf de la ONTI](http://cluster.softwarepublico.gob.ar/redmine/projects/firmador-digital) (el link actualmente se encuentra caido, pero ahí estaba el proyecto originalmente publicado con una licencia libre)

## Integración con Aplicaciones Web

* Publicar `firmador.jar`
* Incluir el siguiente código HTML, definiendo los valores:

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

#### Integración con SIU-Toba
En caso de utilizar un proyecto Toba al menos versión 2.4, ya se encuentra implementada la integración con el  componente [ei_firma](https://repositorio.siu.edu.ar/trac/toba/wiki/Referencia/Objetos/ei_firma)

#### Integración con PHP en general
Para facilitar la integración con aplicaciones PHP, se incluye una clase que tiene algunas utilidades, 
se llama [firmador_pdf.php](https://github.com/SIU-Toba/firmador/blob/master/sample/www/caso_unico_pdf.php) y se utiliza en la Demo del firmador

## Ejecutando la Demo
* Publicar en apache la carpeta sample/www, por ejemplo de esta forma:
```
  ln -s ~/proyectos/firmador/sample/www /var/www/firmador
```
* Abrir en navegador http://localhost/firmador_pdf

## Compilación del Applet
#### Requisitos previos
Las compilaciones se realizaron utilizando las siguientes versiones exactas, probablemente no funcione con versiones más nuevas:
* [JAVA JDK1.6.0.33 x86](http://www.oracle.com/technetwork/java/javase/archive-139210.html)
* [Maven 3.0.4](http://maven.apache.org/download.html)
* [iText 5.0.2](http://olex.openlogic.com/packages/itext/5.0.2)
* Instalar manualmente iText 5.0.2 en el repositorio local de Maven
```
"[Carpeta de Maven]\mvn" install:install-file -Dfile=[Carpeta del IText]\iText-5.0.2.jar -DgroupId=com.lowagie -DartifactId=itext -Dversion=5.0.2 -Dpackaging=jar
```
* Instalar manualmente la libreria de plugin de la implementacion de JDK
```
"[Carpeta de Maven]\mvn" install:install-file -Dfile="[Carpeta de JDK]\jre\lib\plugin.jar" -DgroupId=plugin -DartifactId=plugin -Dversion=1.5 -Dpackaging=jar
```

#### Build
* Recordar setear correctamente el JAVA_HOME para apuntar a la 1.6. Ejemplo:
```
export JAVA_HOME=/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
```
* Por unica vez generar el keystore para autofirmar el mismo applet
```
keytool -genkey -keyalg rsa -alias key -keystore keystore.jks
#Por ej usar ''password'' como contraseña
```
* Configurar `app/resource/properties/firma.properties` , agregando o quitando CNs y certificados de confianza
```
	...
	IssuerCN4=COMODO Client Authentication and Secure Email CA
	...
	trustedCertificates1=COMODO Client Authentication and Secure
	...
```
* En cada sesión de compilación, ejecutar
```
	export FIRMADOR_STORE=keystore.jks
	export FIRMADOR_ALIAS=key
	export FIRMADOR_STOREPASS=password
	export FIRMADOR_KEYPASS=password
```
* Compilar en modo 'producción' (sin eso no va a permitir realizar la firma en sí). Tambien se evita correr los tests (que no andan)
```
	mvn -q compile install -DFIRMADOR_PROD -DskipTests=true
```
