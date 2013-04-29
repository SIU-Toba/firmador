<?php
require_once(dirname(__FILE__).'/firmador_pdf.php');

    class ejemplo_firmador implements firmador_pdf_acciones
    {
        function get_stream_pdf_sin_firmar()
        {
            $file = dirname(__FILE__).'/docOriginal.pdf';
            $fd = fopen($file,'r');
            fpassthru($fd);
        }
        
        function set_pdf_firmado($path)
        {
            $destino = dirname(__FILE__).'/docFirmado.pdf';
            if (! move_uploaded_file($path, $destino)) {
                error_log("Error uploading file");
                header('HTTP/1.1 500 Internal Server Error');
                die;
            }
        }
        
    }


$firmador = new firmador_pdf(new ejemplo_firmador());

//-- Guardar sesion en XML
//$firmador->set_guardar_sesion_en_xml();

//-- Guardar sesion en BD
$nombre_base = "toba_trunk";
$dbh = new PDO("pgsql:host=localhost;dbname=$nombre_base", "postgres", "");
$dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION); // Set Errorhandling to Exception
$firmador->set_guardar_sesion_en_db($dbh);

$firmador->ejecutar();

?>