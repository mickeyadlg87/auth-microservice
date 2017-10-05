package cl.tastets.life.auth.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.tastets.life.auth.dao.FuncionalityDAO;
import cl.tastets.life.objects.Funcionality;
import cl.tastets.life.objects.utils.RequestData;

@Service
public class FuncionalityBO {
	
	@Autowired
	private FuncionalityDAO dao;
	
	/**
	 * Metodo para obtener las funcionalidades dependiendo del filtro
	 * @param filter filtro entregado por el cliente para obtener funcionalidades
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getFuncionality(RequestData data) throws Exception {

        Map<String, Object> resp = new HashMap<String, Object>();
        try {
        	List<Funcionality> funcionality = dao.getFuncionality(data.getFilter());
            if (funcionality != null) {
                resp.put("status", Messages.STATUS_OK);
                resp.put("resp", funcionality);
            } else {
                resp.put("status", Messages.STATUS_FAIL);
            }
        } catch (Exception e) {
            throw e;
        }
        return resp;
    }
	
	/**
	 * Metodo para actualizar si existe una funcionalidad en base de datos o agregarla a base de datos si no existe
	 * @param funcionality Mapa con lo que hay que actualizar o agregar a base de datos.
	 * @throws Exception
	 */
	public void addfuncionality(Funcionality funcionality) throws Exception {
        try {
        	dao.updateFuncionality(funcionality);
        } catch (Exception e) {
            throw e;
        }
    }
}
