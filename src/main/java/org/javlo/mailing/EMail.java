package org.javlo.mailing;

import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.mailing.MailService.Attachment;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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

	public EMail(GlobalContext globalContext, String subject, String content) throws AddressException {
		super(subject, content);
		this.sender = new InternetAddress(globalContext.getMailFrom());
		this.dkim = globalContext.getDKIMBean();
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

	public void addRecipient(String recipient) throws AddressException {
		if (StringHelper.isMail(recipient)) {
			addRecipient(new InternetAddress(recipient));
		}
	}

	public void addCcRecipient(String recipient) throws AddressException {
		if (StringHelper.isMail(recipient)) {
			addCcRecipient(new InternetAddress(recipient));
		}
	}

	public void addBccRecipient(String recipient) throws AddressException {
		if (StringHelper.isMail(recipient)) {
			addBccRecipient(new InternetAddress(recipient));
		}
	}
	
	public void addRecipient(InternetAddress recipient) {
		if (recipient == null) {
			return;
		}
		if (this.recipients == null) {
			this.recipients = new LinkedList<>();
		}
		this.recipients.add(recipient);
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
	
	public void addBccRecipient(InternetAddress bccRecipient) {
		if (bccRecipient == null) {
			return;
		}
		if (this.bccRecipients == null) {
			this.bccRecipients = new LinkedList<>();
		}
		this.bccRecipients.add(bccRecipient);
	}

	public void addCcRecipient(InternetAddress ccRecipient) {
		if (ccRecipients == null) {
			return;
		}
		if (this.ccRecipients == null) {
			this.ccRecipients = new LinkedList<>();
		}
		this.ccRecipients.add(ccRecipient);
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
	
	public void addAttachment(Attachment attachment) {
		if (attachment == null) {
			return;
		}
		if (this.attachments == null) {
			this.attachments = new LinkedList<>();
		}
		this.attachments.add(attachment);
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

	@Override
	public String toString() {
		return "EMail [sender=" + sender + ", recipients=" + recipients + "]";
	}
}
