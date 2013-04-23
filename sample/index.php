<?php
require_once(dirname(__FILE__).'/firmador_pdf.php');

$firmador = new firmador_pdf();


//-- Guardar sesion en XML
$firmador->set_guardar_sesion_en_xml();

//-- Guardar sesion en BD
//$nombre_base = "toba_trunk";
//$dbh = new PDO("pgsql:host=localhost;dbname=$nombre_base", "postgres", "");
//$firmador->set_guardar_sesion_en_bd($dbh);

$firmador->ejecutar();

?>