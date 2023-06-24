package org.javlo.macro;

import org.javlo.context.ContentContext;
import org.javlo.helper.SecurityHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.image.ImageEngine;
import org.javlo.user.IUserFactory;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;
import org.javlo.ztatic.FileCache;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class CreateFakeUsers extends AbstractMacro {

	private static final String NAMES = "Shira Shire,Carter Cashion,Marhta Montalvo,Elouise Evett,Babette Betz,Roma Raine,Delorse Dyar,Markita Machado,Dedra Dileo,Gwenn Gallucci,Shara Story,Liberty Loredo,Nevada Nader,Marinda Mcconnel,Betty Bassler,Sindy Sippel,Jude Janas,Daryl Dardar,Lynn Lown,Gina Giesler,Susanna Sokol,Sue Spriggs,Lacresha Leis,Nola Navarette,Wai Wiggins,Corie Conaway,Sean Schleich,Georgie Gambino,Daina Desjardin,Chiquita Cho,Terrie Tarpley,Jenine Jarnagin,Charlesetta Cabral,Millicent Mondragon,Christian Cafferty,Letha Lambros,Argentina Allyn,Han Husman,Ima Inman,Vernie Verdin,Micah Molyneux,Kiersten Kina,Natacha Nesbitt,Len Lewis,Tillie Tricarico,Nguyet Nuss,Tena Thorsen,Jame Jerome,Alberta Anastasio,Pennie Pollard";

	private static final List<String> COMPAGNIES = Arrays.asList(new String[] {"Amnesty International", "Médecins Sans Frontières", "Greenpeace", "Oxfam", "Save the Children", "WWF", "Human Rights Watch", "ActionAid", "CARE International", "Friends of the Earth"});

	private static  final List<String> COUNTRIES = Arrays.asList(new String[] {"Albania", "Andorra", "Armenia", "Austria", "Azerbaijan", "Belarus", "Belgium", "Bulgaria", "Croatia", "Cyprus", "Czech Republic", "Denmark", "Estonia", "Finland", "France", "Georgia", "Germany", "Greece", "Hungary", "Iceland", "Ireland", "Italy", "Kazakhstan", "Kosovo", "Latvia", "Lithuania", "Luxembourg", "Malta", "Moldova", "Monaco", "Montenegro", "Netherlands", "North Macedonia", "Norway", "Poland", "Portugal", "Romania", "San Marino", "Serbia", "Slovakia", "Slovenia", "Spain", "Sweden", "Switzerland", "Turkey", "Ukraine", "United Kingdom"});

	@Override
	public String getName() {
		return "create-3-fake-users";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		IUserFactory userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());

		List<String> firstName = new LinkedList<String>();
		List<String> lastName = new LinkedList<String>();
		for (String name : NAMES.split(",")) {
			firstName.add(name.split(" ")[0].trim());
			lastName.add(name.split(" ")[1].trim());
		}
		int userNumber = 1;
		for (int i = 0; i < 3; i++) {
			UserInfo userInfo = new UserInfo();
			String userName = "login"+StringHelper.renderNumber(userNumber, 4);
			while (userFactory.getUser(userName) != null) {
				userNumber++;
				userName = "login"+StringHelper.renderNumber(userNumber, 4);
			}
			userInfo.setLogin(userName);
			userInfo.setPassword(SecurityHelper.encryptPassword("changeme"));
			userInfo.setFirstName(firstName.get((int) Math.round(Math.random() * (firstName.size()-1))));
			userInfo.setLastName(lastName.get((int) Math.round(Math.random() * (lastName.size()-1))));
			userInfo.setOrganization(COMPAGNIES.get((int) Math.round(Math.random() * (COMPAGNIES.size()-1))));
			userInfo.setCountry(COUNTRIES.get((int) Math.round(Math.random() * (COUNTRIES.size()-1))));
			userInfo.setEmail(userInfo.getFirstName().toLowerCase() + '.' + userInfo.getLastName().toLowerCase() + "@javlo.org");
			Random random = new Random();
			if (random.nextInt(2) == 0) {
				userInfo.setPhone(generateRandomPhoneNumber());
			}
			if (random.nextInt(2) == 0) {
				userInfo.setMobile(generateRandomMobileNumber());
			}
			userInfo.setRoles(new HashSet<String>(Arrays.asList(new String[] { "test" })));
			userFactory.addUserInfo(userInfo);
			createAvatar(ctx, userInfo);
		}
		userFactory.store();

		return null;
	}

	public static String generateRandomPhoneNumber() {
		Random random = new Random();
		int firstThreeDigits = random.nextInt(900) + 100; // Génère un nombre aléatoire entre 100 et 999
		int secondTwoDigits = random.nextInt(90) + 10; // Génère un nombre aléatoire entre 10 et 99
		int thirdTwoDigits = random.nextInt(90) + 10; // Génère un nombre aléatoire entre 10 et 99

		return "+32 2 " + firstThreeDigits + " " + secondTwoDigits + " " + thirdTwoDigits;
	}

	public static String generateRandomMobileNumber() {
		Random random = new Random();
		int firstThreeDigits = random.nextInt(90) + 10; // Génère un nombre aléatoire entre 10 et 99
		int secondTwoDigits = random.nextInt(90) + 10; // Génère un nombre aléatoire entre 10 et 99
		int thirdTwoDigits = random.nextInt(90) + 10; // Génère un nombre aléatoire entre 10 et 99

		return "+32 475 " + firstThreeDigits + " " + secondTwoDigits + " " + thirdTwoDigits;
	}

	public void createAvatar(ContentContext ctx, UserInfo userInfo) throws IOException {
		String avatarFileName = userInfo.getLogin() + ".png";
		File avatarFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getAvatarFolder(), avatarFileName));
		URL url = new URL("https://thispersondoesnotexist.com/");
		URLConnection conn = url.openConnection();
		try (InputStream in = conn.getInputStream()) {
			BufferedImage img = ImageIO.read(in);
			img = ImageEngine.resizeWidth(img, 255, true);
			avatarFile.getParentFile().mkdirs();
			ImageIO.write(img, "png", avatarFile);
		}
		FileCache.getInstance(ctx.getRequest().getSession().getServletContext()).deleteAllFile(ctx.getGlobalContext().getContextKey(), avatarFileName);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isPreview() {
		return false;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}
}
