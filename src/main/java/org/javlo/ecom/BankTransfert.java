package org.javlo.ecom;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class BankTransfert {
	
	private String tag = "BCD";
	private String version = "001";
	private String charset = "1";
	private String identification = "SCT";
	private String bic = null;
	private String name = null;
	private String iban = null;
	private String currency = "EUR";
	private Double amount = null;
	private String reference = null;
	private String structuredReference = null;
	private String information = null;
	
	public BankTransfert(String iban, String bic, double amount, String name, String reference, String structuredRef) {
		this.iban = iban;
		this.bic = bic;
		this.amount = amount;
		this.setName(name);
		this.reference = reference;
		this.structuredReference = structuredRef;
	}

	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	public String getIdentification() {
		return identification;
	}
	public void setIdentification(String identification) {
		this.identification = identification;
	}
	public String getBic() {
		return bic;
	}
	public void setBic(String bic) {
		this.bic = bic;
	}
	public String getIban() {
		return iban;
	}
	public void setIban(String iban) {
		this.iban = iban;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public String getInformation() {
		return information;
	}
	public void setInformation(String information) {
		this.information = information;
	}
	
	@Override
	public String toString() {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(getTag());
		out.println(getVersion());
		out.println(getCharset());
		out.println(getIdentification());
		out.println(getBic());
		out.println(getName());
		out.println(getIban());
		Double amount = getAmount();
		if (amount == null) {
			amount = 0.0;
		}
		amount = ((double)Math.round(amount*100))/100;
		out.println(getCurrency()+amount);
		if (getStructuredReference() == null) {
			out.println("CHAR");
		}
		out.println("");
		if (getStructuredReference() == null) {
			out.println(getReference());
		} else {
			out.println(getStructuredReference());
		}
		
		out.close();
		return new String(outStream.toByteArray());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStructuredReference() {
		return structuredReference;
	}

	public void setStructuredReference(String structuredReference) {
		this.structuredReference = structuredReference;
	}

}
