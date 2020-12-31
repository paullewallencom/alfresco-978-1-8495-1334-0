package com.bestmoney.test.foundationservice;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

/**
 * This Web Script is just used to test
 * some Foundation Services calls
 * from the MyCmsService
 * <p/>
 * It is called with URL: http://localhost:8080/alfresco/service/3340_03/testfs
 */
public class TestFoundationServicesWebScript extends AbstractWebScript {
    private MyCmsService m_myCmsService;

    public void setMyCmsService(MyCmsService myCmsService) {
        m_myCmsService = myCmsService;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException {
        m_myCmsService.writeSomething();
        m_myCmsService.readSomething();
        m_myCmsService.cleanUp();

        res.getWriter().write("Done with the Foundation Service tests!");
    }
}
