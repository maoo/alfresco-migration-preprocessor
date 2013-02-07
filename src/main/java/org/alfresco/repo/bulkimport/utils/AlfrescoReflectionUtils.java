package org.alfresco.repo.bulkimport.utils;

import com.google.gdata.util.common.base.StringUtil;
import org.alfresco.repo.bulkimport.annotations.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.Triple;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class AlfrescoReflectionUtils {

  static Logger log = Logger.getLogger(AlfrescoReflectionUtils.class);

  public static final java.lang.String PROPERTY_NAME_TYPE = "type";
  public static final java.lang.String PROPERTY_NAME_ASPECTS = "aspects";
  public static final java.lang.String METADATA_FILE_EXTENSION = "properties.xml";

  public static Map<QName, Serializable> getAlfrescoMeta(Object obj) throws InvocationTargetException, IllegalAccessException {
    Map<QName, Serializable> ret = new HashMap<QName, Serializable>();
    if (obj.getClass().isAnnotationPresent(NodeType.class)) {
      QName nodeType = getNodeType(obj);
      String typeNamespace = nodeType.getNamespaceURI();
      for (Field field : obj.getClass().getDeclaredFields()) {
        field.setAccessible(true);
        String fieldName = field.getName();
        NodeProperty nodeProperty = field.getAnnotation(NodeProperty.class);

        if (nodeProperty != null && !StringUtil.isEmpty(fieldName)) {
          String nodePropertyNamespace = nodeProperty.namespace();
          String nodePropertyName = nodeProperty.name();
          String namespace = (!StringUtil.isEmpty(nodePropertyNamespace)) ? nodePropertyNamespace : typeNamespace;
          String propertyName = (!StringUtil.isEmpty(nodePropertyName)) ? nodePropertyName : fieldName;
          Serializable propertyValue = (Serializable) field.get(obj);
          QName nsQname = QName.createQName(namespace, propertyName);
          ret.put(nsQname, propertyValue);
        }
      }
    }
    log.debug("[Adding Alfresco Metas] " + ret);
    return ret;
  }

  public static QName getNodeType(Object obj) throws InvocationTargetException, IllegalAccessException {
    NodeType nodeType = obj.getClass().getAnnotation(NodeType.class);
    if (nodeType != null) {
      String typeNamespace = nodeType.namespace();
      String typeName = nodeType.name();
      return QName.createQName(typeNamespace, typeName);
    } else {
      throw new IllegalAccessException("Object " + obj + " does not contain @NodeType annocation");
    }
  }

  private static List<QName> getNodeTypeAspects(Object obj) throws IllegalAccessException {
    List<QName> ret = new ArrayList<QName>();
    NodeType nodeType = obj.getClass().getAnnotation(NodeType.class);
    if (nodeType != null) {
      String[] aspects = nodeType.aspects();
      for (String aspect : aspects) {
        ret.add(QName.createQName(aspect));
      }
    } else {
      throw new IllegalAccessException("Object " + obj + " does not contain @NodeType annocation");
    }
    return ret;
  }

  public static QName[] getNodeAspects(Object obj) throws InvocationTargetException, IllegalAccessException {
    QName nodeType = getNodeType(obj);
    List<QName> aspects = getNodeTypeAspects(obj);
    String typeNamespace = nodeType.getNamespaceURI();

    for (Field field : obj.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      String fieldName = field.getName();
      NodeAspect nodeAspect = field.getAnnotation(NodeAspect.class);

      if (nodeAspect != null && !StringUtil.isEmpty(fieldName)) {
        String nodeAspectNamespace = nodeAspect.namespace();
        String nodeAspectName = nodeAspect.name();
        String namespace = (!StringUtil.isEmpty(nodeAspectNamespace)) ? nodeAspectNamespace : typeNamespace;
        String aspectName = (!StringUtil.isEmpty(nodeAspectName)) ? nodeAspectName : fieldName;
        Serializable fieldValue = (Serializable) field.get(obj);

        if (fieldValue != null) {
          aspects.add(QName.createQName(namespace, aspectName));
        }
      }
    }
    log.debug("[Added Alfresco Aspects] " + aspects.toArray());
    return aspects.toArray(new QName[]{});
  }

  public static boolean isContainer(Class currentClass) {
    return currentClass.getAnnotation(NodeParent.class) != null;
  }

  public static String getContentUrl(Object currentObject) throws IllegalAccessException {
    for (Field field : currentObject.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      NodeContentUrl contentUrlAnnotation = field.getAnnotation(NodeContentUrl.class);
      if (contentUrlAnnotation != null) {
        return (String) field.get(currentObject);
      }
    }
    return null;
  }

  public static List<Triple<NodeAssociation, Object,Object>> getAlfrescoAssocs(Object currentObject) throws NoSuchFieldException, IllegalAccessException {
    List<Triple<NodeAssociation, Object,Object>> ret = new ArrayList<Triple<NodeAssociation, Object,Object>>();
    for (Field field : currentObject.getClass().getDeclaredFields()) {
      NodeAssociation nodeAssociation = field.getAnnotation(NodeAssociation.class);
      if (nodeAssociation != null) {
        String associationValuesFieldName = nodeAssociation.fieldName();
        Field associationValuesField = currentObject.getClass().getDeclaredField(associationValuesFieldName);
        if (associationValuesField == null) {
          IllegalStateException e = new IllegalStateException("Field "+associationValuesFieldName+" is null on object "+currentObject);
          AlfrescoFileImportUtils.handleException(currentObject, e);
        }
        associationValuesField.setAccessible(true);
        Object fieldValue = associationValuesField.get(currentObject);
        if (fieldValue != null) {
          Collection fieldValueList = new ArrayList();
          if (fieldValue instanceof String[]) {
            fieldValueList = Arrays.asList((String[])fieldValue);
          } else if (fieldValue instanceof Collection) {
            fieldValueList = (Collection)fieldValue;
          } else {
            IllegalStateException e = new IllegalStateException("Peer associations only support String[] and Collection Java objects; cannot parse "+fieldValue);
            AlfrescoFileImportUtils.handleException(currentObject, e);
          }
          for(Object associatedObject : fieldValueList) {
            ret.add(new Triple<NodeAssociation, Object,Object>(nodeAssociation, currentObject,associatedObject));
          }
        }
      }
    }
    return ret;
  }

//  public static <T> Object getBean(NodeService nodeService, NodeRef nodeRef, Class<T> clazz) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
//    T bean = clazz.newInstance();
//    AlfrescoRootElement alfrescoRootElement = clazz.getAnnotation(AlfrescoRootElement.class);
//    String rootNamespace = alfrescoRootElement.namespace();
//    Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
//    for (Method method : clazz.getMethods()) {
//      String methodName = method.getName();
//      String setterName = null;
//      if (methodName.startsWith("get")) {
//        setterName = methodName.replace("get","set");
//      } else if (methodName.startsWith("is")) {
//        setterName = methodName.replace("is","set");
//      }
//      if (setterName != null) {
//        AlfrescoElement alfrescoElement = method.getAnnotation(AlfrescoElement.class);
//        if (alfrescoElement != null) {
//          String namespace = (!StringUtil.isEmpty(alfrescoElement.namespace())) ? alfrescoElement.namespace() : rootNamespace;
//          QName qname = QName.createQName(namespace, alfrescoElement.name());
//          Serializable value = props.get(qname);
//          if (value != null) {
//            Method setter = clazz.getMethod(setterName, value.getClass());
//            setter.invoke(bean,value);
//          }
//        }
//      }
//    }
//    return bean;
//  }
}