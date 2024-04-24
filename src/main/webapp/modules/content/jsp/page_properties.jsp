<<%@ taglib uri="jakarta.tags.core" prefix="c"%><%@ taglib prefix="fn" uri="jakarta.tags.functions"%><div class="widgetbox page-properties">
	<h3>
		<span>${i18n.edit['item.title']}</span>
	</h3>

	<div class="content">

		<form id="form-page-properties" class="standard-form" action="${info.currentURL}" method="post">

			<c:if test="${globalContext.portail}">
				<div class="flow">
					<h4>${i18n.edit['flow.title']}</h4>
					<p>
						<button class="btn btn-default btn-sm ${info.page.flowIndex==1?'btn-primary':'btn-disabled'}" disabled="disabled">Modify</button>
						&nbsp; <span class="glyphicon glyphicon-arrow-right"></span>&nbsp;
						<button class="btn btn-default btn-sm ${info.page.flowIndex==2?'btn-primary':''}" disabled="disabled">Ready</button>
						&nbsp; <span class="glyphicon glyphicon-arrow-right"></span>&nbsp;
						<button class="btn btn-default btn-sm ${info.page.flowIndex==3?'btn-primary':''}" disabled="disabled">Validated</button>
						&nbsp; <span class="glyphicon glyphicon-arrow-right"></span>&nbsp;
						<button class="btn btn-default btn-sm ${info.page.flowIndex==4?'btn-primary':''}" disabled="disabled">Live</button>
					</p>
					<c:if test="${info.admin}">
						<label class="checkbox-inline"><input type="checkbox" name="noval" id="noval" ${info.page.noValidation?' checked="checked"':''}>no validation</label>
					</c:if>
				</div>
			</c:if>

			<div>
				<input type="hidden" name="name" value="${page.name}" />
				<input type="hidden" name="page" value="${page.id}" />
			</div>

			<div class="row">
				<div class="col-md-6">

					<c:if test="${!userInterface.light}">
						<div class="row">
							<div class="col-md-6">
								<div class="form-group template">
									<label for="template">${i18n.edit['item.template']}</label>
									<select id="template" name="template" class="form-control">
										<c:if test="${not empty inheritedTemplate}">
											<option value="">${i18n.edit['global.inherited']}(${inheritedTemplate.name})</option>
										</c:if>
										<c:set var="templateFound" value="${empty page.templateId}" />
										<c:forEach var="template" items="${templates}">
											<option value="${template.name}" ${template.name eq page.templateId?'selected="selected"':''}>${template.name}</option>
											<c:if test="${template.name eq page.templateId}">
												<c:set var="templateFound" value="${true}" />
											</c:if>
										</c:forEach>
										<c:if test="${not templateFound}">
											<option value="${page.templateId}" selected="selected">*${page.templateId}</option>
										</c:if>
									</select>
								</div>
								<c:if test="${userInterface.light}">
									<c:set var="found" value="false" />
									<c:forEach var="template" items="${templates}">
										<c:if test="${template.name eq page.templateId}">
											<div class="line">
												<label>${i18n.edit['item.template']} : </label>${template.name}</div>
											<c:set var="found" value="true" />
										</c:if>
									</c:forEach>
									<c:if test="${not found}">
										<div class="line">
											<label>${i18n.edit['item.template']} : </label>${i18n.edit['global.inherited']} (${inheritedTemplate.name})
										</div>
									</c:if>
								</c:if>
								<div class="form-group">
									<label for="new_name">${i18n.edit['item.name']}</label>
									<input type="text" class="form-control" id="new_name" name="new_name" value="${page.humanName}" />
								</div>
							</div>
							<div class="col-md-6">
								<div class="screenshot">
									<c:if test="${not empty page.screenshotUrl}">
										<a href="${page.screenshotUrl}" target="_blank"> <img id="screenshot-img" src="${page.screenshotUrl}" alt="screenshot" /></a>
									</c:if>
									<button class="btn btn-primary btn-sm" onclick="closePopup(); window.open('${takeSreenshotUrl}'); return false;">update screenshot</button>
								</div>
							</div>
						</div>
					</c:if>
					<c:if test="${taxonomy.active}">
						<div class="taxonomy">
							<label for="taxonomy">${i18n.edit ['taxonomy']}</label> ${taxonomySelect}
						</div>
					</c:if>
					<c:if test="${not globalContext.mailingPlatform}">
						<fieldset class="collapsable time-range-fieldset">
							<legend>${i18n.edit['item.time-range']} (${i18n.edit['item.time-published']}:${page.insideTimeRange})</legend>
							<div class="row">
								<div class="col-sm-6">
									<label for="start_publish">${i18n.edit['item.start-publish']} <input class="datepicker form-control" type="text" id="start_publish" name="start_publish" value="${page.startPublish}" />
									</label>
								</div>
								<div class="col-sm-6">
									<label for="end_publish">${i18n.edit['item.end-publish']} <input class="datepicker form-control" type="text" id="end_publish" name="end_publish" value="${page.endPublish}" />
									</label>
								</div>
							</div>
						</fieldset>
					</c:if>

					<fieldset class="bloc-info collapsable">
						<legend>information</legend>
						<div class="row">
							<div class="col-sm-8">
								<div class="line">
									<label>${i18n.edit['global.title']}</label> <span>${page.info.title}</span>
								</div>
							</div>
							<c:if test="${info.admin}">
								<div class="col-sm-4">
									<div class="line">
										<label>ID</label><span>${page.info.id}</span>
									</div>
								</div>
							</c:if>
						</div>
						<div class="row">
							<div class="col-sm-4">
								<div class="line">
									<label>${i18n.edit['item.modificator']}</label> <span>${page.latestEditor}</span>
								</div>
							</div>
							<div class="col-sm-4">
								<div class="line">
									<label>${i18n.edit['item.creator']}</label> <span>${page.creator}</span>
								</div>
							</div>

							<div class="qrcode col-sm-4">
								<div class="line">
									<label><a href="${info.shortURL}" target="_blank">short url</a></label> <a href="${info.qrCodeURL}" target="_blank">QR Code</a>
								</div>
							</div>

						</div>
						<div class="row">
							<div class="col-sm-4">
								<div class="line">
									<label>${i18n.edit['item.creation-date']}</label> <span>${page.creationDate}</span>
								</div>
							</div>
							<div class="col-sm-4">
								<div class="line">
									<label>${i18n.edit['item.modification-date']}</label> <span>${page.modificationDate}</span>
								</div>
							</div>
							<div class="col-sm-4">
								<div class="line">
									<label>${i18n.edit['item.content-date']}</label>
									<c:if test="${page.timeRange == null || page.timeRange.startDate == page.timeRange.endDate}">
										<span>${page.contentDateValue}</span>
									</c:if>
									<c:if test="${page.timeRange != null && page.timeRange.startDate != page.timeRange.endDate}">
										<span>${page.timeRange.renderDates}</span>
									</c:if>
								</div>

							</div>
						</div>
						<div class="row">
							<div class="col-sm-4">
								<div class="line">
									<label>${i18n.edit['item.path']}</label> <span>${page.path}</span>
								</div>
							</div>
							<div class="col-sm-4">
								<div class="line">
									<label>${i18n.edit['item.last-access']}</label> <span>${page.lastAccess}</span>
								</div>
							</div>
							<div class="col-sm-4">
								<div class="line">
									<label>${i18n.edit['item.page-rank']}</label> <span>${page.pageRank}</span>
								</div>
							</div>
						</div>
						<div class="row">
							<div class="col-sm-4">
								<div class="line">
									<label>Cachable?</label> <span>${page.cacheable} <c:if test="${!page.cacheable}">(${page.notCacheableComponent})</c:if></span>
								</div>
							</div>
							<div class="col-sm-4">
								<div class="line">
									<label>Real Content?</label> <span>${page.realContent} <c:if test="${page.realContent}">(${page.realContentComponent})</c:if> <c:if test="${!page.realContent && not empty page.realContentLanguage}"> [content found : ${page.realContentLanguage}]</c:if></span>
								</div>
							</div>
							<div class="col-sm-4">
								<div class="line">
									<label>Image</label> <span><c:if test="${empty page.info.imageBean}">-</c:if> <c:if test="${not empty page.info.imageBean}">
											<a href="${page.info.imageBean.url}" target="_blank">${page.info.imageBean.name}</a>
										</c:if></span>
								</div>
							</div>
						</div>

					</fieldset>

					<fieldset>
						<legend>stat</legend>
						<div class="row">
							<div class="col-sm-4">
								<div class="line">
									<label>${i18n.edit['item.page.day-visit']}</label> <span>${page.lastDayVisit}</span>
								</div>
							</div>
							<div class="col-sm-4">
								<div class="line">
									<label>${i18n.edit['item.page.mount-visit']}</label> <span>${page.lastMountVisit}</span>
								</div>
							</div>
							<div class="col-sm-4">
								<div class="line">
									<label>${i18n.edit['item.page.year-visit']}</label> <span>${page.lastYearVisit}</span>
								</div>
							</div>
						</div>

					</fieldset>

				</div>

				<div class="col-md-6">
					<div class="row">
						<div class="col-md-6">
							<div class="bloc-default">
								<c:if test="${not globalContext.mailingPlatform}">
									<div class="form-group">
										<label for="seoWeight">${i18n.edit['item.seo-weight']}</label>
										<select class="form-control" id="seoWeight" name="seo_weight">
											<option value="-1" ${page.info.seoWeight == -1?'selected="selected"':''}>${i18n.edit['global.inherited']}</option>
											<option value="0" ${page.info.seoWeight == 0?'selected="selected"':''}>${i18n.edit['item.seo-weight.0']}</option>
											<option value="1" ${page.info.seoWeight == 1?'selected="selected"':''}>${i18n.edit['item.seo-weight.1']}</option>
											<option value="2" ${page.info.seoWeight == 2?'selected="selected"':''}>${i18n.edit['item.seo-weight.2']}</option>
											<option value="3" ${page.info.seoWeight == 3?'selected="selected"':''}>${i18n.edit['item.seo-weight.3']}</option>
										</select>
									</div>
								</c:if>

								<c:if test="${fn:length(info.template.pageTypes) > 1}">
									<fieldset>
										<legend>${i18n.edit['item.types']}</legend>
										<c:forEach var="type" items="${info.template.pageTypes}">
											<div class="form-group">
												<input type="radio" class="form-control" id="page_type_${type}" name="page_type" value="${type}" ${page.info.type == type?' checked="checked"':''} />
												<label class="suffix" for="page_type_${type}">${type}</label>
											</div>
										</c:forEach>
									</fieldset>
								</c:if>

								<c:if test="${!userInterface.light}">

									<div class="checkbox">
										<label> <input type="checkbox" id="association" name="association" ${page.info.childrenAssociation?'checked="checked"':''} value="true" /> ${i18n.edit['item.children-association']}
										</label>
									</div>

									<div class="form-group">
										<label>${i18n.edit['item.short-url']}</label>
										<c:if test="${!page.allreadyShortURL}">
											<input class="action-button" type="submit" id="shorturl-url-creator" name="shorturl" value="${i18n.edit['edit.action.short-url']}" />
										</c:if>
										<c:if test="${page.allreadyShortURL}">
											<span class="noclipboard"><a href="${info.shortURL}">${page.shortURL}</a></span>
											<input class="clipboard" type="button" class="btn btn-default" onclick="clipboardCopy('${info.shortURL}');" value="copy : ${page.shortURL}" />
											<span class="noclipboard">(${info.requestContentLanguage}:<a href="${info.shortLanguageURL}">${page.shortLanguageURL}</a>)
											</span>
											<input class="clipboard" type="button" class="btn btn-default" onclick="clipboardCopy('${info.shortLanguageURL}');" value="copy : ${page.shortLanguageURL}" />
										</c:if>
									</div>
								</c:if>
							</div>
						</div>
						<div class="col-md-6">
							<div class="bloc-default">
								<c:if test="${sharedContent}">
									<div class="form-group">
										<label for="shared_name">${i18n.edit['item.shared']}</label>
										<input type="text" class="form-control" id="shared_name" name="share" ${page.info.visible?'checked="checked"':''} value="${page.info.sharedName}" />
									</div>
								</c:if>
								<c:if test="${ipsecurity}">
									<div class="form-group">
										<label for="shared_name">${i18n.edit['item.ipsecurity']}</label>
										<input type="text" class="form-control" id="ipsecurity" name="ipsecurity" value="${page.page.ipSecurityErrorPageName}" />
									</div>
								</c:if>
							</div>
						</div>
					</div>

					<div class="bloc-default">
						<div class="_jv_check_line">
							<div class="_jv_btn-check">
								<input type="checkbox" id="page_visible" name="view" ${page.info.visible?'checked="checked"':''} value="true" />
								<label class="suffix" for="page_visible">${i18n.edit['item.visible']}</label>
							</div>
							<c:if test="${!userInterface.light}">
								<div class="_jv_btn-check">
									<input type="checkbox" id="page_active" name="active" ${page.info.pageActive?'checked="checked"':''} value="true" />
									<label class="suffix" for="page_active">${i18n.edit['item.active']}</label>
								</div>
							</c:if>
							<c:if test="${userInterface.model && !info.page.root && !page.childrenOfAssociation}">
								<div class="_jv_btn-check">
									<input type="checkbox" id="page_model" name="model" ${page.model?'checked="checked"':''} value="true" />
									<label class="suffix" for="page_model">Model</label>
								</div>
							</c:if>
							<c:if test="${!userInterface.light}">
								<input type="hidden" name="special_input" value="true" />
								<div class="_jv_btn-check  ${page.root?'disabled':''}">
									<input type="checkbox" id="break_repeat" name="break_repeat" ${page.info.breakRepeat?'checked="checked"':''} value="true" ${page.root?'disabled="disabled"':''} />
									<label class="suffix" for="break_repeat">${i18n.edit['item.break-repeat']}</label>
								</div>
							</c:if>
							<c:if test="${userInterface.admin}">
								<div class="_jv_btn-check">
									<input type="checkbox" id="page_admin" name="admin" ${page.admin?'checked="checked"':''} value="true" />
									<label class="suffix" for="page_admin">Admin</label>
								</div>
							</c:if>
						</div>
					</div>

					<c:if test="${fn:length(info.roles) > 0}">

						<c:if test="${fn:length(adminRoles) > 0}">
							<fieldset class="flex-grid">
								<legend>${i18n.edit['item.title.admin-roles']}</legend>
								<c:forEach var="role" items="${adminRoles}">
									<label class="checkbox-inline"> <input type="checkbox" name="admin-${role}" id="admin-${role}" ${not empty page.adminRoles[role]?'checked="checked"':''} /> ${role}
									</label>
								</c:forEach>
								<div class="other-roles">
									<c:forEach var="role" items="${adminOtherRole}">
										<label class="checkbox-inline"> <input type="checkbox" name="admin-${role}" id="admin-${role}" ${not empty page.adminRoles[role]?'checked="checked"':''} /> ${role}
										</label>
									</c:forEach>
								</div>
							</fieldset>
						</c:if>
						<fieldset class="flex-grid">
							<legend>${i18n.edit['item.title.view-roles']}</legend>
							<c:forEach var="role" items="${info.roles}">
								<label class="checkbox-inline"> <input type="checkbox" name="user-${role}" id="user-${role}" ${not empty page.roles[role]?'checked="checked"':''} /> ${role}
								</label>
							</c:forEach>
						</fieldset>

					</c:if>

					<fieldset>
						<legend><a href="#" onclick="updateHistory()">Page History...</a></legend>
						<div id="page-history"></div>
					</fieldset>
					<script>
						function updateHistory() {
							let url = '${pageHistoryUrl}';
							document.getElementById("page-history").innerHTML = "...";
							fetch(url)
									.then(response => {
										// Vérification si la réponse est ok
										if (!response.ok) {
											throw new Error('Réseau ou réponse non ok');
										}
										return response.json(); // Conversion de la réponse en JSON
									})
									.then(data => {
										console.log("data = ",data);
										// Traitement des données JSON
										let historyList = "<ul>";
										for (let timestamp in data) {
											historyList += '<li><b>'+timestamp+'</b> : '+data[timestamp]+'</li>';
										}
										historyList += "</ul>";

										// Mise à jour de l'élément avec l'ID 'page-history'
										document.getElementById("page-history").innerHTML = historyList;
									})
									.catch(error => {
										// Gestion des erreurs
										console.error('Erreur lors de la requête fetch:', error);
									});
						}
					</script>
				</div>

			</div>

			<div class="action flex-line">
				<button type="submit" ${page.parent==null?'disabled="disabled"':''} class="btn btn-warning" name="webaction" value="edit.DeletePage">
					<i class="bi bi-trash"></i> ${i18n.edit['global.delete']}
				</button>
				<button type="submit" class="btn btn-primary btn-color" name="webaction" value="pageProperties">${i18n.edit['global.ok']}</button>
			</div>
		</form>

	</div>

</div>