<?php
require_once(dirname(__FILE__).'/firmador_pdf.php');

$firmador = new firmador_pdf();

//-- Guardar sesion en XML
//$firmador->set_guardar_sesion_en_xml();

//-- Guardar sesion en BD
$nombre_base = "toba_trunk";
$dbh = new PDO("pgsql:host=localhost;dbname=$nombre_base", "postgres", "");
$dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION); // Set Errorhandling to Exception
$firmador->set_guardar_sesion_en_db($dbh);


if (! isset($_GET['accion'])) {
        $url_actual = $firmador->get_url_base_actual(). $_SERVER['REQUEST_URI'];
        $sesion = $firmador->generar_sesion();
?>
        <applet  code="ar/gob/onti/firmador/view/FirmaApplet" 	 
           archive="firmador.jar"  width="700"	height="310" >
         <param  name="URL_DESCARGA"	 value="<?php echo $url_actual; ?>?accion=descargar" >
         <param  name="URL_SUBIR"	value="<?php echo $url_actual; ?>?accion=subir">
         <param  name="MOTIVO"  value="Insertar motivo de la firma">
         <param  name="CODIGO"  value="<?php echo $sesion; ?>" />
         <param name="PREGUNTAS" value='{ "preguntasRespuestas": []}' />
        </applet>
<?php
     die;
}

//-- DESCARGAR
if ($_GET['accion'] == 'descargar') {
	if (! isset($_GET['codigo'])) {
		header('HTTP/1.1 500 Internal Server Error');
		die("Falta indicar el codigo");
	}
	if (! $firmador->validar_sesion($_GET['codigo'])) {
		header('HTTP/1.1 500 Internal Server Error');
		die("Codigo invalido");   
	}	
	//Enviar PDF
	$firmador->enviar_headers_pdf();
        
        $file = dirname(dirname(__FILE__)).'/docOriginal.pdf';
        $fd = fopen($file,'r');
        fpassthru($fd);
	die;
}

//-- SUBIR
if ($_GET['accion'] == 'subir') {
	if (! isset($_POST['codigo'])) {
		header('HTTP/1.1 500 Internal Server Error');
		die("Falta indicar el codigo");
	}
	if ( ! $firmador->validar_sesion($_POST['codigo'])) {
		header('HTTP/1.1 500 Internal Server Error');
		die("Codigo invalido");   
	}
	if ($_FILES["md5_fileSigned"]["error"] != UPLOAD_ERR_OK) {
		error_log("Error uploading file");
		header('HTTP/1.1 500 Internal Server Error');
		die;
	}	
	$path = $_FILES['md5_fileSigned']['tmp_name'];
	$destino = dirname(dirname(__FILE__)).'/docFirmado.pdf';
	if (! move_uploaded_file($path, $destino)) {
		error_log("Error uploading file");
		header('HTTP/1.1 500 Internal Server Error');
		die;
	}

	die;
}

?>