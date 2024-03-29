package org.javlo.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.syncro.FileInfo;
import org.javlo.service.syncro.FileStructureFactory;
import org.javlo.service.syncro.SynchroHelper;
import org.javlo.service.syncro.exception.SynchroNonFatalException;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.ztatic.FileCache;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class SynchronisationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final String FILE_INFO = "file_structure.properties";

	public static final String MAILING_PREFIX = "___MAILING___";

	public static final String MAILING_HISTORY_PREFIX = "___MAILING_HISTORY___";

	public static final String TEMPLATE_PREFIX = "___TEMPLATE___";

	public static final String TEMPLATE_MAILING_PREFIX = "___TEMPLATE_MAILING___";

	public static final String SHARE_PREFIX = "___SHARE___";

	public static final String SHYNCRO_CODE_PARAM_NAME = "synchro-code";
	
	public static final String CLEAR_CACHE_SPECIAL_FILE_NAME = "clear_cache_file";

	public static final String PUSH_RESOURCES_DESCRIPTION_URI = "/synchro";

	public static final Logger logger = Logger.getLogger(SynchronisationServlet.class.getName());

	public static void main(String[] args) {
		/*
		 * File targetFile = new File("c:/trans/test.jpg");
		 * 
		 * String targetURL = "http://localhost:8888/dc/synchro/trans/test.jpg";
		 * 
		 * PostMethod filePost = new PostMethod(targetURL); try { System.out.println("Uploading " + targetFile.getName() + " to " + targetURL); Part[] parts = { new FilePart(targetFile.getName(), targetFile) }; filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams())); HttpClient client = new HttpClient(); client.getHttpConnectionManager().getParams().setConnectionTimeout(5000); int status = client.executeMethod(filePost); if (status == HttpStatus.SC_OK) { System.out.println("Upload complete, response=" + filePost.getResponseBodyAsString()); } else { System.out.println("Upload failed, response=" + HttpStatus.getStatusText(status)); } } catch (Exception ex) { System.out.println("ERROR: " + ex.getClass().getName() + " " + ex.getMessage()); ex.printStackTrace(); } finally { filePost.releaseConnection(); }
		 */

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	private List<FileInfo> keepOnly(List<FileInfo> fileTree, Set<String> included) {
		LinkedList<FileInfo> out = new LinkedList<FileInfo>();
		for (FileInfo info : fileTree) {
			if (URLHelper.contains(included, info.getPath(), true)) {
				out.add(info);
			}
		}
		return out;
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		StaticConfig staticConfig = StaticConfig.getInstance(getServletContext());
		if (staticConfig.getSynchroCode() == null) {
			logger.warning("synchro request but no syncho code define.");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);				
			return;
		}
		OutputStream out = null;
		InputStream fileStream = null;
		try {			
			RequestService requestService = RequestService.getInstance(request);
			String clientSynchroCode = requestService.getParameter(SHYNCRO_CODE_PARAM_NAME, null);

			boolean isOnlyStatic = true;
			if (clientSynchroCode == null) {
				GlobalContext globalContext = GlobalContext.getInstance(request);
				IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, request.getSession());
				User user = adminUserFactory.getCurrentUser(globalContext, request.getSession());
				if (user == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);					
					return;
				}
				AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
				if (!adminUserSecurity.haveRight(user, AdminUserSecurity.SYNCHRO_CLIENT)) {
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);					
					return;
				}
				isOnlyStatic = !adminUserSecurity.haveRight(user, AdminUserSecurity.SYNCHRO_ADMIN);
			} else if (!clientSynchroCode.equals(staticConfig.getSynchroCode())) {
				logger.warning("bad synchro code send to SynchronisationServlet");
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);				
				return;
			} else {
				// Synchro with valid synchro code is always complete.
				isOnlyStatic = false;
			}
			// isOnlyStatic = true;//TODO Remove this override for debug

			String fileName = request.getPathInfo();
			if (fileName == null) {
				fileName = "";
				// logger.warning("file name undefined.");
				// response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				// return;
			}
			while (fileName.startsWith("/")) {
				fileName = fileName.substring(1);
			}
			
			if (fileName.equals(CLEAR_CACHE_SPECIAL_FILE_NAME)) {
				FileCache fileCache = FileCache.getInstance(getServletContext());
				fileCache.delete(null,request.getParameter("file"));
				return;
			}

			logger.info("synchro file : " + fileName);

			GlobalContext globalContext = GlobalContext.getInstance(request);
			String baseFolder = globalContext.getDataFolder();
			Set<String> included = null;			
			if (isOnlyStatic) {
				included = new HashSet<String>();
				String folder = staticConfig.getStaticFolder();
				included.add(folder);
				included.add(folder + "/*");
				folder = SynchroHelper.buildSplitFolderPath(folder);
				included.add(folder);
				included.add(folder + "/*");
			}

			boolean manageDeletedFiles = false; //TODO set back to true
			boolean splitBigFiles = false; //TODO set back to true
			if (fileName.startsWith(MAILING_PREFIX)) {
				fileName = fileName.replace(MAILING_PREFIX, "");
				baseFolder = staticConfig.getMailingFolder();
				manageDeletedFiles = false;
				splitBigFiles = false;
			} else if (fileName.startsWith(MAILING_HISTORY_PREFIX)) {
				fileName = fileName.replace(MAILING_HISTORY_PREFIX, "");
				baseFolder = staticConfig.getMailingHistoryFolder();
				manageDeletedFiles = false;
				splitBigFiles = false;
			} else if (fileName.startsWith(TEMPLATE_PREFIX)) {
				fileName = fileName.replace(TEMPLATE_PREFIX, "");
				baseFolder = staticConfig.getTemplateFolder();
				manageDeletedFiles = false;
				splitBigFiles = false;
			} else if (fileName.startsWith(TEMPLATE_MAILING_PREFIX)) {
				fileName = fileName.replace(TEMPLATE_MAILING_PREFIX, "");
				baseFolder = staticConfig.getMailingTemplateFolder();
				manageDeletedFiles = false;
				splitBigFiles = false;
			}			
			fileName = URLHelper.cleanPath(fileName, true);			
			if (fileName.equals(FILE_INFO)) {
				FileStructureFactory fsf = FileStructureFactory.getInstance(new File(baseFolder));
				List<FileInfo> fileTree = fsf.fileTreeToList(manageDeletedFiles, manageDeletedFiles);
				if (included != null) {
					fileTree = keepOnly(fileTree, included);
				}
				if (splitBigFiles && SynchroHelper.splitBigFiles(new File(baseFolder), FileStructureFactory.asMapByPath(fileTree))) {
					fileTree = fsf.fileTreeToList(manageDeletedFiles, manageDeletedFiles);
					if (included != null) {
						fileTree = keepOnly(fileTree, included);
					}
				}
				out = response.getOutputStream();
				FileStructureFactory.writeToStream(fileTree, out);
			} else {
				if (included != null) {
					if (!URLHelper.contains(included, fileName, true)) {
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
				}

				boolean isMultipart = JakartaServletFileUpload.isMultipartContent(request);

				if (isMultipart) {
					Collection<FileItem> fileIme = requestService.getAllFileItem();
					if (fileIme.size() > 0) {
						for (FileItem fileItem : fileIme) {
							fileName = URLHelper.mergePath(baseFolder, fileName);
							File uploadedFile = new File(fileName);
							InputStream in = fileItem.getInputStream();
							try {
								ResourceHelper.writeStreamToFile(in, uploadedFile);
							} catch (IOException e) {
								e.printStackTrace();
							} finally {
								ResourceHelper.closeResource(in);
							}
						}

						BufferedWriter outWrt = new BufferedWriter(response.getWriter());
						outWrt.append("syncro servlet : 1 file uploaded.");
						outWrt.close();
					} else {
						// BufferedWriter outWrt = new BufferedWriter(response.getWriter());
						// outWrt.append("syncro servlet : no file found.");
						// outWrt.close();
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no file found in request.");
					}

				} else { /* not multipart */

					fileName = URLHelper.mergePath(baseFolder, fileName);

					if (request.getParameter("delete") != null) {
						ResourceHelper.deleteFileAndParentDir(new File(fileName));
					} else if (request.getParameter("refresh") != null) {
						ContentContext ctx = ContentContext.getContentContext(request, response);						
						ContentService.clearCache(ctx, globalContext);
						ContentService.getInstance(globalContext).getNavigation(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE));
						ContentService.getInstance(globalContext).getNavigation(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE));
						PersistenceService.getInstance(globalContext).cleanFile();
					} else if (request.getParameter("refresh-all") != null) {
						ContentContext ctx = ContentContext.getContentContext(request, response);						
						ContentService.getInstance(globalContext).getNavigation(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE));
						ContentService.getInstance(globalContext).getNavigation(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE));
						ContentService.clearAllCache(ctx, globalContext);
						PersistenceService.getInstance(globalContext).cleanFile();
					} else if (request.getParameter("context") != null) {
						// System.out.println("****************************");
						// System.out.println(request.getParameter("context"));
						// System.out.println("****************************");
						String folder = globalContext.getFolder();
						globalContext.setAllValues(request.getParameter("context"));
						globalContext.setFolder(folder);
						globalContext.setDMZServerInter(null); // no DMZ
						globalContext.setDMZServerIntra(null); // no DMZ
						// server,
						// this
						// instance
						// is DMZ
						// server
					} else if (request.getParameter("mergeBigFile") != null) {
						try {
							String checksum = requestService.getParameter("checksum", null);
							SynchroHelper.rebuildSplitted(baseFolder, fileName, checksum);
						} catch (SynchroNonFatalException ex) {
							response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, ex.getMessage());
						}
					} else {
						response.setContentType(ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(fileName)));
						out = response.getOutputStream();
						File file = new File(fileName);
						if (!file.isDirectory()) {
							fileStream = new FileInputStream(file);
							ResourceHelper.writeStreamToStream(fileStream, out);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e.getMessage(), e);
		} finally {
			if (fileStream != null) {
				try {
					fileStream.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
			// request.getSession().invalidate();
		}

	}
}
