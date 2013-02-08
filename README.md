What is it
---
Alfresco Migration Preprocessor is an incredibly long name for such a simple feature! This tool provides a marshaller object
that is (currently) able to
* Unmarshal one or more XML sources and transform them into a FS file/folder structure
* Invoke the File Import (shipped with Alfresco 4.** versions) pointing to the previously created FS structure
* Post-process recorded peer associations and create them accordingly as soon as the import have finished

How does it work
---
The Migration preprocessor extends XStream - the famous XML (de)serializer library - providing an [AlfrescoXStreamMarshaller](https://github.com/maoo/alfresco-migration-preprocessor/blob/master/src/main/java/org/alfresco/repo/bulkimport/xml/AlfrescoXStreamMarshaller.java) object accepts the following inputs:
* One or more [Java Beans](https://github.com/maoo/alfresco-migration-preprocessor/tree/master/src/main/java/org/alfresco/repo/bulkimport/beans), containing
  * [XStream-based annotations](http://xstream.codehaus.org/annotations-tutorial.html) (i.e. XStreamAlias, XStreamImplicit, ...)
  * [Alfresco-based annotations](https://github.com/maoo/alfresco-migration-preprocessor/tree/master/src/main/java/org/alfresco/repo/bulkimport/annotations) (i.e. NodeProperty, or NodeAspect)
* One or more XML [Sources](http://docs.oracle.com/javase/7/docs/api/javax/xml/transform/Source.html)

It also provides an [XmlBulkImporter](https://github.com/maoo/alfresco-migration-preprocessor/blob/master/src/main/java/org/alfresco/repo/bulkimport/xml/XmlBulkImporter.java) which executes the XML unmarshalling and later invokes the FileImport to populate an Alfresco repository.

You can see how to use it in [our JUnit test](https://github.com/maoo/alfresco-migration-preprocessor/blob/master/src/test/java/org/alfresco/repo/bulkimport/ImportableFileTest.java)

Run
---
To run the test
```
mvn clean test -Ppurge
```
Files are created in alf_data_dev/xml
XML files being imported are located in src/test/resources

Contribute
---
Pick some tasks from [TODO.txt](https://github.com/maoo/alfresco-migration-preprocessor/blob/master/TODO.txt)
Contribute ideas and nice to haves into [TODO.txt](https://github.com/maoo/alfresco-migration-preprocessor/blob/master/TODO.txt)
Open [issues](https://github.com/maoo/alfresco-migration-preprocessor/issues)
Provide comments and feedback on [commits](https://github.com/maoo/alfresco-migration-preprocessor/commits/master)