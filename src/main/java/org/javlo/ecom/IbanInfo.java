package org.javlo.ecom;

public class IbanInfo {
	
	public static class BankInfo {
		private String bankCode = null;
		private String name = null;
		private String bic = null;
		public String getBankCode() {
			return bankCode;
		}
		public void setBankCode(String bankCode) {
			this.bankCode = bankCode;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getBic() {
			return bic;
		}
		public void setBic(String bic) {
			this.bic = bic;
		}
		
	}
	
	private boolean valid = false;
	private String iban;
	private BankInfo bankData = null;
	
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getIban() {
		return iban;
	}
	public void setIban(String iban) {
		this.iban = iban;
	}
	public BankInfo getBankData() {
		return bankData;
	}
	public void setBankData(BankInfo bankInfo) {
		this.bankData = bankInfo;
	}

}
