package org.javlo.ecom.coda;

public class CodaBean {
	
	private String accountNumber;
	private String communication;
	
	public static boolean isValid(String line) {
		if (line == null || !line.startsWith("2") || line.length() != 129) {
			return false;
		} else {
			return true;
		}
	}
	
	public static CodaBean parseLine(String line21, String line22, String line23) throws CodaFormatError {
		if (!isValid(line21)) {
			throw new CodaFormatError();
		}
		CodaBean outBean = new CodaBean();
		return outBean;
	}

}
