<?php

class firmador_pdf
{

    protected $db = null;
    protected $sesion;
    
  
   function set_guardar_sesion_en_db($conexion)
   {
       $this->db = $conexion;
   }
   
   function set_guardar_sesion_en_xml()
   {
       $this->db = null;
   }
   
   
   function enviar_headers_pdf()
   {
        header("Cache-Control: private");
        header("Content-type: application/pdf");
        header("Pragma: no-cache");
        header("Expires: 0");
    }
    
    function generar_applet()
    {
        $url_actual = $this->get_url_base_actual(). $_SERVER['REQUEST_URI'];
        $sesion = $this->generar_sesion();
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
     }
     

   function get_url_base_actual() 
   {
        $s = empty($_SERVER["HTTPS"]) ? '' : ($_SERVER["HTTPS"] == "on") ? "s" : "";
        $sp = strtolower($_SERVER["SERVER_PROTOCOL"]);
        $protocol = substr($sp, 0, strpos($sp, "/")) . $s;
        $port = ($_SERVER["SERVER_PORT"] == "80") ? "" : (":" . $_SERVER["SERVER_PORT"]);
        return $protocol . "://" . $_SERVER['SERVER_NAME'] . $port;
   }
   
   
   //-----------------------------------------
   //--- MANEJO DE SESION
   //-----------------------------------------
   
   function generar_sesion()
   {
	   if (! isset($this->sesion)) {
			//Agregar un token a un archivo xml de sesiones (para evitar usar una BD en el ejemplo)
			$this->sesion = hash('sha256', uniqid(mt_rand(), true));

			if ($this->db == null) {
				$archivo_sesiones = dirname(dirname(__FILE__)).'/sesiones.xml';
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
				$xml->addChild("sesion", $this->sesion);
				if (!file_put_contents($archivo_sesiones, $xml->asXML())) {
					die("El usuario apache debe tener permisos sobre esta carpeta");
				}
			 } else {
				//BD
				 $this->generar_tabla();

				//Insertar nueva sesión
				$sql = "INSERT INTO firmador_pdf_sesion (sesion) VALUES (".$this->db->quote($this->sesion).")";
				$this->db->exec($sql);
			 }
		}
        return $this->sesion;
     }
     
     function validar_sesion($sesion)
     {
        if ($this->db == null) {
            $archivo_sesiones = dirname(dirname(__FILE__)).'/sesiones.xml';
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
            $this->generar_tabla();
            
            //Borrar las sesiones expiradas
            $sql = "DELETE FROM firmador_pdf_sesion WHERE creada < now() - interval '1 hour'";
            $this->db->exec($sql);
            
            //Chequear esta sesion
            $sql = "SELECT count(*) from firmador_pdf_sesion where sesion = ".$this->db->quote($sesion);
            $rs = $this->db->query($sql);
            return !empty($rs);
        }
     }
     
     function generar_tabla()
     {
         $sql = "SELECT count(*) as cantidad from pg_tables where tablename = 'firmador_pdf_sesion'
			 and schemaname = current_schema();";
         $fila = $this->db->query($sql)->fetch();
         if ($fila['cantidad'] == 0) {
             $sql = "CREATE TABLE firmador_pdf_sesion
                 (
                      sesion varchar NOT NULL,
                      creada timestamp DEFAULT now(),
                      CONSTRAINT sesion_firma_pdf_pkey PRIMARY KEY (sesion)
                 )
            ";
             $this->db->exec($sql);
         }

     }
     
}

?>
