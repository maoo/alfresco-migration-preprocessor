package org.alfresco.repo.bulkimport;

import org.alfresco.repo.bulkimport.beans.Content;
import org.alfresco.repo.bulkimport.xml.AlfrescoReflectionUtils;
import org.alfresco.repo.bulkimport.xml.AlfrescoXStreamMarshaller;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ImportableFileTest {

  private static final String ADMIN_USER_NAME = "admin";

  static Logger log = Logger.getLogger(ImportableFileTest.class);

  protected static ApplicationContext applicationContext;

  protected static AlfrescoXStreamMarshaller marshaller;

  protected static NodeService nodeService;

  private static URL content1 = ClassLoader.getSystemClassLoader().getResource("content1.xml");

  @BeforeClass
  public static void initAppContext() {
    ApplicationContextHelper.setUseLazyLoading(false);
    ApplicationContextHelper.setNoAutoStart(true);
    applicationContext = ApplicationContextHelper.getApplicationContext(new String[]{"classpath:alfresco/application-context.xml"});
    marshaller = (AlfrescoXStreamMarshaller) applicationContext.getBean("configuredMarshaller");
    nodeService = (NodeService) applicationContext.getBean("NodeService");
    AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
  }

  @Test
  public void testWiring() {
    assertNotNull(marshaller);
  }

  @Test
  public void unmarshalObject() throws IOException {
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
  }

}
