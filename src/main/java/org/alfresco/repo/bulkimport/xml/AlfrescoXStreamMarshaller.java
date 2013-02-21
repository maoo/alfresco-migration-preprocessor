package org.alfresco.repo.bulkimport.xml;

import com.thoughtworks.xstream.converters.ConverterMatcher;
import com.thoughtworks.xstream.mapper.Mapper;
import org.alfresco.repo.bulkimport.annotations.NodeAssociation;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.Triple;
import org.apache.log4j.Logger;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlfrescoXStreamMarshaller extends XStreamMarshaller {

  static Logger log = Logger.getLogger(AlfrescoXStreamMarshaller.class);

  private File fileImportRootLocation;
  private List<Triple<NodeAssociation, Object, Object>> assocsStack;
  private Mapper mapper;
  private ServiceRegistry serviceRegistry;

  public AlfrescoXStreamMarshaller(String rootLocation, ServiceRegistry serviceRegistry) {
    super();
    this.serviceRegistry = serviceRegistry;
    this.mapper = this.getXStream().getMapper();
    this.fileImportRootLocation = new File(rootLocation);
    clearContents();
//    Reflections reflections = new Reflections();
//    Set<Class<?>> annotated =
//        reflections.getTypesAnnotatedWith(NodeType.class);
//
//    log.error("Annotated classes "+annotated);
//    log.error("Annotated classes "+annotated.toArray(new Class[]{}));
//
//    setAnnotatedClasses(annotated.toArray(new Class[]{}));
  }

  public void clearContents() {
    if (!this.fileImportRootLocation.exists()) {
      this.fileImportRootLocation.mkdir();
    }
    this.assocsStack = new ArrayList<Triple<NodeAssociation, Object, Object>>();
    setAlfrescoConverter();
  }

  private void setAlfrescoConverter() {
    ConverterMatcher importableFileConverter = new ImportableFileConverter(this.fileImportRootLocation, mapper, serviceRegistry, assocsStack);
    ConverterMatcher mapEntryConverter = new MapEntryConverter();
    ConverterMatcher[] converters = new ConverterMatcher[]{mapEntryConverter,importableFileConverter};
    setConverters(converters);
  }

  public File getFileImportRootLocation() {
    return this.fileImportRootLocation;
  }

  public List<Triple<NodeAssociation, Object, Object>> getAssocsStack() {
    return this.assocsStack;
  }
}
