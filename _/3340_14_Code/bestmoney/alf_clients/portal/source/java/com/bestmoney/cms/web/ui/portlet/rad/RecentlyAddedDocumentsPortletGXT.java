package com.bestmoney.cms.web.ui.portlet.rad;

import com.bestmoney.cms.web.ui.portlet.rad.gwt.client.RecentlyAddedDocumentsApp;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Portlet displaying recently added documents in the Alfresco repository
 * in a GXT Grid widget
 *
 * @author martin.bergljung@opsera.com
 */
public class RecentlyAddedDocumentsPortletGXT extends GenericPortlet {
    @Override
    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse)
            throws PortletException, IOException {
        // String username = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
        // PortletSession session = FacesUtils.getSession(false);
        // String password = (String) session.getAttribute("USER_PASSWORD", PortletSession.APPLICATION_SCOPE);

        renderResponse.setContentType("text/html");
        PrintWriter writer = renderResponse.getWriter();

        String ticket = getAlfrescoTicket("admin", "admin", writer);

        writer.println("<script language='javascript'>" +
                "function getAlfrescoTicket() {\n" +
                "  return '" + ticket + "';" +
                "}\n</script>");

        writer.println("<script language='javascript' src='" + renderRequest.getContextPath() +
                "/recentlyAddedDocumentsApp.nocache.js'></script>");

        writer.println("<div id='" + RecentlyAddedDocumentsApp.HTML_DIV_ID + "'></div>");

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

    private String getAlfrescoTicket(String userName, String password, PrintWriter writer) {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod("http://localhost:8080/alfresco/service/api/login?u=" +
                userName + "&pw=" + password);

        try {
            int statusCode = client.executeMethod(getMethod);
            if (statusCode == HttpStatus.SC_OK) {
                return getMethod.getResponseBodyAsString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","").
                        replace("<ticket>", "").replace("</ticket>", "").trim();
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
}