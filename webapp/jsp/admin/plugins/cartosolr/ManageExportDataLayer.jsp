<jsp:useBean id="manageexportcartographieSolr" scope="session" class="fr.paris.lutece.plugins.cartography.modules.solr.web.ExportDataLayerJspBean" />
<% String strContent = manageexportcartographieSolr.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
