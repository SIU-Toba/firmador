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

$firmador->ejecutar();

?>