<?php
require_once(dirname(__FILE__).'/firmador_pdf.php');

$firmador = new firmador_pdf();

//---------------------------------
//-- CASO BASE: Generar applet
//---------------------------------
if (! isset($_GET['accion'])) {
	$cant_documentos = 1200;
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
		<h2>Ejemplo de Firma Múltiple</h2>
		<div style="font-size: 10px; margin: 10px;">Este ejemplo permite seleccionar una serie de documentos pdfs pertenecientes a la carpeta <em>"multiples"</em>. A medida que se clickean los checkboxes se comunica
			via javascript con el applet, enviandoles el ID y URL de cada documento. Al presionar <em>"Firmar Documentos"</em> el Applet descarga los documentos hacia el client-side (una carpeta temporal), accede al token-usb (ingresando el PIN una única vez)
			y firma uno a uno los documentos seleccionados. Al finalizar hace un POST por cada documento hacia este mismo .php que almacena los documentos en <em>"multiples_firmados"</em>.
			<br/><br/>
			Para que este ejemplo funcione tiene que dar permisos de escritura al usuario apache (generalmente www-data) sobre la carpeta <em>"multiples_firmados"</em>
		</div>
		<hr/>
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
		function firmaOk() {
			document.getElementById("listado").style.display = 'none';
			document.getElementById("pdf").style.display = 'none';
		}
		
		function toggleDocumento(source, agregar) {
			var ok = false;
			if (agregar) {
				ok = document.AppletFirmador.agregarDocumento(source.id, source.value);
			} else {
				ok = document.AppletFirmador.quitarDocumento(source.id);
			}
			if (ok === null) {
				alert("Hubo un problema al comunicarse con el Applet firmador, chequee que se haya habilitados los permisos de interconexión.");
				return false;
			} else if (ok == false) {
				source.checked = ! agregar;
				return false;
			} else if (ok == true) {
				source.checked = agregar;
				return false;
			}
		}
		
		function seleccionarTodos(source) {
			checkboxes = document.getElementsByName('documentos');
			for (var i=0 ;i < checkboxes.length; i++) {
			  toggleDocumento(checkboxes[i], source.checked);
			  checkboxes[i].checked = source.checked;
			}
		}
		
		function verDocumento(source) {
                        document.getElementById("pdf").data = source.value;
			/*PDFObject.embed(source.value, "#pdf", {
                                pdfOpenParams: { scrollbar: '0', toolbar: '0', statusbar: '0', messages: '1' }
                          });*/
		}
	</script>
        <div style="margin-top: 40px; color: gray">Haga click en los documentos para visualizarlos.</div>	
	<div id="listado" style='float: left; width: 220px; margin-left: 20px; height: 600px;overflow:scroll;'>
	<input id="todos" type="checkbox" onclick="seleccionarTodos(this)" /> <label for="todos">Seleccionar Todos/Ninguno</label><br/><br/>
	<?php 
	for ($i = 1; $i <= $cant_documentos; $i++) {
		echo "<input id='$i' name='documentos' type='checkbox' onclick='toggleDocumento(this, this.checked)' value='".$url_pdf_base."&id=$i' /> 
				<a href='javascript:verDocumento(document.getElementById(\"$i\"))'>Documento $i</a><br/>";
	}
	?>
	</div>
	<object id="pdf" style=" float: right; border: 1px solid black; height:800px; width:600px; text-align: center" type="application/pdf" data=""> </object> 

	</div>
	</div>
	<button onclick="javascript:location.href='visualizar.php'"> Ver Firmados </button>
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
	if (! isset($_GET['id'])) {
		header('HTTP/1.1 502 Internal Server Error');
		die("Falta indicar que documento enviar");   			
	}
	$numero = (int) $_GET['id'];
	//Enviar PDF
	$firmador->enviar_headers_pdf();
	$file = dirname(dirname(__FILE__)).'/multiples/doc_'.$numero.'.pdf';
	$fd = fopen($file,'r');
	fpassthru($fd);
	die;
}

//---------------------------------
//-- RECIBIR PDFs
//---------------------------------
if ($_GET['accion'] == 'recibir') {
	file_put_contents("/tmp/firmador", print_r($_POST, true), FILE_APPEND);
	if (! isset($_POST['codigo'])) {
		header('HTTP/1.1 500 Internal Server Error');
		die("Falta indicar el codigo");
	}
	if (! isset($_POST['id'])) {
		header('HTTP/1.1 500 Internal Server Error');
		die("Falta indicar el ID");
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
	$id = (int) $_POST['id'];
	$destino = dirname(dirname(__FILE__)).'/multiples_firmados/doc_'.$id.'.pdf';
	if (file_exists($destino)) {
            unlink($destino);
	}
	
	if (! move_uploaded_file($path, $destino)) {
		error_log("Error uploading file");
		header('HTTP/1.1 500 Internal Server Error');
		die;
	}

	die;
}

?>
