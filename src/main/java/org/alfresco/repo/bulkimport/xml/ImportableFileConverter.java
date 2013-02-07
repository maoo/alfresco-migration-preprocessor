package org.alfresco.repo.bulkimport.xml;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.bulkimport.annotations.NodeType;
import org.alfresco.repo.bulkimport.utils.AlfrescoFileImportUtils;
import org.alfresco.repo.bulkimport.utils.AlfrescoReflectionUtils;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Triple;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ImportableFileConverter implements Converter {

  static Logger log = Logger.getLogger(ImportableFileConverter.class);
  private static final Object CURRENT_FOLDER_CONTEXT_PARAM = "currentFolder";
  private static final Object ASSOCS_CONTEXT_PARAM = "assocs";

  private File fileImportRootLocation;
  private Mapper mapper;
  private NamespacePrefixResolver namespaceService;
  private final List<Triple<QName, QName, String>> assocsStack;

  public ImportableFileConverter(File fileImportRootLocation, Mapper mapper, ServiceRegistry serviceRegistry, List<Triple<QName, QName, String>> assocsStack) {
    this.fileImportRootLocation = fileImportRootLocation;
    this.mapper = mapper;
    this.namespaceService = serviceRegistry.getNamespaceService();
    this.assocsStack = assocsStack;
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
  public Object unmarshal(HierarchicalStreamReader reader, final UnmarshallingContext context) {

    File fileImportCurrentLocation = (File) context.get(CURRENT_FOLDER_CONTEXT_PARAM);
    if (fileImportCurrentLocation == null) {
      fileImportCurrentLocation = this.fileImportRootLocation;
      log.debug(
          "[ImportableFileConverter.unmarshal] null root folder from context; setting root folder to " + fileImportCurrentLocation.getAbsolutePath());
    } else {
      log.debug(
          "[ImportableFileConverter.unmarshal] root folder from context " + fileImportCurrentLocation.getAbsolutePath());
    }

    FileOutputStream fos = null;
    Object currentObject = null;
    Class currentClass = context.getRequiredType();
    try {
      if (AlfrescoReflectionUtils.isContainer(currentClass)) {
        //Create the folder; since we don't know the name yet, we will create the folder with a timestamp name
        //After unmarshalling the Object with the JavaBeanConverter, we will rename the folder with its real name
        String folderName = (new Date()).getTime() + "";
        File folder = AlfrescoFileImportUtils.createFolder(folderName, fileImportCurrentLocation);

        //Register the folder as current rootFolder where to import all node entities defined
        // within the context of the current object
        context.put(CURRENT_FOLDER_CONTEXT_PARAM, folder);
      }
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
      for (QName aspect : nodeAspects) {
        aspects += aspect.toPrefixString(namespaceService) + ",";
      }
      aspects = aspects.substring(0, aspects.length() - 1);
      properties.put(AlfrescoReflectionUtils.PROPERTY_NAME_ASPECTS, aspects);

      //Handling node properties
      for (QName propertyName : nodeProperties.keySet()) {
        properties.put(propertyName.toPrefixString(namespaceService), nodeProperties.get(propertyName));
      }

      // Handling associations
      List<Triple<QName, QName, String>> assocs = AlfrescoReflectionUtils.getAlfrescoAssocs(currentObject);
      this.assocsStack.addAll(assocs);

      if (AlfrescoReflectionUtils.isContainer(currentClass)) {
        //Rename the folder to its real name
        String name = (String) nodeProperties.get(ContentModel.PROP_NAME);
        File currentFolder = (File) context.get(CURRENT_FOLDER_CONTEXT_PARAM);
        File parent = currentFolder.getParentFile();
        currentFolder.renameTo(new File(parent, name));

        //Register the current parent's folder as current rootFolder where to import next node entities
        context.put(CURRENT_FOLDER_CONTEXT_PARAM, parent);
        log.debug(
            "[ImportableFileConverter] folder " +
                currentFolder +
                "completed; adding parent as new root");
      } else {
        String contentUrl = AlfrescoReflectionUtils.getContentUrl(currentObject);
        if (contentUrl != null) {
          log.debug("[ImportableFileConverter] importing content from url " + contentUrl);
          File metaFile = AlfrescoFileImportUtils.getBinaryFile(nodeProperties, fileImportCurrentLocation);
          AlfrescoFileImportUtils.fetchBinaryContent(metaFile, contentUrl);
        }
      }

      //Creating Meta file and storing Meta Properties
      File metaFile = AlfrescoFileImportUtils.getMetaFile(nodeProperties, fileImportCurrentLocation);
      fos = new FileOutputStream(metaFile);
      properties.storeToXML(fos, null);

      log.debug(
          "[ImportableFileConverter.unmarshal] current class: " + currentClass +
              "current object: " + currentObject +
              "alfresco properties: " + nodeProperties +
              "meta File: " + metaFile.getAbsolutePath());

    } catch (InvocationTargetException e) {
      AlfrescoFileImportUtils.handleException(currentClass, e);
    } catch (IllegalAccessException e) {
      AlfrescoFileImportUtils.handleException(currentClass, e);
    } catch (IOException e) {
      AlfrescoFileImportUtils.handleException(currentClass, e);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          AlfrescoFileImportUtils.handleException(currentObject, e);
        }
      }
    }
    return currentObject;
  }
}
