package org.alfresco.repo.bulkimport;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.bulkimport.annotations.NodeAssociation;
import org.alfresco.repo.bulkimport.beans.Content;
import org.alfresco.repo.bulkimport.beans.Folder;
import org.alfresco.repo.bulkimport.utils.AlfrescoFileImportUtils;
import org.alfresco.repo.bulkimport.utils.AlfrescoReflectionUtils;
import org.alfresco.repo.bulkimport.xml.AlfrescoXStreamMarshaller;
import org.alfresco.repo.bulkimport.xml.XmlBulkImporter;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Triple;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ImportableFileTest {

  private static final String ADMIN_USER_NAME = "admin";

  static Logger log = Logger.getLogger(ImportableFileTest.class);
  private static URL content1 = ClassLoader.getSystemClassLoader().getResource("content1.xml");
  private static URL folder1 = ClassLoader.getSystemClassLoader().getResource("folder1.xml");
  private static URL folder2 = ClassLoader.getSystemClassLoader().getResource("folder2.xml");

  protected static ApplicationContext applicationContext;

  private static AlfrescoXStreamMarshaller marshaller;
  private static NodeService nodeService;
  private static XmlBulkImporter xmlBulkImporter;
  private static Repository repositoryHelper;
  private static FileFolderService fileFolderService;
  private static ContentService contentService;

  @BeforeClass
  public static void initAppContext() {
    ApplicationContextHelper.setUseLazyLoading(false);
    ApplicationContextHelper.setNoAutoStart(true);
    applicationContext = ApplicationContextHelper.getApplicationContext(new String[]{"classpath:alfresco/application-context.xml"});
    nodeService = (NodeService) applicationContext.getBean("NodeService");
    marshaller = (AlfrescoXStreamMarshaller) applicationContext.getBean("alfrescoMarshaller");
    xmlBulkImporter = (XmlBulkImporter) applicationContext.getBean("alfrescoXmlBulkImporter");
    repositoryHelper = (Repository) applicationContext.getBean("repositoryHelper");
    fileFolderService = (FileFolderService) applicationContext.getBean("FileFolderService");
    contentService = (ContentService) applicationContext.getBean("ContentService");

    AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
  }

  @Test
  public void fileExists() throws IOException, InvocationTargetException, IllegalAccessException {
    Source source = new StreamSource(content1.openStream());
    Object unmarshalled = marshaller.unmarshal(source);
    assertEquals(Content.class, unmarshalled.getClass());
    Map<QName, Serializable> nodeProperties = AlfrescoReflectionUtils.getAlfrescoMeta(unmarshalled);
    File metaFile = AlfrescoFileImportUtils.getMetaFile(nodeProperties, marshaller.getFileImportRootLocation());
    assertTrue(metaFile.exists());
    File binaryFile = AlfrescoFileImportUtils.getBinaryFile(nodeProperties, marshaller.getFileImportRootLocation());
    assertTrue(binaryFile.exists());
    log.info("Renaming " + marshaller.getFileImportRootLocation().getAbsolutePath());
    marshaller.getFileImportRootLocation().renameTo(new File(marshaller.getFileImportRootLocation().getParent(), new Date().getTime() + ""));
    marshaller.clearContents();
  }

  @Test
  public void unmarshalFolder() throws IOException {
    Source source = new StreamSource(folder1.openStream());
    Folder folder = (Folder) marshaller.unmarshal(source);
    assertEquals(3, folder.getChildren().size());
    log.info("Renaming " + marshaller.getFileImportRootLocation().getAbsolutePath());
    marshaller.getFileImportRootLocation().renameTo(new File(marshaller.getFileImportRootLocation().getParent(), new Date().getTime() + ""));
    marshaller.clearContents();
  }

  @Test
  public void unmarshalFolder2() throws IOException {
    Source source = new StreamSource(folder2.openStream());
    Folder folder = (Folder) marshaller.unmarshal(source);
    assertEquals(3, folder.getChildren().size());
    List<Triple<NodeAssociation, Object, Object>> assocs = marshaller.getAssocsStack();
    assertNotNull(assocs);
    for (Triple<NodeAssociation, Object, Object> assoc : assocs) {
      Content referencing = (Content) assoc.getSecond();
      Object referenced = assoc.getThird();
      log.info("\nAssoc name: " + assoc.getFirst().name() + "\nFrom: " + referencing.getName() + "\nTo: " + referenced);
    }
    assertEquals(6, assocs.size());
    log.info("Renaming " + marshaller.getFileImportRootLocation().getAbsolutePath());
    marshaller.getFileImportRootLocation().renameTo(new File(marshaller.getFileImportRootLocation().getParent(), new Date().getTime() + ""));
    marshaller.clearContents();
  }


  @Test
  public void runFileImport() throws IOException, InvocationTargetException, IllegalAccessException {
    NodeRef importedFolder = fileFolderService.create(
        repositoryHelper.getCompanyHome(),
        "bulkImport-" + (new Date()).getTime(),
        ContentModel.TYPE_FOLDER).getNodeRef();

    assertTrue(nodeService.exists(importedFolder));

    List<Source> sources = Arrays.asList(new Source[]{
        new StreamSource(content1.openStream()),
        new StreamSource(folder1.openStream()),
        new StreamSource(folder2.openStream())
    });
    List<Object> unmarshalled = xmlBulkImporter.bulkImport(importedFolder, sources);
    assertNotNull(unmarshalled);
    assertNotSame(0, unmarshalled.size());

    List<FileInfo> children = fileFolderService.list(importedFolder);
    assertNotNull(children);
    for (FileInfo fileInfo : children) {
      if (fileInfo.isFolder()) {
        assertFolder(fileInfo);
      } else {
        assertContent(fileInfo.getNodeRef());
      }
    }
    marshaller.clearContents();
  }

  private void assertContent(NodeRef nodeRef) {
    log.info("Asserting content " + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
    assertEquals(ContentModel.TYPE_CONTENT, nodeService.getType(nodeRef));
    assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE));
    assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE));
    assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE));
    assertTrue(((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).startsWith("contentname"));
    assertTrue(((String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).startsWith("Content Title"));
    ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
    assertNotNull(reader);
    assertNotNull(reader.getContentInputStream());
  }

  private void assertFolder(FileInfo fileInfo) {
    log.info("Asserting folder \n" + nodeService.getProperties(fileInfo.getNodeRef()));
    NodeRef nodeRef = fileInfo.getNodeRef();
    assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(nodeRef));
    assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE));
    assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE));
    assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE));
    assertTrue(((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).startsWith("foldername"));
    assertTrue(((String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)).startsWith("Folder Title"));
    List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
    for (ChildAssociationRef child : children) {
      assertContent(child.getChildRef());
    }
  }
}
