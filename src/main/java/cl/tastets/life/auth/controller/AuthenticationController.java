package cl.tastets.life.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import cl.tastets.life.auth.bo.AuthenticationBO;
import cl.tastets.life.auth.bo.Messages;
import cl.tastets.life.auth.utils.DevicesEnum;
import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/auth", produces = "application/json")
@Api(value = "Authentication service")
public class AuthenticationController {

	@Autowired
	private AuthenticationBO bo;
	
	private Logger log = LoggerFactory.getLogger(AuthenticationController.class);

	// servicio encargado de realizar login de usuario
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	@HystrixCommand(fallbackMethod = "defaultLogin")
	public Map<String, Object> authLogin(@RequestParam(value = "realm", required = true) String realm,
			@RequestParam(value = "user", required = true) String user,
			@RequestParam(value = "pass", required = true) String pass,
			@RequestParam(value = "device", required = true) String device) {
		Map<String, Object> resp = null;
		try {
			// metodo encargado de realizar flujo de login
			resp = bo.doLogin(realm, user, pass, DevicesEnum.valueOf(device).toString());
		} catch (Throwable e) {
			log.error("Could not contact backend", e);
			resp = new HashMap<>();
			resp.put("Error", "Device no corresponde");
		}
		return resp;
	}

	// servicio encargado de realizar login de usuario mediante post
	@RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@HystrixCommand(fallbackMethod = "defaultLogin")
	public Map<String, Object> authLoginWithPost(@RequestParam(value = "realm", required = true) String realm,
			@RequestParam(value = "user", required = true) String user,
			@RequestParam(value = "pass", required = true) String pass,
			@RequestParam(value = "device", required = true) String device) {
		Map<String, Object> resp = null;
		try {
			// metodo encargado de realizar flujo de login
			resp = bo.doLogin(realm, user, pass, DevicesEnum.valueOf(device).toString());
		} catch (Throwable e) {
			log.error("Could not contact backend", e);
			resp = new HashMap<>();
			resp.put("Error", "Device no corresponde");
		}
		return resp;
	}

	// servicio encargado de validar solicitudes segun token, equipo y
	// plataforma del usuario en cuestion.
	@RequestMapping(value = "/validate", method = RequestMethod.GET)
	@HystrixCommand(fallbackMethod = "defaultValidate")
	public Map<String, Object> validate(@RequestParam(value = "realm", required = false) String realm,
			@RequestParam(value = "token", required = true) String token,
			@RequestParam(value = "device", required = true) String device) {
		Map<String, Object> resp = null;
		try {
			// metodo encargado del flujo de validación
			resp = bo.doValidate(token, DevicesEnum.valueOf(device).toString(), realm);
		} catch (IllegalArgumentException e) {
			resp = new HashMap<>();
			resp.put("Error", "Device no corresponde");
		}
		return resp;
	}

	// Defatults methods
	public Map<String, String> defaultLogin(String realm, String user, String pass, String device) {
		Map<String, String> resp = new HashMap<>();
		resp.put("status", Messages.STATUS_FAIL);
		return resp;
	}

	public Map<String, String> defaultValidate(String realm, String token, String device) {
		Map<String, String> resp = new HashMap<>();
		resp.put("status", Messages.STATUS_FAIL);
		return resp;
	}
}
