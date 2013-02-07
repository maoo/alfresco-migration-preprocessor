package org.alfresco.repo.bulkimport.beans;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.alfresco.repo.bulkimport.annotations.NodeProperty;
import org.alfresco.repo.bulkimport.annotations.NodeType;

@XStreamAlias("category")
@NodeType(
    name = "category",
    namespace = "http://www.alfresco.org/model/content/1.0")
public final class Category {

  @NodeProperty
  private String name;

  @Override
  public String toString() {
    return "Printing Class: " + this.getClass() + "\n" +
        "* Name: " + this.name + "\n";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
