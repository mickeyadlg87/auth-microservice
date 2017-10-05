package cl.tastets.life.auth.dao;

import cl.tastets.life.objects.Profile;
import cl.tastets.life.objects.RealmEnum;
import cl.tastets.life.objects.utils.Paginated;
import cl.tastets.life.objects.utils.QueryFilter;
import cl.tastets.life.objects.utils.RequestData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Repository
public class ProfileDAO {

	//referenia a colección en mongo
	@Value("${auth.database.collection.profile}")
	String name;

	@Autowired
	private MongoDatabase db;

	@Autowired
	private RestTemplate restTempl;

	private MongoCollection<Document> coll;

	private UpdateOptions uo;

	private ObjectMapper mapperForJson;

	private Logger log = LoggerFactory.getLogger(ProfileDAO.class);

	@PostConstruct
	public void init() {
		this.coll = db.getCollection(name);
		uo = new UpdateOptions().upsert(true);
		mapperForJson = new ObjectMapper();
		restTempl.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
	}

	/**
	 * metodo que ejecuta query en mongodb para obtener perfiles según plataforma y usuario.
	 *
	 * @param realm identificador de la plataforma
	 * @param user nombre de usuario
	 * @return Perfiles
	 */
	public List<Profile> getProfiles(String realm, String user, String device) {
		List<Profile> profiles = new ArrayList<>();
		coll.find(Filters.and(Filters.eq("realm", realm), Filters.eq("user", user), Filters.eq("device", device)))
				.projection(Projections.excludeId()).forEach(new Consumer<Document>() {
					@Override
					public void accept(Document i) {
						// pasar de Document a AggregatedData
						Profile de = new Profile();
						de.putAll(i);
						// agregar a la lista
						profiles.add(de);
					}
				});
		return profiles;
	}

	/**
	 * metodo que ejecuta query en mongodb para obtener perfiles según un filtro entregado por el usuario
	 *
	 * @param realm identificador de la plataforma
	 * @param user nombre de usuario
	 * @return Perfiles
	 */
	public List<Profile> getAllPerfilByFilter(QueryFilter filter) {

		List<Document> documents = new ArrayList<>();
		/*se crea filtro de consulta con hasmap entregado por cliente*/
		for (String key : filter.keySet()) {
			documents.add(new Document(key, filter.get(key)));
		}

		List<Profile> profiles = new ArrayList<>();
		coll.find(new Document("$and", documents))
				.projection(Projections.excludeId())
				.forEach(new Consumer<Document>() {
					@Override
					public void accept(Document i) {
						// pasar de Document a AggregatedData
						Profile de = new Profile();
						de.putAll(i);
						// agregar a la lista
						profiles.add(de);
					}
				});

		return profiles;

	}

	/**
	 * metodo que ejecuta update en mongodb para obtener actualizar/insertar perfiles según plataforma y usuario.
	 * agregando estos.
	 *
	 * @param profile Objeto Profile que contiene el nombre del perfil en el atributo "name"
	 * @param realm identificador de la plataforma
	 * @param user nombre de usuario
	 * @param device
	 */
	public void updateProfile(Profile profile, String realm, String user, String device) {
		profile.put("realm", realm);
		profile.put("device", device);
		Bson query = Filters.and(Filters.eq("title", profile.getString("title")), Filters.eq("realm", realm), Filters.eq("user", user), Filters.eq("device", device));
		Document document = new Document("$set", new Document(profile));
		coll.updateOne(query, document, uo);
	}

	/**
	 * Metodo para actualizar un perfil segun
	 *
	 * @param filter
	 * @param profile
	 */
	public void updateProfileByFilter(HashMap<String, Object> filter, HashMap<String, Object> profile) {
		List<Document> documents = new ArrayList<>();
		/*se crea filtro de consulta con hasmap entregado por cliente*/
		for (String key : filter.keySet()) {
			documents.add(new Document(key, filter.get(key)));
		}

		Document document = new Document("$set", new Document(profile));

		coll.updateOne(new Document("$and", documents), document, uo);

	}

	/**
	 * agrega childs al profile
	 * @param childList
	 * @param realm
	 * @param title
	 * @param device
	 */
	public void updateChildProfile(List<Profile> childList, String realm, String title, String device) {

		List<String> titlesNewOptions = new ArrayList<>();
		List<Document> childDocuments = new ArrayList<>();

		for (Profile child : childList) {
			titlesNewOptions.add(child.getString("title"));
			Document docChild = new Document(child);
			childDocuments.add(docChild);
		}

		// Esta es la query para encontrar los documentos (secciones de perfil) deseados
		Bson query = Filters.and(Filters.eq("title", title), Filters.eq("realm", realm), Filters.eq("device", device), Filters.nin("childs.title", titlesNewOptions));
		// Mediante $push se agregan child al arreglo childs de cada perfil, se ordenan a traves del indice
		Bson addToChild = Updates.pushEach("childs", childDocuments, new PushOptions().sortDocument(Sorts.ascending("index")));
		// Document doc = new Document("$addToSet", new Document("childs", new Document("$each", childDocuments).put("$sort", new Document("index", 1))));
		coll.updateMany(query, addToChild);
		// Para actualizar los paquetes funcionales en el backoffice
		changeFunPackageBackoffice(realm, device);

	}


	/**
	 * elimina childs del profile
	 * @param childList
	 * @param realm
	 * @param title
	 * @param device
	 */
	public void deleteChildFromProfile(List<Profile> childList, String realm, String title, String device) {

		List<Document> childDocuments = new ArrayList<>();

		// Esta es la query para encontrar los documentos (secciones de perfil) deseados
		Bson query = Filters.and(Filters.eq("title", title), Filters.eq("realm", realm), Filters.eq("device", device));

		// Mediante $pull se eliminan uno a uno cada opcion del arreglo "childs" de cada perfil
		for (Profile childToRemove : childList) {
			Document docChild = new Document(childToRemove);
			Bson removeChildFromProf = Updates.pull("childs", docChild);
			coll.updateMany(query, removeChildFromProf);
		}
		// Para actualizar los paquetes funcionales en el backoffice
		changeFunPackageBackoffice(realm, device);

	}

	public void deleteAllProfilesByFilter(Map<String, Object> filter) {
		List<Document> documents = new ArrayList<>();
		/*se crea filtro de consulta con hasmap entregado por cliente*/
		for (String key : filter.keySet()) {
			documents.add(new Document(key, filter.get(key)));
		}

		coll.deleteMany(new Document("$and", documents));

	}
	
	/**
	 * Metodo para eliminar un profile de la base de datos segun un filtro entregado
	 *
	 * @param filter
	 */
	public void deleteProfileByFilter(Map<String, Object> filter) {
		List<Document> documents = new ArrayList<>();
		/*se crea filtro de consulta con hasmap entregado por cliente*/
		for (String key : filter.keySet()) {
			documents.add(new Document(key, filter.get(key)));
		}

		coll.deleteOne(new Document("$and", documents));

	}

	/**
	 * Ejecuta el proceso de actualizacion y carga de las modificaciones en los paquetes funcionales
	 * @param realm
	 * @param device
	 */
	private void changeFunPackageBackoffice(String realm, String device) {
		// Solo aplica para rslite
		if (!realm.equalsIgnoreCase(RealmEnum.rslite.toString())) {
			return;
		}
		// Se obtienen los paquetes funcionales de backoffice, se actualizan de acuerdo a los child
		// agregados, se deben actualizar cada uno de los paquetes de acuerdo al plan (estandar, intermedio, avanzado)
		getActiveFunctionalPackage(RealmEnum.backoffice.toString()).stream().map((Map funpack) -> {
			funpack.put("realm", RealmEnum.backoffice.toString());
			funpack.remove("profile");
			return funpack;
		}).forEach((Map upFunPack) -> {
			String tipoPack = (String) upFunPack.get("name");
			switch (tipoPack) {
				case "estandar":
					updateFunctionalPackageBackoffice(realm, "perfil-plan-estandar", device, upFunPack);
					break;
				case "intermedio":
					updateFunctionalPackageBackoffice(realm, "perfil-plan-intermedio", device, upFunPack);
					break;
				case "avanzado":
					updateFunctionalPackageBackoffice(realm, "perfil-plan-avanzado", device, upFunPack);
					break;
			}
		});

	}

	/**
	 * Actualiza en backoffice el paquete funcional asociado al usuario proporcionado
	 * @param realm
	 * @param user
	 * @param device
	 * @param functionalPack
	 */
	private void updateFunctionalPackageBackoffice(String realm, String user, String device, Map functionalPack) {

		List<Profile> baseProfile = getProfiles(realm, user, device);

		try {
			String profileJson = mapperForJson.writeValueAsString(baseProfile).replaceAll(user, "userSel").replaceAll(realm, "plataformSel");
			List<Map> genericProfile = mapperForJson.readValue(profileJson, new ArrayList<>().getClass());
			functionalPack.put("profile", genericProfile);
			restTempl.put("http://BACKOFFICE/backoffice/customer/package/update", functionalPack);
		} catch (Exception ex) {
			log.error("Error actualizando paquetes funcionales en backoffice: ", ex.getMessage());
		}
	}

	/**
	 * Trae los paquetes funcionales disponibles en el backofffice, siempre y cuando esten activos
	 * @param realmBack
	 * @return
	 */
	private List<Map> getActiveFunctionalPackage(String realmBack) {

		List<Map> listFunctPack = new ArrayList<>();
		try {
			RequestData req = RequestData.from();
			req.setPaginated(Paginated.from().put("limit", 10).put("offset", 0));
			req.setFilter(new QueryFilter().put("filter", Arrays.asList(new Profile().put("active", true))));
			listFunctPack = restTempl.postForObject("http://BACKOFFICE/backoffice/customer/package/getPackageFunctionality?realm=" + realmBack, req, List.class);
		} catch (Exception e) {
			log.error("No hay paquetes funcionales disponibles.. ", e.getMessage());
		}
		return listFunctPack;
	}
}
