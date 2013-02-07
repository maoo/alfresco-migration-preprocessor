package org.alfresco.repo.bulkimport.utils;

import com.google.gdata.util.common.base.StringUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.bulkimport.MetadataLoader;
import org.alfresco.service.namespace.QName;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class AlfrescoFileImportUtils {

  static Logger log = Logger.getLogger(AlfrescoFileImportUtils.class);

  protected static HttpClient httpClient = new HttpClient();

  public static void fetchBinaryContent(final File file, final String url) {
    FileOutputStream fos = null;
    try {
      GetMethod method = new GetMethod(url);

      int statusCode = httpClient.executeMethod(method);

      if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
        String error = String.format("Error fetching url '%s'\n Status Code: %s ", url, statusCode);
        log.error(error);
      } else {
        final byte[] responseBody = method.getResponseBody();

        if (responseBody != null) {
          fos = new FileOutputStream(file);
          fos.write(responseBody);
        } else {
          log.error(String.format("Response body for url '%s' is null; status code: %s", url, statusCode));
        }
      }
    } catch (IOException e) {
      AlfrescoFileImportUtils.handleException(file, e);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          AlfrescoFileImportUtils.handleException(file, e);
        }
      }
    }
  }

  public static File getMetaFile(Map<QName, Serializable> nodeProperties, File fileImportRootLocation) {
    String name = (String) nodeProperties.get(ContentModel.PROP_NAME);
    if (StringUtil.isEmpty(name)) {
      name = (new Date()).getTime() + ".bin";
    }
    String metaFileName = name + MetadataLoader.METADATA_SUFFIX + AlfrescoReflectionUtils.METADATA_FILE_EXTENSION;
    return new File(fileImportRootLocation, metaFileName);
  }

  public static File getBinaryFile(Map<QName, Serializable> nodeProperties, File fileImportRootLocation) {
    String name = (String) nodeProperties.get(ContentModel.PROP_NAME);
    if (StringUtil.isEmpty(name)) {
      name = (new Date()).getTime() + ".bin";
    }
    return new File(fileImportRootLocation, name);
  }

  public static File createFolder(String folderName, File fileImportRootLocation) {
    if (StringUtil.isEmpty(folderName)) {
      folderName = (new Date()).getTime() + "";
    }
    File folder = new File(fileImportRootLocation, folderName);
    folder.mkdir();
    return folder;
  }

  public static void handleException(Object currentObject, Throwable e) {
    throw new IllegalStateException("Error convering object " + currentObject, e);
  }
}