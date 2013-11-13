<?php
require_once(dirname(__FILE__).'/firmador_pdf.php');

$firmador = new firmador_pdf();

//---------------------------------
//-- CASO BASE: Generar applet
//---------------------------------
if (! isset($_GET['accion'])) {
	$cant_documentos = 68;
	$sesion = $firmador->generar_sesion();
	$url_actual = $firmador->get_url_base_actual(). $_SERVER['REQUEST_URI'];
	$url_pdf_base = $url_actual."?accion=enviar&codigo=$sesion";
	?>
	<html>
		<head>
			<style type="text/css">
				* {    
					font-family: Verdana, Arial, 'sans-serif' !important; 
					font-size: 12px;
				}
			</style>
		</head>
	<body>
	<div style="width:850px;margin:0 auto;">
	<div>
	<?php
	$firmador->generar_applet_firma_multiple("firmador.jar",  $url_actual."?accion=recibir");
	?>
	</div>
	
	<script type="text/javascript" src="pdfobject.min.js"></script>
	<script type="text/javascript">
		function appletLoaded() {
			document.getElementById("listado").style.display = '';
			document.getElementById("pdf").style.display = '';
		}
		
		function toggleDocumento(source, agregar) {
			var ok = false;
			if (window.console) console.debug((agregar ? "Agregando" : "Quitando") + " " + source.value);
			//try {
				if (agregar) {
					ok = document.AppletFirmador.agregarDocumento(source.id, source.value);
				} else {
					ok = document.AppletFirmador.quitarDocumento(source.id);
				}
				if (window.console) console.debug("Value" + ok);
			/*} catch (e) {
				alert(e.name + " " +  e.message);
				if (window.console) console.error(e);
			}*/
			if (ok === null) {
				alert("Hubo un problema al comunicarse con el Applet firmador, chequee que se haya habilitados los permisos de interconexión.");
				return false;
			} else if (ok == false) {
				source.checked = false;
				return false;
			} else if (ok == true) {
				source.checked = true;
				return true;
			}
		}
		
		function seleccionarTodos(source) {
			checkboxes = document.getElementsByName('documentos');
			var ok = true;
			for(var i=0 ;i < checkboxes.length; i++) {
			  if (! toggleDocumento(checkboxes[i], source.checked)) {
				  ok = false;
				  break;
			  }
			}
			if (ok) {
				for(var i=0 ;i < checkboxes.length; i++) {
				  checkboxes[i].checked = source.checked;
				}
			} else {
				//Restore previous state
				source.checked = ! source.checked;
			}
		}
		function verDocumento(source) {
			var success = new PDFObject(
			{ 
				url: source.value, 	
				pdfOpenParams: { toolbar: "0", statusbar: "0" }
			}).embed("pdf");
		}
	</script>
	
	<div id="listado" style='display: none;float: left; width: 220px; margin-left: 20px; height: 600px;overflow:scroll;'>
	<input id="todos" type="checkbox" onclick="seleccionarTodos(this)" /> <label for="todos">Seleccionar Todos/Ninguno</label><br/><br/>
	<?php 
	for ($i = 1; $i <= $cant_documentos; $i++) {
		echo "<input id='doc$i' name='documentos' type='checkbox' onclick='toggleDocumento(this, this.checked)' value='".$url_pdf_base."&numero=$i' /> 
				<a href='javascript:verDocumento(document.getElementById(\"doc$i\"))'>Documento $i</a><br/>";
	}
	?>
	</div>
	<div id="pdf" style="display: none; float: right; border: 1px solid black; height:800px; width:600px; text-align: center">Haga click en algún documento para visualizarlo</a>.
	</div>
	</div>
	</body>
	</html>
	<?php
	die;
}

//---------------------------------
//-- ENVIAR PDFs
//---------------------------------
if ($_GET['accion'] == 'enviar') {
	if (! isset($_GET['codigo'])) {
		header('HTTP/1.1 500 Internal Server Error');
		die("Falta indicar el codigo");
	}
	if (! $firmador->validar_sesion($_GET['codigo'])) {
		header('HTTP/1.1 501 Internal Server Error');
		die("Codigo invalido");   
	}	
	if (! isset($_GET['numero'])) {
		header('HTTP/1.1 502 Internal Server Error');
		die("Falta indicar que documento enviar");   			
	}
	$numero = (int) $_GET['numero'];
	//Enviar PDF
	$firmador->enviar_headers_pdf();
	$file = dirname(dirname(__FILE__)).'/multiples/'.$numero.'.pdf';
	$fd = fopen($file,'r');
	fpassthru($fd);
	die;
}

//---------------------------------
//-- RECIBIR PDFs
//---------------------------------
if ($_GET['accion'] == 'recibir') {
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
	$destino = dirname(dirname(__FILE__)).'/docFirmado1.pdf';
	if (! move_uploaded_file($path, $destino)) {
		error_log("Error uploading file");
		header('HTTP/1.1 500 Internal Server Error');
		die;
	}

	die;
}

?>