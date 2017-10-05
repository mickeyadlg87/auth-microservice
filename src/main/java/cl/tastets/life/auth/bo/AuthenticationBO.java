package cl.tastets.life.auth.bo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.mail.Session;
import javax.mail.Store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cl.tastets.life.auth.dao.AuthDAO;
import cl.tastets.life.objects.ServicesEnum;
import cl.tastets.life.objects.User;

@Service
public class AuthenticationBO {

	@Autowired
	private AuthDAO bd;

	@Autowired
	private RestTemplate rest;

	@Value("${auth.gmail.server}")
	private String server;

	@Value("${auth.gmail.port}")
	private int port;

	@Value("${auth.gmail.domain}")
	private String domain;

	MessageDigest md = null;

	final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static final Logger log = LoggerFactory.getLogger(AuthenticationBO.class);

	@PostConstruct
	public void init() {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			log.error("Error al iniciar auth", e);
		}
	}

	/**
	 * Metodo que realiza el proceso de login para un usuario en todas las plataformas
	 *
	 * @param realm
	 * @param user
	 * @param password
	 * @return
	 */
	public Map<String, Object> doLogin(String realm, String user, String password, String device) {
		User resp = new User();
		switch (realm) {
			case "rslite":
				String url1 = "http://" + ServicesEnum.METADATAUSER + "/metadata/user/getUserByUserAndPass?realm=" + realm + "&userName=" + user + "&password=" + password;
				log.info("{} -> {}", realm, url1);
				resp = this.verifyLogin(() -> rest.getForObject(url1, User.class));
				break;
			case "rastreosat":
				String url2 = "http://" + ServicesEnum.METADATAUSER + "/metadata/user/getUserByUserAndPass?realm=" + realm + "&userName=" + user + "&password=" + password;
				log.info("{} -> {}", realm, url2);
				resp = this.verifyLogin(() -> rest.getForObject(url2, User.class));
				break;
			case "entel":
				String url3 = "http://" + ServicesEnum.METADATAUSER + "/metadata/user/getUserByUserAndPass?realm=" + realm + "&userName=" + user + "&password=" + password;
				log.info("{} -> {}", realm, url3);
				resp = this.verifyLogin(() -> rest.getForObject(url3, User.class));
				break;
			case "gps":
				String url4 = user + "@" + domain;
				log.info("{} -> {}", realm, url4);
				resp = this.verifyLogin(() -> {
                                    
                                    Properties props = new Properties();
                                    props.setProperty("mail.store.protocol", "imaps");
                                    User users = new User();

                                    try {
                                        Session sesion = Session.getDefaultInstance(props, null);
                                        Store store = sesion.getStore("imaps");
                                        store.connect(server, port, user + "@" + domain, password);
                                        if (store.isConnected()) {
                                            users.put("username", user);
                                        }
                                        store.close();

                                    } catch (Exception e) {
                                        log.error("Error al loguear a gmail", e);
                                    }
                                    return users;
				});
				break;
			case "optimatix":

				break;
			default:
				//deberia ser rslite
				break;
		}
		if (resp.get("status").equals(Messages.STATUS_OK)) { // generar token y guardar en BD
			String token = generateToken(user, password);
			resp.put("token", token);
			bd.saveToken(resp.getInteger("userId"),user, token, realm, resp.getOrDefault("companyId", 0, Integer.class) , device);
		}
		return resp;
	}

	/**
	 * Metodo que valida un token
	 *
	 * @param token
	 * @param device
	 * @param realm
	 * @return
	 */
	public Map<String, Object> doValidate(String token, String device, String realm) {
		Map<String, Object> resp = new HashMap<>();
		User user = bd.getUserByTokenFromDB(token, device);
		if (user != null) {
			User userInfo = rest.getForObject("http://" + ServicesEnum.METADATAUSER + "/metadata/user/getById?realm=" +
							user.getOrDefault("realm", "rslite", String.class) + "&id=" + user.getInteger("userId"),
					User.class);
			userInfo.remove("profiles");
			resp.put("status", Messages.STATUS_OK);
			resp.put("token", token);
			resp.put("username", user.getString("username"));
			resp.put("companyId", user.getInteger("companyId").toString());
			resp.put("userId", user.getInteger("userId").toString());
			resp.put("metadataUser", userInfo);
		} else {
			resp.put("status", Messages.STATUS_FAIL);
		}
		return resp;
	}

	/**
	 * Metodo que verifica la respuesta desde datos locales (bd) u servicios externos para realizar login
	 *
	 * @param userResp
	 * @return
	 */
	private User verifyLogin(Supplier<User> userResp) {
		User user = userResp.get();
		User resp = new User();
		if (!user.isEmpty()) {
			resp.put("status", Messages.STATUS_OK);
			resp.putAll((Map) user);
		} else {
			resp.put("status", Messages.STATUS_FAIL);
		}
		return resp;
	}

	/**
	 * Metodo que retorna un token para la session creada
	 *
	 * @return
	 */
	private String generateToken(String user, String pass) {
		byte[] token = md.digest((user + pass + System.currentTimeMillis()).getBytes());
		String tokenString = bytesToHex(token);
		md.reset();
		return tokenString;
	}

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
