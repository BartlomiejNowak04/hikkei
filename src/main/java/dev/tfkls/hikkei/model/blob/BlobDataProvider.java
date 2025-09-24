package dev.tfkls.hikkei.model.blob;

import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.util.List;

public interface BlobDataProvider {
  int createBlob(byte[] data, String mime, int userId) throws InternalErrorException;

  void deleteBlob(int blobId) throws InternalErrorException;

  BlobData getBlobById(int blobId) throws InternalErrorException;

  byte[] getBlobData(int blobId) throws InternalErrorException;

  List<BlobData> getUserBlobs(int userId) throws InternalErrorException;
}
