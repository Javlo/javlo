package org.javlo.mailing;

import org.javlo.helper.StringHelper;

/**
 * Valeur de l'en-tête List-Unsubscribe d'un message, et décision d'émettre ou
 * non List-Unsubscribe-Post.
 *
 * Immuable. La règle du one-click est isolée ici pour rester testable sans
 * session mail.
 */
public class UnsubscribeInfo {

	private final String url;

	private final boolean oneClick;

	private UnsubscribeInfo(String url, boolean oneClick) {
		this.url = url;
		this.oneClick = oneClick;
	}

	/**
	 * Lien saisi à la main dans les propriétés du site. Utilisé tel quel : on ne
	 * peut pas garantir qu'une URL externe respecte le protocole one-click.
	 */
	public static UnsubscribeInfo manual(String url) {
		return new UnsubscribeInfo(url, false);
	}

	/**
	 * Lien généré par Javlo. Le one-click n'est annoncé que sur HTTPS, le
	 * RFC 8058 l'exigeant.
	 */
	public static UnsubscribeInfo oneClick(String url) {
		boolean https = !StringHelper.isEmpty(url) && url.trim().toLowerCase().startsWith("https://");
		return new UnsubscribeInfo(url, https);
	}

	public boolean isEmpty() {
		return StringHelper.isEmpty(url);
	}

	public boolean isOneClick() {
		return oneClick && !isEmpty();
	}

	public String getUrl() {
		return url;
	}

	/**
	 * @return la valeur exacte à poser dans l'en-tête. Le RFC 2369 impose les
	 *         chevrons ; on les pose donc toujours, sauf si l'auteur du lien
	 *         manuel les a déjà écrits — un administrateur qui a saisi une URL
	 *         nue obtiendrait sinon un en-tête malformé.
	 */
	public String getHeaderValue() {
		if (isEmpty()) {
			return null;
		}
		String value = url.trim();
		if (value.startsWith("<")) {
			return value;
		}
		return '<' + value + '>';
	}
}
