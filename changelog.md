# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

Added for new features.
Changed for changes in existing functionality.
Deprecated for soon-to-be removed features.
Removed for now removed features.
Fixed for any bug fixes.
Security in case of vulnerabilities.

## [Unreleased]


## [2.0] - 15/06/2012
### Added
- alpha version online. 

## [2.0.0.8] - 18/10/2012
### Added
- Cache abstraction
- PageBean and infoBean transform as smart bean.
- short url  
- auto import data 1.4 >> 2.0

## [2.0.0.9] - 24/10/2012
### Added
- page-reference : user default renderer mecanisms

## [2.0.0.10] - 08/11/2012
### Added
- work list of MenuElement as List in place of Array
- jalvo work without portlet lib
- preview edition work with template with absolute reference to jQuery lib
- can desactivated preview mode

## [2.0.0.11] - 23/11/2012
### Added
- preview move component
- some optimisation (reduce File access)
- Always close stream in ImageTransformServlet

## [2.0.0.12] - 12/12/2012
### Added
- display error position if parsing error on content + interactive macro interface + import remote html page macro
- images transform at the same time.

## [2.0.0.13] - 22/12/2012
### Added
- master user and master site
- facebook module ok
- no prefix is host 

## [2.0.0.14] - 22/01/2013
### Added
- export component as csv and excel file
- SmartPageDescription - speed optimisation
- upload documents with drap and drop in preview mode.

## [2.0.0.15] - 11/02/2013
### Added
- ehcache usage refactoring
- edit template css / image config
- browse all files for god user
- do not call disktemplates methods if not necessary

## [2.0.0.16] - 12/03/2013
### Added
- i18n optimisation
- active wizz optioin 
- UserInterfaceContext bug init correction

## [2.0.0.17] - 14/03/2013
### Added
- correction bug : url special width context.
- correction bug : edit empty page with children

## [2.0.0.18] - 21/03/2013]
### Added

## [2.0.1] - stable 28/03/2013
### Added

## [2.0.2] - new unstable 28/03/2013
### Added
- sychronize optimisation
- import odt with external link
- fix preview edition

## [2.0.2.1] - 25/4/2013
### Added
- move folder and file (and mofidy reference in content)
- image rotation
- autologin correction
- vimeo integration
- new text editor
- new css editor
- list service
- collaborative mode

## [2.0.2.2] - 31/5/2013
### Added
- don't display account size
- upload from url
- definition of mail inside template
- don't load config of component from webapp but only from template

## [2.0.2.3] - 12/6/2013
### Added
- mailing unsubscribe
- shared content

## [2.0.2.4] - 21/6/2013
### Added
- access image with jstl
- change wisiwig editor option
- getBean in abstractVisualComponent without duplication
- encode all files with default encoding
- remove mailing servelt
- bug correction : file explorer file cleared if no change done.
- send mailing from preview
- 404 management
- black preview template + edit template mode

## [2.0.2.5] - 25/07/2013
### Added
- String to collection with escape separator + test
- back mecanism + forward message
- light template + template mode in globalContext
- vrac user import
- import .odt optimisation
- editPreview manage in URLHelper
- forward message
- no cache in menu element exept in view mode (MenuElement.java line 2507)
- site map module
- autosize plugin for textarea (thanks : http://www.jacklmoore.com/autosize/)
- shared content provider (http://www.stockvault.net/ and http://fotogrph.com/)
- ecom refactoring + externals modules
- remove portlet integration
- cache optimisation (configure Last-Modified header)
- children association
- auto close tag management (hr, input, link...)
- possibility to define other template than current for mobile device
- XMLManipulationHelper test case

## [2.0.2.6] - 29/09/2013
### Added
- you can use .less in place of .css
- QRcode generation
- preview two cols on 1280
- mailing template inside specific window
- correct RSS order
- import content macro
- cols plugins + 12 cols mecanims for BusinessComponent and smart GenericForm

## [2.0.2.7] - 18/12/2013
### Added
- child association
- name of page can be human readable.
- import local image in wysiwyg

## [2.0.3] - 13/01/2014
### Added
- minimize js + compress css + resolve @import when import template (if option checked in template config)
- robots.txt
- import docx

## [2.0.3.1] - 20/02/2014
### Added
- reverse link : only on previous component
- update tinymce 4.0.16 (2014-01-31) - replace .on to .live - pdfUpdate algo revision
- transaction writing of globalcontext + synchro optimisation
- pdf rendering with xhtml parser + local article id correction + comment component reapeat correction
- user manager can change role of visitor user list.
- if login is email, the email is copied in email field.
- template editor module
- tinymce 4.0.20 + clean folder list in dynamicComponent
- mailing feedback + history
- menu generation optimisation + XML parsing correction '>' not considered if inside char sequence
- undo on current page (by default unactive, active with static-config.properties) !alpha version
- component layout (on title and subtile, only left, right and center)
- persistence synchronisation, load wait save thread
- change image resize method   
- static file sorting
- css inliner without repetition
- trim users roles
- css style parser, don't insert inline two same attribute.
- stat. mailing active
- add editable template as default template
- optimisation of cache (more components cacheable + display cache status in website management) 
- import default template on clear cache.
- tinymce 4.0.26
- area end tag + push template from template list

## [2.0.3.2] - 20/02/2014
### Added
- search real content in navigation area correction + RSS link inside infoBean 
- link in navigation area don't change the navigation language
- help button on interface
- Reverse link correction (remove area)
- tinymce 4.1 
- page reference (ajax filter)
- exclude context for some domains.

## [2.0.3.3] - 20/04/2014
### Added
- copy and paste component in preview.
- copy and paste page in preview.
- Dynamic Component can be title and description.
- Table component

## [2.0.3.6] - 11/08/2014
### Added
- forward bad url to same page name url.
- create DynamicComponent macro
- image engine high-quality option
- jv:pageurl tag
- Import gallery correction + all prefix in image-config
- attach user role to resource
- image cache optimisation
- less 1.7 compilation (remove openoffice lib)
- page reference :all filter

## [2.0.3.7] - 22/09/2014
### Added
- TinyMCE 4.1.6
- specific.css by site
- remote service update for mobile apps
- multimedia select forlder with browsing
- sitemap module
- init module correction
- link wysywig correction

## [2.0.3.8] - 22/09/2014
### Added
- transform PDF as image for preview
- ImageTransform work with SVG format
- FieldMulti value
- don't call prepareView is there are no renderer defined.
- sitemap filter
- integration of lucent search engine (test integration, by default not actived)

## [2.0.3.8.1] - 05/12/2014
### Added
- add bootstrap cols in edit mode
- export business component directly in xlsx file
- image priority
- raw filter option + cols
- multimedia bootstrap layout
- multimedia browse optimisation
- subtitle link parametrisation
- edit user bootstrap layout + sort children macro
- tracking paramter activation
- new debug notes (priority, users, creation date)
- correct copy component between 2 language bug
- force not read content
- no same url mecanism (method UrlNumber in MenuElement)
- change isLabel with labelLevel
- GlobalContext thread, now have no reference to GlobalContext
- keep params for langugae link

## [2.0.3.8.2] - 14/02/2015
### Added
- update preview edition user bootstrap and less, rewrite preview js
- insert edit_preview at the end of the page in place of start

## [2.1.0.0.0] - 24/03/2015
### Added
- flat edit template
- preview scroll positionning
- table component error correction
- tag component ready for bootstrap

## [2.1.0.1.0] - 06/04/2015
### Added
- first element mark with "first-component" class
- GitHub

## [2.1.0.2.0] - 22/04/2015
### Added
- GitHub change version for finalize integration 

## [2.1.0.2.1] - 22/04/2015
### Added
- send mailing email vrac import structured
- responsive editable template
- ImageTransformServlet : create and wrap ImageTransforming thread with ExecuterService for timeout

## [2.1.0.2.2] - 30/04/2015
### Added
- create temp image folder
- TextInList if content
- AbstractVisualComponent explode compPage (page bean of the page of the component)

## [2.1.0.2.3] - 11/05/2015
### Added
- page can be a event, a user can register to it.

## [2.1.0.2.4] - 13/05/2015
### Added
- ImageTransform thread manager optimisation
- Social network login: google
- ticket accessible from preview, call img.flush in ImageTransformServlet and force SC_ACCEPTED in CssLess filter.
- check password on autologin + reset cache on rename ressources 
- preview.js ok for IE

## [2.1.0.2.5] - 01/07/2015
### Added
- multimedia filter
- integrity check
- store tracker optimisation

## [2.1.0.2.6] - 08/07/2015
### Added
- store old url
- preview responsive optimisation
- shared file between master context and other

## [2.1.0.2.7] - 28/07/2015
### Added
- multimedia random gallery

## [2.1.0.2.8] - 30/07/2015
### Added
- box delete, move and copied grouped
- different type of grayscale 

## [2.1.0.2.9] - 08/09/2015
### Added
- duplicate page
- block preview popup on error
- text component + font style
- change header icone for export  

## [2.1.0.2.10] - 18/09/2015
### Added
- correction bug on firefox : drop a element on him self

## [2.1.0.2.11] - 22/09/2015
### Added
- Edit directely on create in preview mode
- Synchronization block the persistence
- you can stop the undo (sample: in macro)

## [2.1.0.2.12] - 25/09/2015
### Added
- reverlink better display + .less bug on deploy correction
- remove jstl replacement in MenuElement and transfert it in InfoBean.getPageDescription
- add active property on page 
- IubendaRemoteImport
- Role editor

## [2.1.0.2.13] - 03/10/2015
### Added
- Field condition
- SmartForm event
- edit direct metadata from sharing
- need admin right for change shared-content config
- add NoExtURLPathTitleCreatorOneLevel only section and title in url

## [2.1.0.2.14] - 21/10/2015
### Added
- cache-control for static resources
- create filtered reference to a template local image
- delete folder from files meta-data view
- tag change filter / short image url
- change update of the page
- user right update
- correct bug on delete image
- disabled tab if no access to the page
- preview font-awesome url error fixed
- localfile sharing module + mimetype mecanisme and svg
- get label of a page without repeat

## [2.1.0.2.15] - 28/10/2015 
### Added
- help link
- page:${page.name} for create link to a page
- amelioration of resource browser for full wysiwyg
- sitemap.xml page with reference is considered of daily updated
- wysiwyg height optimisation for HD screen
- pdfbox update version to 1.8.10
- page reference layout on 2 cols in edition
- filter children page correction + title on option tag
- change component list directly from preview (if admin)
- remove reference to contentcontext in imagetransform thread

## [2.1.0.2.16] - 04/11/2015
### Added
- Rest + Mailing config 

## [2.1.0.2.17] - 08/11/2015
### Added
- undelete page
- image edit layout
- image header
- Mailing smtp authentification correction
- reverlink with email on external-link
- remove FileUtils.copyFile replace with transactional copy on ResoureceHelper

## [2.1.0.3.0] - 18/11/2015
### Added
- pushbullet notif

## [2.1.0.3.1] - 20/11/2015
### Added
- defautl menu value (repeat + current page as reference) + update same component figure
- g-recaptcha
- openlist field
- remove delete file in cache
- export wait synchronization

## [2.1.0.3.2] - 14/12/2015
### Added
- reverse link not on my self + select box first item select is'nt a selection
- mailing registration ready for bootstrap

## [2.1.0.3.3] - 12/01/2016
### Added
- active module on action call (solve i18n bug)
- correction smtp authentification
- owasp encode

## [2.1.0.3.4] - 20/01/2016
### Added
- no share image on mailing platform, undo depth configurable from static config
- clean security + correction bug paste comp on him self
- optimize session size

## [2.1.0.3.5] - 31/01/2016
### Added
- clear session + not found page (wihtout 404 page in content) return 404 in place of 503
- fancybox correction + border size of table can be just number

## [2.1.0.3.6] - 05/02/2016
### Added
- autologin on pdf generation
- logo as font
- mailing tracking without images
- paypal reduction report
- list of template usage in master mode
- delete old xml file on internet instance after synchronization 
- check url extension
- large separation
- test mailing boolean stored
- set hashcode as image transfourm url param in preview mode
- field file and image browser boutton

## [2.1.0.3.7] - 26/02/2016
### Added
- Field container sorting
- remove loader on image
- pdf cache in file
- page-reference optimisation

## [2.1.0.3.8] - 09/03/2016
### Added
- remove face detection lib (jjil)
- optimisation robots.txt remove edition url and add sitemap.xml
- smart miroring
- report site quality
- description and heading with wysiwyg

## [2.1.0.3.9] - 18/03/2016
### Added
- expose label in page reference
- don't send mailing if smtp port is 0 + unlink mirror only on current page

## [2.1.0.3.10] - 20/04/2016
### Added
- shared import folder better filtered + page reference filter on label too
- 304 on site map
- DKIM mail signature
- forward on bad url + i18n component

## [2.1.0.3.11] - 02/05/2016
### Added
- math captcha DateBean
- remove jsessionid by default 
- LabelAndSectionURLCreator
- create article bug correction
- LabelAndSectionURLCreatorNoExt remove section if like root

## [2.1.0.3.12] - 26/05/2016
### Added
- merge file and image same preview
- commit template from file explorer 
- force special renderer
- heading color wrapped correction
- recursive mirror protection
- 3/ mail pattern correction
- 4/ file select css correction + TimeTracker configurable
- 5/ encode and decode url by default
- 6/ dynamic component configurable cache + header cannonical + cannonical with absolute URL
- 7/ import user merge role
- 8/ time tracker
- 9/ mail bean + dynamic component filter sort on more than one field 
- 10/ time traveler bootstrap

## [2.1.0.3.13] - 04/07/2016
### Added
- 1/ get label as title if no title found
- 2/ force html as format on url construction list
- 3/ check if resource as title + don't list trashed page on group list when create article
- 4/ correct PDF justification / remove some check on mailing template 
- 5/ insert page as first / replace jstl optimisation / special config
- 2.1.0.3.14 22/07/2016
- 1/ createI18NURL insert more unauthorized chars and replace with regular expression
- 2/ reverse logic, remove unauthorised chars from url in place of list of authaurised chars
- 3/ AccessServlet : decode url before comparaison
- 4/ search module

## [2.1.0.3.15] - 28/07/2016
### Added
- 1/ google anaytics define in a external file 
- 2/ pdf justification with link correction / no redirection on not found page / no short image link on mailing platform / duplicated imported content on copy 
- 3/ image in search result in search module / NotificationService remove weak reference if object not found / Menu displayed with main language in place of content language / DashImage marco persit
- 4/ encode xml site map content / integrity checker activated by default
- 5/ user same image filter in explorer and meta-data view in file module / create redirection macro / pagelist remote / ask overwrite or rename / large image filter for popup
- 6/ template can define the theme in preview mode
- 7/ face detection

## [2.1.0.3.16] - 28/08/2016
### Added
- 1/ remove empty maps
- 2/ clean access data
- 3/ 404 servlet 3.0 redirection content separation color, error list links, hidden 404 page if not admin
- 4/ change meta directly form content / correction bad check title hierachy with dynamic component. 
- 5/ default filter define in template / don't diplay prefix and suffix when component is'nt displayable / cache optimisation 
- 6/ update common lang + remove common configuration

## [2.1.0.3.17] 18/09/2016  - - STABLE
### Added
- 1/ update file manager + edit text file
- 2/ display integrity in edit mode / default template folder : wktp / correct image editor / correct image resize on height/ link on correction / GlobalImage link correction
- 3/ display access by mount on one year / reverse link with same string bug correction
- 4/ replaceJSTLData optimisation + replaceJSTLData in page reference
- 5/ date type configurable / add user role upload-resource / box title and footer managed by default / tab by component complexity

## [2.1.0.3.18] - 10/10/2016
### Added
- 1/ secure access with ip / display memory status
- 2/ import with meta data / time range default end of day for the end date / DynamicComponentFilter default value / marcro clean content / clean import folder
- 3/ add js bootstrap on preview
- 4/ check external link only on 404 error / filter report by creation date / bug insert file link / search reference to resource in the content
- 5/ remove getAllChildren / replace all roll by add in calendar API

## [2.1.0.19] - 19/10/2015
### Added
- 1/ check file extension on import / add creation date on default user list / update bootrapmin.js for preview edit / forced https
- 2/ independent bootstrap js for preview edition + image edition (crop+flip) / Persistence blocage correction / expose dynamiccomponet for jstl / ip security

## [2.1.0.3.20] - 27/10/2015
### Added
- 1/ bug dynamiccomponent i18n field / ipsecurity null pointer
- 2/ drop sound on page / dynamiccomponent : delete field on merge / Filed value with " / field date isPast method and getFormatedDate / Field : isPertient with contentcontext / persistence with date in properties file (desactivate by default) / update pdfbox / update file message correction /
- 3/ clean history, clean only previous version for optimisation / update tinymce to 4.4.3 / add first-in-cell class on component if needed / LanguageMap / TableComponent padding as px
- 4/ save template as structured properties / template css edition in accordeon / correct slide.jsp in default template / correction encoding for NoExtURLPathTitleCreatorOneLevel and NoExtURLCreator / PersistenceService bug analyse version correction
- 5/ sort area / clouds link 
- 6/ Manual Description display as meta data / synchronize isTemplateImported / absolute url can start with '//' / globalImage integrity correctionglobalImage integrity correction
- 7/ error module for manage 404 url / isRedirectWidthName return false by default / getcontentdate from IDate components / toTheTopComponent / mineType in AbstractFileComponent / active face detection / 	

## [2.1.0.3.21] - 28/11/2015
### Added
- 1/ retrieve meta data of picture from language with content / add filter in image url
- 2/ meta color component / page association work with empty page / unescape text title of heading (for url factory) / duplicate article + template display info / no ticket without title and better email notification. / image internal links
- 3/ create cookie manager before check url / title in default aria have proiority / auto redeploy template if index.html not found / correction real content any language detection / static parameter in template for create static site
- 4/ compare event to the end of the current day / remove special char method / VFS optimisation
- 5/ update library / info.getNowSortable / interest thread / countries as list / update lib / ComponentContextBean / GlobalImage set page in with key / GenericForm participation / simplification of isempty / set mailing config / StaticInfo weekref to MenuElement

## [2.1.0.3.22] - 02/02/17
### Added
- 1/ get title and label from not main area if not found in main area 
- 2/ google login / update ticket layout and email + imageheader realcontent correction / import export page to zip file / remove static key / paste image in preview mode
- 3/ change editable default template with a table based template / template editor amelioration (edit row name + resolve some bugs) / empty area design / background image and color in PDF / ordre area with editable template

## [2.1.0.3.23] - 20/02/17
### Added
- 1/ editable with outlook with bug / splash screen googleanalytics jsp reference / forward component / add button / import gallery bug correction / editable template PDF size correction
- 2/ image feed back js optimisation 
- 3/ device stored in request in place of session
- 4/ main help url / close event new return
- 5/ drop file on page n+1 correction / reverse link correction / registration mail format
- 6/ page-reference link to / stay pdf mode / area not empty if contain repeat item (infoBean) / generic form return email layout / drop page directly from navigation to content

## [2.1.0.3.24] - 24/03/17
### Added
- 1/ expose mount date in multimedia resource / geocoder reactivation / reverse link in message
- 2/ Resource description can be removed from config / remove '/' at the end of the url
- 3/ TOMCAT 8 / JDK 8 compatibility / component expose css and id info for edit

## [2.1.0.3.25] - 28/04/17
### Added
- 1/ create context component update / encrypt password 256bit method / placeholder in field mecanism / GenericMessage no diplication method / im mecanisme update / defautl avatar image if empty / reset password methods / pixabay shared content / hierachy with of template
- 2/ error message, check xhtml structure / vfs default encoding / check user on get user and not on action update / area level / import VFS file / export child resourece / flow : page validation

## [2.1.0.4] - 09/06/17
### Added
- 1/ mailing stat by page with macro / getCurrentUser with globalContext / remove reference to user in contentcontext / sunchronize process method in VFS / editPreview method correction
- 2/ editable outlook optimisation / text float amelioration / remove special char on some url factory / image feedback correction / trace all url / unencode heading text before create url
- 3/ ticket responsive / html insertion (meta, header, footer) / padding with table (mailing)
- 4/ ticket layout / getLabel with contentcontext for field / maximum login error
- 5/ send mail to authors of ticket / pop3 mail delivery failure analyse
- 6/ login update / getRessourceURL / trim image / taxonomy

## [2.1.0.5] 16/08/17  - - STABLE
### Added
- 1/ field validation / taxonomy integration in list service / update REST query possibility / document ref and lang / pdf correction justify / external link check internet connection 
- 2/ clear cache on publish (partial) / change sass compiler / DynamicComponet creator
- 3/ refuse cookies mecanism / DynamicComponent add sound field + edit layout / download page as EML / search fileter on file field
- 4/ collaborative mode + null pointer in PaginationContext / correction memory leak with old link between page and ressources

## [2.1.0.6] - 19/10/17
### Added
- 1/ optimmisation crypted link / one page rendering + merge dynamic form result + follow page / get path optimisaiton and search child correction / follow inherited / component optimisation for mailing
- 2/ correct transform url tag / image preload version + user list ajax sorting correction
- 3/ ticket day by day 
- 4/ create link in ticket / display components with http://fontawesome.io/

## [2.1.0.7] - 20/11/2017
### Added
- 1/ autolink tag create reverse links / SmartPageBean optimisation / font selection in title, tinymce 4.7.3, update flying saucer / xml attribute encoding correction / calendar / tinymce update
- 2/ htmlId on page / searchPage smart method / video inline optimisation / rename dynamic component field / correction pagereference page withtout content in current language not correctly corrected to default language
- 3/ don't remove &nbsp; in autolink method / row component / style-default can be set in config file / linkon link to # only if direct child of page association / preview.js : no scroll on drag start / update ace editor / update elfinder / update image field in dynamiccomponent

## [2.1.0.8] - 22/12/2017
### Added
- 1/ null pointer correction if no image on page / HtmlPart for description / add message color in template data
- 2/ product edit layout + update opensagres
- 3/ remove linked-page-id / structure smart-generic-form storage / user JAVLO_HOME env. var. for localise the static-config.properties folder

## [2.1.1.0] - 26/01/2018 - - STABLE
### Added
- 1/ component structure html correction / reset i18n on publish / add user-agent on internet connection / push static version of the site on ftp / static config cache folder / contact information component
- 2/ imagetransform update tag, can reference resources / display array component / smart generic form editable / html.head / smart link and components list / macro list / smart generic form parametrable subject and auto file name
- 3/ add location in generic form if defined / get desription from page mirror (bug corretion) / image transform projection / definitively delete del pages / sort pages in undelte macro / default countries list from JDK / page reference : direct link with page id / transfert to static file + excel export synchro on first line
- 4/ use CSVParser in place of ExcelCSVParser / remove reference to contentContext in editContext / print info on publish on garbadge / rhino 1.7R4 > rhino 1.7.9 / remove market module / remove component list in application 

## [2.1.1.1] - 16/03/2018
### Added
- 1/ optimisation reference to contentContext (JavloELFile and SearchResult) / import with translation
- 2/ language mirror / convert pdf and png to jpg / definition / persistence lock on global context and not on static variable / display bad template inheritance / media servlet / javlo design plugin / plugin relative path in url: / ticket with screenshot / multimedia filter optimisation

## [2.1.1.2] - 09/04/2018
### Added
- 1/ elfinder to 2.1.37 / LDAP login / default cache time to 3600 / token correction / meta head component / return to anchor afther edit
- 2/ DynamicComponent from HTML

## [2.1.1.3] - 09/04/2018
### Added
- 1/ colorList / extraceFieldsFromRenderer / MapDbUserFactory / Wall / password encrypt engine
- 2/ correction file servlet / syntax erreur description in static info html
- 3/ LoginLogout with login (pseudo) / tracking can be anonymised / forum update
- 4/ resources taxonomy / TinyMCE 4.8.0 / add image border + correct close insered resource / rename -sm image like -small / taxonomy extension for pageBean / taxonomy session / image background on area / chat / children link can be open as popup + cookies policy link
- 5/ accesibility escape menu parametrable target / remove type="javascript" from script tag / don't display prefix and suffix if component hidden / jsp minified / include font in graphics charter / template can be for dedicated for children association / component css class / social api key / clean resource reference macro / default filter banner in header and banner area

## [2.1.1.4] - 15/08/2018
### Added
- mobile preview
- protect preview mode

## [2.1.2.0] - 30/08/2018
### Added
- dynamic component colomnable / columnanble tag and style configuration from template
- external component
- components modules
- load js optimisation in edit mode
- face detection : display thread info on dashboard
- Quiet area : area can be removed from site properties
- transform external font in local font for PDF generation
- code mirror editor
- change language in preview mode
- pdf-multimedia expose link to pdf file
- can remove ressource from site map with special option in global context
- token ##BUILD_ID## reference the build id, can be use in html,css or js in template
- manual order of gallery
### Fixed
- resource servlet return 404 if no path defined
- face detection thread managing
- image resize if high quality by default
- reset preview and next component on delete and on insert
- preview.js update area on delete and insert component
- copy/paste from language to an other
- change password with login and not necessary with email
- change image transform url with real extenstion (sp. : .pdf.jpg in place of .pdf for filtered images)
- correct bug for link template to site in preview mode
### Removed
- imgscalr-lib
- ace editor
## [2.1.2.1] - 17/09/2018
### Added
- backup thread
- insert bar (preview mode) position absolute and red (content don't move)
- webaction servlet, call webaction from path in place of parameter
- role for smartgeneric form : count-participants, one inscription could count for more than one person.
- page reference columnalbe
- page reference popup
- properties component label
- login menu
- static files in www folder
- events list by user (MyEvents)
- forms result backup thread
- bright interface
- page reference no rediplay item
- upload user as excel file
- smart-link download image localy
- minimal interface
- page screenshot generator
- preview duplicate component
- macro sort on priority
- macro icon
- multimedia taxonomy filter
- page reference date range
- no dash macro
- REST resource service
- init meta info of the file from mp3 metadata
- PageURL : force page url
- Etag
- force cachable on component config
- merge js file
### Modified
- track cache by days in place of month
- add button unactive by default
- page properties layout
- mutltimedia, store data as properties in place of string
- new internal link - ajax search + columnable
- link to file, real content true if two link to file in the page
- DynamicComponent : field wrapped by default
- DynamicComponent : add no-renderer as class if no renderer defined
- Wall : 30 items bu pages
- edit authors information for resource
- file upload in page inherit page right
- google authentification from global context in place of static config
- background image on resize set background color in place of black (if alpha in source)
- update lucene to 7.6.0
- rest update
### Fixed
- edit index.html of template, load CodeMirror
- reset navigation if navigation renderer change
- don't delete meta data of source when copy resources
- paste element in edit mode, null pointer corrected
- tracker take only view url
- security : update lib
## [2.1.2.2] - 27/12/2018
### Added
- add edit renderer
- GeoService : convert address to coord.
- admin user can simulate access from a other user
- textToHTML tag
- mailService : list of page template
- beanutils copy with boolean
- data document
- path tracking
### modified
- defautl edit template : /jsp/edit/template/mandy-lane-premium-bright
- FieldFile use file servlet in place of resourceServlet
- autologin in view mode
- replace JSTL in title
- email wysiwyg editor
- bean utils copy with set false if boolean not found
- wysiwig not cachable if contains vars
### fixed
- menu if no add button (preview)
- page reference list of page display correction
## [2.1.2.3] - 17/02/2019
### Added
- data document with date
- use report
- stat by days
- user mail in template
- status.html page
- default component value can be defined in a "component_default" page.
### Modified
- update code mirror
- no trash by default
- help link reference to local file in place of remote site.
- update tooltipser to version 4
### fixed
- image can be repeated for represent page (but linked with the page)
- not return in preview on save in edit mode
## [2.1.2.4] - 31/07/2019
### Added
 - secure text component
 - restore published version
 - ExtendedWidget
 - introduction SLF4J
 - add forward url as canonical url
 - more info in ical service
 - mailing feedback factory
 - create mirror site with other language list
 - reset recaptcha from central config
 - create sepa QRCode
 - IP2location localisation of ip localy
 - update datatables version to 1.10.20
 - section component
 - secure file storage
 - zip file storage without encyrption
### Modified
 - security update
 - file-finder optimisation
 - jsmirror in content module
 - internal-link use default search engine
 - shared.import-document default true
 - import title link heading in place of title and subtitle
 - new layout + depth rules
 - remove use report from dashboard
 - add crop option on layer image filter
 - replace link on extended widget
 - update integrity rules
 - update DataTables to 1.10.20
 - container recursive
### fixed
 - change heading level correction
 - close popup site propoerties in preview mode if no error
 - attachement order in mail
 - ical close tag correction
 - contentcontext null request correction
 - reset hashcode on edit image
 - action servlet work with a sessionId
## [2.1.2.5] - 13/03/2020
### Added
 - display persistence size
 - annotation for security
 ## [2.1.2.6] - 13/06/2020
 ### Added
  - Survey new component and API  
  - lg image for srcset
  - publish macro
  - CTRL+ALT+e for edit current page
  - ticket budget
 ### Modified
  - status in debug note + title of debug note ticket with more info
  - dynamic filter with title, parent page and collunable
  - change resize image method
  - update to tinymce 5.4.1
  - optimize copy file of template
### fixed
  - correct bad char in user list (best json encoding)
  - remove % from url if present in title of the page
  - close transport on test connection
## [2.1.2.7] - 28/09/2020
### Added
  - access to dynamiccomponent from contentAsMap method
  - generic form : add first, add last
  - resources download on dashboard
  - autocomplete on smartgenericform
  - default area can be defined in template
  - DeepL translator
  - field color
### Modified
  - load view nav before preview nav
  - cache globalContext in application context
  - cache contentAsMap
### fixed  
  - don't redirect old url if no urlfactory asked
  - js content type
  - update security on file servlet