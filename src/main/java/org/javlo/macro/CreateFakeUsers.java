package org.javlo.macro;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;

public class CreateFakeUsers extends AbstractMacro {

	private static final String NAMES = "Shira Shire,Carter Cashion,Marhta Montalvo,Elouise Evett,Babette Betz,Roma Raine,Delorse Dyar,Markita Machado,Dedra Dileo,Gwenn Gallucci,Shara Story,Liberty Loredo,Nevada Nader,Marinda Mcconnel,Betty Bassler,Sindy Sippel,Jude Janas,Daryl Dardar,Lynn Lown,Gina Giesler,Susanna Sokol,Sue Spriggs,Lacresha Leis,Nola Navarette,Wai Wiggins,Corie Conaway,Sean Schleich,Georgie Gambino,Daina Desjardin,Chiquita Cho,Terrie Tarpley,Jenine Jarnagin,Charlesetta Cabral,Millicent Mondragon,Christian Cafferty,Letha Lambros,Argentina Allyn,Han Husman,Ima Inman,Vernie Verdin,Micah Molyneux,Kiersten Kina,Natacha Nesbitt,Len Lewis,Tillie Tricarico,Nguyet Nuss,Tena Thorsen,Jame Jerome,Alberta Anastasio,Pennie Pollard";

	@Override
	public String getName() {
		return "create-10-fake-users";
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
		for (int i = 0; i < 10; i++) {
			IUserInfo userInfo = new UserInfo();
			String userName = "login"+StringHelper.renderNumber(userNumber, 4);
			while (userFactory.getUser(userName) != null) {
				userNumber++;
				userName = "login"+StringHelper.renderNumber(userNumber, 4);
			}
			userInfo.setLogin(userName);
			userInfo.setPassword("changeme");
			userInfo.setFirstName(firstName.get((int) Math.round(Math.random() * (firstName.size()-1))));
			userInfo.setLastName(lastName.get((int) Math.round(Math.random() * (lastName.size()-1))));
			userInfo.setEmail(userInfo.getFirstName().toLowerCase() + '.' + userInfo.getLastName().toLowerCase() + "@javlo.org");
			userInfo.setRoles(new HashSet<String>(Arrays.asList(new String[] { "test" })));
			userFactory.addUserInfo(userInfo);
		}
		userFactory.store();

		return null;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

}
