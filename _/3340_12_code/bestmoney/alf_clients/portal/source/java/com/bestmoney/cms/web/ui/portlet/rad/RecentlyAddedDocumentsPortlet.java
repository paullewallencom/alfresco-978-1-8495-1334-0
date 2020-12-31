package com.bestmoney.cms.web.ui.portlet.rad;

//import javax.faces.context.FacesContext;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.bestmoney.cms.web.ui.portlet.rad.model.ContentModel;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Portlet displaying recently added documents in the Alfresco repository
 * with pure Java and HTML approach
 *
 * @author martin.bergljung@opsera.com
 */
public class RecentlyAddedDocumentsPortlet extends GenericPortlet {
    @Override
    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse)
            throws PortletException, IOException {
        // String username = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        // PortletSession session = FacesUtils.getSession(false);
        // String password = (String) session.getAttribute("USER_PASSWORD", PortletSession.APPLICATION_SCOPE);

        renderResponse.setContentType("text/html");
        PrintWriter writer = renderResponse.getWriter();
        String json = callAlfrescoWebScript("http://localhost:8080/alfresco/service/bestmoney/recent", writer);
        List<ContentModel> contentItems = readContentJSON(json);
        writer.write(convert2HtmlTable(contentItems));
        writer.close();
    }

    @Override
    protected void doHelp(RenderRequest renderRequest, RenderResponse renderResponse)
            throws PortletException, IOException {
        renderResponse.setContentType("text/html");
        PrintWriter writer = renderResponse.getWriter();
        writer.write("Help");
        writer.close();
    }

    @Override
    protected void doEdit(RenderRequest renderRequest, RenderResponse renderResponse)
            throws PortletException, IOException {
        renderResponse.setContentType("text/html");
        PrintWriter writer = renderResponse.getWriter();
        writer.println("Edit");
        writer.close();
    }

    private String callAlfrescoWebScript(String url, PrintWriter writer) {
        HttpClient client = new HttpClient();
        Credentials defaultcreds = new UsernamePasswordCredentials("admin", "admin");
        client.getState().setCredentials(AuthScope.ANY, defaultcreds);
        GetMethod getMethod = new GetMethod(url);
        getMethod.setDoAuthentication(true);

        try {
            int statusCode = client.executeMethod(getMethod);
            if (statusCode == HttpStatus.SC_OK) {
                return getMethod.getResponseBodyAsString();
            } else {
                writer.write("Got Error " + statusCode + " when calling Alfresco: " + getMethod.getURI());
            }
        } catch (HttpException e) {
            writer.write("Fatal protocol violation: " + e.getMessage());
        } catch (IOException e) {
            writer.write("Fatal transport error: " + e.getMessage());
        } finally {
            getMethod.releaseConnection();
        }

        return "";
    }

    private List<ContentModel> readContentJSON(String json) {
        List<ContentModel> contentItems = new ArrayList<ContentModel>();

        if (json == null || json.trim().length() == 0) {
            return contentItems;
        }

        try {
            ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
            JsonNode rootNode = mapper.readValue(json, JsonNode.class);
            JsonNode contentNodes = rootNode.path("content");

            for (JsonNode itemNode : contentNodes) {
                ContentModel contentItem = new ContentModel();
                contentItem.id = itemNode.path("id").getTextValue();
                contentItem.locked = itemNode.path("locked").getTextValue();
                contentItem.path = itemNode.path("path").getTextValue();
                contentItem.childrenSize = itemNode.path("childrenSize").getTextValue();
                contentItem.cmName = itemNode.path("cmName").getTextValue();
                contentItem.cmTitle = itemNode.path("cmTitle").getTextValue();
                contentItem.cmDescription = itemNode.path("cmDescription").getTextValue();
                contentItem.cmModifier = itemNode.path("cmModifier").getTextValue();
                contentItem.cmModified = itemNode.path("cmModified").getTextValue();
                contentItems.add(contentItem);
            }
        } catch (IOException e) {
            System.err.println("Fatal JSON Parsing error: " + e.getMessage());
            e.printStackTrace();
        }

        return contentItems;
    }

    private String convert2HtmlTable(List<ContentModel> contentItems) {
        StringBuffer sb = new StringBuffer("<table id=\"docsTable\"’>");
        sb.append("<thead>");
        sb.append("<tr>");
        sb.append("<th scope=\"col\">Name</th>");
        sb.append("<th scope=\"col\">Path</th>");
        sb.append("<th scope=\"col\">Modified</th>");
        sb.append("<th scope=\"col\">Modifier</th>");
        sb.append("</tr>");
        sb.append("</thead>");
        sb.append("<tbody>");
        for (ContentModel item : contentItems) {
            sb.append("<tr>");
            sb.append("<td>");sb.append(item.cmName);sb.append("</td>");
            sb.append("<td>");sb.append(item.path);sb.append("</td>");
            sb.append("<td>");sb.append(item.cmModified);sb.append("</td>");
            sb.append("<td>");sb.append(item.cmModifier);sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody>");
        sb.append("</table>");

        return sb.toString();
    }
}


