package cl.tastets.life.auth.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import cl.tastets.life.objects.Funcionality;
import cl.tastets.life.objects.utils.QueryFilter;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;

@Repository
public class FuncionalityDAO {

    //referenia a colección en mongo
    @Value("${auth.database.collection.funcionality}")
    String name;
    
    @Autowired
    private MongoDatabase db;

    private MongoCollection<Document> coll;

    private UpdateOptions uo;
    
    @PostConstruct
    public void init() {
        this.coll = db.getCollection(name);
        uo = new UpdateOptions().upsert(true);
    }
    
    /**
     * Metodo para actualizar o guardar una funcionalidad de las aplicaciones
     * @param funcionality mapa que se guardara con los datos de la funcionalidad
     */
    public void updateFuncionality(Funcionality funcionality) {
    	
        Bson query = Filters.and(Filters.eq("name", funcionality.getString("name")), Filters.eq("realm", funcionality.getString("realm")), Filters.eq("device", funcionality.getString("device")));
        funcionality.remove("token");
        Document document = new Document("$set", new Document(funcionality));
        coll.updateOne(query, document, uo);
        
    }
    
    
    /**
     * Metodo para obtener todas las funcionalidades correspondientes al filtro entregado
     * @param filter mapa de filtro que se desea realizar
     * @return
     */
    public List<Funcionality> getFuncionality(QueryFilter filter) {
    	
    	List<Document> documents = new ArrayList<>();
    	/*se crea filtro de consulta con hasmap entregado por cliente*/
    	for (String key : filter.keySet()) {
    	    documents.add(new Document(key, filter.get(key)));
    	}
    	
    	List<Funcionality> funcionalitys = new ArrayList<Funcionality>();
    	coll.find(new Document("$and", documents))
                .projection(Projections.excludeId())
                .forEach(new Consumer<Document>() {
    				@Override
    				public void accept(Document i) {
    					// pasar de Document a AggregatedData
    					Funcionality de = new Funcionality();
    					de.putAll(i);
    					// agregar a la lista
    					funcionalitys.add(de);
    				}
    		    });
    	
        return funcionalitys;
    }
}
