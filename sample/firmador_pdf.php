<?php

class firmador_pdf
{
    protected $usar_xml = false;
    protected $db = null;
    
   function ejecutar() {
       $accion = isset($_GET['accion']) ? $_GET['accion'] : "applet";
       switch ($accion) {
           case 'descargar':
               $this->descargar_pdf();
               break;
                       
           case 'subir':
               $this->subir_pdf();
               break;
           
           case 'applet':
               $this->generar_applet();
               break;
       }
   }
   
   function set_guardar_sesion_en_db($conexion)
   {
       $this->db = $conexion;
   }
   
   function set_guardar_sesion_en_xml()
   {
       $this->db = null;
   }
   
   
   protected function descargar_pdf()
   {
        if (! isset($_GET['codigo'])) {
            header('HTTP/1.1 500 Internal Server Error');
            die("Falta indicar el codigo");
        }
        if (! $this->validar_sesion($_GET['codigo'])) {
            header('HTTP/1.1 500 Internal Server Error');
            die("Codigo invalido");   
        }
        
        header("Cache-Control: private");
        header("Content-type: application/pdf");
        header("Pragma: no-cache");
        header("Expires: 0");

        $file = dirname(__FILE__).'/docOriginal.pdf';
        $fd = fopen($file,'r');
        fpassthru($fd);
    }
    
    protected function subir_pdf()
    {
        if (! isset($_POST['codigo'])) {
            header('HTTP/1.1 500 Internal Server Error');
            die("Falta indicar el codigo");
        }
        if ( ! $this->validar_sesion($_POST['codigo'])) {
            header('HTTP/1.1 500 Internal Server Error');
            die("Codigo invalido");   
        }
        
        //---- DEBUG
//        unlink("/tmp/subida");
//        file_put_contents("/tmp/subida", "Get: ".var_export($_GET, true)."\n", FILE_APPEND);
//        file_put_contents("/tmp/subida", "Post: ".var_export($_POST, true)."\n", FILE_APPEND);
//        file_put_contents("/tmp/subida", "Files: ".var_export($_FILES, true)."\n", FILE_APPEND);

        $destino = dirname(__FILE__).'/docFirmado.pdf';
        if (! move_uploaded_file($_FILES['md5_fileSigned']['tmp_name'], $destino)) {
            error_log("Error uploading file");
            header('HTTP/1.1 500 Internal Server Error');
            die;
        }
    }
    
    protected function generar_applet()
    {
        $url_actual = $this->get_url_actual();
        $sesion = $this->generar_sesion();
?>
        <html>
        <body>
        <applet  code="ar/gob/onti/firmador/view/FirmaApplet" 	 
           archive="firmador.jar"  width="700"	height="410" >
         <param  name="URL_DESCARGA"	 value="<?php echo $url_actual; ?>?accion=descargar" >
         <param  name="URL_SUBIR"	value="<?php echo $url_actual; ?>?accion=subir">
         <param  name="MOTIVO"  value="Insertar motivo de la firma">
         <param  name="CODIGO"  value="<?php echo $sesion; ?>" />
         <param name="PREGUNTAS" value='{ "preguntasRespuestas": []}' />
        </applet>
        </body>
        </html>
<?php
     }
     

   protected function get_url_actual() 
   {
        $s = empty($_SERVER["HTTPS"]) ? '' : ($_SERVER["HTTPS"] == "on") ? "s" : "";
        $sp = strtolower($_SERVER["SERVER_PROTOCOL"]);
        $protocol = substr($sp, 0, strpos($sp, "/")) . $s;
        $port = ($_SERVER["SERVER_PORT"] == "80") ? "" : (":" . $_SERVER["SERVER_PORT"]);
        $url_actual = $protocol . "://" . $_SERVER['SERVER_NAME'] . $port . $_SERVER['REQUEST_URI'];
        //$url_base = substr($url_actual, 0, (strlen($url_actual)-1) - strlen(basename($_SERVER['REQUEST_URI'])));
        return $url_actual;
   }
   
   protected function generar_sesion()
   {
        //Agregar un token a un archivo xml de sesiones (para evitar usar una BD en el ejemplo)
        $sesion = hash('sha256', uniqid(mt_rand(), true));
        
        if ($this->db == null) {
            $archivo_sesiones = dirname(__FILE__).'/sesiones.xml';
            if (file_exists($archivo_sesiones)) {
                $xml = simplexml_load_file('sesiones.xml');
            } else {
                $string = <<<XML
<?xml version='1.0'?> 
<sesiones>
</sesiones>
XML;
                $xml  = simplexml_load_string($string);
            }
            $xml->addChild("sesion", $sesion);
            if (!file_put_contents($archivo_sesiones, $xml->asXML())) {
                die("El usuario apache debe tener permisos sobre esta carpeta");
            }
         } else {
            //BD
         }
        return $sesion;
     }
     
     function validar_sesion($sesion)
     {
        if ($this->db == null) {
            $archivo_sesiones = dirname(__FILE__).'/sesiones.xml';
            if (file_exists($archivo_sesiones)) {
                $xml = simplexml_load_file('sesiones.xml');
            } else {
                die("No hay sesiones registradas");
            }
            foreach ($xml->children() as $valid) {
                if ($sesion == (string) $valid) {
                    return true;
                }
            }
            return false;
        } else {
            //BD
        }
     }
     
}

?>
