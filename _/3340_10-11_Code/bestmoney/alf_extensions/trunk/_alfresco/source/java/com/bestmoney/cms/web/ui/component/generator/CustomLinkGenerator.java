package com.bestmoney.cms.web.ui.component.generator;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.generator.LinkGenerator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;

/**
 * Override standard link generator so we can actually display the
 * link in edit mode.
 *
 * @author martin.bergljung@opsera.com
 */
public class CustomLinkGenerator extends LinkGenerator {

    /**
     * Ovveride and always setup as a link
     *
     * @param context
     * @param id
     * @return
     */
    @Override
    public UIComponent generate(FacesContext context, String id) {
        UIComponent component = null;
        component = context.getApplication().createComponent(UIOutput.COMPONENT_TYPE);
        component.setRendererType("javax.faces.Link");
        //component.getAttributes().put("target", "new");

        FacesHelper.setupComponentId(context, component, id);
        return component;
    }

}

