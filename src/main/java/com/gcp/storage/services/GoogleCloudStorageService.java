package com.gcp.storage.services;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleCloudStorageService {

	private final Storage storage = StorageOptions.getDefaultInstance().getService();
	private final String bucketName = "angelapp";
	private final String proyectId = "angelapp-434421";

	public String uploadFile(MultipartFile file, String folderPath) throws IOException {

		String fileName = folderPath + "/" + file.getOriginalFilename();

		BlobId blobId = BlobId.of(bucketName, fileName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();

		storage.create(blobInfo, file.getBytes());

		return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
	}

	public boolean deleteFolder(String folderPath) {
		try {
			// Listar todos los blobs (archivos) dentro de la carpeta del usuario
			Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(folderPath));

			// Verificar si se encontraron blobs
			boolean folderHasFiles = false;

			// Iterar sobre los blobs y eliminarlos
			for (Blob blob : blobs.iterateAll()) {
				folderHasFiles = true;
				System.out.println("Deleting file: " + blob.getName());
				blob.delete(); // Eliminar cada archivo en la carpeta
			}

			// Después de eliminar los archivos, eliminar la "carpeta" vacía si existe
			Blob folderMarker = storage.get(BlobId.of(bucketName, folderPath));
			if (folderMarker != null && folderMarker.exists()) {
				System.out.println("Deleting folder marker: " + folderPath);
				folderMarker.delete(); // Eliminar la carpeta vacía explícitamente
			}

			System.out.println("All files in folder and folder marker deleted successfully: " + folderPath);
			return true;
		} catch (Exception e) {
			System.out.println("Error deleting folder: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public void downloadFile(String fileName, Path destFilePath) throws IOException {
		BlobId blobId = BlobId.of(bucketName, fileName);
		storage.get(blobId).downloadTo(destFilePath);
	}

	public List<String> getImagesFromFolder(String folderPath) {
		List<String> imageUrls = new ArrayList<>();

		try {
			// Imprimir el folderPath y el bucketName para verificar que sean correctos
			System.out.println("Bucket Name: " + bucketName);
			System.out.println("Folder Path: " + folderPath);

			// Obtener todos los blobs (archivos) en el bucket que están dentro de la
			// carpeta del usuario
			Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(folderPath),
					Storage.BlobListOption.currentDirectory());

			// Verificar si se obtuvieron blobs
			if (blobs == null) {
				System.out.println("No blobs found for the folder: " + folderPath);
			} else {
				System.out.println("Blobs found, iterating through the list...");
			}

			// Iterar sobre los blobs y añadir sus URLs a la lista
			for (Blob blob : blobs.iterateAll()) {
				// Imprimir el nombre del blob para depuración
				System.out.println("Found blob: " + blob.getName());

				// Verificar si el archivo es una imagen (por extensión)
				if (blob.getName().endsWith(".jpg") || blob.getName().endsWith(".jpeg")
						|| blob.getName().endsWith(".png")) {
					// Obtener la URL pública del archivo
					String imageUrl = "https://storage.googleapis.com/" + bucketName + "/" + blob.getName();
					imageUrls.add(imageUrl);
					System.out.println("Image URL added: " + imageUrl);
				} else {
					System.out.println("Skipping non-image file: " + blob.getName());
				}
			}

			System.out.println("Total images found: " + imageUrls.size());

		} catch (Exception e) {
			// Imprimir el error para depuración
			System.out.println("Error retrieving images: " + e.getMessage());
			e.printStackTrace();
		}

		return imageUrls;
	}

}
