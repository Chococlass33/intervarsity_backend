package com.example.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsRequestInitializer;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class HelloController {
	@Value("${app.currentdance}")
	public String currentdance;
	@Value("${app.dancernamecolumn}")
	public String column;
	@Value("${app.judgeids}")
	public List<String> judgeid;
	@Value("${app.judgecolumns}")
	public List<String> judgecolumn;
	@Value("${app.judgestartingrow}")
	public String startingrow;
	@Value("${app.spreadsheetid}")
	public String spreadsheetid;
	@Value("${app.appname}")
	private String appname;

	public Sheets sheetsService = getSheetsService();;

	public HelloController() throws IOException, GeneralSecurityException
	{
	}

	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@GetMapping("/getDancers")
	public List<List<Object>> getDancers() throws GeneralSecurityException, IOException
	{
		ValueRange dance = sheetsService.spreadsheets().values().get(spreadsheetid,currentdance).execute();
		String curdancers = "'"+dance.getValues().get(0).get(0).toString()+"'!"+column;

		ValueRange dancers= sheetsService.spreadsheets().values().get(spreadsheetid,curdancers).execute();

		System.out.print(dancers);

		return dancers.getValues();
	}


	@PostMapping("/setDancers/{dance}")
	public void setDancers(@RequestParam String judgeID, @RequestParam List<String> vote, @PathVariable String dance) throws GeneralSecurityException, IOException{

		String jcolumn = judgecolumn.get(judgeid.indexOf(judgeID));
		String range = "'" + dance +"'" + "!" + jcolumn + startingrow + ":" + jcolumn;
		ValueRange values = new ValueRange();

		List<List<Object>> i = new ArrayList<>();
		for(String stuff:vote)
		{
			i.add(Arrays.asList(stuff));
		}
		values.setValues(i);

		System.out.println(i);

		sheetsService.spreadsheets().values().update(spreadsheetid,range,values).setValueInputOption("RAW").execute();
	}

	public Sheets getSheetsService() throws IOException, GeneralSecurityException
	{
		Credential cred = GoogleAuthorizeUtil.authorize();
		return new Sheets.Builder(
				GoogleNetHttpTransport.newTrustedTransport(),
				JacksonFactory.getDefaultInstance(), cred)
				.setApplicationName(appname)
				.build();
	}

}
