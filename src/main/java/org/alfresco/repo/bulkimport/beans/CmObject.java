package org.alfresco.repo.bulkimport.beans;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.alfresco.repo.bulkimport.annotations.NodeAspect;
import org.alfresco.repo.bulkimport.annotations.NodeProperty;

public class CmObject {

  @NodeProperty
  protected String name;

  @NodeProperty
  protected Long id;

  @NodeProperty
  protected String title;

  @NodeProperty
  protected String description;

  @NodeAspect
  @XStreamAlias("isVersionable")
  protected String versionable;

  @Override
  public String toString() {
    return this.getClass().getName() + "(" +
        this.name + "," +
        this.title + "," +
        this.description + "," +
        this.versionable + ")";
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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
