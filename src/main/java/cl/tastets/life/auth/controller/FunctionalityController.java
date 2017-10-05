package cl.tastets.life.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cl.tastets.life.auth.bo.AuthenticationBO;
import cl.tastets.life.auth.bo.FuncionalityBO;
import cl.tastets.life.auth.bo.Messages;
import cl.tastets.life.auth.utils.DevicesEnum;
import cl.tastets.life.objects.Funcionality;
import cl.tastets.life.objects.RealmEnum;
import cl.tastets.life.objects.utils.RequestData;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/auth/funcionality", produces = "application/json", consumes = "application/json")
@Api(value = "Functionality service")
public class FunctionalityController {
	
	@Autowired
	private FuncionalityBO funcionalityBO;

    @Autowired
    private AuthenticationBO authBO;
	
    /**
     * Servicio para guardar las funcialidades
     * @param funcionality
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Map<String, Object> addFuncionality(
            @RequestBody Funcionality funcionality) {
        Map<String, Object> resp = new HashMap<>();

        try {
            //valida que el usuario exista, llendo al servicio de metadata-user
            Map<String, Object> userValidate = authBO.doValidate(funcionality.getString("token"), DevicesEnum.valueOf(funcionality.getString("device")).toString(), RealmEnum.valueOf(funcionality.getString("realm")).toString());
            //si el usuario es validado, comienza el flujo de agregar los perfiles asociados 
            if (userValidate.get("status").equals(Messages.STATUS_OK)) {
                funcionalityBO.addfuncionality(funcionality);
                resp.put("status", Messages.STATUS_OK);
            } else {
                resp.putAll(userValidate);
            }
        } catch (Exception e) {
            resp.put("Error", "Error al registrar perfiles");
        }
        return resp;
    }

    /**
	 * servicio encargado de obtener una funcionalidad segun un filtro
	 * @param filter filtro a ejecutar para obtener la funcionalidad
	 * @return
	 */
	@RequestMapping(value = "/get", method = RequestMethod.POST)
    @HystrixCommand(fallbackMethod = "defaultGet")
	public Map<String, Object> getFuncionalitys(
			@RequestBody RequestData data){
		Map<String, Object> resp = new HashMap<>();
		try {
            //llama al metodo encargado de la obtención del listado de perfiles asociados
            resp = funcionalityBO.getFuncionality(data);

        } catch (Exception e) {
            resp.put("Error", "Error al obtener perfiles");
        }
        return resp;
	}
    
    /**
     * defaul get
     * @param funcionality
     * @return
     */
    public Map<String, String> defaultGet(RequestData data) {
        Map<String, String> resp = new HashMap<>();
        resp.put("status", Messages.STATUS_FAIL);
        return resp;
    }
}
