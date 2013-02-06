package org.alfresco.repo.bulkimport.xml;

import com.thoughtworks.xstream.converters.ConverterMatcher;
import com.thoughtworks.xstream.mapper.Mapper;
import org.alfresco.service.ServiceRegistry;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.io.File;

public class AlfrescoXStreamMarshaller extends XStreamMarshaller {

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
  }

  public File getFileImportRootLocation() {
    return fileImportRootLocation;
  }
}
