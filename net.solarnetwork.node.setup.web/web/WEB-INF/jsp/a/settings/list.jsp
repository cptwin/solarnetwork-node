<section class="intro">
	<p>
		<fmt:message key="settings.intro"/>
	</p>
</section>

<c:if test="${fn:length(factories) > 0}">
	<section id="factories">
		<h2><fmt:message key="settings.factories.title"/></h2>
		<p><fmt:message key="settings.factories.intro"/></p>	
		<table class="table">
			<tbody>
			<c:forEach items="${factories}" var="factory" varStatus="factoryStatus">
				<!--  ${factory.factoryUID} -->
				<tr>
					<td><strong><setup:message key="title" messageSource="${factory.messageSource}" text="${factory.displayName}"/></strong></td>
					<td>
						<a class="btn" href="<c:url value='/settings/manage.do?uid=${factory.factoryUID}'/>">
							<i class="icon-edit icon-large"></i> 
							<fmt:message key="settings.factory.manage.label"/>
						</a>
					</td>
				</tr>
			</c:forEach>
			</tbody>
		</table>
	</section>
</c:if>

<c:if test="${fn:length(providers) > 0}">
	<section id="settings">
		<h2><fmt:message key="settings.providers.title"/></h2>
		<p><fmt:message key="settings.providers.intro"/></p>	

		<form class="form-horizontal" action="<c:url value='/settings/save.do'/>" method="post">
		<c:forEach items="${providers}" var="provider" varStatus="providerStatus">
			<!--  ${provider.settingUID} -->
			<c:set var="provider" value="${provider}" scope="request"/>
			<fieldset>
				<legend><setup:message key="title" messageSource="${provider.messageSource}" text="${provider.displayName}"/></legend>
				<c:forEach items="${provider.settingSpecifiers}" var="setting" varStatus="settingStatus">
					<c:set var="settingId" value="s${providerStatus.index}i${settingStatus.index}" scope="request"/>
					<c:set var="setting" value="${setting}" scope="request"/>
					<c:import url="/WEB-INF/jsp/a/settings/setting-control.jsp"/>
				</c:forEach>
			</fieldset>
		</c:forEach>
			<div class="form-actions">
				<button type="button" class="btn btn-primary" id="submit"><fmt:message key='settings.save'/></button>
			</div>
		</form>
	</section>
	<script>
	$(function() {
		$('#submit').click(function() {
			SolarNode.Settings.saveUpdates($(this.form).attr('action'), {
				success: '<fmt:message key="settings.save.success.msg"/>',
				error: '<fmt:message key="settings.save.error.msg"/>',
				title: '<fmt:message key="settings.save.result.title"/>',
				button: '<fmt:message key="ok.label"/>'
			});
		});
		SolarNode.Settings.reset();
	});
	</script>
</c:if>

<c:if test="${not empty backupManager}">
<section>
	<h2><fmt:message key="backup.title"/></h2>
	<p><fmt:message key="backup.intro"/></p>
	<div class="form-horizontal">
		<fieldset>
			<c:set var="provider" value="${backupManager}" scope="request"/>
			<c:forEach items="${provider.settingSpecifiers}" var="setting" varStatus="settingStatus">
				<c:set var="settingId" value="bm${providerStatus.index}i${settingStatus.index}" scope="request"/>
				<c:set var="setting" value="${setting}" scope="request"/>
				<c:import url="/WEB-INF/jsp/a/settings/setting-control.jsp"/>
			</c:forEach>
			<c:if test="${not empty backupService}">
				<c:set var="provider" value="${backupService.settingSpecifierProvider}" scope="request"/>
				<c:forEach items="${provider.settingSpecifiers}" var="setting" varStatus="settingStatus">
					<c:set var="settingId" value="bs${providerStatus.index}i${settingStatus.index}" scope="request"/>
					<c:set var="setting" value="${setting}" scope="request"/>
					<c:import url="/WEB-INF/jsp/a/settings/setting-control.jsp"/>
				</c:forEach>
			</c:if>
		</fieldset>

		<fieldset>
			<c:if test="${fn:length(backups) > 0}">
				<div class="control-group">
					<label class="control-label" for="backup-backups">
						<fmt:message key="backup.backups.label"/>
					</label>
					<form class="controls form-inline" action="<c:url value='/settings/exportBackup.do'/>">
						<select name="backup" class="span3" id="backup-backups">
							<c:forEach items="${backups}" var="backup" varStatus="backupStatus">
								<option value="${backup.key}">
									<fmt:formatDate value="${backup.date}" pattern="dd MMM yyyy HH:mm"/>
								</option>
							</c:forEach>
						</select>
						<button type="submit" class="btn btn-primary">
							<fmt:message key="backup.download.button"/>
						</button>
						<button type="button" class="help-popover help-icon" tabindex="-1"
								data-content="<fmt:message key='backup.backups.info'/>"
								data-html="true">
							<i class="icon-question-sign"></i>
						</button>
					</form>
				</div>
			</c:if>
			<div class="control-group">
				<label class="control-label" for="backup-import-field">
					<fmt:message key="backup.import.label"/>
				</label>
				<form class="controls form-inline" action="<c:url value='/settings/importBackup.do'/>" method="post" enctype="multipart/form-data">
  					<input class="span3" id="backup-import-field" type="file" name="file"/>
  					<button class="btn btn-primary" type="submit"><fmt:message key="backup.import.button"/></button>
					<button type="button" class="help-popover help-icon" tabindex="-1"
							data-content="<fmt:message key='backup.import.info'/>"
							data-html="true">
						<i class="icon-question-sign"></i>
					</button>
				</form>
			</div>
		</fieldset>
		<div class="form-actions">
			<form class="form-inline" action="<c:url value='/settings/backupNow.do'/>" method="post">
 				<button class="btn btn-primary" type="submit" id="backup-now-btn"
 					data-loading-text=" "><fmt:message key="backup.now.button"/></button>
			</form>
		</div>
	</div>
</section>
</c:if>

<section>
	<h2><fmt:message key="settings.io.title"/></h2>
	<p><fmt:message key="settings.io.intro"/></p>
	<div class="form-horizontal">
		<fieldset>
			<div class="control-group">
				<label class="control-label" for="export.btn">
					<fmt:message key="settings.io.export.label"/>
				</label>
				<div class="controls">
					<a class="btn btn-primary" id="export.btn" href="<c:url value='/settings/export.do'/>">
						<fmt:message key="settings.io.export.button"/>
					</a>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="import.field">
					<fmt:message key="settings.io.import.label"/>
				</label>
				<form class="controls form-inline" action="<c:url value='/settings/import.do'/>" method="post" enctype="multipart/form-data">
  					<input class="span3" id="import.field" type="file" name="file"/>
  					<button class="btn btn-primary" type="submit"><fmt:message key="settings.io.import.button"/></button>
				</form>
			</div>
			<c:if test="${fn:length(settingsBackups) > 0}">
				<div class="control-group">
					<label class="control-label" for="auto-backups">
						<fmt:message key="settings.autobackup.label"/>
					</label>
					<div class="controls">
						<ul id="auto-backups">
							<c:forEach items="${settingsBackups}" var="backup" varStatus="backupStatus">
								<li>
				  					<a class="btn btn-small" id="export.btn" href="<c:url value='/settings/export.do'/>?backup=${backup.backupKey}">
										<fmt:message key="settings.autobackup.download.button">
											<fmt:param value="${backup.standardDateString}"/>
										</fmt:message>
									</a>
									<c:if test="${backupStatus.first}">
										<button type="button" class="help-popover help-icon" tabindex="-1"
												data-content="<fmt:message key='settings.autobackup.info'/>"
												data-html="true">
											<i class="icon-question-sign"></i>
										</button>
									</c:if>
								</li>
							</c:forEach>
						</ul>
					</div>
				</div>
			</c:if>
		</fieldset>
	</div>
</section>
