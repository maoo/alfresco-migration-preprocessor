package org.alfresco.repo.bulkimport;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.bulkimport.beans.Content;
import org.alfresco.repo.bulkimport.beans.Folder;
import org.alfresco.repo.bulkimport.impl.MultiThreadedBulkFilesystemImporter;
import org.alfresco.repo.bulkimport.impl.StreamingNodeImporterFactory;
import org.alfresco.repo.bulkimport.impl.StripingFilesystemTracker;
import org.alfresco.repo.bulkimport.xml.AlfrescoReflectionUtils;
import org.alfresco.repo.bulkimport.xml.AlfrescoXStreamMarshaller;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.util.ResourceUtils;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ImportableFileTest {

  private static final String ADMIN_USER_NAME = "admin";

  static Logger log = Logger.getLogger(ImportableFileTest.class);
  private static URL content1 = ClassLoader.getSystemClassLoader().getResource("content1.xml");
  private static URL folder1 = ClassLoader.getSystemClassLoader().getResource("folder1.xml");

  protected static ApplicationContext applicationContext;

  protected static AlfrescoXStreamMarshaller marshaller;
  protected static NodeService nodeService;
  private static StreamingNodeImporterFactory streamingNodeImporterFactory;
  private static BulkFilesystemImporter bulkImporter;
  private static Repository repositoryHelper;
  private static FileFolderService fileFolderService;
  private static ContentService contentService;

  @BeforeClass
  public static void initAppContext() {
    ApplicationContextHelper.setUseLazyLoading(false);
    ApplicationContextHelper.setNoAutoStart(true);
    applicationContext = ApplicationContextHelper.getApplicationContext(new String[]{"classpath:alfresco/application-context.xml"});
    marshaller = (AlfrescoXStreamMarshaller) applicationContext.getBean("configuredMarshaller");
    nodeService = (NodeService) applicationContext.getBean("NodeService");
    streamingNodeImporterFactory = (StreamingNodeImporterFactory)applicationContext.getBean("streamingNodeImporterFactory");
    bulkImporter = (MultiThreadedBulkFilesystemImporter)applicationContext.getBean("bulkFilesystemImporter");
    repositoryHelper = (Repository)applicationContext.getBean("repositoryHelper");
    fileFolderService = (FileFolderService)applicationContext.getBean("FileFolderService");
    contentService = (ContentService)applicationContext.getBean("ContentService");

    AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
  }

  @Test
  public void testWiring() {
    assertNotNull(marshaller);
  }

  @Test
  public void unmarshalContent() throws IOException {
    Source source = new StreamSource(content1.openStream());
    Object unmarshalled = marshaller.unmarshal(source);
    assertEquals(Content.class, unmarshalled.getClass());
  }

  @Test
  public void fileExists() throws IOException, InvocationTargetException, IllegalAccessException {
    Source source = new StreamSource(content1.openStream());
    Object unmarshalled = marshaller.unmarshal(source);
    Map<QName, Serializable> nodeProperties = AlfrescoReflectionUtils.getAlfrescoMeta(unmarshalled);
    File metaFile = AlfrescoReflectionUtils.getMetaFile(nodeProperties,marshaller.getFileImportRootLocation());
    assertTrue(metaFile.exists());
    File binaryFile = AlfrescoReflectionUtils.getBinaryFile(nodeProperties,marshaller.getFileImportRootLocation());
    assertTrue(binaryFile.exists());
  }

  @Test
  public void unmarshalFolder() throws IOException {
    Source source = new StreamSource(folder1.openStream());
    Folder folder = (Folder)marshaller.unmarshal(source);
    assertEquals(3,folder.getChildren().size());
  }

  @Test
  public void runFileImport() throws IOException {
    NodeRef importedFolder = fileFolderService.create(
        repositoryHelper.getCompanyHome(),
        "bulkImport-"+(new Date()).getTime(),
        ContentModel.TYPE_FOLDER).getNodeRef();

    assertTrue(nodeService.exists(importedFolder));

    NodeImporter nodeImporter = streamingNodeImporterFactory.getNodeImporter(marshaller.getFileImportRootLocation());
    BulkImportParameters bulkImportParameters = new BulkImportParameters();
    bulkImportParameters.setTarget(importedFolder);
    bulkImportParameters.setReplaceExisting(true);
    bulkImportParameters.setDisableRulesService(true);
    bulkImportParameters.setBatchSize(40);
    bulkImporter.bulkImport(bulkImportParameters, nodeImporter);

    List<FileInfo> children = fileFolderService.list(importedFolder);
    assertNotNull(children);
    for(FileInfo fileInfo : children) {
      if(fileInfo.isFolder()) {
        assertFolder(fileInfo);
      } else {
        assertContent(fileInfo);
      }
    }
  }

  private void assertContent(FileInfo fileInfo) {
    assertContent(fileInfo.getNodeRef());
  }

  private void assertContent(NodeRef nodeRef) {
    assertEquals(ContentModel.TYPE_CONTENT, nodeService.getType(nodeRef));
    assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE));
    assertTrue(nodeService.hasAspect(nodeRef,ContentModel.ASPECT_GEN_CLASSIFIABLE));
    assertTrue(nodeService.hasAspect(nodeRef,ContentModel.ASPECT_AUDITABLE));
    assertTrue(((String)nodeService.getProperty(nodeRef,ContentModel.PROP_NAME)).startsWith("contentname"));
    assertTrue(((String)nodeService.getProperty(nodeRef,ContentModel.PROP_TITLE)).startsWith("Content Title"));
    ContentReader reader = contentService.getReader(nodeRef,ContentModel.PROP_CONTENT);
    assertNotNull(reader);
    assertNotNull(reader.getContentInputStream());
  }

  private void assertFolder(FileInfo fileInfo) {
    NodeRef nodeRef = fileInfo.getNodeRef();
    assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(nodeRef));
    assertTrue(nodeService.hasAspect(nodeRef,ContentModel.ASPECT_VERSIONABLE));
    assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE));
    assertTrue(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE));
    assertEquals("foldername",nodeService.getProperty(nodeRef,ContentModel.PROP_NAME));
    assertEquals("Folder Title",nodeService.getProperty(nodeRef,ContentModel.PROP_TITLE));
    List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
    for(ChildAssociationRef child : children) {
      assertContent(child.getChildRef());
    }
  }
}
