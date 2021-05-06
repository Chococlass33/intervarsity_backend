package com.example.springboot;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.google.api.client.json.gson.GsonFactory;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@CrossOrigin
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
	public String appname;
    @Value("${app.frontendclientid}")
    public String frontendclientid;
//	public String googlesheetclientid;
//	public String googlesheetsclientsecret;

	public String serviceacc;

	public Sheets sheetsService;
    public NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

	public HelloController(@Value("${app.googlesheetclientid}") String googlesheetclientid, @Value("${app.googlesheetsclientsecret}") String googlesheetsclientsecret,@Value("${app.serviceacc}") String serviceacc) throws IOException, GeneralSecurityException
	{
		this.serviceacc= serviceacc;
//		this.googlesheetclientid = googlesheetclientid;
//		this.googlesheetsclientsecret = googlesheetsclientsecret;
		sheetsService =getSheetsService(serviceacc);
	}

    @PostMapping("/postDancers")
    public Dancers postDancers(@RequestBody String token) throws GeneralSecurityException, IOException
    {

		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, GsonFactory.getDefaultInstance())
				// Specify the CLIENT_ID of the app that accesses the backend:
				.setAudience(Arrays.asList(frontendclientid))
				// Or, if multiple clients access the backend:
				//.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
				.build();

//		System.out.println(token);
		GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), token);

		boolean tokenIsValid = (idToken != null) && verifier.verify(idToken);

		GoogleIdToken.Payload payload = idToken.getPayload();
		String email = payload.getEmail();

		if (tokenIsValid && judgeid.indexOf(email) != -1)
        {
            ValueRange dance = sheetsService.spreadsheets().values().get(spreadsheetid, currentdance).execute();
            String curdancers = "'" + dance.getValues().get(0).get(0).toString() + "'!" + column;

            ValueRange dancers = sheetsService.spreadsheets().values().get(spreadsheetid, curdancers).setMajorDimension("COLUMNS").execute();

            System.out.print(dancers);

            Dancers d = new Dancers();
            d.currentDance = dance.getValues().get(0).get(0).toString();
            d.dancers = dancers.getValues();

            return d;
        }
        else
        {
            return null;
        }
    }

	@PostMapping("/postCurrent")
	public String postCurrent(@RequestBody String token) throws GeneralSecurityException, IOException
	{
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, GsonFactory.getDefaultInstance())
				// Specify the CLIENT_ID of the app that accesses the backend:
				.setAudience(Arrays.asList(frontendclientid))
				// Or, if multiple clients access the backend:
				//.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
				.build();

//		System.out.println(token);
		GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), token);

		boolean tokenIsValid = (idToken != null) && verifier.verify(idToken);

		GoogleIdToken.Payload payload = idToken.getPayload();
		String email = payload.getEmail();

		if (tokenIsValid && judgeid.indexOf(email) != -1)
		{
			ValueRange dance = sheetsService.spreadsheets().values().get(spreadsheetid, currentdance).execute();
			String curdance = dance.getValues().get(0).get(0).toString();

			return (curdance);
		}
		return null;
	}


	@PostMapping("/setDancers/{dance}")
	public void setDancers(@RequestBody JudgeVote judgevote, @PathVariable String dance) throws GeneralSecurityException, IOException{

		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, GsonFactory.getDefaultInstance())
				// Specify the CLIENT_ID of the app that accesses the backend:
				.setAudience(Arrays.asList(frontendclientid))
				// Or, if multiple clients access the backend:
				//.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
				.build();

		GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), judgevote.token);

		boolean tokenIsValid = (idToken != null) && verifier.verify(idToken);


		if (tokenIsValid)
        {
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            if (judgeid.indexOf(email) != -1)
            {

                String jcolumn = judgecolumn.get(judgeid.indexOf(email));
                String range = "'" + dance + "'" + "!" + jcolumn + startingrow + ":" + jcolumn;
                ValueRange values = new ValueRange();

                List<List<Object>> i = new ArrayList<>();
                for (String stuff : judgevote.vote)
                {
                    i.add(Arrays.	asList(stuff));
                }
                values.setValues(i);

//                System.out.println(i);

                sheetsService.spreadsheets().values().update(spreadsheetid, range, values).setValueInputOption("RAW").execute();
            }
        }
        else
        {
            System.out.println("Invalid ID token.");
        }
	}

	public Sheets getSheetsService(String serviceacc) throws IOException, GeneralSecurityException
	{
		GoogleCredential cred = GoogleAuthorizeUtil.authservice(serviceacc);
		return new Sheets.Builder(
				GoogleNetHttpTransport.newTrustedTransport(),
				GsonFactory.getDefaultInstance(), cred)
				.setApplicationName(appname)
				.build();
	}


	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

//	@GetMapping("/getDancers")
//	public Dancers getDancers() throws GeneralSecurityException, IOException
//	{
//		ValueRange dance = sheetsService.spreadsheets().values().get(spreadsheetid,currentdance).execute();
//		String curdancers = "'"+dance.getValues().get(0).get(0).toString()+"'!"+column;
//
//		ValueRange dancers= sheetsService.spreadsheets().values().get(spreadsheetid,curdancers).setMajorDimension("COLUMNS").execute();
//
//		System.out.print(dancers);
//
//		Dancers d = new Dancers();
//		d.currentDance = dance.getValues().get(0).get(0).toString();
//		d.dancers = dancers.getValues();
//
//		return d;
//	}
	//	@GetMapping("/checkCurrent/{currentdance}")
//	public boolean checkCurrent(@PathVariable String currentdance) throws GeneralSecurityException, IOException
//	{
//		ValueRange dance = sheetsService.spreadsheets().values().get(spreadsheetid,currentdance).execute();
//		String curdance = dance.getValues().get(0).get(0).toString();
//
//		return (curdance.contentEquals(currentdance));
//	}

//	@GetMapping("/getCurrent")
//	public String getCurrent() throws GeneralSecurityException, IOException
//	{
//		ValueRange dance = sheetsService.spreadsheets().values().get(spreadsheetid,currentdance).execute();
//		String curdance = dance.getValues().get(0).get(0).toString();
//
//		return (curdance);
//	}
}
