package de.intranda.goobi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlToTEIConvert {

	private static final int HEADER_HIERARCHY_DEPTH = 9;
	private static final String HEADER_DIV_REGEX = "(<hx[\\S\\s]*?)(?=((<h\\d)|$))";

	private ConverterMode mode;

	public HtmlToTEIConvert(ConverterMode mode) {
		this.mode = mode;
	}

	public String convert(String text) {
		text = removeUrlEncoding(text);
		text = "<div xmlns=\"http://www.tei-c.org/ns/1.0\">" + text + "</div>";

		for (int i = HEADER_HIERARCHY_DEPTH; i > 0; i--) {
			String regex = HEADER_DIV_REGEX.replace("x", Integer.toString(i));
			for (MatchResult r : findRegexMatches(regex, text)) {
				text = text.replace(r.group(), "<div>" + r.group() + "</div>");
			}
			// replace header
			for (MatchResult r : findRegexMatches("<h" + i + ".*?>(.*?)</h" + i + ">", text)) {
				text = text.replace(r.group(), "<head>" + r.group(1) + "</head>");
			}
		}

		// remove empty <p>'s
		text = text.replace("<p />", "").replace("<p/>", "").replace("<p></p>", "");

		// replace bold

		for (MatchResult r : findRegexMatches("<strong>(.*?)</strong>", text)) {
			text = text.replace(r.group(), "<hi rend=\"bold\">" + r.group(1) + "</hi>");
		}
		// replace italic
		for (MatchResult r : findRegexMatches("<em>(.*?)</em>", text)) {
			text = text.replace(r.group(), "<hi rend=\"italic\">" + r.group(1) + "</hi>");
		}
		// replace underline
		for (MatchResult r : findRegexMatches("<span style=\"text-decoration: underline;\">(.*?)</span>", text)) {
			text = text.replace(r.group(), "<hi rend=\"underline\">" + r.group(1) + "</hi>");
		}

		// replace anm
		for (MatchResult r : findRegexMatches("\\[anm\\](.*?)\\[/anm\\]", text)) {
			text = text.replace(r.group(), "<note type=\"editorial\"><p>" + r.group(1) + "</p></note>");
		}

		// tables
		text = text.replaceAll("<table.*?>", "<table>").replace("<tbody>", "").replace("</tbody>", "");
		text = text.replace("<caption>", "<head>").replace("</caption>", "</head>");
		text = text.replace("<tbody>", "").replace("</tbody>", "");
		text = text.replace("<thead>", "").replace("</thead>", "");
		text = text.replaceAll("<tr.*?>", "<row>").replace("<tr>", "<row>").replace("</tr>", "</row>");
		text = text.replaceAll("<td.*?>", "<cell>").replace("</td>", "</cell>");

		// lists
		if(mode.equals(ConverterMode.annotation)) {			
			text = text.replaceAll("<ul.*?>", "<list rend=\"bulleted\">").replace("</ul>", "</list>");
			text = text.replace("<li>", "<item>").replace("</li>", "</item>");
			text = text.replaceAll("<ol.*?style=\".*?-alpha.*?>", "<list rend=\"alphabetical\">").replace("</ol>", "</list>");
			text = text.replaceAll("<ol.*?style=\".*?-greek.*?>", "<list rend=\"alphabetical\">").replace("</ol>", "</list>");
			text = text.replaceAll("<ol.*?>", "<list rend=\"numbered\">").replace("</ol>", "</list>");	
		} else {
			text = text.replaceAll("<ul.*?>", "<list>").replace("</ul>", "</list>");
			text = text.replace("<li>", "<item>").replace("</li>", "</item>");
//			text = text.replace("<ol>", "<list>").replace("</ol>", "</list>");
			text = text.replaceAll("<ol.*?>", "<list>").replace("</ol>", "</list>");			
		}

		// images
		// <img src="none" alt="Bildbeschriftung" />
		for (MatchResult r : findRegexMatches("<img src=\"(.*?)\" alt=\"(.*?)\" />", text)) {
			text = text.replace(r.group(),
					"<figure><head>" + r.group(2) + "</head><graphic url=\"" + r.group(1) + "\"/></figure>");
		}
		
		// Blockquote (old)
		for (MatchResult r : findRegexMatches("<blockquote>\\s*<p>\\[Q=(.*?)\\](.*?)\\[/Q\\]</p>\\s*</blockquote>",
				text)) {
			text = text.replace(r.group(), "<cit><q source=\"#" + r.group(1) + "\">" + r.group(2) + "</q></cit>");
		}
		
		// Blockquote (with reference)
		int quoteRefCounter = 1;
		for (MatchResult r : findRegexMatches("<blockquote\\s+cite=\"(.*?)\">\\s*([\\s\\S]*?)\\s*<\\/blockquote>",
				text)) {
			StringBuilder replacement = new StringBuilder();
			replacement
				.append("<cit> ")
				.append(mode.equals(ConverterMode.resource) ? "<q" : "<quote")
				.append( " source=\"#quoteref")
				.append(quoteRefCounter)
				.append("\">")
				.append(r.group(2))
				.append(mode.equals(ConverterMode.resource) ? "</q>" : "</quote>")
				.append(" <ref type=\"bibl\" xml:id=\"quoteref")
				.append(quoteRefCounter)
				.append("\" target=\"#ref")
				.append(quoteRefCounter)
				.append("\">")
				.append(r.group(1))
				.append("</ref>")
				.append("</cit>");
				text = text.replace(r.group(), replacement.toString());
			quoteRefCounter++;
		}

		// Blockquote (no reference)
//		for (MatchResult r : findRegexMatches("<blockquote>\\s*(<p>)*([\\s\\S]*?)(<\\/p>)*\\s*<\\/blockquote>",
		for (MatchResult r : findRegexMatches("<blockquote>\\s*([\\s\\S]*?)\\s*<\\/blockquote>",
				text)) {
			StringBuilder replacement = new StringBuilder();
			replacement
				.append("<cit>")
				.append(mode.equals(ConverterMode.resource) ? "<q" : "<quote")
				.append( " source=\"#\"")
				.append(">")
				.append(r.group(1))
				.append(mode.equals(ConverterMode.resource) ? "</q>" : "</quote>")
				.append("</cit>");
				text = text.replace(r.group(), replacement.toString());
		}

		for (MatchResult r : findRegexMatches("\\[Q=(.*?)\\](.*?)\\[/Q\\]", text)) {
			text = text.replace(r.group(), "<q source=\"#" + r.group(1) + "\">" + r.group(2) + "</q>");
		}
		
		//q with cite
		for (MatchResult r : findRegexMatches("<q\\s+cite=\"(.*?)\">([\\s\\S]*?)<\\/q>", text)) {
			if(mode.equals(ConverterMode.annotation)) {
				text = text.replace(r.group(), "<quote source=\"#quoteref" + quoteRefCounter + "\" type=\"direct\">" + r.group(2) + "</quote>" + "(<ref type=\"bibl\" xml:id=\"quoteref" + quoteRefCounter + "\" target=\"#ref" + quoteRefCounter + "\">" + r.group(1) + "</ref>)");
			} else {				
				text = text.replace(r.group(), "<q source=\"#quoteref" + quoteRefCounter + "\" type=\"direct\">" + r.group(2) + "</q>" + "(<ref type=\"bibl\" xml:id=\"quoteref" + quoteRefCounter + "\" target=\"#ref" + quoteRefCounter + "\">" + r.group(1) + "</ref>)");
			}
			quoteRefCounter++;
		}

		for (MatchResult r : findRegexMatches("\\[q\\](.*?)\\[/q\\]", text)) {
			text = text.replace(r.group(), "<q>" + r.group(1) + "</q>");
		}

		for (MatchResult r : findRegexMatches("<a\\s*(\\w+=\".*\"\\s*)*href=\"(.*)\">(.*)<\\/a>", text)) {
			text = text.replace(r.group(), "<ref target=\"" + r.group(2) + "\" type=\"url\">" + r.group(3) + "</ref>");
		}
		
		for (MatchResult r : findRegexMatches("<a\\s*(\\w+=\".*\"\\s*)*>(.*?)</a>", text)) {
			text = text.replace(r.group(), r.group(2));
		}

		text = text.replace("<br />", "");
		text = text.replace("<p />", "");

		return text.trim();
	}

	/**
	 * @param text
	 * @return
	 */
	public static String removeUrlEncoding(String text) {
		text = text.replace("&amp;", "&");
		text = text.replace("&Auml;", "Ä");
		text = text.replace("&Ouml;", "Ö");
		text = text.replace("&Uuml;", "Ü");

		text = text.replace("&auml;", "ä");
		text = text.replace("&ouml;", "ö");
		text = text.replace("&uuml;", "ü");

		text = text.replace("&szlig;", "ß");
		text = text.replace("&nbsp;", "");
		text = text.replace("&shy;", "-");
		return text;
	}

	public static Iterable<MatchResult> findRegexMatches(String pattern, CharSequence s) {
		List<MatchResult> results = new ArrayList<MatchResult>();
		for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();) {
			results.add(m.toMatchResult());
		}
		return results;
	}
	

	public static enum ConverterMode {
		annotation, resource
	}

}
