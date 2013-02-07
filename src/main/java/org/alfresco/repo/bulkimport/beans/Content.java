package org.alfresco.repo.bulkimport.beans;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.alfresco.repo.bulkimport.annotations.*;

import java.util.List;

@XStreamAlias("content")
@NodeType(
    name = "content",
    namespace = "http://www.alfresco.org/model/content/1.0",
    aspects = {
        "{http://www.alfresco.org/model/content/1.0}referencing",
        "{http://www.alfresco.org/model/content/1.0}auditable",
        "{http://www.alfresco.org/model/content/1.0}generalclassifiable"})
public class Content extends CmObject {

  @NodeContentUrl
  private String contentUrl;

  @NodeAssociation(
      name = "references",
      namespace = "http://www.alfresco.org/model/content/1.0",
      fieldName = "referenceNames",
      fkPropertyName = "{http://www.alfresco.org/model/content/1.0}name",
      fkPropertyType = "{http://www.alfresco.org/model/content/1.0}content")
  private List<Content> references;

  private String[] referenceNames;

  @Override
  public String toString() {
    return this.getClass().getName() + "(" +
        super.name + "," +
        this.title + "," +
        this.description + "," +
        this.versionable + ")";
  }

  public String getContentUrl() {
    return contentUrl;
  }

  public void setContentUrl(String contentUrl) {
    this.contentUrl = contentUrl;
  }

  public List<Content> getReferences() {
    return references;
  }

  public void setReferences(List<Content> references) {
    this.references = references;
  }

  public String[] getReferenceNames() {
    return referenceNames;
  }

  public void setReferenceNames(String[] referenceNames) {
    this.referenceNames = referenceNames;
  }
}
