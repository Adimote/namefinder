import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// Extremely robust web scraper
public class namefinder {
	
	private static String base_uri = "http://www.ecs.soton.ac.uk/people/%s";
	
	// This code gathers the appropriate information to get the correct info for the page
	private static String given_user = "dem";
	private static String given_name = "Dr David Millard";
	
	// unique identifier of the html tag surrounding the user
	private String unique_html_identifier = "";
	// string returned if user doesn't exist.
	// You would initially think it would return a blank string, however, if the username is being used as the title, it would display the title of the 404 page when there isn't a user of that name. 
	private String failure_response = "";
	
	
	// given a user and real name, this will set the unique_html_identifier as the exact contents of the tag that precedes the username
	// if we know this, we will know where to look for any page given
	public void initialise_html_layout() throws Exception {
		BufferedReader reader = get_page_for_user(given_user);
		// Create a scanner, which uses a regex to find the user
		// We hope the id of the html element is unique. if not, it'll fail
		Pattern regex = Pattern.compile("<([^>]*)>[ \\n\\r\\t]*"+given_name+"[ \\n\\r\\t]*<",Pattern.MULTILINE);
		
		Scanner scanner = new Scanner(reader);
		String match = scanner.findWithinHorizon(regex,0);
		scanner.close();
		if (match.isEmpty()) {
			throw new Exception("couldn't find username in page");
		}
		// Parse the match again with the regex to get the contents of the group
		Matcher m = regex.matcher(match);
		m.find();
		this.unique_html_identifier = m.group(1);
	}
	
	// Look up the user in the webpage
	public String look_up(String user) throws Exception {
		BufferedReader reader = get_page_for_user(user);
		
		// If we don't know how the page is laid out, figure it out.
		if (this.unique_html_identifier == "") {
			initialise_html_layout();
			// Confirm it's working:
			String name = look_up(given_user);
			if (!name.equals(given_name)){ 
				// Something's gone wrong with the processing
				throw new Exception(String.format("Self-test failed after initialisation, expected name '%s', got '%s' instead.",name,given_name));
			}
			// Failure response 
			this.failure_response = look_up("definitely_not_a_user");
		}

		// Create a scanner, which uses a regex to find the user
		Scanner scanner = new Scanner(reader);
		Pattern regex = Pattern.compile("<"+Pattern.quote(this.unique_html_identifier)+">\\s*([^<]*)\\s*<",Pattern.MULTILINE);
		String match = scanner.findWithinHorizon(regex,0);
		scanner.close();
		
		if (match.equals("")) {
			System.out.println("page doesn't contain a username.");
			return "";
		}
		
		// get the full name of the user
		Matcher m = regex.matcher(match);
		m.find();
		String name = m.group(1);
		if (name.equals(this.failure_response)) {
			return "user doesn't exist";
		}
		return name;
	}
	
	// get the stream reader for a user with a given username
	public BufferedReader get_page_for_user(String user) {
		URL url;
		try {
			url = new URL(String.format(base_uri,user));
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			return new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} catch (MalformedURLException e) {
			System.out.println("ERROR: Username contains strange characters");
			return null;
		} catch (IOException e) {
			System.out.println("ERROR: Couldn't connect to internet");
			return null;
		}
	}

	// Manual mode, runs forever when the code is ran in a terminal window
	public void term_username_get(Console console) throws Exception {
		String user = console.readLine("Please Enter the username you wish to query (ctrl+c to quit): ");
		System.out.println("User is: "+look_up(user));
	}
	
	// Automatic mode, runs when the code is ran outside of a terminal window (ie in a debugger)
	// the reasoning for this is there is no way to input data otherwise.
	public void auto_username_get() throws Exception {
		System.out.println("Automatic test run:");
		System.out.println("Testing DEM (Dave millard):");
		System.out.println("response: "+look_up("dem"));
		System.out.println("Testing Invalid User (asdasdasd):");
		System.out.println("response: "+look_up("asdasdasd"));
		System.out.println("Testing valid User (nan):");
		System.out.println("response: "+look_up("nan"));
	}
	

	public static void main(String[] args) throws Exception {
		Console console = System.console();

		namefinder finder = new namefinder();
		// If being debugged in eclipse, run automatic mode
		if (console == null) {
			finder.auto_username_get();
		} else {
			while(true)
				finder.term_username_get(console);
		}
	}
		
}
