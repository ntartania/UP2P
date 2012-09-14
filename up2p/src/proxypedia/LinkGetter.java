package proxypedia;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  utility class to extract links from html
 *  taken/modified from 
 * @author adavoust
 *
 */
public class LinkGetter {
	private Pattern htmltag;
	private Pattern link;
	//private final String root;

	public LinkGetter() {
		//this.root = root;
		htmltag = Pattern.compile("<img[^>]*src=\"(.*?)\"");
		link = Pattern.compile("src=\".*\"");
	}

	public List<String> getLinks(String html) {
		List<String> links = new ArrayList<String>();
		
			/*BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new URL(url).openStream()));
			String s;
			StringBuilder builder = new StringBuilder();
			while ((s = bufferedReader.readLine()) != null) {
				builder.append(s);
			}*/

			//System.out.println("len : "+ html.length());
			Matcher tagmatch = htmltag.matcher(html);
			while (tagmatch.find()) {
				String candidate = tagmatch.group();
				//System.out.println("trying candidate: "+ candidate);
				Matcher matcher = link.matcher(candidate);
				matcher.find();
				String link = matcher.group().replaceFirst("src=\"", "")
						.replaceFirst("\"", "");
				if (valid(link)) {
					links.add(link);
				}
			}
		
		return links;
	}

	private boolean valid(String s) {
		if (s.matches("javascript:.*|mailto:.*")) {
			return false;
		}
		return true;
	}

	/*private String makeAbsolute(String url, String link) {
		if (link.matches("http://.*")) {
			return link;
		}
		if (link.matches("/.*") && url.matches(".*$[^/]")) {
			return url + "/" + link;
		}
		if (link.matches("[^/].*") && url.matches(".*[^/]")) {
			return url + "/" + link;
		}
		if (link.matches("/.*") && url.matches(".*[/]")) {
			return url + link;
		}
		if (link.matches("/.*") && url.matches(".*[^/]")) {
			return url + link;
		}
		throw new RuntimeException("Cannot make the link absolute. Url: " + url
				+ " Link " + link);
	}*/
}

