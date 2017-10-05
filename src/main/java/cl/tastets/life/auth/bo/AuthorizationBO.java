package cl.tastets.life.auth.bo;

import cl.tastets.life.auth.dao.ProfileDAO;
import cl.tastets.life.objects.Profile;
import cl.tastets.life.objects.utils.RequestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthorizationBO {

    @Autowired
    private ProfileDAO dao;

    /**
     * metodo que obtiene perfiles. Envia al dao los parametros y con la
     * respuesta forma objeto profile.
     *
     * @param realm identificador de la plataforma
     * @param user nombre de usuario
     * @param device
     * @return status ok - Perfiles agreados
     * @throws java.lang.Exception
     */
    public Map<String, Object> selectProfile(String realm, String user, String device) throws Exception {

        Map<String, Object> resp = new HashMap<>();
        try {
            List<Profile> profiles = dao.getProfiles(realm, user, device);
            if (profiles != null) {
                resp.put("status", Messages.STATUS_OK);
                resp.put("profiles",profiles);
            } else {
                resp.put("status", Messages.STATUS_FAIL);
            }
        } catch (Exception e) {
            throw e;
        }
        return resp;

    }
    
    
    /**
     * metodo que obtiene perfiles segun el filtro entregado por el cliente
     *
     * @param realm identificador de la plataforma
     * @param user nombre de usuario
     * @param device
     * @return status ok - Perfiles agreados
     * @throws java.lang.Exception
     */
    public Map<String, Object> selectAllProfileByFilter(RequestData data) throws Exception {

        Map<String, Object> resp = new HashMap<>();
        try {
            List<Profile> profiles = dao.getAllPerfilByFilter(data.getFilter());
            if (profiles != null) {
                resp.put("status", Messages.STATUS_OK);
                resp.put("profiles", profiles);
            } else {
                resp.put("status", Messages.STATUS_FAIL);
            }
        } catch (Exception e) {
            throw e;
        }
        return resp;

    }


    /**
     * metodo encargado de recibir lista de perfiles e iterarla para ir
     * agregando estos.
     *
     * @param profiles
     * @param realm identificador de la plataforma
     * @param user nombre de usuario
     * @param device
     * @throws java.lang.Exception
     */
    public void updateProfiles(List<Profile> profiles, String realm, String user, String device) throws Exception {
        try {
			Map<String,Object> deleteFilter =new HashMap<>();
			deleteFilter.put("user", user);
			deleteFilter.put("realm", realm);
			deleteFilter.put("device", device);			
			dao.deleteAllProfilesByFilter(deleteFilter);
            profiles.forEach((profile) -> {
                dao.updateProfile(profile, realm, user, device);
            });
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * se encarga de agregar child al profile indicado por el title
     *
     * @param listChild
     * @param realm
     * @param title
     * @param device
     * @throws Exception
     */
    public void updateProfileAddChilds(List<Profile> listChild, String realm, String title, String device) throws Exception {
        try {
            dao.updateChildProfile(listChild, realm, title, device);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * elimina opciones del perfil indicado por el title
     *
     * @param listChild
     * @param realm
     * @param title
     * @param device
     * @throws Exception
     */
    public void deleteFromProfileChilds(List<Profile> listChild, String realm, String title, String device) throws Exception {
        try {
            dao.deleteChildFromProfile(listChild, realm, title, device);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * Servicio encargado de guardar un profile segun un filtro entregado
     * @param filter
     * @param profile
     * @throws Exception
     */
    public void addProfilesByFilter(HashMap<String, Object> filter, HashMap<String, Object> profile) throws Exception {
        try {
        	dao.updateProfileByFilter(filter, profile);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * Servicio encargado de eliminar un profile segun un filtro entregado
     * @param filter
     * @throws Exception
     */
    public void deleteProfilesByFilter(HashMap<String, Object> filter) throws Exception {
        try {
        	dao.deleteProfileByFilter(filter);
        } catch (Exception e) {
            throw e;
        }
    }

}
