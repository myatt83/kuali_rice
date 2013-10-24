import ojbmetadata.ClassDescriptor
import ojbmetadata.CollectionDescriptor;
import ojbmetadata.Field
import ojbmetadata.ReferenceDescriptor;

/**
 * Copyright 2005-2013 The Kuali Foundation
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
def generateJPABO(classes, sourceDirectories, projHome, dry, verbose, backupDir, logger, pkClassesOnly){
	/*
	The second pass iterates over all of the class descriptors found above and generates JPA annotations.
	*/
	if(pkClassesOnly)
		logger = JPAConversionHandlers.cpk_log
		
	def type_handler = new CustomerTypeHandler();
	classes.values().each {
	    ClassDescriptor c ->     
	        println "Class Name: $c.className"
			logger.log "Annotating:\t ${c.className}"
	        def javaFile
	        def backupFile
	        sourceDirectories.each {
	            dir ->
					def filePathWithinSource = "${c.className.replaceAll('\\.', '/')}.java"
	                def file = "$dir/$filePathWithinSource"
					if ( verbose ) {
						println "Looking for: $file"
					}
	                if (new File(file).exists()) {
	                    javaFile = new File(file)
	                    backupFile = new File(backupDir + filePathWithinSource)
	                    c.cpkFilename = dir + "/" + c.className.replaceAll("\\.", "/") + "Id.java"
	                }
	        }
	        
	        if (!javaFile) {
	            logger.log "${file} does not exist.  Can not annotate ${c.className}.  Please check that its source directory is included in this script."
					if ( verbose ) {
						println "Unable to find $file, skipping"
					}
	            return
	        }
	        
	        def classAnnotation = ""
	        def text = javaFile.text
	        def cpkFieldText = ""
	        def cpkGetterText = ""
			def cpkConstructorArgs = ""
			def cpkConstructorBody = ""
	        def packageName  = c.className[0..c.className.lastIndexOf('.') - 1]
	        def cpkClassName = c.className[c.className.lastIndexOf('.') + 1..-1] + "Id"
	        
	        if (!dry) {
		        if (backupFile.exists()) {
		            backupFile.delete()
		        }
		        backupFile << text
		        //backupFile << "\n"
	        }
	        
	        if (c.compoundPrimaryKey) {
	            classAnnotation += "@IdClass(${c.className}Id.class)\n"
	            text = addJpaImport(text, "IdClass")
	            logger.log "\tPlease check generated compound primary key class [${c.className}Id.java]"
				c.pkClassIdText += handle_pkClass_Info(packageName, cpkClassName)
	        }
	        
			
			def un_overring_fields = [:]
			//todo: need find out how to log this
			//fields with obj_id, vern_num, anonymous are not counted 
			//UnOverridingFields function needs to check them and unlog them...
			if(hasSuperClass(javaFile.text) != null ){
				if(hasUnOverridingFields(javaFile.text, c.fields, un_overring_fields)){
					//classAnnotation += "\n//Please check Super classes for AttributeOverriding"
					logger.log("\tPlease check Super classes for AttributeOverriding on ${un_overring_fields.values()}")
				}	
			}	
			
	        text = addJpaImport(text, "Entity")
	        text = addJpaImport(text, "Table")
	        text = addJpaImport(text, "CascadeType")                    
			text = addJpaImport(text, "Column")
			text = addJpaImport(text, "Id")
			
			classAnnotation += "@Entity\n@Table(name=\"${c.tableName}\")"
	        text = annotate(text, "public class", classAnnotation)
			try {
		        c.fields.values().each {
		            Field f ->
						//println("**********************PK\t" + f.name + f.primarykey);
		                def annotation = ""
		                def nullable = '' 
						// handling PK fields
		                if (f.primarykey) {
		                    annotation += "@Id\n\t"
							// If a compound PK - start building the Java code for that class 
		                    if (c.compoundPrimaryKey) {
		                        def temp = new String(text).trim()
		                        temp = temp.replaceFirst("(private|protected).*(\\b${f.name})(\\s)*;", "ABCD1\$0ABCD2")
		                        if (temp.indexOf('ABCD1') > -1 && temp.indexOf('ABCD2') > -1) {
		                            temp = temp[temp.indexOf('ABCD1') + 5 .. -1]
		                            temp = temp[0 .. temp.indexOf('ABCD2') - 1]
	                                cpkFieldText += "\t@Id\n"
	                                cpkFieldText += "\t@Column(name=\"${f.column}\")\n"
		                            cpkFieldText += "\t\t${temp.trim()}\n\t"
		                            temp = temp[temp.indexOf(' ') .. -1].trim()
		                            temp = temp[0 .. temp.indexOf(' ')].trim()
		                            def temp2 = f.name[0].toUpperCase() + f.name[1 .. -1]
									cpkConstructorArgs += "${temp} ${temp2}," 
									cpkConstructorBody += "${f.name} = ${temp2};\n\t\t"
		                            cpkGetterText += "\tpublic ${temp} get${temp2}() { return ${f.name}; }\n\t"
		                        }
		                    }   
							//println("*****************text \n" + cpkFieldText)
		                }
		                if(f.sequenceName){
							annotation += "@GeneratedValue(generator=\"${f.sequenceName}\")\n\t"
							annotation += "@PortableSequenceGenerator(name=\"${f.sequenceName}\")\n\t"
							text = addJpaImport(text,"GeneratedValue")
							text = addOtherImport(text,"org.kuali.rice.krad.data.jpa.eclipselink.PortableSequenceGenerator")
		                }
		                if (f.locking) {
		                    annotation += "@Version\n\t"
		                    text = addJpaImport(text, "Version")
		                }
		                if (['clob', 'blob'].contains(f.jdbcType?.toLowerCase())) {
		                    annotation += "@Lob\n\t"
							annotation += "@Basic(fetch=FetchType.LAZY)\n\t"
		                    text = addJpaImport(text, "Lob")
		                    text = addJpaImport(text, "Basic")
		                    text = addJpaImport(text, "FetchType")
		                }
//		                if (f.conversion?.toString()?.size() > 0){
//		                	logger.log "\tHandling custom Types for ${c.className}.${f.name}"
//							text = addImportHibernate(text, "Type")
//							def this_anna = type_handler.handleCustomerTypes(f.conversion, f)
//							annotation += this_anna
//							
//							if(f.primarykey){
//								c.pkClassIdText = addImportHibernate(c.pkClassIdText, "Type")
//								def pkImport = findImportType(text, f.name)
//								c.pkClassIdText = addOtherImport(c.pkClassIdText, pkImport)
//								cpkFieldText = annotate(cpkFieldText, "(private|protected).*(\\b${f.name})((\\s)*|(\\s)+.*);", this_anna)
//							}
//		                }					
		                if (f.jdbcType?.equalsIgnoreCase("date") || f.jdbcType?.equalsIgnoreCase("timestamp")) {
		                	annotation += "\t@Temporal(TemporalType.TIMESTAMP)\n\t";
		                	text = addJpaImport(text, "Temporal") 
		                	text = addJpaImport(text, "TemporalType")                    
		                }
						
		                if (f.nullable == true) nullable = ", nullable=${f.nullable}"
		                annotation += "@Column(name=\"${f.column}\"${nullable})"
		                text = annotate(text, "(private|protected).*(\\b${f.name})((\\s)*|(\\s)+.*);", annotation)
					}
			} catch(Exception e) {
				JPAConversionHandlers.error_log.log "Found exception in Annotating: ${c.className} fields \n\t${e.getMessage()} " 
			}
			//handle reference objects
			try{
				c.referenceDescriptors.values().each {
					ReferenceDescriptor rd ->
					//test find bi-direction
					findBiDirectionRelationships(classes, c, rd, logger)
					def annotation = ""
	                annotation += determineOneOrManyToOne(classes, c, rd, logger)
	                text = addJpaImport(text, annotation[1..-1])
	                def cascade = determineCascadeTypes(rd)
                    def readOnlyFK = ", insertable=false, updatable=false"
	                annotation += "(${determineFetchType(rd)}"
	                text = addJpaImport(text, "FetchType")
	                if (cascade) annotation += ", ${cascade}" 
	                annotation += ")\n\t"
	                def size = rd.foreignKeys.size()
	                if (size > 1) {
	                    annotation += "@JoinColumns({"
	                    text = addJpaImport(text, "JoinColumns")
	                }
					//checking each foreign keys
	                rd.foreignKeys.eachWithIndex {
	                    fk, i ->
	                    //println "\n***********CLASSREFS************\t" + rd.classRef + "\treferencing field----\t" + fk.fieldRef
	                        def fkColumn
	                        fk.fieldRef ? (fkColumn = c.fields[fk.fieldRef].column) : (fkColumn = findFieldIdRef(c.fields, fk.fieldIdRef).column)
							if(size > 1)
							{
								def ref_colmn = findReferencedColumnName(classes, rd.classRef.toString(), fk.fieldRef.toString())
								annotation += "@JoinColumn(name=\"${fkColumn}\", referencedColumnName=\"${fkColumn}\""
							}
							else
								annotation += "@JoinColumn(name=\"${fkColumn}\""
	                        if (matchColumn(fkColumn, c.fields)) annotation += readOnlyFK
	                        annotation += ")"
	                        text = addJpaImport(text, "JoinColumn")
	                        if (i < size - 1) annotation += ", "
	                }
	                if (size > 1) annotation += "})"
	                text = annotate(text, "(private|protected).*(\\b${rd.name})(\\s)*.*;", annotation)
				}
			} catch(Exception e) { 
				JPAConversionHandlers.error_log.log "Found exception in Annotating: ${c.className} refenced objects \n\t${e.getMessage()} " 
			}
		
			try{
				c.collectionDescriptors.values().each {
					CollectionDescriptor cd ->
		                def annotation = ""
		                def error
		                def annotationName = determineOneOrManyToMany(classes, c, cd)
		                text = addJpaImport(text, annotationName[1..-1])
		                def cascade = determineCascadeTypes(cd)
		                if (cd.manyToMany) {
		                    annotation += annotationName
		                    annotation += "(${determineFetchType(cd)}"
		                    if (cascade) annotation += ", ${cascade}" 
		                    annotation += ")"
		                    annotation += "@JoinTable(name=\"${cd.indirectionTable}\", \n\t"
		                    annotation += "           joinColumns=@JoinColumn(name=\"${cd.fkPointingToThisClassColumn[0]}\"), \n\t"
		                    annotation += "           inverseJoinColumns=@JoinColumn(name=\"${cd.fkPointingToElementClassColumn[0]}\"))"
		                    text = addJpaImport(text, "JoinTable")
		                    text = addJpaImport(text, "JoinColumn")
		                    text = addJpaImport(text, "FetchType")
		                } else {
		                    def mappedBy
		                    def keys = []
		                    def columns = []
		                    def ifks = []
		                    cd.inverseForeignKeys.each {
		                        ifk ->
		                            def f
		                            ifk.fieldRef ? (f = c.fields[ifk.fieldRef]) : (f = findFieldIdRef(c.fields, ifk.fieldIdRef))
		                            if (f?.name) keys.add(f.name)
		                            if (f?.column) columns.add(f.column)
		                            ifks.add(ifk.fieldRef)
		                    }
		                    def rdClass = classes[cd.elementClassRef]
		                    if (!rdClass) {                         
		                    	error = "When determining mappedBy, class descriptor for [${cd.elementClassRef}] does not have a table defined. Please check it."
		                    } else {
			                    rdClass.referenceDescriptors.values().each {
			                        rd -> 
			                            def rdKeys = []
			                            rd.foreignKeys.each {
			                                fk ->
			                                    def fkName
			                                    fk.fieldRef ? (fkName = rdClass.fields[fk.fieldRef]?.name) : (fkName = findFieldIdRef(rdClass.fields, fk.fieldIdRef)?.name)
			                                    rdKeys.add(fkName)
			                            }
			                            if (!keys.isEmpty() && keys.containsAll(rdKeys) && rdKeys.containsAll(keys)) {
			                                mappedBy = rd.name
			                            } else if (!ifks.isEmpty() && ifks.containsAll(rdKeys) && rdKeys.containsAll(ifks)) {
		                                    mappedBy = rd.name	                                
			                            }
			                    }
			                    if (!mappedBy) {
			                        mappedBy = "ERROR: See log"
			                        //error = "Uni-directional one-to-manys not yet supported by JPA.  Please add a reference back to ${cd.name} in ${rdClass.className}"
									error = "\tFound Uni-Directional one-to-manys from ${c.className}.${cd.name} to ${rdClass.className}"
			                    }
			                    else{
									error = "\tFound Bi-Directional one-to-manys between [${c.className}] and [${rdClass.className}] on [${cd.name}]"
									}
		                    }
		                    annotation += annotationName
		                    def comma = false
		                    if (determineFetchType(cd) == "FetchType.LAZY") {
		                        annotation += "(${determineFetchType(cd)}"
		                        comma = true
		                    } else {
		                        annotation += "("
		                    }
		                    if (cascade) {
		                        if (comma) {
		                            annotation += ", "
		                        }
		                        annotation += "${cascade}"
		                        comma = true
		                    }
	                        if (comma) {
	                            annotation += ", "
	                        }
		                    annotation += "\n           targetEntity=${cd.elementClassRef}.class"
		                    if (mappedBy) annotation += ", mappedBy=\"${mappedBy}\""
		                    annotation += ")"
		                    text = addJpaImport(text, "FetchType")
		                }
							
						if(!cd.orderByElements.isEmpty()){
							cd.orderByElements.each{
								odb ->
								annotation +="\n\t@OrderBy (\"${odb._name} ${odb._sort}\")"  
							}
							text = addJpaImport(text, "OrderBy")
							}
						
		                text = annotate(text, "(private|protected).*(\\b${cd.name})(\\s)*.*;", annotation)
		                if (error) logger.log error                                	
				}
			} catch(Exception e) {
				JPAConversionHandlers.error_log.log "Found exception in Annotating: ${c.className} referenced collections \n\t${e.getMessage()} " 
			}
		
	        text = cleanText(text)
	        
	        if (c.compoundPrimaryKey) {	
				c.pkClassIdText += handle_pkClass(cpkClassName, cpkFieldText, cpkGetterText, cpkConstructorArgs, cpkConstructorBody)		
	        }
	        
	        if (!dry) {            
                if (!pkClassesOnly) {
					c.pkClassIdText = ""
		            if (javaFile.exists()) {
		                javaFile.delete()
		            }
					println('generating JPA class...')
		            javaFile << text
		            javaFile << "\n"
                } else {
                    if (c.compoundPrimaryKey) {
	                    def cpkFile = new File(c.cpkFilename)
	                    if (cpkFile.exists()) {
	                        cpkFile.delete()
	                    }
						println('generating id class...')
	                    cpkFile << c.pkClassIdText
	                    cpkFile << "\n"
                    }
                }
	        }
	        
	        if (dry || verbose) {
	            if (c.compoundPrimaryKey) {
	                println "${c.pkClassIdText}\n"
	            }
	            if (!pkClassesOnly) {
	                println "${text}\n\n"
	            }
	        }
	}
}

/*
This function add the given annotation before the given java statement in the java source file.
*/
def annotate(javaText, before, annotation) {
	def ret = javaText
	if(!javaText.find("//" + before) 
			|| javaText.findAll("//" + before).size() < javaText.findAll(before).size())
	{	
		def indent = ""
		if (before != "public class") indent = "\t"
	    ret = javaText.replaceFirst(before, annotation + "\n${indent}\$0")
	}
	return ret
}

/*
This function cleans up the java source a bit.
*/
def cleanText(javaText) {
    return javaText.replaceFirst("package.*;", "\$0\n")
}

/*
This function adds the import statement to the java source file.
*/
def addJpaImport(javaText, importText) {
    importText = "import javax.persistence.${importText};"
    if (!javaText.contains(importText)) {
        javaText = javaText.replaceFirst("package.*;", "\$0\n" + importText)
    }
    return javaText 
}


def addOtherImport(javaText, importText) {
	importText = "import ${importText};"
	if (!javaText.contains(importText)) {
		javaText = javaText.replaceFirst("package.*;", "\$0\n" + importText)
	}
	return javaText 
}
/*
Given a reference descriptor or a class descriptor, return a JPA fetch types.
*/
def determineFetchType(descriptor) {
    def ret = "FetchType.EAGER"
    if (['true'].contains(descriptor.proxy)) ret = "FetchType.LAZY" 
	else ret = ['true', null, ''].contains(descriptor.autoRetrieve)? "FetchType.EAGER":"FetchType.LAZY" 
    return "fetch=${ret}"
}

/*
Given a reference descriptor or a class descriptor, return a comma separated list of JPA cascade types.
*/
def determineCascadeTypes(descriptor) {
    def ret = []
	
    if (['true', 'yes'].contains(descriptor.autoRetrieve)) {
		ret += "CascadeType.REFRESH"
    }
    if (['true', 'object', 'yes'].contains(descriptor.autoDelete)) { 
		ret += "CascadeType.REMOVE"
    }
    if (['true', 'object', 'yes'].contains(descriptor.autoUpdate)) {
		ret += "CascadeType.MERGE"
		ret += "CascadeType.DETACH"
		ret += "CascadeType.PERSIST"
    }
    return ret.size > 0 ? ("cascade={${ret.join(', ')}}") : null
}

/*
This function will look at the collection descriptor and determine if it should be a 'one to many' or 
a 'many to many' relationship.
*/
def determineOneOrManyToMany(classes, parent, cd) {
    def ret = "@OneToMany"
    cd.manyToMany = false
    if (cd.indirectionTable) { ret = "@ManyToMany"; cd.manyToMany = true }
    return ret
}

/*
This function will look find the class that the given reference descriptor references. Then, it will iterate
over all of its collection descriptors and determine if any of their inverse foreign key set exactly matches
the foreign key set of the given reference descriptor. If true, the relationship is 'many to one' otherwise it
is 'one to one'.
*/
def determineOneOrManyToOne(classes, parent, rd, logger) {
	
	println 'startng determineOneOrManyToOne on \t' + parent.className + '\tvs\t' + rd.classRef +'\ton\t' + rd.name
	
    def ret = "@OneToOne"
    def c = classes[rd.classRef]
    def anonymous = false
    rd.foreignKeys.each {
        fk ->
            def fkfield
            fk.fieldRef ? (fkfield = parent.fields[fk.fieldRef]) : (fkfield = findFieldIdRef(parent.fields, fk.fieldIdRef))
            if (fkfield.access == 'anonymous') {
                anonymous = true 
                logger.log "\tFound anonymous reference-descriptor... defaulting to One-To-One for [${c.className}]"
            }
    }                
    if (!anonymous) {    
        c.collectionDescriptors.values().each {
            cd ->
                def keys = []
                cd.inverseForeignKeys.each {
                    ifk ->
                        def ifkName
                        ifk.fieldRef ? (ifkName = parent.fields[ifk.fieldRef]?.name) : (ifkName = findFieldIdRef(parent.fields, ifk.fieldIdRef)?.name)
                        keys.add(ifkName)
                }                
                def fkName
                def rdKeys = []
                rd.foreignKeys.each {
                    fk ->
                        fk.fieldRef ? (fkName = parent.fields[fk.fieldRef].name) : (fkName = findFieldIdRef(parent.fields, fk.fieldIdRef).name)
                        rdKeys.add(fkName)
                }               
                if (keys.containsAll(rdKeys) && rdKeys.containsAll(keys)) {
					ret = "@ManyToOne"
                } 
			} 
    }
    ret
}

/*
This function iterates over the given fields looking for a field with the given id.    If found, returns the 
field otherwise null.
*/
def findFieldIdRef(fields, fieldIdRef) {
    def ret
    fields.values().each {
        f -> if (f.id == fieldIdRef) ret = f 
    }
    ret
}

/*
This function checks to see if there is a field with the given column name and that the field 
is not an anonymous field. 
*/
def matchColumn(column, fields) {
    def ret = false
    fields.values().each {
        f -> 
        if (f.column.equalsIgnoreCase(column) && f.access != 'anonymous') ret = true 
    }
    ret
}

def handle_pkClass_Info(packageName, cpkClassName){
	def  text = """/**
 * Copyright 2005-2013 The Kuali Foundation
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
package ${packageName};

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;

import org.kuali.rice.core.framework.persistence.jpa.CompositePrimaryKeyBase;

/**
 * This Compound Primary Class has been generated by the rice ojb2jpa Groovy script.  Please
 * note that there are no setter methods, only getters.  This is done purposefully as cpk classes
 * can not change after they have been created.  Also note they require a public no-arg constructor.
 * The equals() and hashCode() methods are inherited from CompositPrimaryKeyBase. 
 */
@Embeddable
public class ${cpkClassName} extends CompositePrimaryKeyBase {
"""
	
	return text
}

def handle_pkClass(cpkClassName, cpkFieldText, cpkGetterText, cpkConstructorArgs, cpkConstructorBody){
	
	def ret = """
	${cpkFieldText}
		public ${cpkClassName}() {}
	${handle_pkClass_constructor(cpkClassName, cpkConstructorArgs, cpkConstructorBody)}
	${cpkGetterText} 
	}
	"""
	
	ret
}

def handle_pkClass_defaultConstructor(cpkClassName, cpkFieldText, cpkGetterText){
	
	def ret = """
	${cpkFieldText}
		    public ${cpkClassName}() {}
	
	${cpkGetterText} 
	
	}
	"""
	
	ret
}
def handle_pkClass_constructor(cpkClassName, cpkConstructorArgs, cpkConstructorBody){
	
	def ret = ""
	if(cpkConstructorArgs.length() > 0){
		def args = cpkConstructorArgs.substring(0, cpkConstructorArgs.length() - 1 );
		ret += "\tpublic ${cpkClassName}(${args})\n\t\t{\n\t\t${cpkConstructorBody}}"
	}
	//println("result**********\n" + ret);
	ret
}

def findReferencedColumnName(java.util.LinkedHashMap classes, String classRef, String fieldRef) throws Exception{
	//println "***********looking for ref column**************\t" + classRef + "\t" + fieldRef
	def ret = "";
	classes.values().each {
		this_class -> 
		if(this_class.className.equals(classRef)){
			Field fd = this_class.fields.get(fieldRef)
			if(fd != null)
				ret = fd.column	
			}
	}
	//println "***********got ref column**************\t" + ret
	ret
}


def hasSuperClass(javaText){
	println 'checking super class'
	javaText.find(/public class (\w+) extends/)
}

def boolean hasUnOverridingFields(javaText,  fields, un_overring_fields){
	
	def ret = false
	def temp = fields;
	
	temp.remove("objectId");
    temp.remove("versionNumber")	

	temp.values().each{
		f-> attName = f.name
			//println 'checking field\t' + attName
			if(javaText.find(/(private|protected) (\w+) ${attName}/) == null && !((f.access)!= null && (f.access).equalsIgnoreCase("anonymous")))
				un_overring_fields.put(attName, f.column)
		}
	if(un_overring_fields.size() != 0)
		ret = true
		
	ret
	}

def findBiDirectionRelationships(classes, cls, refcls, logger ) throws Exception{	
	println '*******************start looking for bi-directions between\t' + 
		cls.className + ' vs ' + refcls.classRef + ' on ' + refcls.name	
		
	def ret = 'LOOK'
	
	if (!determineOneOrManyToOne(classes, cls, refcls, JPAConversionHandlers.info_log).equalsIgnoreCase("@ManyToOne"))
	{
		def keys = [];
		def fkeys = [];
		refcls.foreignKeys.each {
			fk ->
			def f
			fk.fieldRef ? (f = cls.fields[fk.fieldRef]) : (f = findFieldIdRef(cls.fields, fk.fieldIdRef));
			if (f?.name) keys.add(f.name)
			fkeys.add(fk.fieldRef)
		}
		
		def rdClass = classes[refcls.classRef];
	
		def refMappedBy=''
		def rdKeys = []
	
	    rdClass.referenceDescriptors.values().each {
			rd -> 
			rdKeys = []
			rd.foreignKeys.each {
				fk ->
				def fkName
				fk.fieldRef ? (fkName = rdClass.fields[fk.fieldRef]?.name) : (fkName = findFieldIdRef(rdClass.fields, fk.fieldIdRef)?.name)
			    rdKeys.add(fkName)
			}
			if (!keys.isEmpty() && keys.containsAll(rdKeys) && rdKeys.containsAll(keys)) {
				refMappedBy = rd.name
			} else if (!fkeys.isEmpty() && fkeys.containsAll(rdKeys) && rdKeys.containsAll(fkeys)) {
				refMappedBy = rd.name	                                
			}
		}
	
		if(refMappedBy){
			ret = 'FOUND'
			println "++++++++++++++++++++++keys\t" + keys + "\tfkeys\t" + fkeys + "\t" + ret;
			logger.log( "\tFound a bi-directional one-to-one mapping between ${cls.className} vs ${refcls.classRef} on ${refcls.name}");
		}
	}
	ret
}

def findImportType(_text, _name) throws Exception{
	
	def _fieldPattern = ~/(private|protected)\s+\w+\s+${_name}/
	def _field = _text.find(_fieldPattern)    
    def _type = (_field.split())[1]
    def _importPattern = "import.*(${_type})"  
	def _importStatment = _text.find(_importPattern)
    def _ret = _importStatment.split()[1];

	println("+++++++++++++++++++++++got type\t" + _type + "\timport\t" +  _importStatment + "\tret\t" + _ret)
	
	_ret
}
	