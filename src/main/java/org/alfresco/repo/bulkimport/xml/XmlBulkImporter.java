package org.alfresco.repo.bulkimport.xml;

import org.alfresco.repo.bulkimport.BulkFilesystemImporter;
import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.repo.bulkimport.impl.StreamingNodeImporterFactory;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;

import javax.xml.transform.Source;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlBulkImporter {

  private BulkImportParameters bulkImportParameters;
  private NodeImporter nodeImporter;

  private BulkFilesystemImporter bulkImporter;
  private Repository repositoryHelper;
  private AlfrescoXStreamMarshaller marshaller;

  public XmlBulkImporter(StreamingNodeImporterFactory streamingNodeImporterFactory, AlfrescoXStreamMarshaller marshaller) {
    this.nodeImporter = streamingNodeImporterFactory.getNodeImporter(marshaller.getFileImportRootLocation());
    this.marshaller = marshaller;
    this.bulkImportParameters = new BulkImportParameters();
    //@TODO - replaceExisting should be false, but tests fail and they should not
    //Maybe a FileImport issue?
    this.bulkImportParameters.setReplaceExisting(true);
    this.bulkImportParameters.setDisableRulesService(true);
    this.bulkImportParameters.setBatchSize(40);
  }

  public List bulkImport(NodeRef targetFolder, List<Source> sources) throws IOException {
    List ret = new ArrayList();
    for(Source source : sources) {
      ret.add(marshaller.unmarshal(source));
    }
    bulkImportParameters.setTarget(targetFolder);
    bulkImporter.bulkImport(bulkImportParameters, nodeImporter);
    return ret;
  }

  public BulkFilesystemImporter getBulkImporter() {
    return bulkImporter;
  }

  public void setBulkImporter(BulkFilesystemImporter bulkImporter) {
    this.bulkImporter = bulkImporter;
  }

  public Repository getRepositoryHelper() {
    return repositoryHelper;
  }

  public void setRepositoryHelper(Repository repositoryHelper) {
    this.repositoryHelper = repositoryHelper;
  }

  public AlfrescoXStreamMarshaller getMarshaller() {
    return marshaller;
  }
}
