package org.alfresco.repo.bulkimport.xml;

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import java.io.*;

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
        fos = new FileOutputStream(file);

        if (responseBody != null) {
          fos.write(responseBody);
        } else {
          log.error(String.format("Response body for url '%s' is null; status code: %s", url, statusCode));
        }
      }
    } catch (HttpException e) {
      throw new IllegalStateException(e);
    } catch (FileNotFoundException e) {
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
      }
    }
  }
}