package cl.tastets.life.auth.dao;

import cl.tastets.life.objects.User;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;


@Repository
public class AuthDAO {

	@Value("${auth.database.collection.authentication}")
	String name;

	@Autowired
	private MongoDatabase db;

	private MongoCollection<Document> coll;

	private UpdateOptions uo;

	@PostConstruct
	public void init(){
		this.coll = db.getCollection(name);
		uo = new UpdateOptions().upsert(true);
	}
        

        //*metodo que usuario segun el token en la bd
	public User getUserByTokenFromDB(String token, String device) {
		User user =new User();
		user.putAll(coll.find(Filters.and(Filters.eq("token", token), Filters.eq("device", device))).first());
		return user;
	}

        //* metodo encargado de guardar el token en la bd 
	public void saveToken(Integer id,String user, String token, String realm ,Integer companyId, String device){
		String _id = new StringBuilder(user).append("_").append(realm).append("@").append(device).toString();
		Bson query = Filters.and(Filters.eq("_id", _id), Filters.eq("device", device));
		Document doc = new Document("_id", _id)
				.append("userId", id).append("username", user).append("companyId", companyId).append("token", token).append("device", device).append("timestamp", new Date()).append("realm", realm);
		coll.updateOne(query, new Document("$set",doc), uo);
	}
}
