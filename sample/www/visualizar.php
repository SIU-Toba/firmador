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
		<div style="font-size: 10px; margin: 10px;">Este ejemplo permite visualizar una serie de documentos pdfs firmados en la carpeta <em>"multiples"</em>. 
		</div>
		<hr/>
	<div style="width:850px;margin:0 auto;">
	<script type="text/javascript" src="pdfobject.min.js"></script>
	<script type="text/javascript">				
		function toggleDocumento(source, agregar) {
			var ok = false;
                        if (ok == false) {
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
		}
	</script>
        <div style="margin-top: 40px; color: gray">Haga click en los documentos para visualizarlos.</div>	
	<div id="listado" style='float: left; width: 220px; margin-left: 20px; height: 600px;overflow:scroll;'>
	<input id="todos" type="checkbox" onclick="seleccionarTodos(this)" /> <label for="todos">Seleccionar Todos/Ninguno</label><br/><br/>
	<?php 
	for ($i = 1; $i <= $cant_documentos; $i++) {
		echo "<input id='$i' name='documentos' type='checkbox' onclick='toggleDocumento(this, this.checked)' value='".$url_pdf_base."&id=$i' /> 
				<a href='javascript:verDocumento(document.getElementById(\"$i\"))'>Ver docum. $i</a> | &nbsp; 
				<a href='".$url_pdf_base."&id=$i' target='_blank'>Bajar</a><br/>";
	}
	?>
	</div>
	<object id="pdf" style=" float: right; border: 1px solid black; height:800px; width:600px; text-align: center" type="application/pdf" data=""> </object> 

	</div>
	<div>
	<button onclick="javascript: location.href='caso_multiples_pdfs.php'"> Volver a Multiples </button>
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
	$file = dirname(dirname(__FILE__)).'/multiples_firmados/doc_'.$numero.'.pdf';
	$fd = fopen($file,'r');
	fpassthru($fd);
	die;
}

?>
