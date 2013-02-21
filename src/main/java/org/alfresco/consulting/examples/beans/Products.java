package org.alfresco.consulting.examples.beans;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("products")
public class Products {

  @XStreamImplicit(itemFieldName="product")
  private List<Product> product;

  public List<Product> getProduct() {
    return product;
  }

  public void setProduct(List<Product> product) {
    this.product = product;
  }
}
