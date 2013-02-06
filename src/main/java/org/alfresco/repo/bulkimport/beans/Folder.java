package org.alfresco.repo.bulkimport.beans;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.alfresco.repo.bulkimport.annotations.*;

import java.util.List;

@XStreamAlias("folder")
@NodeParent
@NodeType(name = "folder", namespace = "http://www.alfresco.org/model/content/1.0")
public final class Folder {

  @NodeProperty
  private String name;

  @NodeProperty
  private String title;

  @NodeProperty
  private String description;

  @NodeAspect
  @XStreamAlias("isVersionable")
  private String versionable;

  @NodeChildAssociation
  private List<Content> children;

  @Override
  public String toString() {
    return "Printing Class: " + this.getClass() + "\n" +
        "* Name: " + this.name + "\n" +
        "* Title: " + this.title + "\n" +
        "* Description: " + this.description + "\n" +
        "* is versionable: " + this.versionable + "\n";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVersionable() {
    return versionable;
  }

  public void setVersionable(String versionable) {
    this.versionable = versionable;
  }

  public List<Content> getChildren() {
    return children;
  }

  public void setChildren(List<Content> children) {
    this.children = children;
  }
}
