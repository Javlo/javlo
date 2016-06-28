package org.javlo.mailing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.javlo.helper.ResourceHelper;
import org.javlo.mailing.MailService.Attachment;

public class EMail extends Mail {

	private InternetAddress sender;
	private List<InternetAddress> recipients;
	private List<InternetAddress> ccRecipients;
	private List<InternetAddress> bccRecipients;
	private String txtContent;
	private Collection<Attachment> attachments;
	private String unsubscribeLink;
	private DKIMBean dkim;
	
	public EMail() {
		super(null, null);
	}

	public EMail(String subject, String content) {
		super(subject, content);
	}

	public EMail(String subject, String content, boolean html) {
		super(subject, content, html);
	}

	public InternetAddress getSender() {
		return sender;
	}

	public void setSender(InternetAddress sender) {
		this.sender = sender;
	}

	public List<InternetAddress> getRecipients() {
		if (recipients == null) {
			recipients = new LinkedList<InternetAddress>();
		}
		return recipients;
	}

	public void setRecipients(List<InternetAddress> recipients) {
		this.recipients = recipients;
	}
	
	public void addRecipients(InternetAddress recipient) {
		getRecipients().add(recipient);
	}

	public List<InternetAddress> getCcRecipients() {
		if (ccRecipients == null) {
			ccRecipients = new LinkedList<InternetAddress>();
		}
		return ccRecipients;
	}

	public void setCcRecipients(List<InternetAddress> ccRecipients) {
		this.ccRecipients = ccRecipients;
	}

	public List<InternetAddress> getBccRecipients() {
		if (bccRecipients == null) {
			bccRecipients = new LinkedList<InternetAddress>();
		}
		return bccRecipients;
	}

	public void setBccRecipients(List<InternetAddress> bccRecipients) {
		this.bccRecipients = bccRecipients;
	}

	public String getTxtContent() {
		return txtContent;
	}

	public void setTxtContent(String txtContent) {
		this.txtContent = txtContent;
	}

	public Collection<Attachment> getAttachments() {
		if (attachments == null) {
			attachments = new LinkedList<MailService.Attachment>();
		}
		return attachments;
	}

	public void setAttachments(Collection<Attachment> attachments) {
		this.attachments = attachments;
	}

	public String getUnsubscribeLink() {
		return unsubscribeLink;
	}

	public void setUnsubscribeLink(String unsubscribeLink) {
		this.unsubscribeLink = unsubscribeLink;
	}

	public DKIMBean getDkim() {
		return dkim;
	}

	public void setDkim(DKIMBean dkim) {
		this.dkim = dkim;
	}
	
	public void addAttachement(String name, InputStream content) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ResourceHelper.writeStreamToStream(content, out);
		out.flush();
		getAttachments().add(new Attachment(name, out.toByteArray()));
		out.close();
	}

}
