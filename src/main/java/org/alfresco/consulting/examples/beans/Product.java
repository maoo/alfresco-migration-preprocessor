package org.alfresco.consulting.examples.beans;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.alfresco.repo.bulkimport.annotations.NodeContentUrl;
import org.alfresco.repo.bulkimport.annotations.NodeProperty;
import org.alfresco.repo.bulkimport.annotations.NodeType;
import org.alfresco.repo.bulkimport.beans.CmObject;
import org.alfresco.repo.bulkimport.xml.MapEntryConverter;

import java.util.Map;

@XStreamAlias("product")
@NodeType(
    name = "product",
    namespace = "http:/alfresco.com/examples/tradedoubler",
    aspects = {
        "{http://www.alfresco.org/model/content/1.0}referencing",
        "{http://www.alfresco.org/model/content/1.0}auditable",
        "{http://www.alfresco.org/model/content/1.0}generalclassifiable"})
public class Product extends CmObject {

// Inherited by CmObject
// private String name;
// private String description;

  @XStreamConverter(MapEntryConverter.class)
  private Map<String, String> fields;

  @NodeProperty
  @XStreamAlias("productUrl")
  private String productUrl;

  @NodeContentUrl
  private String imageUrl;

  @NodeProperty
  private String price;

  @NodeProperty
  private String currency;

  @NodeProperty
  @XStreamAlias("TDProductId")
  private String productId;

  @NodeProperty
  @XStreamAlias("TDCategoryID")
  private String categoryId;

  @NodeProperty
  @XStreamAlias("TDCategoryName")
  private String categoryName;

  @NodeProperty
  private String merchantCategoryName;

  @NodeProperty
  private String sku;

  @NodeProperty
  private String shortDescription;

  @NodeProperty
  private String promoText;

  @NodeProperty
  private String previousPrice;

  @NodeProperty
  private String warranty;

  @NodeProperty
  private String availability;

  @NodeProperty
  private String inStock;

  @NodeProperty
  private String shippingCost;

  @NodeProperty
  private String deliveryTime;

  @NodeProperty
  private String weight;

  @NodeProperty
  private String size;

  @NodeProperty
  private String brand;

  @NodeProperty
  private String model;

  @NodeProperty
  private String ean;

  @NodeProperty
  private String upc;

  @NodeProperty
  private String isbn;

  @NodeProperty
  private String condition;

  @NodeProperty
  private String mpn;

  @NodeProperty
  private String techSpecs;

  @NodeProperty
  private String manufacturer;

  @NodeProperty
  private String programName;

  @NodeProperty
  private String programLogoPath;

  @NodeProperty
  private String programId;

  @Override
  public String toString() {
    return this.getClass().getName() + "(" +
        super.name + "," +
        super.description + ")";
  }

  public String getProductUrl() {
    return productUrl;
  }

  public void setProductUrl(String productUrl) {
    this.productUrl = productUrl;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getPrice() {
    return price;
  }

  public void setPrice(String price) {
    this.price = price;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public String getMerchantCategoryName() {
    return merchantCategoryName;
  }

  public void setMerchantCategoryName(String merchantCategoryName) {
    this.merchantCategoryName = merchantCategoryName;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public void setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
  }

  public String getPromoText() {
    return promoText;
  }

  public void setPromoText(String promoText) {
    this.promoText = promoText;
  }

  public String getPreviousPrice() {
    return previousPrice;
  }

  public void setPreviousPrice(String previousPrice) {
    this.previousPrice = previousPrice;
  }

  public String getWarranty() {
    return warranty;
  }

  public void setWarranty(String warranty) {
    this.warranty = warranty;
  }

  public String getAvailability() {
    return availability;
  }

  public void setAvailability(String availability) {
    this.availability = availability;
  }

  public String getInStock() {
    return inStock;
  }

  public void setInStock(String inStock) {
    this.inStock = inStock;
  }

  public String getShippingCost() {
    return shippingCost;
  }

  public void setShippingCost(String shippingCost) {
    this.shippingCost = shippingCost;
  }

  public String getDeliveryTime() {
    return deliveryTime;
  }

  public void setDeliveryTime(String deliveryTime) {
    this.deliveryTime = deliveryTime;
  }

  public String getWeight() {
    return weight;
  }

  public void setWeight(String weight) {
    this.weight = weight;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getEan() {
    return ean;
  }

  public void setEan(String ean) {
    this.ean = ean;
  }

  public String getUpc() {
    return upc;
  }

  public void setUpc(String upc) {
    this.upc = upc;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public String getMpn() {
    return mpn;
  }

  public void setMpn(String mpn) {
    this.mpn = mpn;
  }

  public String getTechSpecs() {
    return techSpecs;
  }

  public void setTechSpecs(String techSpecs) {
    this.techSpecs = techSpecs;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getProgramName() {
    return programName;
  }

  public void setProgramName(String programName) {
    this.programName = programName;
  }

  public String getProgramLogoPath() {
    return programLogoPath;
  }

  public void setProgramLogoPath(String programLogoPath) {
    this.programLogoPath = programLogoPath;
  }

  public String getProgramId() {
    return programId;
  }

  public void setProgramId(String programId) {
    this.programId = programId;
  }

  public Map<String, String> getFields() {
    return fields;
  }

  public void setFields(Map<String, String> fields) {
    this.fields = fields;
  }
}
