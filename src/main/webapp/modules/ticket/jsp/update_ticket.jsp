<%@ taglib uri="jakarta.tags.core" prefix="c"%><%@ taglib prefix="fn" uri="jakarta.tags.functions"%><div class="content">
	<form class="standard-form" id="create-ticket" method="post" action="${info.currentURL}">
		<div class="row">
			<div class="col-md-2">
				<div id="screenshot">
					<c:if test="${not empty newTicket}">
						<c:url var="screenshot" value="/web-tmp/${globalContext.contextKey}_${info.page.name}.jpg"></c:url>
					</c:if>
					<c:if test="${empty newTicket && not empty ticket.screenshot}">
						<c:set var="screenshot" value="${ticket.screenshot}" />
					</c:if>
					<c:if test="${empty screenshot}">no screenshot</c:if>
					<c:if test="${not empty screenshot}">
						<a href="${screenshot}" target="_blank"><img id="screenshot-container" src="${info.ajaxLoaderURL}" alt="page screenshot" lang="en" /></a>
					</c:if>
					<script>
						setTimeout(function() {
							jQuery("#screenshot-container").attr("src",
									'${screenshot}');
						}, 100);
					</script>
				</div>
			</div>
			<div class="col-md-5">
				<c:if test="${not empty newTicket}">
					<div class="form-group">
						<input class="form-control" type="text" name="title" placeholder="title" />
					</div>
				</c:if>
				<c:if test="${empty newTicket}">
					<h2>${ticket.title}</h2>
				</c:if>
				<div class="form-group">
					<c:if test="${empty newTicket && not empty ticket.message}">
						<p class="message">${ticket.htmlMessage}</p>
					</c:if>
					<c:if test="${not empty newTicket}">
						<textarea class="form-control" rows="4" cols="20" name="message" placeholder="message"></textarea>
					</c:if>
				</div>
					<c:if test="${not info.editContext.lightInterface}">
						<div class="_jv_flex-line-start">
							<label class="main-label">priority</label>
							<div class="btn-group">
								<div class="_jv_btn-check">
									<input id="p1" type="radio" name="priority" value="0" ${ticket.priority == 0?'checked="checked"':''}>
									<label for="p1">none</label>
								</div>
								<div class="_jv_btn-check">
									<input id="p2" type="radio" name="priority" value="1" ${ticket.priority == 1?'checked="checked"':''}>
									<label for="p2">low</label>
								</div>
								<div class="_jv_btn-check">
									<input id="p3" type="radio" name="priority" value="2" ${ticket.priority == 2?'checked="checked"':''}>
									<label for="p3">middle</label>
								</div>
								<div class="_jv_btn-check">
									<input id="p4" type="radio" name="priority" value="3" ${ticket.priority == 3?'checked="checked"':''}>
									<label for="p4">high</label>
								</div>
							</div>
						</div>
					</c:if>
					<div class="_jv_flex-line-start">
						<c:if test="${info.editContext.lightInterface}">
							<label for="status">status : </label>
							<span>${ticket.status}</span>
							<input type="hidden" name="status" value="${empty ticket.status ? 'new' : ticket.status}" />
						</c:if>
						<c:if test="${not info.editContext.lightInterface}">
							<label class="main-label">status</label>
							<div class="btn-group">
								<div class="_jv_btn-check">
									<input id="s1" type="radio" name="status" value="new" ${ticket.status == 'new'?'checked="checked"':''}>
									<label for="s1">new</label>
								</div>
								<div class="_jv_btn-check">
									<input id="s2" type="radio" name="status" value="working" ${ticket.status == 'working'?'checked="checked"':''}>
									<label for="s2">working</label>
								</div>
								<div class="_jv_btn-check">
									<input id="s3" type="radio" name="status" value="on hold" ${ticket.status == 'on hold'?'checked="checked"':''}>
									<label for="s3">on hold</label>
								</div>
								<div class="_jv_btn-check">
									<input id="s4" type="radio" name="status" value="rejected" ${ticket.status == 'rejected'?'checked="checked"':''}>
									<label for="s4">rejected</label>
								</div>
								<div class="_jv_btn-check">
									<input id="s5" type="radio" name="status" value="done" ${ticket.status == 'done'?'checked="checked"':''}>
									<label for="s5">done</label>
								</div>
								<div class="_jv_btn-check">
									<input id="s6" type="radio" name="status" value="archived" ${ticket.status == 'archived'?'checked="checked"':''}>
									<label for="s6">archived</label>
								</div>
							</div>
							
						</c:if>
					</div>
					<c:if test="${info.editContext.lightInterface}">
						<input type="hidden" name="share" value="${empty ticket.share ? 'site' : ticket.share}" />
					</c:if>
					<c:if test="${not info.editContext.lightInterface}">
						<div class="_jv_flex-line-start">
							<label class="main-label">share</label>
							<div class="btn-group">
								<div class="_jv_btn-check">
									<input id="sh1" type="radio" name="share" value="new" ${empty ticket.share || ticket.share == 'site'?'checked="checked"':''}>
									<label for="sh1">${ticket.context}</label>
								</div>
								<div class="_jv_btn-check">
									<input id="sh2" type="radio" name="share" value="working" ${ticket.share == 'allsites'?'checked="checked"':''}>
									<label for="sh2">all sites</label>
								</div>
								<div class="_jv_btn-check">
									<input id="sh3" type="radio" name="share" value="on hold" ${ticket.share == 'public'?'checked="checked"':''}>
									<label for="sh3">public</label>
								</div>
							</div>
							
						</div>
					</c:if>
				<c:if test="${contentContext.globalContext.businessTicket && (contentContext.currentUser.customer || contentContext.currentUser.provider)}">
					<div class="business-ticket">
						<fieldset>
							<legend>${i18n.edit['ticket.business.title']}</legend>
							<c:if test="${ticket.bstatus == 'wait'}">
								<c:if test="${contentContext.currentUser.customer}">
									<div class="alert alert-info">${i18n.edit['ticket.business.status.wait']}</div>
								</c:if>
								<c:if test="${contentContext.currentUser.provider}">
									<div class="form-group">
										<label>Proposition (&euro;)</label>
										<input type="number" class="form-control" id="proposition" name="proposition" placeholder="">
									</div>

								</c:if>
							</c:if>
							<c:if test="${ticket.bstatus == 'ask'}">
								<c:if test="${contentContext.currentUser.customer}">
									<div class="alert alert-info">${i18n.edit['ticket.business.status.proposition']}:${ticket.price} &euro;</div>
									<div class="form-group">
										<label for="bvalid">${i18n.edit['ticket.business.status.reply']}</label>
										<select id="bvalid" name="bvalid" class="form-control">
											<option value="">none</option>
											<option value="yes">${i18n.edit['ticket.business.status.reply.yes']}</option>
											<option value="no">${i18n.edit['ticket.business.status.reply.no']}</option>
										</select>
									</div>
								</c:if>
							</c:if>
							<c:if test="${ticket.bstatus == 'valided'}">
								<div class="alert alert-success">${i18n.edit['ticket.business.status.valided']}:${ticket.price} &euro;</div>
							</c:if>
							<c:if test="${ticket.bstatus == 'rejedcted'}">
								<div class="alert alert-danger">${i18n.edit['ticket.business.status.refused']}:${ticket.price} &euro;</div>
							</c:if>
						</fieldset>
					</div>
				</c:if>
			</div>
			<div class="col-md-5">
				<strong><div class="authors" title="authors"><i class="bi bi-person-circle"></i> ${ticket.authors}</div></strong>
				<c:if test="${not info.editContext.lightInterface}">
					<div class="line">
						<label>creation date :</label>${ticket.creationDateLabel}
					</div>
				</c:if>
				<div class="line">
					<label>last update date :</label>${ticket.lastUpdateDateLabel}
				</div>
				<c:if test="${not info.editContext.lightInterface}">
					<c:if test="${not empty ticket.category}">
						<div class="line">
							<label>category : </label>${ticket.category}
						</div>
					</c:if>
				</c:if>
				<c:if test="${not empty ticket.url}">
					<div class="line">
						<label>url : </label><a target="_blank" href="${ticket.url}">${ticket.url}</a>
					</div>
				</c:if>
			</div>

		</div>

		<input type="hidden" name="webaction" value="ticket.update" />
		<input type="hidden" name="id" value="${ticket.id}" />
		<c:if test="${info.editContext.lightInterface}">
			<input type="hidden" name="priority" value="${empty ticket.priority ? '1' : ticket.priority}" />
		</c:if>

		<c:if test="${not info.editContext.lightInterface}">
			<c:set var="strListSeparator" value="|||" />
			<c:set var="knownUsers" value="${strListSeparator}" />
			<c:set var="ticketUsers" value="${strListSeparator}" />
			<c:forEach var="userLogin" items="${ticket.users}">
				<c:set var="ticketUsers" value="${ticketUsers}${userLogin}${strListSeparator}" />
			</c:forEach>
			<fieldset>
				<legend><i class="bi bi-people-fill"></i> ${i18n.edit['ticket.title.user']}</legend>
				<c:forEach var="user" items="${ticketAvailableUsers}">
					<c:set var="knownUsers" value="${knownUsers}${user.login}${strListSeparator}" />
					<c:set var="userKey" value="${strListSeparator}${user.login}${strListSeparator}" />
					<div class="_jv_btn-check">
					<input id="${user.login}" type="checkbox" name="users" value="${user.login}" ${fn:contains(ticketUsers, userKey)?'checked="checked"':''} />
					<label class="checkbox-inline" for="${user.login}"> ${user.login}</label>
					</div>
					</label>
				</c:forEach>
			</fieldset>
			<c:set var="minOne" value="false" />
			<c:set var="buffer">
				<fieldset>
					<legend>Unknown/deleted users</legend>
					<c:forEach var="userLogin" items="${ticket.users}">
						<c:set var="userKey" value="${strListSeparator}${userLogin}${strListSeparator}" />
						<c:if test="${not fn:contains(knownUsers,userKey)}">
							<label class="checkbox-inline"> <input type="checkbox" name="users" value="${userLogin}" checked="checked" /> ${userLogin}
							</label>
							<c:set var="minOne" value="true" />
						</c:if>
					</c:forEach>
				</fieldset>
			</c:set>
			<c:if test="${minOne}">
				<c:out value="${buffer}" escapeXml="false" />
			</c:if>
		</c:if>
		<c:if test="${empty newTicket}">
			<h2>comments</h2>
			<c:if test="${fn:length(ticket.comments) > 0}">
				<c:forEach var="comment" items="${ticket.comments}" varStatus="status">
					<div class="comment ${status.last?'last':''}">
						<span class="date">${comment.creationDateString}</span><span class="authors">${comment.authors} : </span>${comment.htmlMessage}
					</div>
				</c:forEach>
			</c:if>

			<div class="line">
				<label for="comment">new comments</label>
				<textarea id="comment" name="comment"></textarea>
			</div>
		</c:if>

		<div class="action">
			<input type="submit" name="delete" class="warning needconfirm btn btn-default" title="${i18n.edit['global.delete']}" value="${i18n.edit['global.delete']}" />
			<input type="submit" name="back" title="${i18n.edit['global.back']}" value="${i18n.edit['global.back']}" class="btn btn-default" />
			<input type="submit" name="ok" value="${i18n.edit['global.ok']}" class="btn btn-default" />
		</div>

	</form>

</div>