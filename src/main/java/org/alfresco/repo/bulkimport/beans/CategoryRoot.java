package org.alfresco.repo.bulkimport.beans;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.alfresco.repo.bulkimport.annotations.NodeProperty;
import org.alfresco.repo.bulkimport.annotations.NodeType;

import java.util.List;

@XStreamAlias("categoryRoot")
@NodeType(
    name = "categoryRoot",
    namespace = "http://www.alfresco.org/model/content/1.0")
public final class CategoryRoot {

  @NodeProperty
  private String name;

  private List<Category> categories;

  @Override
  public String toString() {
    return this.getClass().getName() + "(" +
        this.name + ")";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Category> getCategories() {
    return categories;
  }

  public void setCategories(List<Category> categories) {
    this.categories = categories;
  }
}
