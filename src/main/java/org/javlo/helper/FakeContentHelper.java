package org.javlo.helper;

import java.util.Arrays;
import java.util.List;

public class FakeContentHelper {
    public static final List<String> NAMES = Arrays.asList("Shira Shire,Carter Cashion,Marhta Montalvo,Elouise Evett,Babette Betz,Roma Raine,Delorse Dyar,Markita Machado,Dedra Dileo,Gwenn Gallucci,Shara Story,Liberty Loredo,Nevada Nader,Marinda Mcconnel,Betty Bassler,Sindy Sippel,Jude Janas,Daryl Dardar,Lynn Lown,Gina Giesler,Susanna Sokol,Sue Spriggs,Lacresha Leis,Nola Navarette,Wai Wiggins,Corie Conaway,Sean Schleich,Georgie Gambino,Daina Desjardin,Chiquita Cho,Terrie Tarpley,Jenine Jarnagin,Charlesetta Cabral,Millicent Mondragon,Christian Cafferty,Letha Lambros,Argentina Allyn,Han Husman,Ima Inman,Vernie Verdin,Micah Molyneux,Kiersten Kina,Natacha Nesbitt,Len Lewis,Tillie Tricarico,Nguyet Nuss,Tena Thorsen,Jame Jerome,Alberta Anastasio,Pennie Pollard");

    public static final List<String> COMPAGNIES = Arrays.asList(new String[] {"Amnesty International", "Médecins Sans Frontières", "Greenpeace", "Oxfam", "Save the Children", "WWF", "Human Rights Watch", "ActionAid", "CARE International", "Friends of the Earth"});

    public static  final List<String> COUNTRIES = Arrays.asList(new String[] {"Albania", "Andorra", "Armenia", "Austria", "Azerbaijan", "Belarus", "Belgium", "Bulgaria", "Croatia", "Cyprus", "Czech Republic", "Denmark", "Estonia", "Finland", "France", "Georgia", "Germany", "Greece", "Hungary", "Iceland", "Ireland", "Italy", "Kazakhstan", "Kosovo", "Latvia", "Lithuania", "Luxembourg", "Malta", "Moldova", "Monaco", "Montenegro", "Netherlands", "North Macedonia", "Norway", "Poland", "Portugal", "Romania", "San Marino", "Serbia", "Slovakia", "Slovenia", "Spain", "Sweden", "Switzerland", "Turkey", "Ukraine", "United Kingdom"});

    public static  final List<String> FUNCTIONS = Arrays.asList(new String[] {"Strategy", "Finance", "Sales and marketing" , "Research and development", "Information technology", "Customer service" , "Human resources", "Design", "Communications" , "Production", "Sourcing", "Quality management" , "Distribution", "Operations"});


    public static String getName() {
        return NAMES.get((int) Math.round(Math.random() * (NAMES.size()-1)));
    }

    public static String getCompany() {
        return COMPAGNIES.get((int) Math.round(Math.random() * (COMPAGNIES.size()-1)));
    }

    public static String getCountry() {
        return COUNTRIES.get((int) Math.round(Math.random() * (COUNTRIES.size()-1)));
    }
}
