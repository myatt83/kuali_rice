/**
 * Copyright 2005-2012 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krad.uif.field;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.krad.datadictionary.validator.ErrorReport;
import org.kuali.rice.krad.datadictionary.validator.RDValidator;
import org.kuali.rice.krad.datadictionary.validator.TracerToken;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.container.Container;
import org.kuali.rice.krad.uif.element.Message;
import org.kuali.rice.krad.uif.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Field wrapper for a Message
 *
 * <p>
 * The <code>Message</code> is used to display static text in the user
 * interface
 * </p>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class MessageField extends FieldBase {
    private static final long serialVersionUID = -7045208136391722063L;
    
    private Message message;

    public MessageField() {
        super();
    }

    /**
     * @see org.kuali.rice.krad.uif.component.ComponentBase#getComponentsForLifecycle()
     */
    @Override
    public List<Component> getComponentsForLifecycle() {
        List<Component> components = super.getComponentsForLifecycle();

        components.add(message);

        return components;
    }

    /**
     * PerformFinalize override - calls super, corrects the field's Label for attribute to point to this field's content
     *
     * @param view the view
     * @param model the model
     * @param parent the parent component
     */
    @Override
    public void performFinalize(View view, Object model, Component parent) {
        super.performFinalize(view, model, parent);

        //determine what id to use for the for attribute of the label, if present
        if(this.getFieldLabel() != null && this.getMessage() != null
                && StringUtils.isNotBlank(this.getMessage().getId())){

            if(this.getMessage().getMessageComponentStructure() != null
                    && !this.getMessage().getMessageComponentStructure().isEmpty()){
                //wrapper will be a rich message div - no suffix
                this.getFieldLabel().setLabelForComponentId(this.getMessage().getId());
            }
            else{
                //wrapper will be a normal message span - add suffix
                this.getFieldLabel().setLabelForComponentId(this.getMessage().getId() + UifConstants.IdSuffixes.SPAN);
            }
        }
    }

    /**
     * Convenience method for setting the message text
     *
     * @param messageText - text to display for the message
     */
    public void setMessageText(String messageText) {
        if (message != null) {
            message.setMessageText(messageText);
        }
    }

    /**
     * Nested @{link org.kuali.rice.krad.uif.element.Message} component wrapped in the field
     * 
     * @return Message instance
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Setter for the nested message instance
     * 
     * @param message
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * @see org.kuali.rice.krad.uif.component.Component#completeValidation
     */
    @Override
    public ArrayList<ErrorReport> completeValidation(TracerToken tracer){
        ArrayList<ErrorReport> reports=new ArrayList<ErrorReport>();
        tracer.addBean(this);

        // Checks that the message is set
        if(getMessage()==null){
            if(RDValidator.checkExpressions(this,"message")){
                ErrorReport error = ErrorReport.createWarning("Message should not be null",tracer);
                error.addCurrentValue("message ="+getMessage());
                reports.add(error);
            }
        }

        // Checks that the label is set
        if(getLabel()==null){
            if(RDValidator.checkExpressions(this,"label")){
                ErrorReport error = ErrorReport.createWarning("Label is null, message should be used instead",tracer);
                error.addCurrentValue("label ="+getLabel());
                error.addCurrentValue("Message ="+getMessage());
                reports.add(error);
            }
        }

        reports.addAll(super.completeValidation(tracer.getCopy()));

        return reports;
    }
}
