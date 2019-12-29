package org.javlo.service.name;

public class NameGenerator {
	
	private static final String[] FIRST_NAMES = {"Emma", "Louise", "Alice", "Chloé", "Lina", "Rose", "Léa", "Anna", "Mila", "Mia", "Gabriel", "Raphaël", "Léo", "Louis", "Lucas", "Adam", "Arthur", "Hugo", "Jules", "Maël", "Patrick", "Philippe", "Catherine", "Anne", "Sylvie"};
	
	private static final String[] LAST_NAMES = {"Martin", "Bernard", "Thomas", "Petit", "Robert", "Richard", "Durand", "Dubois", "Moreau", "Laurent", "Simon", "Michel", "Lefebvre", "Leroy", "Roux", "David", "Bertrand", "Morel", "Fournier", "Bonnet", "Girard", "Dupont", "Lambert", "Fontaine", "Rousseau", "Vincent", "Muller", "Lefevre", "Faure", "Andre", "Mercier", "Blanc", "Guerin", "Boyer", "Garnier"};
	
	public static String getFirstName() {
		return FIRST_NAMES[(int)Math.round((FIRST_NAMES.length-1)*Math.random())];
	}
	
	public static String getLastName() {
		return LAST_NAMES[(int)Math.round((LAST_NAMES.length-1)*Math.random())];
	}
	
	public static String getFirstNameLastName() {
		return getFirstName()+' '+getLastName();
	}
	
	public static String getLastNameFirstName() {
		return getLastName()+' '+getFirstName();
	}
	
	public static void main(String[] args) {
		for (int i=0; i<1000000; i++) {
			System.out.println(i+" : "+getLastNameFirstName());
		}
	}
}
