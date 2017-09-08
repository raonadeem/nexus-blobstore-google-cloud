package org.sonatype.nexus.blobstore.gcloud.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.sonatype.nexus.blobstore.api.BlobAttributes;
import org.sonatype.nexus.blobstore.api.BlobMetrics;

import com.google.cloud.storage.Bucket;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.blobstore.api.BlobAttributesConstants.CONTENT_SIZE_ATTRIBUTE;
import static org.sonatype.nexus.blobstore.api.BlobAttributesConstants.CREATION_TIME_ATTRIBUTE;
import static org.sonatype.nexus.blobstore.api.BlobAttributesConstants.DELETED_ATTRIBUTE;
import static org.sonatype.nexus.blobstore.api.BlobAttributesConstants.DELETED_REASON_ATTRIBUTE;
import static org.sonatype.nexus.blobstore.api.BlobAttributesConstants.HEADER_PREFIX;
import static org.sonatype.nexus.blobstore.api.BlobAttributesConstants.SHA1_HASH_ATTRIBUTE;

public class GoogleCloudBlobAttributes
    implements BlobAttributes
{
  private Map<String, String> headers;

  private BlobMetrics metrics;

  private boolean deleted = false;

  private String deletedReason;

  private final GoogleCloudPropertiesFile propertiesFile;

  public GoogleCloudBlobAttributes(final Bucket bucket, final String key) {
    checkNotNull(key);
    checkNotNull(bucket);
    this.propertiesFile = new GoogleCloudPropertiesFile(bucket, key);
  }

  public GoogleCloudBlobAttributes(final Bucket bucket, final String key, final Map<String, String> headers,
                          final BlobMetrics metrics) {
    this(bucket, key);
    this.headers = checkNotNull(headers);
    this.metrics = checkNotNull(metrics);
  }

  @Override
  public Map<String, String> getHeaders() {
    return headers;
  }

  @Override
  public BlobMetrics getMetrics() {
    return metrics;
  }

  @Override
  public boolean isDeleted() {
    return deleted;
  }

  @Override
  public void setDeleted(final boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public void setDeletedReason(final String deletedReason) {
    this.deletedReason = deletedReason;
  }

  @Override
  public String getDeletedReason() {
    return deletedReason != null ? deletedReason : "No reason supplied";
  }

  public boolean load() throws IOException {
    if (!propertiesFile.exists()) {
      return false;
    }
    propertiesFile.load();
    readFrom(propertiesFile);
    return true;
  }

  public void store() throws IOException {
    writeTo(propertiesFile);
    propertiesFile.store();
  }

  @Override
  public Properties getProperties() {
    return new Properties(propertiesFile);
  }

  private void readFrom(Properties properties) {
    headers = new HashMap<>();
    for (Entry<Object, Object> property : properties.entrySet()) {
      String key = (String) property.getKey();
      if (key.startsWith(HEADER_PREFIX)) {
        headers.put(key.substring(HEADER_PREFIX.length()), String.valueOf(property.getValue()));
      }
    }

    metrics = new BlobMetrics(
        new DateTime(Long.parseLong(properties.getProperty(CREATION_TIME_ATTRIBUTE))),
        properties.getProperty(SHA1_HASH_ATTRIBUTE),
        Long.parseLong(properties.getProperty(CONTENT_SIZE_ATTRIBUTE)));

    deleted = properties.containsKey(DELETED_ATTRIBUTE);
    deletedReason = properties.getProperty(DELETED_REASON_ATTRIBUTE);
  }

  private Properties writeTo(final Properties properties) {
    for (Entry<String, String> header : getHeaders().entrySet()) {
      properties.put(HEADER_PREFIX + header.getKey(), header.getValue());
    }
    BlobMetrics blobMetrics = getMetrics();
    properties.setProperty(SHA1_HASH_ATTRIBUTE, blobMetrics.getSha1Hash());
    properties.setProperty(CONTENT_SIZE_ATTRIBUTE, Long.toString(blobMetrics.getContentSize()));
    properties.setProperty(CREATION_TIME_ATTRIBUTE, Long.toString(blobMetrics.getCreationTime().getMillis()));

    if (deleted) {
      properties.put(DELETED_ATTRIBUTE, Boolean.toString(deleted));
      properties.put(DELETED_REASON_ATTRIBUTE, getDeletedReason());
    }
    return properties;
  }
}