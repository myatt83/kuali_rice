<#--

    Copyright 2005-2013 The Kuali Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.opensource.org/licenses/ecl2.php

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<#macro uif_lightTable group params...>
    <@krad.groupWrap group=group>

        <#if !group.emptyTable>
            <#local row>
                <#compress>
                <#list group.items as item>
                @quot@<@krad.template component=item/>@quot@,
                </#list>
                </#compress>
            </#local>
        </#if>

        <table id="${group.id}_lightTable">
            <thead>
                <tr>
                    <#list group.headerLabels as label>
                        <th><@krad.template component=label/></th>
                    </#list>
                </tr>
            </thead>
        </table>

        <#-- build the rows and add the content to aaData in templateOptions -->
        ${group.buildRows(row, KualiForm)}

        <@krad.script value="createTable('${group.id}_lightTable', ${group.richTable.templateOptionsJSString}); "/>
    </@krad.groupWrap>
</#macro>