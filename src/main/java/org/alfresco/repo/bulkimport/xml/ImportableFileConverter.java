package org.alfresco.repo.bulkimport.xml;

import com.google.gdata.util.common.base.StringUtil;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.bulkimport.MetadataLoader;
import org.alfresco.repo.bulkimport.annotations.NodeType;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class ImportableFileConverter implements Converter {

  static Logger log = Logger.getLogger(ImportableFileConverter.class);

  private MimetypeService mimetypeService;
  private ContentService contentService;
  private File fileImportRootLocation;
  private Mapper mapper;
  private NamespacePrefixResolver namespaceService;

  public ImportableFileConverter(File fileImportRootLocation, Mapper mapper, ServiceRegistry serviceRegistry) {
    this.fileImportRootLocation = fileImportRootLocation;
    this.mapper = mapper;
    this.namespaceService = serviceRegistry.getNamespaceService();
  }

  @Override
  public boolean canConvert(Class aClass) {
    boolean ret = aClass.getAnnotation(NodeType.class) != null;
    log.debug("[ImportableFileConverter] canConvert " + aClass + "? " + ret);
    return ret;
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    context.convertAnother(source);
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    FileOutputStream fos = null;
    Object currentObject = null;
    Class currentClass = context.getRequiredType();
    try {
      Converter javaBeanConverter = new JavaBeanConverter(this.mapper);
      currentObject = javaBeanConverter.unmarshal(reader, context);
      Properties properties = new Properties();

      //Getting Alfresco node properties from current object
      Map<QName, Serializable> nodeProperties = AlfrescoReflectionUtils.getAlfrescoMeta(currentObject);
      QName nodeType = AlfrescoReflectionUtils.getNodeType(currentObject);
      QName[] nodeAspects = AlfrescoReflectionUtils.getNodeAspects(currentObject);

      //Handling node type
      properties.put(AlfrescoReflectionUtils.PROPERTY_NAME_TYPE, nodeType.toPrefixString(namespaceService));

      //Handling node aspects
      String aspects = "";
      for(QName aspect : nodeAspects) {
        aspects += aspect.toPrefixString(namespaceService) + ",";
      }
      aspects = aspects.substring(0,aspects.length()-1);
      properties.put(AlfrescoReflectionUtils.PROPERTY_NAME_ASPECTS,aspects);

      //Handling node properties
      for(QName propertyName : nodeProperties.keySet()) {
        properties.put(propertyName.toPrefixString(namespaceService), nodeProperties.get(propertyName));
      }

      //Handling meta filename and File creation
      File metaFile = AlfrescoReflectionUtils.getMetaFile(nodeProperties, this.fileImportRootLocation);

      log.error(
          "[ImportableFileConverter.unmarshal] current class: " + currentClass +
              "current object: " + currentObject +
              "alfresco properties: " + nodeProperties +
              "meta File: " + metaFile.getAbsolutePath());

      //Store Properties into file
      fos = new FileOutputStream(metaFile);
      properties.storeToXML(fos, null);
    } catch (InvocationTargetException e) {
      handleException(currentClass, e);
    } catch (IllegalAccessException e) {
      handleException(currentClass, e);
    } catch (IOException e) {
      handleException(currentClass, e);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          handleException(currentObject, e);
        }
      }
    }
    return currentObject;
  }

  private void handleException(Object currentObject, Throwable e) {
    throw new IllegalStateException("Error convering object "+currentObject,e);
  }
}
