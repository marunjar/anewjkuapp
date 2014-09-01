package org.voidsink.library.contributors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Display a dialog showing contributors.
 */
public class Contributors {

	/**
	 * Tag that is used when sending error/debug messages to the log.
	 */
	protected static final String LOG_TAG = "voidsinkContributors";

	/**
	 * Default CSS styles used to format contributors.
	 */
	public static final String DEFAULT_CSS = "h1 { margin: 0.4em;  margin-left: 0px; font-size: 1.2em;}"
			+ "\n"
			+ "dl { margin: 0px;}"
			+ "\n"
			+ "dt { margin-bottom: 0.4em; margin-top: 0.4em; padding-left: 1em}";

	/**
	 * Context that is used to access the resources and to create the
	 * contributor dialog.
	 */
	protected final Context mContext;

	/**
	 * Contains the CSS rules used to format contributors.
	 */
	protected final String mCss;

	/**
	 * Contains constants for the root element of {@code contributors.xml}.
	 */
	protected interface ContributorsTag {
		static final String NAME = "contributors";
	}

	/**
	 * Contains constants for the contributor element of
	 * {@code contributors.xml}.
	 */
	protected interface ContributorTag {
		static final String NAME = "contributor";
		static final String ATTRIBUTE_TITLE = "title";
	}

	/**
	 * Contains constants for the name element of {@code contributors.xml}.
	 */
	protected interface NameTag {
		static final String NAME = "name";
	}

	/**
	 * Contains constants for the email element of {@code contributors.xml}.
	 */
	protected interface EMailTag {
		static final String NAME = "email";
	}

	/**
	 * Contains constants for the website element of {@code contributors.xml}.
	 */
	protected interface WebSiteTag {
		static final String NAME = "website";
		static final String ATTRIBUTE_TITLE = "title";
	}

	/**
	 * Create a {@code Contributors} instance using the default
	 * {@link SharedPreferences} file.
	 * 
	 * @param context
	 *            Context that is used to access the resources and to create the
	 *            Contributors dialog.
	 */
	public Contributors(Context context) {
		this(context, DEFAULT_CSS);
	}

	/**
	 * Create a {@code Contributors} instance.
	 * 
	 * @param context
	 *            Context that is used to access the resources and to create the
	 *            Contributors dialog.
	 * @param css
	 *            CSS styles used to format the contributor list (excluding
	 *            {@code <style>} and {@code </style>}).
	 * 
	 */
	public Contributors(Context context, String css) {
		mContext = context;
		mCss = css;
	}

	/**
	 * Create a dialog containing all contributors.
	 * 
	 * @return A dialog containing all contributors
	 */
	public AlertDialog getDialog(int resId) {
		return getDialog(resId, false);
	}

	/**
	 * Create a dialog containing all contributors.
	 * 
	 * @return A dialog containing all contributors
	 */
	public AlertDialog getDialog(int resId, boolean shuffle) {
		WebView wv = new WebView(mContext);
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				final String MAILTO = "mailto:";
				if (url.startsWith(MAILTO)) {
					// send me an eMail
					Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri
							.fromParts("mailto",
									url.substring(MAILTO.length()), null));
					emailIntent
							.putExtra(
									android.content.Intent.EXTRA_SUBJECT,
									mContext.getString(R.string.contributor_email_subject));
					emailIntent
							.putExtra(
									android.content.Intent.EXTRA_TEXT,
									mContext.getString(R.string.contributor_email_text));

					mContext.startActivity(Intent.createChooser(emailIntent,
							mContext.getString(R.string.contributor_send_email)));
					return true;
				} else {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					mContext.startActivity(i);
					return true;
				}
			}
		});

		ContributorItems ci = getContributorItems(resId, shuffle);

		// wv.setBackgroundColor(0); // transparent
		String html = generateContributorsHtml(ci.items);
		// Log.d(LOG_TAG, html);
		wv.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(ci.title)
				.setView(wv)
				.setCancelable(false)
				// OK button
				.setPositiveButton(
						mContext.getString(R.string.contributor_ok_button),
						null);

		return builder.create();
	}

	/**
	 * Get all contributors as HTML string.
	 * 
	 * @return the contributors
	 */
	protected String generateContributorsHtml(List<ContributorItem> items) {
		StringBuilder sb = new StringBuilder();

		sb.append("<html><head><style type=\"text/css\">");
		sb.append(mCss);
		sb.append("</style></head><body>");

		for (ContributorItem c : items) {
			sb.append("<h1>");
			sb.append(c.name);
			sb.append("</h1><dl>");
			if (c.hasEMail()) {
				sb.append("<dt>");
				sb.append(String.format("<a href=\"mailto:%s\">%s</a>",
						c.eMail, c.eMail));
				sb.append("</dt>");
			}
			if (c.hasWebsite()) {
				sb.append("<dt>");
				sb.append(String.format("<a href=\"%s\">%s</a>", c.website,
						c.websiteTitle));
				sb.append("</dt>");
			}
			sb.append("</dl>");
		}

		sb.append("</body></html>");

		return sb.toString();
	}

	/**
	 * Returns the contributors.
	 * 
	 * @param shuffle
	 *            If this is {@code true} the contributors get shuffled
	 * 
	 * @return A {@code List} containing {@link ContributorItem}s
	 */
	public ContributorItems getContributorItems(int resId, boolean shuffle) {
		ContributorItems result = readContributorsFromResource(resId);

		if (shuffle) {
			Collections.shuffle(result.items);
		}

		return result;
	}

	/**
	 * Read contributors from XML resource file.
	 * 
	 * @param resId
	 *            Resource ID of the XML file to read the contributors from.
	 * 
	 * @return A {@code List} containing {@link ContributorItem}s representing
	 *         contributors.
	 */
	protected final ContributorItems readContributorsFromResource(int resId) {
		XmlResourceParser xml = mContext.getResources().getXml(resId);
		try {
			return readContributors(xml);
		} finally {
			xml.close();
		}
	}

	/**
	 * Read contributors from an XML file.
	 * 
	 * @param xml
	 *            The {@code XmlPullParser} instance used to read the
	 *            contributors.
	 * 
	 * @return A {@code List} containing the contributors.
	 */
	protected ContributorItems readContributors(XmlPullParser xml) {
		List<ContributorItem> items = new ArrayList<ContributorItem>();
		String title = null;

		try {
			int eventType = xml.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					switch (xml.getName()) {
					case ContributorTag.NAME:
						parseContributorTag(xml, items);
						break;
					case ContributorsTag.NAME:
						title = xml.getAttributeValue(null,
								ContributorTag.ATTRIBUTE_TITLE);
						try {
							if (title != null) {
								int resId = mContext.getResources()
										.getIdentifier(title, null,
												mContext.getPackageName());
								if (resId != 0) {
									title = mContext.getString(resId);
								}
							}
						} catch (Exception e) {
							title = null;
						}
						break;
					default:
						break;
					}
				}
				eventType = xml.next();
			}
		} catch (XmlPullParserException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}

		if (title == null || title.isEmpty()) {
			title = mContext.getString(R.string.contributor_title);
		}

		return new ContributorItems(title, items);
	}

	/**
	 * Parse the {@code contributor} tag of a contributors XML file.
	 * 
	 * @param xml
	 *            The {@code XmlPullParser} instance used to read the
	 *            contributors.
	 * @param cs
	 *            The {@code List} to add a new {@link ContributorItem} instance
	 *            to.
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void parseContributorTag(XmlPullParser xml, List<ContributorItem> cs)
			throws XmlPullParserException, IOException {

		String name = "";
		String eMail = "";
		String website = "";
		String websiteTitle = "";

		int eventType = xml.getEventType();

		while (!(eventType == XmlPullParser.END_TAG && xml.getName().equals(
				ContributorTag.NAME))) {

			switch (eventType) {
			case XmlPullParser.START_TAG:
				switch (xml.getName()) {
				case NameTag.NAME:
					xml.next();
					name = xml.getText();
					break;
				case EMailTag.NAME:
					xml.next();
					eMail = xml.getText();
					break;
				case WebSiteTag.NAME:
					websiteTitle = xml.getAttributeValue(null,
							WebSiteTag.ATTRIBUTE_TITLE);
					xml.next();
					website = xml.getText();
					break;
				default:
					break;
				}
				break;
			// case XmlPullParser.TEXT:
			// break;
			// case XmlPullParser.END_TAG:
			// break;
			default:
				break;
			}
			eventType = xml.next();
		}

		if (!name.isEmpty()) {
			cs.add(new ContributorItem(name, eMail, website, websiteTitle));
		}
	}

	public static class ContributorItems {
		public final String title;
		public final List<ContributorItem> items;

		public ContributorItems(String title, List<ContributorItem> items) {
			this.title = title;
			this.items = items;
		}
	}

	/**
	 * Container used to store information about a release/version.
	 */
	public static class ContributorItem {

		/**
		 * Name of the contributor
		 */
		public final String name;

		/**
		 * eMail of the contributor
		 */
		public final String eMail;

		/**
		 * website of the contributor
		 */
		public final String website;

		/**
		 * title of website of the contributor
		 */
		public final String websiteTitle;

		ContributorItem(String name, String eMail, String website,
				String websiteTitle) {
			this.name = name;
			this.eMail = eMail;
			this.website = website;
			if (websiteTitle == null || websiteTitle.isEmpty()) {
				this.websiteTitle = this.website;
			} else {
				this.websiteTitle = websiteTitle;
			}
		}

		public boolean hasWebsite() {
			return !this.website.isEmpty();
		}

		public boolean hasEMail() {
			return !this.eMail.isEmpty();
		}
	}
}
