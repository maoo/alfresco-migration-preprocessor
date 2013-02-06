package org.alfresco.repo.bulkimport.xml;

import com.thoughtworks.xstream.converters.ConverterMatcher;
import com.thoughtworks.xstream.mapper.Mapper;
import org.alfresco.repo.bulkimport.annotations.NodeType;
import org.alfresco.service.ServiceRegistry;
import org.apache.log4j.Logger;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.io.File;
import java.util.Set;

public class AlfrescoXStreamMarshaller extends XStreamMarshaller {

  static Logger log = Logger.getLogger(AlfrescoXStreamMarshaller.class);

  private File fileImportRootLocation;

  public AlfrescoXStreamMarshaller(String rootLocation, ServiceRegistry serviceRegistry) {
    super();
    Mapper mapper = this.getXStream().getMapper();
    this.fileImportRootLocation = new File(rootLocation);
    if (!this.fileImportRootLocation.exists()) {
      this.fileImportRootLocation.mkdir();
    }
    ConverterMatcher importableFileConverter = new ImportableFileConverter(this.fileImportRootLocation, mapper, serviceRegistry);
    ConverterMatcher[] converters = new ConverterMatcher[]{importableFileConverter};
    setConverters(converters);

//    Reflections reflections = new Reflections();
//    Set<Class<?>> annotated =
//        reflections.getTypesAnnotatedWith(NodeType.class);
//
//    log.error("Annotated classes "+annotated);
//    log.error("Annotated classes "+annotated.toArray(new Class[]{}));
//
//    setAnnotatedClasses(annotated.toArray(new Class[]{}));
  }

  public File getFileImportRootLocation() {
    return fileImportRootLocation;
  }
}
