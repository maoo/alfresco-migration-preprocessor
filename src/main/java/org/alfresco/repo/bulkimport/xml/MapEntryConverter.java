package org.alfresco.repo.bulkimport.xml;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.HashMap;
import java.util.Map;

public class MapEntryConverter implements Converter {
  public boolean canConvert(Class clazz) {
    return Map.class.isAssignableFrom(clazz);
  }

  public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    Map<String, String> map = (Map<String, String>) value;
    for (Map.Entry<String, String> entry : map.entrySet()) {
      writer.startNode(entry.getKey().toString());
      writer.setValue(entry.getValue().toString());
      writer.endNode();
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Map<String, String> map = new HashMap<String, String>();

    while (reader.hasMoreChildren()) {
      reader.moveDown();
      map.put(reader.getNodeName(), reader.getValue());
      reader.moveUp();
    }
    return map;
  }
}