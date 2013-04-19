package ar.gob.onti.firmador.model;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class PreguntasRespuestas {

	private ArrayList<PreguntaRespuesta> _items;
	
	public ArrayList<PreguntaRespuesta> getItems() {
		return _items;
	}
	
	public void setItems(ArrayList<PreguntaRespuesta> items) {
		_items = items;
	}

	public boolean parse(String json) {
		_items = new ArrayList<PreguntaRespuesta>();
		ArrayList<PreguntaRespuesta> results = new ArrayList<PreguntaRespuesta>();
		
		try {
			JSONObject rootObj = (JSONObject) JSONValue.parse(json);
			JSONArray preguntasRespuestas = (JSONArray) rootObj.get("preguntasRespuestas");
			for (int i = 0; i < preguntasRespuestas.size(); i++) {
				JSONObject item = (JSONObject) preguntasRespuestas.get(i);
				PreguntaRespuesta preguntaRespuesta = new PreguntaRespuesta();
				preguntaRespuesta.setPregunta((String) item.get("pregunta")); 
				preguntaRespuesta.setRespuesta((String) item.get("respuesta")); 
				results.add(preguntaRespuesta);
			}
		} catch (Exception e) {
			System.out.println("Error en el parseo de las preguntas: " + e.getMessage());
			return false;
		}
		
		_items = results;
		return true;
	}	

	
}
