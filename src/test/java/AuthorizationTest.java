
import cl.tastets.life.auth.TastetsAuthApplication;
import cl.tastets.life.objects.Funcionality;
import cl.tastets.life.objects.Profile;
import cl.tastets.life.objects.RealmEnum;
import cl.tastets.life.objects.utils.QueryFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author glucero
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TastetsAuthApplication.class)
@WebAppConfiguration
public class AuthorizationTest {

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		this.mapper = new ObjectMapper();
	}

	private ObjectMapper mapper;

	@Test
	public void addProfilesTest() throws Exception {
                
                
                Profile prof1 = new Profile();
		prof1.put("Administracion", Arrays.asList(new Profile().put("flotas", "flotas"),new Profile().put("flotas", "conductores")));
                Profile prof2 = new Profile();
		prof2.put("rol", "rol2");
                
                List<Profile> listProfX = new ArrayList<>();
                listProfX.add(prof1);
                listProfX.add(prof2);
            
                Profile profileObject = new Profile();
		profileObject.put("name", "Profile13");
                profileObject.put("ListaRoles", listProfX);
                Profile profileObject1 = new Profile();
		profileObject1.put("name", "Profile14");
                Profile profileObject2 = new Profile();
		profileObject2.put("name", "Profile15");
                
                
                List<Profile> listProfiles = new ArrayList<>();
                listProfiles.add(profileObject);
                listProfiles.add(profileObject1);
                listProfiles.add(profileObject2);
                
                String jsonInString = mapper.writeValueAsString(listProfiles);
                
		ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.put("/auth/authorization/addProfiles")
				.contentType(MediaType.APPLICATION_JSON)
				.param("realm", RealmEnum.rslite.toString())
                                .param("user", "ptoro")
				.param("token", "6B6BA967E01F40A81C5867A1D2096E89")
				.param("device", "desktop")
                            	.content(jsonInString)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		Map profiles = mapper.readValue(result.andReturn().getResponse().getContentAsByteArray(), Map.class);
		Assert.assertTrue(profiles.size() > 0);
	}
        
    @Test
	public void selectProfilesTest() throws Exception {
		ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.get("/auth/authorization/selectProfiles")
				.contentType(MediaType.APPLICATION_JSON)
				.param("realm", RealmEnum.rslite.toString())
                                .param("device", "desktop")
                                .param("user", "ptoro")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		Map profiles = mapper.readValue(result.andReturn().getResponse().getContentAsByteArray(), Map.class);
		Assert.assertTrue(profiles.size() > 0);
	}
	
	@Test
	public void AddFuncionalityTest()  throws Exception{
		
		Funcionality funcionality = new Funcionality();
		
		funcionality.put("token", "78F4D4F62FF1C64D6FE61EF7D63917FB");
		funcionality.put("device", "desktop");
		funcionality.put("realm", "entel");
		funcionality.put("name", "reporte de Incidentes ultimos 7 dias");
		funcionality.put("id", "5");
		
		HashMap<String, String> incident = new HashMap<String, String>();
		incident.put("idIncident", "1");
		incident.put("nameIncident", "exceso de velocidad");
		
		List<HashMap<String, String>> incidents = new ArrayList<HashMap<String,String>>();
		incidents.add(incident);
		
		HashMap<String, Object> parameterDefaul = new HashMap<String, Object>();
		parameterDefaul.put("incidents", incidents);
		
		funcionality.put("parameterDefaul", parameterDefaul);
		
		String jsonInString = mapper.writeValueAsString(funcionality);
		
		ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post("/auth/funcionality/add")
				.contentType(MediaType.APPLICATION_JSON)
                .content(jsonInString)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		Map funcionaliy = mapper.readValue(result.andReturn().getResponse().getContentAsByteArray(), Map.class);
		Assert.assertTrue(funcionaliy.size() > 0);
	}
	
	@Test
	public void getFuncionalityTest() throws Exception{
		QueryFilter filter = new QueryFilter();
		
		filter.put("realm", "entel")
			  .put("id", "5");
		
		String jsonInString = mapper.writeValueAsString(filter);
		ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post("/auth/funcionality/get")
				.contentType(MediaType.APPLICATION_JSON)
                .content(jsonInString)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		Map funcionaliy = mapper.readValue(result.andReturn().getResponse().getContentAsByteArray(), Map.class);
		Assert.assertTrue(funcionaliy.size() > 0);
		
	}
	
	@Test
	public void addParameterFilterTest() throws Exception{
		
		HashMap<String, Object> filter = new HashMap<>();
		
		filter.put("type", "company");
		filter.put("aplication", "dashboard");
		filter.put("company", "6");
		
		HashMap<String, Object> profile = new HashMap<>();
		
		profile.put("device", "desktop");
		profile.put("name", "dashBoard full");
		profile.put("realm", "entel");
		profile.put("user", "overgara");
		profile.put("aplication", "dashboard");
		profile.put("type", "company");
		profile.put("idPerfil", "dashBoardfull_6");
		
		
		HashMap<String, Object> funcionality = new HashMap<>();
		funcionality.put( "name", "reporte kilometros recorridos");
		funcionality.put( "id", "1");
		
		profile.put("funcionality", funcionality);
		
		HashMap<String, Object> request = new HashMap<>();
		request.put("filter", filter);
		request.put("profile", profile);
		
		String jsonInString = mapper.writeValueAsString(request);
		
		ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post("/auth/authorization/addProfilesByFilter")
				.contentType(MediaType.APPLICATION_JSON)
                .content(jsonInString)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		Map funcionaliy = mapper.readValue(result.andReturn().getResponse().getContentAsByteArray(), Map.class);
		Assert.assertTrue(funcionaliy.size() > 0);
		
	}
	
	@Test
	public void getParametersTest() throws Exception{
		
		HashMap<String, Object> data = new HashMap<>();
		
		
		QueryFilter filter = new QueryFilter();
		
		filter.put("type", "company");
		filter.put("aplication", "dashboard");
		filter.put("company", "6");
		
		data.put("filter", filter);
		
		String jsonInString = mapper.writeValueAsString(data);
		ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post("/auth/authorization/selectAllProfilesByFilter")
				.contentType(MediaType.APPLICATION_JSON)
                .content(jsonInString)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		Map profiles = mapper.readValue(result.andReturn().getResponse().getContentAsByteArray(), Map.class);
		Assert.assertTrue(profiles.size() > 0);
		
	}
}
