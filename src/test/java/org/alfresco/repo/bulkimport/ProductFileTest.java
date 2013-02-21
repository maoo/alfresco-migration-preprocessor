package org.alfresco.repo.bulkimport;

import org.alfresco.consulting.examples.beans.Products;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.bulkimport.xml.AlfrescoXStreamMarshaller;
import org.alfresco.repo.bulkimport.xml.XmlBulkImporter;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class ProductFileTest {

  private static final String ADMIN_USER_NAME = "admin";

  static Logger log = Logger.getLogger(ProductFileTest.class);
  private static URL single = ClassLoader.getSystemClassLoader().getResource("tradedoubler-single.xml");
  private static URL full = ClassLoader.getSystemClassLoader().getResource("tradedoubler.xml");

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
    marshaller = (AlfrescoXStreamMarshaller) applicationContext.getBean("productMarshaller");
    xmlBulkImporter = (XmlBulkImporter) applicationContext.getBean("productBulkImporter");
    repositoryHelper = (Repository) applicationContext.getBean("repositoryHelper");
    fileFolderService = (FileFolderService) applicationContext.getBean("FileFolderService");
    contentService = (ContentService) applicationContext.getBean("ContentService");

    AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
  }


  @Test
  public void unmarshalOneProduct() throws IOException {
    Source source = new StreamSource(single.openStream());
    Products products = (Products) marshaller.unmarshal(source);
    assertEquals(1, products.getProduct().size());
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
        new StreamSource(full.openStream())
    });
    List<Object> unmarshalled = xmlBulkImporter.bulkImport(importedFolder, sources);
    assertNotNull(unmarshalled);
    assertNotSame(0, unmarshalled.size());

    List<FileInfo> children = fileFolderService.list(importedFolder);
    assertNotNull(children);
    marshaller.clearContents();
  }

  public void unmarshalAllProducts() throws IOException {
    Source source = new StreamSource(full.openStream());
    Products products = (Products) marshaller.unmarshal(source);
    assertEquals(99876, products.getProduct().size());
  }

}
