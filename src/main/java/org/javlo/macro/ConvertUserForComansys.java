package org.javlo.macro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.utils.Cell;
import org.javlo.utils.XLSTools;

public class ConvertUserForComansys extends AbstractMacro {

	@Override
	public String getName() {
		return "users-file-to-comansys";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		HttpServletResponse response = ctx.getResponse();
		String fileName = "users.xlsx";
		response.setContentType(ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(fileName)));
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Accept-Ranges", "bytes");				
		response.setHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"");
		
		IUserFactory uf = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		List<IUserInfo> users = uf.getUserInfoList();
		Cell[][] arrays = XLSTools.createArray(29, users.size()+1);
		
		int i=0;
		arrays[0][i].setValue("Title"); i++;
		arrays[0][i].setValue("Job title (Job Position)"); i++;
		int gender = i;
		arrays[0][i].setValue("Gender"); i++;
		int lastname = i;
		arrays[0][i].setValue("Lastname (*)"); i++;
		int firstname = i;
		arrays[0][i].setValue("Firstname (*)"); i++;
		arrays[0][i].setValue("Category of contact (*)"); i++;
		arrays[0][i].setValue("Sector of activity (*)"); i++;
		arrays[0][i].setValue("Preferred Language"); i++;
		arrays[0][i].setValue("Address (Street)"); i++;
		arrays[0][i].setValue("Post code (Zip)"); i++;
		arrays[0][i].setValue("City (EN)"); i++;
		arrays[0][i].setValue("City (OR)"); i++;
		arrays[0][i].setValue("State / Region (Local)"); i++;
		arrays[0][i].setValue("Country (EN) (*)"); i++;
		arrays[0][i].setValue("Country (OR)"); i++;
		int email = i;
		arrays[0][i].setValue("Email (*)"); i++;
		arrays[0][i].setValue("Email 1"); i++;
		arrays[0][i].setValue("Email 2"); i++;
		arrays[0][i].setValue("Website"); i++;
		arrays[0][i].setValue("Phone"); i++;
		arrays[0][i].setValue("Private phone"); i++;		
		arrays[0][i].setValue("Mobile"); i++;
		arrays[0][i].setValue("Fax"); i++;
		arrays[0][i].setValue("Social media contact"); i++;
		arrays[0][i].setValue("Keywords"); i++;
		arrays[0][i].setValue("Name of Organisation"); i++;
		int tags = i;
		arrays[0][i].setValue("Tags"); i++;
		int notes = i;
		arrays[0][i].setValue("Internal Notes"); i++;
		arrays[0][i].setValue("Birthdate"); i++;
		
		int j=1;
		for (IUserInfo user : users) {
			arrays[j][gender].setValue(user.getGender());
			arrays[j][lastname].setValue(user.getLastName());
			arrays[j][firstname].setValue(user.getFirstName());
			arrays[j][email].setValue(user.getEmail());
			arrays[j][tags].setValue(StringHelper.collectionToString(user.getRoles(), ","));
			arrays[j][notes].setValue(StringHelper.renderDate(user.getCreationDate()));
			j++;
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XLSTools.writeXLSX(arrays, out);
		response.setContentLength(out.size());
		InputStream in = new ByteArrayInputStream(out.toByteArray());
		ResourceHelper.writeStreamToStream(in, response.getOutputStream(), -1);
		
		ctx.setStopRendering(true);
		
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
	
	@Override
	public boolean isAdmin() {
		return false;
	}

};
