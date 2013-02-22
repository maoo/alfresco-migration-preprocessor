package org.alfresco.repo.bulkimport.beans;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.alfresco.repo.bulkimport.annotations.NodeParent;
import org.alfresco.repo.bulkimport.annotations.NodeType;

import java.util.List;

@XStreamAlias("folder")
@NodeParent
@NodeType(
    name = "folder",
    namespace = "http://www.alfresco.org/model/content/1.0",
    aspects = {
        "{http://www.alfresco.org/model/content/1.0}auditable",
        "{http://www.alfresco.org/model/content/1.0}generalclassifiable"})
public final class Folder extends CmObject {

  private List<Content> children;

  @Override
  public String toString() {
    return this.getClass().getName() + "(" +
        this.name + "," +
        this.title + "," +
        this.description + "," +
        this.versionable + ")";
  }

  public List<Content> getChildren() {
    return children;
  }

  public void setChildren(List<Content> children) {
    this.children = children;
  }
}
