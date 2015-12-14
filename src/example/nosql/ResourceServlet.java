package example.nosql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.cloudant.client.api.Database;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

@Path("/favorites")
/**
 * CRUD service of todo list table. It uses REST style.
 */
public class ResourceServlet {

	public ResourceServlet() {
	}



	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@QueryParam("id") Long id, @QueryParam("cmd") String cmd) throws Exception {

		Database db = null;
		try {
			db = getDB();
		} catch (Exception re) {
			re.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		JsonObject resultObject = new JsonObject();
		JsonArray jsonArray = new JsonArray();

		if (id == null) {
			try {
				// get all the document present in database
				System.out.println("infoooo"+db.getDBUri());
				System.out.println("infoooo"+db.info().getDbName());
	
				List<HashMap> allDocs = db.getAllDocsRequestBuilder().includeDocs(true).build().getResponse()
						.getDocsAs(HashMap.class);
				
				System.out.println("numnero"+allDocs.size());
				for (HashMap doc : allDocs) {
					Gson gson = new Gson();
					HashMap<String, Object> obj = db.find(HashMap.class, doc.get("_id") + "");
					System.out.println("infoooooooo0"+doc.toString());
					System.out.println("infoooooooo"+obj.toString());
					
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("identificador", obj.get("identificador") + "");
					jsonObject.addProperty("resumenIncidencia", obj.get("resumenIncidencia") + "");
					jsonObject.addProperty("textoDescriptivo", obj.get("textoDescriptivo") + "");
					jsonObject.addProperty("criticidad", obj.get("criticidad") + "");
					jsonObject.addProperty("usuario", obj.get("usuario") + "");					
					jsonObject.addProperty("fechaReporte",  obj.get("fechaReporte")+"");
					
					jsonObject.addProperty("operacion",  gson.toJson(obj.get("operacion")).replace("\\", ""));					 
				
					List<Object> prueb=(List<Object>) obj.get("operacion");
					jsonObject.addProperty("resumenEstado", ((LinkedTreeMap<String, Object>)prueb.get(prueb.size()-1)).get("estado")+"");				
				
		
					jsonArray.add(jsonObject);
				}
				if(allDocs.isEmpty()){
	
				}
			} catch (Exception dnfe) {
				System.out.println("Exception thrown : " + dnfe.getMessage());
			}

			resultObject.addProperty("id", "all");
			resultObject.add("body", jsonArray);

			return Response.ok(resultObject.toString()).build();
		}else{
			Gson gson = new Gson();
			return Response.ok(gson.toJson(pruebasUnitariasBackEnd())).build();
		}


	}

	private HashMap<String,Object> pruebasUnitariasBackEnd(){
		
		HashMap<String,Object> map=new HashMap<String,Object> ();
		
		Database db = null;
		
		try {
			db = getDB();
			map.put("conexionBD", "ok");
		} catch (Exception re) {
			map.put("conexionBD", "fallido"+re.toString());
		}
		
		List<HashMap> allDocs = null;
		try {
			allDocs = db.getAllDocsRequestBuilder().includeDocs(true).build().getResponse()
					.getDocsAs(HashMap.class);
			map.put("recuperacionDocsBd", "ok");
			
			
			if(!allDocs.isEmpty()){
				List<String> jsonArray = new ArrayList<String> ();
				for (HashMap doc : allDocs) {
					HashMap<String, Object> obj=db.find(HashMap.class, doc.get("_id") + "");									
					jsonArray.add(comprobarEstructuraDocumentoBd(obj,doc.get("_id").toString()));
	
				}
				map.put("info", jsonArray);
			}
			
		} catch (IOException e) {
			map.put("recuperacionDocsBd", "fallido"+e.toString());
		}

		
		return map;
		
	}

	public String comprobarEstructuraDocumentoBd(HashMap<String, Object> obj,String id){
		if(obj.get("resumenIncidencia")!=null&&obj.get("textoDescriptivo")!=null
				&&obj.get("criticidad")!=null&&obj.get("usuario")!=null&&obj.get("fechaReporte")!=null
				&&(obj.get("operacion")!=null&&obj.get("operacion") instanceof List))
			return id+"-->ok";
		else
			return id+"-->err";
		
	}
	


	private Database getDB() {
		return CloudantClientMgr.getDB();
	}

}
