package org.javlo.test.javlo;

import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class TestPersistenceService extends PersistenceService {
	
	private static String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<content cmsversion=\"2.0.2.3\" version=\"165\">\n<page id=\"0\" name=\"root\" creationDate=\"2012/10/18 10:57:08\" modificationDate=\"2013/06/19 23:16:00\" latestEditor=\"pvandermaesen\" priority=\"1\" layout=\"coloregiallo\" visible=\"true\" shorturl=\"Uaaa\">\n<component id=\"135058795678815819450\" type=\"title\" language=\"fr\" authors=\"\" creationDate=\"19/06/2013 23:26:48\" modificationDate=\"19/06/2013 23:26:48\" ><![CDATA[home]]></component>\n<component id=\"135058796557458780253\" type=\"paragraph\" language=\"fr\" authors=\"\" creationDate=\"19/06/2013 23:26:48\" modificationDate=\"19/06/2013 23:26:48\" style=\"normal\" ><![CDATA[premi�re page.]]></component>\n<page id=\"135058812739852786865\" name=\"page1\" creationDate=\"2012/10/18 21:22:07\" creator=\"demo\" modificationDate=\"2013/06/19 23:43:16\" latestEditor=\"pvandermaesen\" priority=\"10\" visible=\"true\" shorturl=\"Ubaa\">\n<component id=\"137167818352384633803\" type=\"global-image\" area=\"highlight\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:43:03\" modificationDate=\"19/06/2013 23:43:03\" style=\"image-left\" renderer=\"no-label\" ><![CDATA[#file storage V.1.1 #Wed Jun 19 23:17:48 CEST 2013 label= dir= description= encoding=default image-filter=full file-name=desert.jpg reverse-lnk=none ]]></component>\n<component id=\"137167816530488831827\" type=\"title\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:42:45\" modificationDate=\"19/06/2013 23:42:45\" ><![CDATA[home]]></component>\n<component id=\"137167816540019461995\" type=\"description\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:42:45\" modificationDate=\"19/06/2013 23:42:45\" style=\"visible\" ><![CDATA[agnaSed nisi quam varius in ultrices non, auctor in dui. Nulla ac nisl lectus, id pellentesque nisi. Vivamus dapibus odio nec eros venenatis nec pulvinar orci lobortis. Duis mattis dolor sed nibh posuere a mollis libero suscipit. Aliquam rutrum ]]></component>\n<component id=\"137167816546148219823\" type=\"global-image\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:42:45\" modificationDate=\"19/06/2013 23:42:45\" style=\"image-center\" renderer=\"no-label\" ><![CDATA[#file storage V.1.1 #Wed Jun 19 23:19:16 CEST 2013 label= dir= description= encoding=default image-filter=full file-name=cinquentenaires_bxl.jpg reverse-lnk=none ]]></component>\n<component id=\"137167816555459382781\" type=\"paragraph\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:42:45\" modificationDate=\"19/06/2013 23:42:45\" style=\"normal\" ><![CDATA[torconsectetur Curabitur vel quam quis urna accumsan pulvinar faucibus sed arcu. Fusce ac magna libero, at tempor turpis. In hac habitasse platea dictumst. Vivamus tempor, arcu non fermentum hendrerit, sapien urna semper lacus, vel sagittis ante lorem et lacus. Maecenas ac mattis dui. Praesent nunc ante, bibendum sed fermentum quis, scelerisque ut nisi. Maecenas sit amet neque urna. Quisque et quam eu turpis sagittis cursus. Fusce ante ante, cursus id adipiscing id, egestas id enim. Vestibulum ante ipsum primis ]]></component>\n<component id=\"137167816555785539518\" type=\"text-list\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:42:45\" modificationDate=\"19/06/2013 23:42:45\" style=\"ul-ul\" ><![CDATA[mauriset pulvinar tristique arcu elit consequat nisl . apienvehicula vitae egestas magna consectetur Vestibulum eget . ommodoid porta vitae lectus Pellentesque habitant morbi . dolornibh et lorem Sed sollicitudin luctus fermentum . mollislibero Donec ultricies scelerisque leo eu faucibus . porttitorfringilla Suspendisse lacinia consequat facilisis Donec placerat . salobortis odio et fermentum lectus sem eu . ]]></component>\n</page>\n<page id=\"135058813246581815292\" name=\"page2\" creationDate=\"2012/10/18 21:22:12\" creator=\"demo\" modificationDate=\"2013/06/19 23:43:57\" latestEditor=\"pvandermaesen\" priority=\"20\" visible=\"true\">\n<component id=\"137167823270411577864\" type=\"global-image\" area=\"highlight\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:43:52\" modificationDate=\"19/06/2013 23:43:52\" style=\"image-left\" renderer=\"no-label\" ><![CDATA[#file storage V.1.1 #Wed Jun 19 23:22:36 CEST 2013 label= dir=import/contact description= encoding=default file-name=1001864_58140113.jpg image-filter=full reverse-lnk=none ]]></component>\n<component id=\"137167821276450462434\" type=\"title\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:43:32\" modificationDate=\"19/06/2013 23:44:05\" ><![CDATA[Contact]]></component>\n<component id=\"137167821285174205769\" type=\"smart-generic-form\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:43:32\" modificationDate=\"19/06/2013 23:44:05\" renderer=\"default\" ><![CDATA[#comp:137167763620566413547 #Wed Jun 19 23:31:58 CEST 2013 filename=contact.csv mail.from.field= message.thanks=Thanks for you participation mail.bcc= field.Lastname=Lastname|text|||20||20 mail.subject= mail.subject.field= mail.to=test@javlo.org label.captcha=captcha mail.from= captcha=on field.Firstname=Firstname|text|||10||10 error.required=please fill all requiered fields message.error=Sorry, technical errror. title= mail.cc= field.Like=Do you like  ?|yes-no|||30||30 ]]></component>\n<page id=\"137167765972988423470\" name=\"media\" creationDate=\"2013/06/19 23:32:14\" creator=\"pvandermaesen\" modificationDate=\"2013/06/19 23:44:46\" latestEditor=\"pvandermaesen\" priority=\"30\" visible=\"true\">\n<component id=\"137167826995192283220\" type=\"title\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:44:29\" modificationDate=\"19/06/2013 23:44:30\" ><![CDATA[media]]></component>\n<component id=\"137167826998383647525\" type=\"multimedia\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:44:29\" modificationDate=\"19/06/2013 23:44:29\" style=\"image\" ><![CDATA[%%12,128%/gallery/import/media%%%]]></component>\n<component id=\"137167824967098372302\" type=\"global-image\" area=\"highlight\" language=\"en\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:44:09\" modificationDate=\"19/06/2013 23:44:09\" style=\"image-left\" renderer=\"no-label\" ><![CDATA[#file storage V.1.1 #Wed Jun 19 23:34:18 CEST 2013 label= dir= description= encoding=default image-filter=full file-name=1248972_30284718.jpg reverse-lnk=none ]]></component>\n<component id=\"137167765977363165248\" type=\"title\" language=\"nl\" authors=\"pvandermaesen\" creationDate=\"19/06/2013 23:32:14\" modificationDate=\"19/06/2013 23:32:14\" ><![CDATA[media]]></component>\n</page>\n</page>\n</page>\n<properties name=\"global\">\n<property key=\"staticinfo-/images/import/home/desert.jpg-focus-zone-x\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/gallery/import/media/1194841_97442879.jpg-focus-zone-y\"><![CDATA[265]]></property>\n<property key=\"staticinfo-/gallery/import/media/1194841_97442879.jpg-focus-zone-x\"><![CDATA[548]]></property>\n<property key=\"staticinfo-/images/1248972_30284718.jpg-shared\"><![CDATA[false]]></property>\n<property key=\"staticinfo-/gallery/import/media/1214436_26967451.jpg-focus-zone-y\"><![CDATA[318]]></property>\n<property key=\"staticinfo-/gallery/import/media/1208847_35671158.jpg-focus-zone-y\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/gallery/import/media/1214436_26967451.jpg-focus-zone-x\"><![CDATA[460]]></property>\n<property key=\"staticinfo-/images/default.jpg-focus-zone-y\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/gallery/import/media/1208847_35671158.jpg-focus-zone-x\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/images/default.jpg-focus-zone-x\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/gallery/import/media/1327279_97873045.jpg-focus-zone-y\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/gallery/import/media/1327279_97873045.jpg-focus-zone-x\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/images/1248972_30284718.jpg-focus-zone-y\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/images/1248972_30284718.jpg-focus-zone-x\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/gallery/import/media/1281865_76881485.jpg-focus-zone-y\"><![CDATA[375]]></property>\n<property key=\"staticinfo-/gallery/import/media/1281865_76881485.jpg-focus-zone-x\"><![CDATA[564]]></property>\n<property key=\"staticinfo-/images/import/contact/1001864_58140113.jpg-focus-zone-y\"><![CDATA[313]]></property>\n<property key=\"staticinfo-/images/import/contact/1001864_58140113.jpg-focus-zone-x\"><![CDATA[153]]></property>\n<property key=\"staticinfo-/gallery/import/media/1195463_12959657.jpg-focus-zone-y\"><![CDATA[239]]></property>\n<property key=\"staticinfo-/gallery/import/media/1195463_12959657.jpg-focus-zone-x\"><![CDATA[212]]></property>\n<property key=\"staticinfo-/gallery/import/media/1186871_30612039.jpg-focus-zone-y\"><![CDATA[589]]></property>\n<property key=\"staticinfo-/gallery/import/media/1186871_30612039.jpg-focus-zone-x\"><![CDATA[541]]></property>\n<property key=\"staticinfo-/gallery/import/media/923_9293.jpg-focus-zone-y\"><![CDATA[481]]></property>\n<property key=\"staticinfo-/gallery/import/media/923_9293.jpg-focus-zone-x\"><![CDATA[604]]></property>\n<property key=\"staticinfo-/images/cinquentenaires_bxl.jpg-linked-page-id\"><![CDATA[0]]></property>\n<property key=\"staticinfo-/images/cinquentenaires_bxl.jpg-shared\"><![CDATA[false]]></property>\n<property key=\"staticinfo-/images/cinquentenaires_bxl.jpg-focus-zone-y\"><![CDATA[368]]></property>\n<property key=\"staticinfo-/gallery/import/media/1248972_30284718.jpg-focus-zone-y\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/images/cinquentenaires_bxl.jpg-focus-zone-x\"><![CDATA[668]]></property>\n<property key=\"staticinfo-/gallery/import/media/1248972_30284718.jpg-focus-zone-x\"><![CDATA[500]]></property>\n<property key=\"user.update\"><![CDATA[pvandermaesen]]></property>\n<property key=\"staticinfo-/images/desert.jpg-shared\"><![CDATA[false]]></property>\n<property key=\"staticinfo-/images/desert.jpg-focus-zone-y\"><![CDATA[643]]></property>\n<property key=\"staticinfo-/gallery/import/media/1191009_95687231.jpg-focus-zone-y\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/images/desert.jpg-focus-zone-x\"><![CDATA[658]]></property>\n<property key=\"staticinfo-/gallery/import/media/1191009_95687231.jpg-focus-zone-x\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/gallery/import/media/1192481_42407892.jpg-focus-zone-y\"><![CDATA[339]]></property>\n<property key=\"staticinfo-/gallery/import/media/1192481_42407892.jpg-focus-zone-x\"><![CDATA[369]]></property>\n<property key=\"staticinfo-/gallery/import/media/1210450_17583979.jpg-focus-zone-y\"><![CDATA[474]]></property>\n<property key=\"staticinfo-/gallery/import/media/1210450_17583979.jpg-focus-zone-x\"><![CDATA[589]]></property>\n<property key=\"staticinfo-/images/1248972_30284718.jpg-linked-page-id\"><![CDATA[137167765972988423470]]></property>\n<property key=\"staticinfo-/gallery/import/media/1213518_51314109.jpg-focus-zone-y\"><![CDATA[478]]></property>\n<property key=\"staticinfo-/gallery/import/media/1213518_51314109.jpg-focus-zone-x\"><![CDATA[518]]></property>\n<property key=\"staticinfo-/gallery/import/media/1210451_12965007.jpg-focus-zone-y\"><![CDATA[343]]></property>\n<property key=\"staticinfo-/images/import/home/desert.jpg-focus-zone-y\"><![CDATA[500]]></property>\n<property key=\"staticinfo-/gallery/import/media/1210451_12965007.jpg-focus-zone-x\"><![CDATA[514]]></property>\n<property key=\"staticinfo-/images/desert.jpg-linked-page-id\"><![CDATA[135058812739852786865]]></property>\n</properties>\n</content>";
	
	@Override
	protected MenuElement load(ContentContext ctx, int renderMode, Map<String, String> contentAttributeMap, Date timeTravelDate, boolean correctXML, Integer version) throws Exception {
		Reader in = new StringReader(data);
		LoadingBean bean = load(ctx,in,contentAttributeMap, renderMode);
		in.close();
		return bean.getRoot();
	}

}
