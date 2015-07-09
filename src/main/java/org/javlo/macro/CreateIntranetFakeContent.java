package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.service.ContentService;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;

public class CreateIntranetFakeContent extends AbstractMacro {
	
	private static final String NAMES = "Shira Shire,Carter Cashion,Marhta Montalvo,Elouise Evett,Babette Betz,Roma Raine,Delorse Dyar,Markita Machado,Dedra Dileo,Gwenn Gallucci,Shara Story,Liberty Loredo,Nevada Nader,Marinda Mcconnel,Betty Bassler,Sindy Sippel,Jude Janas,Daryl Dardar,Lynn Lown,Gina Giesler,Susanna Sokol,Sue Spriggs,Lacresha Leis,Nola Navarette,Wai Wiggins,Corie Conaway,Sean Schleich,Georgie Gambino,Daina Desjardin,Chiquita Cho,Terrie Tarpley,Jenine Jarnagin,Charlesetta Cabral,Millicent Mondragon,Christian Cafferty,Letha Lambros,Argentina Allyn,Han Husman,Ima Inman,Vernie Verdin,Micah Molyneux,Kiersten Kina,Natacha Nesbitt,Len Lewis,Tillie Tricarico,Nguyet Nuss,Tena Thorsen,Jame Jerome,Alberta Anastasio,Pennie Pollard";

	@Override
	public String getName() {
		return "create-intranet-fake-content";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		
		ContentService content = ContentService.getInstance(ctx.getRequest());
		if (content.getNavigation(ctx).getChildList().length>0) {
			return "site must be empty.";
		} else {
			IUserFactory userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
			int i=0;
			for (String name : NAMES.split(",")) {
				i++;
				IUserInfo userInfo = new UserInfo();
				userInfo.setLogin("login"+i);
				userInfo.setPassword("changeme");
				userInfo.setFirstName(name.split(" ")[0]);
				userInfo.setLastName(name.split(" ")[1]);
				userInfo.setEmail(name.split(" ")[0]+'.'+name.split(" ")[1]+"@javlo.org");
				userFactory.addUserInfo(userInfo);
			}
		}
		
		
		return null;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

}
