/*
 * Copyright 2007 The Kuali Foundation Licensed under the Educational Community
 * License, Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/ecl1.php Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.kuali.rice.kns.uif.util;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.util.type.TypeUtils;
import org.kuali.rice.kns.uif.UifConstants;
import org.kuali.rice.kns.uif.container.Container;
import org.kuali.rice.kns.uif.core.Component;
import org.kuali.rice.kns.uif.core.DataBinding;
import org.kuali.rice.kns.uif.core.Ordered;
import org.kuali.rice.kns.uif.field.AttributeField;
import org.kuali.rice.kns.uif.field.Field;
import org.kuali.rice.kns.uif.field.GroupField;
import org.kuali.rice.kns.uif.layout.LayoutManager;
import org.kuali.rice.kns.util.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.OrderComparator;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class providing methods to help create and modify
 * <code>Component</code> instances
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ComponentUtils {

    public static <T extends Component> T copy(T component, String idSuffix) {
        T copy = copyObject(component);
        updateIdsWithSuffixNested(copy, idSuffix);
        
        return copy;
    }

    public static <T extends Object> T copyObject(T object) {
        if (object == null) {
            return null;
        }

        T copy = null;
        try {
            copy = CloneUtils.deepClone(object);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return copy;
    }

    protected static Object getCopyPropertyValue(Set<String> propertiesForReferenceCopy, String propertyName,
            Object propertyValue) {
        if (propertyValue == null) {
            return null;
        }

        Object copyValue = propertyValue;

        Class<?> valuePropertyType = propertyValue.getClass();
        if (propertiesForReferenceCopy.contains(propertyName) || TypeUtils.isSimpleType(valuePropertyType)
                || TypeUtils.isClassClass(valuePropertyType)) {
            return copyValue;
        }

        if (Component.class.isAssignableFrom(valuePropertyType)
                || LayoutManager.class.isAssignableFrom(valuePropertyType)) {
            copyValue = copyObject(propertyValue);
        }
        else {
            copyValue = ObjectUtils.deepCopy((Serializable) propertyValue);
        }

        return copyValue;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Object> T getNewInstance(T object) {
        T copy = null;
        try {
            copy = (T) object.getClass().newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to create new instance of class: " + object.getClass());
        }

        return copy;
    }

    public static <T extends Field> List<T> copyFieldList(List<T> fields, String addBindingPrefix, String idSuffix) {
        List<T> copiedFieldList = copyFieldList(fields, idSuffix);

        prefixBindingPath(copiedFieldList, addBindingPrefix);

        return copiedFieldList;
    }

    public static <T extends Field> List<T> copyFieldList(List<T> fields, String idSuffix) {
        List<T> copiedFieldList = new ArrayList<T>();

        for (T field : fields) {
            T copiedField = copy(field, idSuffix);
            copiedFieldList.add(copiedField);
        }

        return copiedFieldList;
    }

    public static <T extends Component> T copyComponent(T component, String addBindingPrefix, String idSuffix) {
        T copy = copy(component, idSuffix);

        prefixBindingPathNested(component, addBindingPrefix);

        return copy;
    }

    public static <T extends Component> List<T> copyComponentList(List<T> components, String idSuffix) {
        List<T> copiedComponentList = new ArrayList<T>();

        for (T field : components) {
            T copiedComponent = copy(field, idSuffix);
            copiedComponentList.add(copiedComponent);
        }

        return copiedComponentList;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Component> List<T> getComponentsOfType(List<? extends Component> items,
            Class<T> componentType) {
        List<T> typeComponents = new ArrayList<T>();

        for (Component component : items) {
            if (componentType.isAssignableFrom(component.getClass())) {
                typeComponents.add((T) component);
            }
        }

        return typeComponents;
    }
    
    public static <T extends Component> List<T> getComponentsOfTypeDeep(List<? extends Component> items,
            Class<T> componentType) {
        List<T> typeComponents = new ArrayList<T>();

        for (Component component : items) {
            typeComponents.addAll(getComponentsOfTypeDeep(component, componentType));
        }

        return typeComponents;
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Component> List<T> getComponentsOfTypeDeep(Component component, Class<T> componentType) {
        List<T> typeComponents = new ArrayList<T>();

        if (component == null) {
            return typeComponents;
        }

        if (componentType.isAssignableFrom(component.getClass())) {
            typeComponents.add((T) component);
        }

        for (Component nested : component.getNestedComponents()) {
            typeComponents.addAll(getComponentsOfTypeDeep(nested, componentType));
        }

        return typeComponents;
    }   

    public static void prefixBindingPath(List<? extends Field> fields, String addBindingPrefix) {
        for (Field field : fields) {
            if (field instanceof DataBinding) {
                prefixBindingPath((DataBinding) field, addBindingPrefix);
            }
            else if (field instanceof GroupField) {
                List<Field> groupFields = getComponentsOfType(((GroupField) field).getItems(), Field.class);
                prefixBindingPath(groupFields, addBindingPrefix);
            }
        }
    }

    public static void prefixBindingPathNested(Component component, String addBindingPrefix) {
        if (component instanceof DataBinding) {
            prefixBindingPath((DataBinding) component, addBindingPrefix);
        }

        for (Component nested : component.getNestedComponents()) {
           if (nested != null) {
              prefixBindingPathNested(nested, addBindingPrefix);
           }
        }
    }

    public static void prefixBindingPath(DataBinding field, String addBindingPrefix) {
        String bindingPrefix = addBindingPrefix;
        if (StringUtils.isNotBlank(field.getBindingInfo().getBindByNamePrefix())) {
            bindingPrefix += "." + field.getBindingInfo().getBindByNamePrefix();
        }
        field.getBindingInfo().setBindByNamePrefix(bindingPrefix);
        
        if (field instanceof AttributeField){
        	AttributeField attrfield = (AttributeField)field;
        	if (StringUtils.isNotBlank(attrfield.getAdditionalDisplayAttributeName())){
        		attrfield.getAdditionalDisplayAttributeBindingInfo().setBindByNamePrefix(bindingPrefix);
        	}
        	if (StringUtils.isNotBlank(attrfield.getAlternateDisplayAttributeName())){
        		attrfield.getAlternateDisplayAttributeBindingInfo().setBindByNamePrefix(bindingPrefix);
        	}
        }
    }

    public static void updateIdsWithSuffixNested(List<? extends Component> components, String idSuffix) {
        for (Component component : components) {
            updateIdsWithSuffixNested(component, idSuffix);
        }
    }

    public static void updateIdsWithSuffixNested(Component component, String idSuffix) {
        updateIdsWithSuffix(component, idSuffix);

        if (Container.class.isAssignableFrom(component.getClass())) {
            LayoutManager layoutManager = ((Container) component).getLayoutManager();
            layoutManager.setId(layoutManager.getId() + idSuffix);
        }

        for (Component nested : component.getNestedComponents()) {
            if (nested != null) {
                updateIdsWithSuffixNested(nested, idSuffix);
            }
        }
    }

    public static void updateIdsWithSuffix(Component component, String idSuffix) {
        // make sure id has two underscore delimiter so we can pick off original dictionary id
        component.setId(component.getId() + idSuffix);
    }

    public static void setComponentsPropertyDeep(List<? extends Component> components, String propertyPath,
            Object propertyValue) {
        for (Component component : components) {
            setComponentPropertyDeep(component, propertyPath, propertyValue);
        }
    }

    public static void setComponentPropertyDeep(Component component, String propertyPath, Object propertyValue) {
        ObjectPropertyUtils.setPropertyValue(component, propertyPath, propertyValue, true);

        for (Component nested : component.getNestedComponents()) {
            if (nested != null) {
                setComponentPropertyDeep(nested, propertyPath, propertyValue);
            }
        }
    }

    public static List<String> getComponentPropertyNames(Class<? extends Component> componentClass) {
        List<String> componentProperties = new ArrayList<String>();

        PropertyDescriptor[] properties = BeanUtils.getPropertyDescriptors(componentClass);
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor descriptor = properties[i];
            if (descriptor.getReadMethod() != null) {
                componentProperties.add(descriptor.getName());
            }
        }

        return componentProperties;
    }

    public static void pushObjectToContext(List<? extends Component> components, String contextName, Object contextValue) {
        for (Component component : components) {
            pushObjectToContext(component, contextName, contextValue);
        }
    }

    public static void pushObjectToContext(Component component, String contextName, Object contextValue) {
        if (component == null) {
            return;
        }

        component.pushObjectToContext(contextName, contextValue);

        // special container check so we pick up the layout manager
        if (Container.class.isAssignableFrom(component.getClass())) {
            LayoutManager layoutManager = ((Container) component).getLayoutManager();
            if (layoutManager != null) {
                layoutManager.pushObjectToContext(contextName, contextValue);

                for (Component nestedComponent : layoutManager.getNestedComponents()) {
                    pushObjectToContext(nestedComponent, contextName, contextValue);
                }
            }
        }

        for (Component nestedComponent : component.getNestedComponents()) {
            pushObjectToContext(nestedComponent, contextName, contextValue);
        }
    }

    public static void updateContextsForLine(List<? extends Component> components, Object collectionLine,
            int lineIndex) {
        for (Component component : components) {
            updateContextForLine(component, collectionLine, lineIndex);
        }
    }

    public static void updateContextForLine(Component component, Object collectionLine, int lineIndex) {
        pushObjectToContext(component, UifConstants.ContextVariableNames.LINE, collectionLine);
        pushObjectToContext(component, UifConstants.ContextVariableNames.INDEX, new Integer(lineIndex));
        
        boolean isAddLine = (lineIndex == -1);
        pushObjectToContext(component, UifConstants.ContextVariableNames.IS_ADD_LINE, isAddLine);
    }

    public static void processIds(Component component, Map<String, Integer> seenIds) {
        String componentId = component.getId();
        Integer seenCount = new Integer(0);
        if (StringUtils.isNotBlank(componentId)) {
            if (seenIds.containsKey(componentId)) {
                seenCount = seenIds.get(componentId);
                seenCount += 1;

                component.setId(componentId + "_" + seenCount);
            }

            seenIds.put(componentId, seenCount);
        }

        if (Container.class.isAssignableFrom(component.getClass())) {
            LayoutManager layoutManager = ((Container) component).getLayoutManager();
            if ((layoutManager != null) && StringUtils.isNotBlank(layoutManager.getId())) {
                seenCount = new Integer(0);
                if (seenIds.containsKey(layoutManager.getId())) {
                    seenCount = seenIds.get(layoutManager.getId());
                    seenCount += 1;

                    layoutManager.setId(layoutManager.getId() + "_" + seenCount);
                }

                seenIds.put(layoutManager.getId(), seenCount);
            }
        }

        for (Component nested : component.getNestedComponents()) {
            if (nested != null) {
                processIds(nested, seenIds);
            }
        }
    }

    /**
     * Performs sorting logic of the given list of <code>Ordered</code>
     * instances by its order property
     * <p>
     * Items list is sorted based on its order property. Lower order values are
     * placed higher in the list. If a item does not have a value assigned for
     * the order (or is equal to the default order of 0), it will be assigned
     * the a value based on the given order sequence integer. If two or more
     * items share the same order value, all but the last item found in the list
     * will be removed.
     * </p>
     * 
     * @param items
     * @param defaultOrderSequence
     * @return List<Ordered> sorted items
     * @see org.kuali.rice.kns.uif.Component.getOrder()
     * @see @see org.springframework.core.Ordered
     */
    public static List<? extends Ordered> sort(List<? extends Ordered> items, int defaultOrderSequence) {
        List<Ordered> orderedItems = new ArrayList<Ordered>();

        // do replacement for items with the same order property value
        Set<Integer> foundOrders = new HashSet<Integer>();

        // reverse the list, so items later in the list win
        Collections.reverse(items);
        for (Ordered component : items) {
            int order = component.getOrder();

            // if order not set just add to list
            if (order == 0) {
                orderedItems.add(component);
            }
            // check if the order value has been used already
            else if (!foundOrders.contains(new Integer(order))) {
                orderedItems.add(component);
                foundOrders.add(new Integer(order));
            }
        }

        // now reverse the list back so we can assign defaults for items without
        // an order value
        Collections.reverse(items);
        for (Ordered component : items) {
            int order = component.getOrder();

            // if order property not set assign default
            if (order == 0) {
                defaultOrderSequence++;
                while (foundOrders.contains(new Integer(defaultOrderSequence))) {
                    defaultOrderSequence++;
                }
                component.setOrder(defaultOrderSequence);
            }
        }

        // now sort the list by its order property
        Collections.sort(orderedItems, new OrderComparator());

        return orderedItems;
    }
    
    /**
     * This method takes in an expression and a list to be filled in with names(property names)
     * of controls found in the expression. This method returns a js expression which can
     * be executed on the client to determine if the original exp was satisfied before
     * interacting with the server - ie, this js expression is equivalent to the one passed in.
     * 
     * There are limitations on the Spring expression language that can be used as this method.
     * It is only used to parse expressions which are valid case statements for determining if
     * some action/processing should be performed.  ONLY Properties, comparison operators, booleans,
     * strings, matches expression, and boolean logic are supported.  Properties must
     * be a valid property on the form, and should have a visible control within the view.
     * 
     * Example valid exp: account.name == 'Account Name'
     * 
     * @param exp
     * @param controlNames
     * @return
     */
    public static String parseExpression(String exp, List<String> controlNames){
        //Clean up expression to ease parsing
        exp = StringUtils.replace(exp, "!=", " != ");
        exp = StringUtils.replace(exp, "==", " == ");
        exp = StringUtils.replace(exp, ">", " > ");
        exp = StringUtils.replace(exp, "<", " < ");
        exp = StringUtils.replace(exp, "<=", " <= ");
        exp = StringUtils.replace(exp, ">=", " >= ");
        exp = exp.trim();

        String conditionJs = exp;
        String stack = "";
        boolean expectingSingleQuote = false;
        boolean ignoreNext = false;
        for(int i = 0; i < exp.length(); i++) { 
            char c = exp.charAt(i);
            if(!expectingSingleQuote && !ignoreNext && (c == '(' || c == ' ' || c == ')')){
                evaluateCurrentStack(stack.trim(), controlNames);
                //reset stack
                stack = "";
                continue;
            }
            else if(!ignoreNext && c == '\''){
                stack = stack + c;
                expectingSingleQuote = !expectingSingleQuote;
            }
            else if(c == '\\'){
                stack = stack + c;
                ignoreNext = !ignoreNext;
            }
            else{
                stack = stack + c;
                ignoreNext = false;
            }
        }
        
        conditionJs = conditionJs
        .replaceAll("\\s(?i:ne)\\s", " != ")
        .replaceAll("\\s(?i:eq)\\s", " == ")
        .replaceAll("\\s(?i:gt)\\s", " > ")
        .replaceAll("\\s(?i:lt)\\s", " < ")
        .replaceAll("\\s(?i:lte)\\s", " <= ")
        .replaceAll("\\s(?i:gte)\\s", " >= ")
        .replaceAll("\\s(?i:and)\\s", " && ")
        .replaceAll("\\s(?i:or)\\s", " || ")
        .replaceAll("\\s+(?i:matches)\\s+'.*'", ".match(/" 
                + "$0".replaceAll("\\s+(?i:matches)\\s+", "") + "/).length > 0 ");
        
        for(String propertyName: controlNames){
            conditionJs = conditionJs.replaceAll(propertyName, 
                    "coerceValue('"+ propertyName +"')");
        }
        return conditionJs;
    }
    
    /**
     * Used internally by parseExpression to evalute if the current stack is a property
     * name (ie, will be a control on the form)
     * 
     * @param stack
     * @param controlNames
     */
    private static void evaluateCurrentStack(String stack, List<String> controlNames){
       if(StringUtils.isNotBlank(stack)){
           if(!(stack.equals("==") 
                   || stack.equals("!=")
                   || stack.equals(">")
                   || stack.equals("<")
                   || stack.equals(">=")
                   || stack.equals("<=")
                   || stack.equalsIgnoreCase("ne")
                   || stack.equalsIgnoreCase("eq")
                   || stack.equalsIgnoreCase("gt")
                   || stack.equalsIgnoreCase("lt")
                   || stack.equalsIgnoreCase("lte")
                   || stack.equalsIgnoreCase("gte")
                   || stack.equalsIgnoreCase("matches"))){
               
               boolean isNumber = false;
               if((StringUtils.isNumeric(stack.substring(0,1)) 
                       || stack.substring(0,1).equals("-"))){
                   try{
                       Double.parseDouble(stack);
                       isNumber = true;
                   }
                   catch(NumberFormatException e){
                       isNumber = false;
                   }
               }
               
               if(!(stack.equalsIgnoreCase("false") || stack.equalsIgnoreCase("true") || isNumber
                       || stack.startsWith("'") || stack.endsWith("'"))){
                   if(!controlNames.contains(stack)){
                       controlNames.add(stack);
                   }
               }
           }
       }
    }

}
