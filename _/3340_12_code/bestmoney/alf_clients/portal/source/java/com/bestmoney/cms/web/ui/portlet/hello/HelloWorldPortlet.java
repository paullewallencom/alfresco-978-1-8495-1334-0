package com.bestmoney.cms.web.ui.portlet.hello;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Hello World Portlet
 */
public class HelloWorldPortlet extends GenericPortlet {
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.println("Hello World!");
    }
}
