package cl.tastets.life.auth.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import cl.tastets.life.auth.bo.AuthenticationBO;
import cl.tastets.life.auth.bo.AuthorizationBO;
import cl.tastets.life.auth.bo.Messages;
import cl.tastets.life.objects.Profile;
import cl.tastets.life.objects.utils.RequestData;
import io.swagger.annotations.Api;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(value = "/auth/authorization")
@Api(value = "Authorization service")
public class AuthorizationController {

    @Autowired
    private AuthenticationBO authBO;
    @Autowired
    private AuthorizationBO authorizationBO;

    /**
     * servicio encargado de agregar una lista de perfiles asociados un usuario
     * y plataforma
     *
     * @param realm identificador de la plataforma
     * @param user nombre de usuario
     * @param token token designado al usuario
     * @param device tipo de equipo en que se conecto el usuario   
     * @param profileName Lista de perfiles asociados al usuario
     * @return status ok - Perfiles agreados
     */
    @RequestMapping(value = "/addProfiles", method = RequestMethod.PUT,produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> addProfile(
            @RequestParam(value = "realm", required = true) String realm,
            @RequestParam(value = "user", required = true) String user,            
            @RequestParam(value = "device", required = true) String device,
            @RequestBody List<Profile> profileName) {
        Map<String, String> resp = new HashMap<>();
        try {
            //valida que el usuario exista, llendo al servicio de metadata-user
//            Map<String, String> userValidate = authBO.doValidate(token, DevicesEnum.valueOf(device).toString(), RealmEnum.valueOf(realm).toString());
            //si el usuario es validado, comienza el flujo de agregar los perfiles asociados 
//            if (userValidate.get("status").equals(Messages.STATUS_OK)) {
                authorizationBO.updateProfiles(profileName, realm, user, device);
                resp.put("status", Messages.STATUS_OK);
//            } else {
//                resp.putAll(userValidate);
//            }
        } catch (Exception e) {
            resp.put("Error", "Error al registrar perfiles");
        }
        return resp;
    }

    /**
     * Se agrega un nuevo profile a base de datos segun un filtro entregado
     * @param profile
     * @return
     */
    @RequestMapping(value = "/addProfilesByFilter", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> addProfileByFilter(
            @RequestBody HashMap<String, Object> profile) {
        Map<String, String> resp = new HashMap<>();
        try {
        	HashMap<String, Object> filter  = (HashMap<String, Object>) profile.get("filter");
        	HashMap<String, Object> profil = (HashMap<String, Object>) profile.get("profile");
            authorizationBO.addProfilesByFilter(filter, profil);
            resp.put("status", Messages.STATUS_OK);
      } catch (Exception e) {
            resp.put("Error", "Error al registrar perfiles");
        }
        return resp;
    }
    
    /**
     * 
     * @param profile
     * @return
     */
    @RequestMapping(value = "/deleteProfilesByFilter", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> deleteProfileByFilter(
            @RequestBody HashMap<String, Object> profile) {
        Map<String, String> resp = new HashMap<>();
        try {
        	HashMap<String, Object> filter  = (HashMap<String, Object>) profile.get("filter");
            authorizationBO.deleteProfilesByFilter(filter);
            resp.put("status", Messages.STATUS_OK);

        } catch (Exception e) {
            resp.put("Error", "Error al registrar perfiles");
        }
        return resp;
    }

    /**
     * servicio encargado de obtener una lista de perfiles asociados un usuario y plataforma
     * 
     * @param realm identificador de la plataforma
     * @param device
     * @param user nombre de usuario
     * @return Map de Perfiles
     */
    @RequestMapping(value = "/selectProfiles", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @HystrixCommand(fallbackMethod = "defaultSelectProfiles")
    public Map<String, Object> selectProfile(
            @RequestParam(value = "realm", required = true) String realm,
            @RequestParam(value = "device", required = true) String device,
            @RequestParam(value = "user", required = true) String user) {
        Map<String, Object> resp = new HashMap<>();
        try {
            //llama al metodo encargado de la obtención del listado de perfiles asociados
            resp = authorizationBO.selectProfile(realm, user, device);

        } catch (Exception e) {
			resp.put("status", Messages.STATUS_FAIL);
            resp.put("Error", "Error al obtener perfiles");
        }
        return resp;
    }
    
    /**
     * servicio encargado de obtener una lista de perfiles asociados a un filtro entregado por el cliente
     * 
     * @param realm identificador de la plataforma
     * @param device
     * @param user nombre de usuario
     * @return Map de Perfiles
     */
    @RequestMapping(value = "/selectAllProfilesByFilter", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @HystrixCommand(fallbackMethod = "defaultSelectProfilesAllByFilter")
    public Map<String, Object> selectAllProfileByFilter(
    		@RequestBody RequestData data) {
        Map<String, Object> resp = new HashMap<>();
        try {
            //llama al metodo encargado de la obtención del listado de perfiles asociados
            resp = authorizationBO.selectAllProfileByFilter(data);

        } catch (Exception e) {
        	resp.put("status", Messages.STATUS_FAIL);
            resp.put("Error", "Error al obtener perfiles");
        }
        return resp;
    }

    /**
     * Agrega opciones a la seccion del perfil deseada
     * @param realm
     * @param title
     * @param device
     * @param listChild
     * @return
     */
    @RequestMapping(value = "/addChildToProfile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> addChildProfile(
            @RequestParam(value = "realm", required = true) String realm,
            @RequestParam(value = "title", required = true) String title,
            @RequestParam(value = "device", required = true) String device,
            @RequestBody List<Profile> listChild) {
        Map<String, String> resp = new HashMap<>();
        try {
            authorizationBO.updateProfileAddChilds(listChild, realm, title, device);
            resp.put("status", Messages.STATUS_OK);

        } catch (Exception e) {
            resp.put("status", Messages.STATUS_FAIL);
            resp.put("Error", "Error al registrar opciones para perfiles" + e.getMessage());
        }
        return resp;
    }


    /**
     * Elimina una o mas opciones a la seccion del perfil deseada
     *
     * @param realm
     * @param title
     * @param device
     * @param listTitleChild
     * @return
     */
    @RequestMapping(value = "/deleteChildFromProfile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> deleteChildProfile(
            @RequestParam(value = "realm", required = true) String realm,
            @RequestParam(value = "title", required = true) String title,
            @RequestParam(value = "device", required = true) String device,
            @RequestBody List<Profile> listTitleChild) {
        Map<String, String> resp = new HashMap<>();
        try {
            authorizationBO.deleteFromProfileChilds(listTitleChild, realm, title, device);
            resp.put("status", Messages.STATUS_OK);

        } catch (Exception e) {
            resp.put("status", Messages.STATUS_FAIL);
            resp.put("Error", "Error al eliminar opciones del perfil" + e.getMessage());
        }
        return resp;
    }

    //Defatults methods
    
    /**
     * Defauls methods of SelectProfilesAllByFilter
     * @param filter filtro de la consulta
     * @return
     */
    public Map<String, String> defaultSelectProfilesAllByFilter(RequestData data) {
        Map<String, String> resp = new HashMap<>();
        resp.put("status", Messages.STATUS_FAIL);
        return resp;
    }

    /**
     * defatults methods of selectProfiles
     * @param realm identificador de la plataforma
     * @param device
     * @param user nombre de usuario
     * @return status fail - select perfiles
     */
    public Map<String, String> defaultSelectProfiles(String realm, String device, String user) {
        Map<String, String> resp = new HashMap<>();
        resp.put("status", Messages.STATUS_FAIL);
        return resp;
    }
}
