package org.alfresco.repo.bulkimport.xml;

import org.alfresco.repo.bulkimport.BulkFilesystemImporter;
import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.repo.bulkimport.annotations.NodeAssociation;
import org.alfresco.repo.bulkimport.impl.StreamingNodeImporterFactory;
import org.alfresco.repo.bulkimport.utils.AlfrescoFileImportUtils;
import org.alfresco.repo.bulkimport.utils.AlfrescoReflectionUtils;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Triple;
import org.apache.log4j.Logger;

import javax.xml.transform.Source;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class XmlBulkImporter {

  static Logger log = Logger.getLogger(XmlBulkImporter.class);

  private BulkImportParameters bulkImportParameters;
  private NodeImporter nodeImporter;

  private BulkFilesystemImporter bulkImporter;
  private Repository repositoryHelper;
  private AlfrescoXStreamMarshaller marshaller;
  private NamespaceService namespaceService;
  private SearchService searchService;
  private NodeService nodeService;
  private DictionaryService dictionaryService;

  public XmlBulkImporter(StreamingNodeImporterFactory streamingNodeImporterFactory, AlfrescoXStreamMarshaller marshaller, ServiceRegistry serviceRegistry) {
    this.nodeImporter = streamingNodeImporterFactory.getNodeImporter(marshaller.getFileImportRootLocation());
    this.marshaller = marshaller;
    this.bulkImportParameters = new BulkImportParameters();
    //@TODO - replaceExisting should be false, but tests fail and they should not
    //Maybe a FileImport issue?
    this.bulkImportParameters.setReplaceExisting(true);
    this.bulkImportParameters.setDisableRulesService(true);
    this.bulkImportParameters.setBatchSize(40);
    this.namespaceService = serviceRegistry.getNamespaceService();
    this.searchService = serviceRegistry.getSearchService();
    this.nodeService = serviceRegistry.getNodeService();
    this.dictionaryService = serviceRegistry.getDictionaryService();
  }

  public List bulkImport(NodeRef targetFolder, List<Source> sources) throws IOException, InvocationTargetException, IllegalAccessException {
    List ret = new ArrayList();
    for (Source source : sources) {
      ret.add(marshaller.unmarshal(source));
    }
    bulkImportParameters.setTarget(targetFolder);
    bulkImporter.bulkImport(bulkImportParameters, nodeImporter);

    List<Triple<NodeAssociation, Object, Object>> assocs = marshaller.getAssocsStack();
    for (Triple<NodeAssociation, Object, Object> assocTriple : assocs) {
      String assocName = assocTriple.getFirst().name();
      String assocNamespace = assocTriple.getFirst().namespace();
      QName assoc = QName.createQName(assocNamespace, assocName);
      QName fkPropertyType = QName.createQName(assocTriple.getFirst().fkPropertyType());
      QName fkPropertyName = QName.createQName(assocTriple.getFirst().fkPropertyName());
      NodeRef associating = findNodeRef(assocTriple.getSecond());

      Map<QName, Serializable> meta = new HashMap<QName, Serializable>();
      meta.put(QName.createQName(AlfrescoReflectionUtils.PROPERTY_NAME_TYPE), fkPropertyType.toPrefixString(namespaceService));
      meta.put(fkPropertyName, (String) assocTriple.getThird());
      NodeRef associated = findNodeRef(meta);
      this.nodeService.createAssociation(associating, associated, assoc);
    }

    return ret;
  }

  private NodeRef findNodeRef(Object object) throws InvocationTargetException, IllegalAccessException {
    return findNodeRef(AlfrescoReflectionUtils.getAlfrescoMeta(object));
  }

  private NodeRef findNodeRef(Map<QName, Serializable> meta) throws InvocationTargetException, IllegalAccessException {
    String ftsQuery = "";
    for (QName propName : meta.keySet()) {
      if (propName.getLocalName().equals(AlfrescoReflectionUtils.PROPERTY_NAME_TYPE)) {
        ftsQuery += "TYPE:\"" + meta.get(propName) + "\" AND ";
      } else if (propName.getLocalName().equals(AlfrescoReflectionUtils.PROPERTY_NAME_ASPECTS)) {
        String aspects = (String) meta.get(propName);
        StringTokenizer st = new StringTokenizer(aspects, ",");
        while (st.hasMoreTokens()) {
          ftsQuery += "ASPECT:\"" + st.nextToken() + "\" AND ";
        }
      } else {
        PropertyDefinition propertyDef = dictionaryService.getProperty(propName);
        if (propertyDef != null) {
          String propValue = (String) meta.get(propName);
          ftsQuery += "@" + propName.toPrefixString(this.namespaceService) + ":\"" + propValue + "\" AND ";
        }
      }
    }
    ftsQuery = ftsQuery.substring(0, ftsQuery.length() - 5);

    log.debug("Executing FTS query '" + ftsQuery + "'");
    ResultSet resultSet = this.searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, ftsQuery);
    if (resultSet.length() == 0 || resultSet.length() > 1) {
      AlfrescoFileImportUtils.handleException(meta,
          new IllegalStateException(
              "Following query returned " +
                  resultSet.length() +
                  " while expecting one\n" +
                  ftsQuery));
    }
    return resultSet.iterator().next().getNodeRef();

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
